package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.util.FormValidation;

import net.praqma.clearcase.util.ExceptionUtils;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.jenkins.configrotator.AbstractConfigurationComponent;
import net.praqma.jenkins.configrotator.AbstractConfigurationRotatorSCM;
import net.praqma.jenkins.configrotator.ConfigurationRotator;
import net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType;
import net.praqma.jenkins.configrotator.ConfigurationRotatorBuildAction;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.jenkins.configrotator.ConfigurationRotatorPublisher;
import net.praqma.jenkins.configrotator.ConfigurationRotatorSCMDescriptor;
import net.praqma.jenkins.utils.remoting.DetermineProject;
import net.praqma.jenkins.utils.remoting.LoadEntity;
import net.praqma.jenkins.utils.remoting.GetBaselines;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.StreamAppender;

public class ClearCaseUCM extends AbstractConfigurationRotatorSCM implements Serializable {
	
	private static Logger logger = Logger.getLogger();

	private String config;
	private String streamName;
	
	transient private Stream stream;
	transient private String projectName;

	/**
	 * Version 0.1.0 constructor
	 * 
	 * Parse config
	 * Each line represents a {@link Component}, {@link Stream}, {@link Baseline} and a {@Plevel plevel}
	 * @param config
	 */
	@DataBoundConstructor
	public ClearCaseUCM( String streamName, String config ) {
		this.config = config;
		this.streamName = streamName;
	}

	public String getConfig() {
		return config;
	}
	
	public String getStreamName() {
		return streamName;
	}

	@Override
	public String getName() {
		return "ClearCase UCM";
	}

	@Override
	public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) throws IOException {
		PrintStream out = listener.getLogger();
		Appender app = new StreamAppender( out );
		app.setMinimumLevel( LogLevel.DEBUG );
		app.setTemplate( "[%level]%space %message%newline" );
		Logger.addAppender( app );
		
		/* Resolve streamName */
		try {
			logger.debug( "Resolving " + streamName );
			stream = (Stream) workspace.act( new LoadEntity( Stream.get( streamName, true ) ) );
		} catch( ClearCaseException e ) {
			e.print( out );
			return false;
		} catch( InterruptedException e ) {
			out.println( "Connection failed while loading + " + streamName + ": " + e.getMessage() );
			return false;
		}
		
		try {
			projectName = stream.getProject().getShortname();
		} catch( ClearCaseException e ) {
			out.println( ConfigurationRotator.LOGGERNAME + "Unable to get project" );
		}
		
		ClearCaseUCMConfiguration configuration = null;
		ConfigurationRotatorBuildAction action = getLastResult( build.getProject(), ClearCaseUCM.class );
		out.println( fresh ? "Job is fresh" : "Job is not fresh" );
		/* If there's no action, this is the first run */
		if( action == null || fresh ) {
			try {
				logger.debug( "Action was null(" + fresh + "), getting input as configuration" );
				configuration = ClearCaseUCMConfiguration.getConfigurationFromString( config, workspace, listener );
			} catch( ConfigurationRotatorException e ) {
				out.println( "Unable to parse input: " + e.getMessage() );
				ExceptionUtils.print( e, out, false );
				return false;
			}
		} else {
			logger.debug( "Action was NOT null" );
			/* Get the configuration from the action */
			configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
			/* Get next configuration */
			try {
				logger.debug( "Obtaining new configuration based on old" );
				nextConfiguration( listener, build, configuration, workspace, stream.getPVob() );
			} catch( Exception e ) {
				out.println( "Unable to get next configuration: " + e.getMessage() );
				ExceptionUtils.print( e, out, false );
				return false;
			}
		}
		
		out.println( "---> " + configuration );
		
		/* Just try to save */
		logger.debug( "Adding action" );
		ConfigurationRotatorBuildAction action1 = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, configuration );
		build.addAction( action1 );
		
		fresh = false;
		build.getProject().save();

		/* Adding publisher */
		out.println( "Adding publisher" );
		build.getProject().getPublishersList().add( new ConfigurationRotatorPublisher() );
		
		return true;
	}
	
	private void nextConfiguration( TaskListener listener, AbstractBuild<?, ?> build, ClearCaseUCMConfiguration configuration, FilePath workspace, PVob pvob ) throws IOException, InterruptedException, ConfigurationRotatorException {
		Project project = null;

		logger.debug( "Getting project" );
		project = workspace.act( new DetermineProject( Arrays.asList( new String[] { "jenkins", projectName } ), pvob ) );
		
		logger.debug( "Project is " + project );
		
		Baseline oldest = null, current;
		ClearCaseUCMConfigurationComponent chosen = null;
		
		logger.debug( "Foreach configuration component" );
		for( ClearCaseUCMConfigurationComponent config : configuration.getList() ) {
			logger.debug( "CONFIG: " + config );
			/* This configuration is not fixed */
			if( !config.isFixed() ) {
				logger.debug( "Wasn't fixed: " + config.getBaseline().getNormalizedName() );
				try {
					current = workspace.act( new GetBaselines( listener, config.getBaseline().getComponent(), config.getBaseline().getStream(), config.getPlevel(), 1, config.getBaseline() ) ).get( 0 );
					if( oldest == null || current.getDate().before( oldest.getDate() ) )  {
						logger.debug( "Was older: " + current );
						oldest = current;
						chosen = config;
					}

				} catch( Exception e ) {
					/* No baselines found */
					logger.debug( "No baselines found: " + e.getMessage() );
					ExceptionUtils.print( e, listener.getLogger(), false );
				}
				
			}
		}
		
		/**/
		if( chosen != null && oldest != null ) {
			logger.debug( "There was a baseline: " + oldest );
			chosen.setBaseline( oldest );
		} else {
			listener.getLogger().println( ConfigurationRotator.LOGGERNAME + "" );
			throw new ConfigurationRotatorException( "No new baselines" );
		}
		
		/* Create baselines list */
		List<Baseline> selectedBaselines = new ArrayList<Baseline>();
		logger.debug( "Selected baselines:" );
		for( ClearCaseUCMConfigurationComponent config : configuration.getList()) {
			logger.debug( config.getBaseline().getNormalizedName() );
			selectedBaselines.add( config.getBaseline() );
		}
		
		/* Make a view tag*/
		String viewtag = "cr-" + build.getProject().getDisplayName().replaceAll( "\\s", "_" ) + "-" + System.getenv( "COMPUTERNAME" );
		
		SnapshotView view = workspace.act( new PrepareWorkspace( project, selectedBaselines, viewtag, listener ) );
	}
	


	@Extension
	public static final class ClearCaseUCMDescriptor extends ConfigurationRotatorSCMDescriptor<ClearCaseUCM> {

		@Override
		public String getDisplayName() {
			return "ClearCase UCM";
		}
		
		public FormValidation doTest(  ) throws IOException, ServletException {
			return FormValidation.ok();
		}

	}
}
