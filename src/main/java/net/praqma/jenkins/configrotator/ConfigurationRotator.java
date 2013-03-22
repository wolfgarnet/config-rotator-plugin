package net.praqma.jenkins.configrotator;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.StreamAppender;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.scm.SCM;
import hudson.tasks.Publisher;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import jenkins.model.Jenkins;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseGetBaseLineCompare;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfiguration;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfigurationComponent;

public class ConfigurationRotator extends SCM {

	private AbstractConfigurationRotatorSCM acrs;
        // public to be able to print the name in debug everywhere
	public static Logger logger = Logger.getLogger();
	private boolean printDebug;

	public enum ResultType {

		/*
		 * Tested and configuration is compatible
		 */
		COMPATIBLE,
		
		/*
		 * Tested and configuration is NOT compatible
		 */
		INCOMPATIBLE, 
		
		FAILED,
		
		 /*
		 * The tests failed and was unable to determine compatibility
		 */
		UNDETERMINED
	}

    public static final String URL_NAME = "config-rotator";
	public static final String NAME = "ConfigRotator";
	public static final String LOGGERNAME = "[" + NAME + "] ";
	public boolean justConfigured = false;

	public static final String SEPARATOR = System.getProperty( "file.separator" );
	public static final String FEED_DIR = "config-rotator-feeds" + SEPARATOR;
	public static final String FEED_FULL_PATH = Jenkins.getInstance().getRootDir() + SEPARATOR + FEED_DIR;

	/**
	 * Added file to feed.
	 */
	public static final File FEED_DIRFILE = new File( FEED_FULL_PATH );

	/**
	 * Determines whether a new configuration has been entered. If true, the
	 * input is new.
	 */
	public boolean reconfigure;

	@DataBoundConstructor
	public ConfigurationRotator( AbstractConfigurationRotatorSCM acrs, boolean printDebug ) {
		this.acrs = acrs;
		this.justConfigured = true;
		this.printDebug = printDebug;
	}

	public AbstractConfigurationRotatorSCM getAcrs() {
		return acrs;
	}

	public boolean doReconfigure() {
		return reconfigure;
	}

	public void setReconfigure( boolean reconfigure ) {
		this.reconfigure = reconfigure;
	}

	public boolean doPrintDebug() {
		return printDebug;
	}

	@Override
	public SCMRevisionState calcRevisionsFromBuild( AbstractBuild<?, ?> arg0, Launcher arg1, TaskListener arg2 ) throws IOException, InterruptedException {
		if( !doReconfigure() ) {
			return new SCMRevisionState() {
			};
		} else {
			return null;
		}
	}

	@Override
	public boolean checkout( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File file ) throws IOException, InterruptedException {
		PrintStream out = listener.getLogger();
		
		out.println( "Config-rotator version: " + Jenkins.getInstance().getPlugin( "config-rotator" ).getWrapper().getVersion() );

		/*
		 * Configure debugger
		 */
		if( printDebug ) {
			Appender app = new StreamAppender( out );
			app.setMinimumLevel( LogLevel.DEBUG );
			app.setTemplate( "[%level]%space %message%newline" );
			app.lockToCurrentThread();
			Logger.addAppender( app );
		}
        
       
        
       
		/*
		 * Determine if the job was reconfigured
		 */
		if( justConfigured ) {
			reconfigure = acrs.wasReconfigured( build.getProject() );
			logger.debug( "Was reconfigured: " + reconfigure );
		}
        

        ensurePublisher( build );
		boolean performResult = false;
		try {
			performResult = acrs.perform( build, launcher, workspace, listener, reconfigure );
            try {
                acrs.writeChangeLog(file, listener, build);
            } catch (ConfigurationRotatorException ex) {
                out.println("Cleartool Checkout exception: "+ex);
            }
		} catch( AbortException e ) {
			out.println( LOGGERNAME + "Failed to check out" );
			throw e;
		}

		if( !performResult ) {
			// ConfigurationRotator.perform will return false only if no new baselines found
			// We fail build if there is now new baseline.
			// An alternative would be to do like the CCUCM plugin and make the
			// build result "grey" with an comment "nothing to do".
			throw new AbortException( "No new baselines found!" );
		} else {

			/*
			 * Config is not fresh anymore
			 */
			reconfigure = false;
			justConfigured = false;
			build.getProject().save();

			return true;
		}
	}

    public void ensurePublisher( AbstractBuild build ) throws IOException {
        Describable describable = build.getProject().getPublishersList().get( ConfigurationRotatorPublisher.class );
        if( describable == null ) {
            logger.info( "Adding publisher to project" );
            build.getProject().getPublishersList().add( new ConfigurationRotatorPublisher() );
        }
    }

	public void setConfigurationByAction( AbstractProject<?, ?> project, ConfigurationRotatorBuildAction action ) throws IOException {
		acrs.setConfigurationByAction( project, action );
		reconfigure = true;
	}

	@Override
	protected PollingResult compareRemoteRevisionWith( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState arg4 ) throws IOException, InterruptedException {
                PrintStream out = listener.getLogger();	
                // This little check ensures changes are not found while building, as
                // this with many concurrent builds and polling leads to issues where
                // we saw a build was long in the queue, and when started, the polling found
                // changed a did schedule the same build with the same changes as last time
                // between the last one started and finished.
                //Basically this disables polling while the job has a build in the queue.
                if (project.isInQueue())
                {
                    out.println("A build already in queue - cancelling poll");
                    logger.debug("A build already in queue - cancelling poll");
                    return PollingResult.NO_CHANGES;
                }
		/*
		 * Determine if the job was reconfigured
		 */
		if( justConfigured ) {
			reconfigure = acrs.wasReconfigured( project );
			logger.debug( "Was reconfigured: " + reconfigure );
		}

		PollingResult result = acrs.poll( project, launcher, workspace, listener, reconfigure );
		
		return result;
	}

    /**
     * Delegate the change log parser to abstract subtypes.
     * @return 
     */
    @Override
    public ChangeLogParser createChangeLogParser() {
        logger.debug("Creating change log parser");
        return acrs.createChangeLogParser();
    }

	@Extension
	public static final class RotatorDescriptor extends SCMDescriptor<ConfigurationRotator> {

		public RotatorDescriptor() {
			super( ConfigurationRotator.class, null );
		}

		@Override
		public String getDisplayName() {
			return "Config rotator";
		}

		@Override
		public SCM newInstance( StaplerRequest req, JSONObject formData ) throws FormException {
			System.out.println( "FORM: " + formData.toString( 2 ) );
			ConfigurationRotator r = (ConfigurationRotator) super.newInstance( req, formData );
			ConfigurationRotatorSCMDescriptor<AbstractConfigurationRotatorSCM> d = (ConfigurationRotatorSCMDescriptor<AbstractConfigurationRotatorSCM>) r.getAcrs().getDescriptor();
			r.acrs = d.newInstance( req, formData, r.acrs );
			save();
			return r;
		}

		public List<ConfigurationRotatorSCMDescriptor<?>> getSCMs() {
			return AbstractConfigurationRotatorSCM.getDescriptors();
		}
	}    
}
