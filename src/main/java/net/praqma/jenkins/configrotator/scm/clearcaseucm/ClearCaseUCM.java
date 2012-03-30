package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.Descriptor.FormException;
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
import net.sf.json.JSONObject;

public class ClearCaseUCM extends AbstractConfigurationRotatorSCM implements Serializable {
	
	private static Logger logger = Logger.getLogger();

	//private String config;
	private String streamName;
	
	private ClearCaseUCMConfiguration projectConfiguration;
	
	public List<ClearCaseUCMTarget> targets;
	
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
	public ClearCaseUCM( String streamName, boolean printDebug ) {
		//this.config = config;
		System.out.println( "CONSTRUCTOR=" + streamName );
		this.streamName = streamName;
		//this.targets = targets;
		this.printDebug = printDebug;
		fresh = true;
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
		
		//ClearCaseUCMConfiguration configuration = null;
		ConfigurationRotatorBuildAction action = getLastResult( build.getProject(), ClearCaseUCM.class );
		out.println( fresh ? "Job is fresh" : "Job is not fresh" );
		/* If there's no action, this is the first run */
		if( action == null || fresh ) {
			logger.debug( "Action was null(" + fresh + "), getting input as configuration" );
			
			/* Resolve the configuration */
			ClearCaseUCMConfiguration inputconfiguration = null;
			try {
				inputconfiguration = ClearCaseUCMConfiguration.getConfigurationFromTargets( getTargets(), workspace, listener );
				out.println( "INPUT CONFIG IS " + inputconfiguration );
			} catch( ConfigurationRotatorException e ) {
				out.println( "Unable to parse configuration: " + e.getMessage() );
				ExceptionUtils.print( e, out, false );
				throw new AbortException();
			}
			
			projectConfiguration = inputconfiguration;
		} else {
			logger.debug( "Action was NOT null" );
			/* Get the configuration from the action */
			ClearCaseUCMConfiguration configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
			/* Get next configuration */
			try {
				logger.debug( "Obtaining new configuration based on old" );
				/* No new baselines */
				if( ( projectConfiguration = nextConfiguration( listener, configuration, workspace ) ) == null ) {
					return false;
				}
			} catch( Exception e ) {
				out.println( "Unable to get next configuration: " + e.getMessage() );
				ExceptionUtils.print( e, out, false );
				throw new AbortException();
			}
		}
		
		/* Store the next configuration as the project configuration */
		//projectConfiguration = configuration;
		
		/* Create the view */
		try {
			out.println( ConfigurationRotator.LOGGERNAME + "Creating view" );
			SnapshotView view = createView( listener, build, projectConfiguration, workspace, stream.getPVob() );
			projectConfiguration.setView( view );
		} catch( Exception e ) {
			out.println( ConfigurationRotator.LOGGERNAME + "Unable to create view: " + e.getMessage() );
			ExceptionUtils.print( e, out, false );
			throw new AbortException();
		}
		
		out.println( "---> " + projectConfiguration );
		
		/**/
		out.println( "The configuration is:" );
		for( ClearCaseUCMConfigurationComponent c : projectConfiguration.getList() ) {
			out.println( " * " + c.getBaseline().getNormalizedName() );
		}
		out.println( "" );

		fresh = false;
		build.getProject().save();
		
		/* Just try to save */
		logger.debug( "Adding action" );
		final ConfigurationRotatorBuildAction action1 = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, projectConfiguration );
		build.addAction( action1 );
		
