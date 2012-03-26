package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.scm.PollingResult;
import hudson.util.FormValidation;

import net.praqma.clearcase.util.ExceptionUtils;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
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
	
	private ClearCaseUCMConfiguration origin;
	
	transient private Stream stream;
	transient private String projectName;
	
	private boolean printDebug;

	/**
	 * Version 0.1.0 constructor
	 * 
	 * Parse config
	 * Each line represents a {@link Component}, {@link Stream}, {@link Baseline} and a {@Plevel plevel}
	 * @param config
	 */
	@DataBoundConstructor
	public ClearCaseUCM( String streamName, String config, boolean printDebug ) {
		this.config = config;
		this.streamName = streamName;
		
		this.printDebug = printDebug;
	}

	public String getConfig() {
		return config;
	}
	
	public String getStreamName() {
		return streamName;
	}
	
	public boolean doPrintDebug() {
		return printDebug;
	}

	@Override
	public String getName() {
		return "ClearCase UCM";
	}

	@Override
	public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) throws IOException {
		PrintStream out = listener.getLogger();
		
		if( printDebug ) {
			Appender app = new StreamAppender( out );
			app.setMinimumLevel( LogLevel.DEBUG );
			app.setTemplate( "[%level]%space %message%newline" );
			app.lockToCurrentThread();
			Logger.addAppender( app );
		}
		
		/* Resolve streamName */
		try {
			logger.debug( "Resolving " + streamName );
			stream = (Stream) workspace.act( new LoadEntity( Stream.get( streamName ) ) );
		} catch( ClearCaseException e ) {
			e.print( out );
			throw new AbortException();
		} catch( Exception e ) {
			out.println( ConfigurationRotator.LOGGERNAME + "Unable to load " + streamName + ": " + e.getMessage() );
			throw new AbortException();
		}
		
		projectName = stream.getProject().getShortname();
		
		ClearCaseUCMConfiguration inputconfiguration = null;
		try {
			inputconfiguration = ClearCaseUCMConfiguration.getConfigurationFromString( config, workspace, listener );
		} catch( ConfigurationRotatorException e ) {
			out.println( "Unable to parse configuration: " + e.getMessage() );
			ExceptionUtils.print( e, out, false );
			throw new AbortException();
		}
		
		ClearCaseUCMConfiguration configuration = null;
		ConfigurationRotatorBuildAction action = getLastResult( build.getProject(), ClearCaseUCM.class );
		out.println( fresh ? "Job is fresh" : "Job is not fresh" );
		/* If there's no action, this is the first run */
		if( action == null || fresh ) {
			logger.debug( "Action was null(" + fresh + "), getting input as configuration" );
			configuration = inputconfiguration;
			if( origin == null ) {
				origin = inputconfiguration;
			}
		} else {
			logger.debug( "Action was NOT null" );
			/* Get the configuration from the action */
			configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
			/* Get next configuration */
			try {
				logger.debug( "Obtaining new configuration based on old" );
				/* No new baselines */
				if( !nextConfiguration( listener, configuration, workspace ) ) {
					return false;
				}
			} catch( Exception e ) {
				out.println( "Unable to get next configuration: " + e.getMessage() );
				ExceptionUtils.print( e, out, false );
				throw new AbortException();
			}
			
			/* Update configuration */
			try {
				updateConfiguration( configuration );
			} catch( ClearCaseException e ) {
				out.println( "Unable to get update configuration: " + e.getMessage() );
				ExceptionUtils.print( e, out, false );
				throw new AbortException();
			}
		}
		
		/* Create the view */
		try {
			out.println( ConfigurationRotator.LOGGERNAME + "Creating view" );
			SnapshotView view = createView( listener, build, configuration, workspace, stream.getPVob() );
			configuration.setView( view );
		} catch( Exception e ) {
			out.println( ConfigurationRotator.LOGGERNAME + "Unable to create view: " + e.getMessage() );
			ExceptionUtils.print( e, out, false );
			throw new AbortException();
		}
		
		out.println( "---> " + configuration );
		
		/* Just try to save */
		logger.debug( "Adding action" );
		ConfigurationRotatorBuildAction action1 = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, configuration );
		build.addAction( action1 );
		
		fresh = false;
		build.getProject().save();
		
		return true;
	}
	
	private void updateConfiguration( ClearCaseUCMConfiguration configuration ) throws UnableToLoadEntityException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		logger.debug( "Updating configuration" );
		
		List<ClearCaseUCMConfigurationComponent> adding = new ArrayList<ClearCaseUCMConfigurationComponent>();
		
		/* Stupid N^2 running time */
		for( ClearCaseUCMConfigurationComponent c : origin.getList() ) {

			boolean add = true;
			
			for( ClearCaseUCMConfigurationComponent c2 : configuration.getList() ) {
				logger.debug( "Comparing " + c2.getBaseline().getComponent() + " and " + c.getBaseline().getComponent() );
				logger.debug( "Comparing " + c2.getBaseline().getStream() + " and " + c.getBaseline().getStream() );
				
				if( c2.getBaseline().getComponent().equals( c.getBaseline().getComponent() ) &&
					c2.getBaseline().getStream().equals( c.getBaseline().getStream() ) ) {
					logger.debug( "EQUAL!!!! Not adding!" );
					add = false;
					break;
				}
			}
			
			if( add ) {
				logger.debug( "Adding " + c );
				adding.add( c );
			}
		}
		
		logger.debug( "Processing findings" );
		
		for( ClearCaseUCMConfigurationComponent c : adding ) {
			logger.debug( "Adding " + c );
			configuration.getList().add( c );
		}
		
		logger.debug( "Done updating" );
	}
	
	private boolean nextConfiguration( TaskListener listener, ClearCaseUCMConfiguration configuration, FilePath workspace ) throws IOException, InterruptedException, ConfigurationRotatorException {
		
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
			listener.getLogger().println( ConfigurationRotator.LOGGERNAME + "No new baselines" );
			return false;
		}
		
		
		return true;
	}
	
	public SnapshotView createView( TaskListener listener, AbstractBuild<?, ?> build, ClearCaseUCMConfiguration configuration, FilePath workspace, PVob pvob ) throws IOException, InterruptedException {
		Project project = null;

		logger.debug( "Getting project" );
		project = workspace.act( new DetermineProject( Arrays.asList( new String[] { "jenkins", projectName } ), pvob ) );
		
		logger.debug( "Project is " + project );
		
		/* Create baselines list */
		List<Baseline> selectedBaselines = new ArrayList<Baseline>();
		logger.debug( "Selected baselines:" );
		for( ClearCaseUCMConfigurationComponent config : configuration.getList() ) {
			logger.debug( config.getBaseline().getNormalizedName() );
			selectedBaselines.add( config.getBaseline() );
		}
		
		/* Make a view tag*/
		String viewtag = "cr-" + build.getProject().getDisplayName().replaceAll( "\\s", "_" ) + "-" + System.getenv( "COMPUTERNAME" );
		
		return workspace.act( new PrepareWorkspace( project, selectedBaselines, viewtag, listener ) );
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



	@Override
	public PollingResult poll( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener ) throws IOException, InterruptedException {
		if( origin != null ) {
			try {
				boolean n = nextConfiguration( listener, origin, workspace );
				if( n ) {
					return PollingResult.BUILD_NOW;
				} else {
					return PollingResult.NO_CHANGES;
				}
			} catch( ConfigurationRotatorException e ) {
				throw new IOException( "Unable to poll: " + e.getMessage(), e );
			}
		}
		
		return PollingResult.BUILD_NOW;
	}
}
