package net.praqma.jenkins.configrotator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

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
import java.util.logging.Level;
import javax.security.auth.callback.ConfirmationCallback;
import jenkins.model.Jenkins;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseGetBaseLineCompare;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfiguration;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfigurationComponent;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.GetConfiguration;

public class ConfigurationRotator extends SCM {

	private AbstractConfigurationRotatorSCM acrs;
	private static Logger logger = Logger.getLogger();
	private boolean printDebug;

	public enum ResultType {

		COMPATIBLE, /*
					 * Tested and configuration is compatible
					 */
		INCOMPATIBLE, /*
					 * Tested and configuration is NOT compatible
					 */
		FAILED, /*
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
        


		boolean performResult = false;
		try {
			performResult = acrs.perform( build, launcher, workspace, listener, reconfigure );
            try {
                writeChangeLog(file, listener, build);
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

			/*
			 * If not aborted, add publisher
			 */
			boolean added = false;
			for( Publisher p : build.getParent().getPublishersList() ) {
				if( p instanceof ConfigurationRotatorPublisher ) {
					added = true;
					break;
				}
			}
			if( !added ) {
				build.getProject().getPublishersList().add( new ConfigurationRotatorPublisher() );
			}

			return true;
		}
	}

	public void setConfigurationByAction( AbstractProject<?, ?> project, ConfigurationRotatorBuildAction action ) throws IOException {
		acrs.setConfigurationByAction( project, action );
		reconfigure = true;
	}

	@Override
	protected PollingResult compareRemoteRevisionWith( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState arg4 ) throws IOException, InterruptedException {
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
        // TODO Auto-generated method stub
        return acrs.createChangeLogParser();
    }
    
    public void writeChangeLog(File f, BuildListener listener, AbstractBuild<?, ?> build) throws IOException, ConfigurationRotatorException, InterruptedException {
        PrintWriter writer = null;
        String name = "NoName ";
        List<String> changes = new ArrayList<String>();
        //First obtain last succesful result
        ConfigurationRotatorBuildAction crbac = acrs.getLastResult(build.getProject(), acrs.getClass());
        
        //Special case: This is the first build
         if(crbac == null) {
            
        } else {
             
             List<AbstractConfigurationComponent> previousComponentList = crbac.getConfiguration().getList();
             List<AbstractConfigurationComponent> currentComponentList = null;
             ConfigurationRotatorBuildAction current = build.getAction(ConfigurationRotatorBuildAction.class);
             if(current != null)
                 currentComponentList = current.getConfiguration().getList();
             
             int compareIndex = -1;
             
             if(currentComponentList != null) {
                for(AbstractConfigurationComponent acc : currentComponentList) {
                    if(acc.isChangedLast()) {
                        compareIndex = currentComponentList.indexOf(acc);
                        break;
                    }
                }
             }
             
             //The compare is totally new. Else compare the previous component
             if(compareIndex == -1) {
                 
             } else {
                 if(currentComponentList.get(compareIndex) instanceof ClearCaseUCMConfigurationComponent) {
                    //ClearCaseUCMConfigurationComponent now = (ClearCaseUCMConfigurationComponent)currentComponentList.get(compareIndex);
                    //ClearCaseUCMConfigurationComponent before = (ClearCaseUCMConfigurationComponent)previousComponentList.get(compareIndex);
                    changes = build.getWorkspace().act(new ClearCaseGetBaseLineCompare(listener, current.getConfiguration(ClearCaseUCMConfiguration.class), crbac.getConfiguration(ClearCaseUCMConfiguration.class)));
                 }
             }
        }
        
        try {
            
            writer = new PrintWriter(new FileWriter(f));
            if(changes.size() > 0) {
                writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                writer.println("<changelog>");
                writer.println("<entry>");
                writer.println(String.format("<owner>%s</owner>", "TestOwner"));
                writer.println(String.format("<componentChange>%s</componentChange>", changes.get(0)));
                writer.println(String.format("<date>%s</date>", new Date().toString()));
                writer.println("</entry>");      
                writer.println("</changelog>");
                listener.getLogger().println("Finished writing to change log!");
            }
        
        } catch (IOException e) {
            listener.getLogger().println("Unable to create change log!" +e);
        } finally {
            writer.close();
        }
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