		return true;
	}
	
	/*
	private void updateConfiguration( ClearCaseUCMConfiguration configuration ) throws UnableToLoadEntityException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		logger.debug( "Updating configuration" );
		
		List<ClearCaseUCMConfigurationComponent> adding = new ArrayList<ClearCaseUCMConfigurationComponent>();
		
		for( ClearCaseUCMConfigurationComponent c : origin.getList() ) {

			boolean add = true;
			
			for( ClearCaseUCMConfigurationComponent c2 : configuration.getList() ) {
				logger.debug( "Comparing " + c2.getBaseline().getComponent() + " and " + c.getBaseline().getComponent() );
				logger.debug( "Comparing " + c2.getBaseline().getStream() + " and " + c.getBaseline().getStream() );
				
				if( c2.getBaseline().getComponent().equals( c.getBaseline().getComponent() ) &&
					c2.getBaseline().getStream().equals( c.getBaseline().getStream() ) && !c.doChange() ) {
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
*/
	
	private ClearCaseUCMConfiguration nextConfiguration( TaskListener listener, ClearCaseUCMConfiguration configuration, FilePath workspace ) throws IOException, InterruptedException, ConfigurationRotatorException {
		
		Baseline oldest = null, current;
		ClearCaseUCMConfigurationComponent chosen = null;
		
		ClearCaseUCMConfiguration nconfig = configuration.clone();
		
		logger.debug( "Foreach configuration component" );
		for( ClearCaseUCMConfigurationComponent config : nconfig.getList() ) {
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
			return null;
		}
		
		
		return nconfig;
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
	
	public List<ClearCaseUCMTarget> getTargets() {
		return getConfigurationAsTargets( projectConfiguration );
	}
	
	private List<ClearCaseUCMTarget> getConfigurationAsTargets( ClearCaseUCMConfiguration config ) {
		List<ClearCaseUCMTarget> list = new ArrayList<ClearCaseUCMTarget>();
		if( config != null ) {
			for( ClearCaseUCMConfigurationComponent c : config.getList() ) {
				ClearCaseUCMTarget t = new ClearCaseUCMTarget();
				t.setComponent( c.getBaseline().getNormalizedName() + ", " + c.getPlevel().toString() + ", " + c.isFixed() );
				t.setChange( c.doChange() );
				list.add( t );
				//list.add( new ClearCaseUCMTarget( c.getBaseline().getFullyQualifiedName(), c.doChange() ) );
			}
			
			return list;
		} else {
			return targets;
		}
	}

	public void setConfigurationByAction( AbstractProject<?, ?> project, ConfigurationRotatorBuildAction action ) throws IOException {
		if( action.getConfiguration() instanceof ClearCaseUCMConfiguration ) {
			ClearCaseUCMConfiguration c = (ClearCaseUCMConfiguration)action.getConfiguration();
			this.projectConfiguration = c;
			setFreshness( true );
			project.save();
		} else {
			throw new AbortException( "Not a valid configuration" );
		}
	}

	@Override
	public PollingResult poll( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener ) throws IOException, InterruptedException {
		PrintStream out = listener.getLogger();
		if( projectConfiguration != null ) {
			try {
				ClearCaseUCMConfiguration other;
				other = nextConfiguration( listener, projectConfiguration, workspace );
				/**/
				out.println( "The configuration is:" );
				for( ClearCaseUCMConfigurationComponent c : projectConfiguration.getList() ) {
					out.println( " * " + c.getBaseline().getNormalizedName() );
				}
				out.println( "" );
				
				if( other != null ) {
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
	
	@Extension
	public static final class DescriptorImpl extends ConfigurationRotatorSCMDescriptor<ClearCaseUCM> {

		@Override
		public String getDisplayName() {
			return "ClearCase UCM";
		}
		
		public FormValidation doTest(  ) throws IOException, ServletException {
			return FormValidation.ok();
		}

		
		@Override
		public AbstractConfigurationRotatorSCM newInstance( StaplerRequest req, JSONObject formData, AbstractConfigurationRotatorSCM i ) throws FormException {
			System.out.println( "BAM!" );
			//ClearCaseUCM instance = req.bindJSON( ClearCaseUCM.class, formData );
			System.out.println( formData.toString( 2 ) );
			System.out.println( "FORMDATA: " + formData.getJSONObject( "acrs" ).getJSONArray( "targets" ) );
			ClearCaseUCM instance = (ClearCaseUCM)i;
			//List<ClearCaseUCMTarget> targets = req.bindParametersToList( ClearCaseUCMTarget.class, "cc.target." );
			List<ClearCaseUCMTarget> targets = req.bindJSONToList( ClearCaseUCMTarget.class, formData.getJSONObject( "acrs" ).getJSONArray( "targets" ) );
			System.out.println( "Targets before: " + instance.targets );
			instance.targets = targets;
			System.out.println( "Targets after: " + instance.targets );
			System.out.println( "Targets: " + targets );
			System.out.println( "STREAM: " + instance.getStreamName() );
			save();
			return instance;
		}
		
		public List<ClearCaseUCMTarget> getTargets( ClearCaseUCM instance ) {
			System.out.println("WHOA!");
			if( instance == null ) {
				System.out.println("1");
				return new ArrayList<ClearCaseUCMTarget>();
			} else {
				System.out.println("2");
				//return instance.targets;
				return instance.getTargets();
			}
		}
		
	}
}
