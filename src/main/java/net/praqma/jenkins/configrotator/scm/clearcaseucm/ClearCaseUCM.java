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
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.scm.PollingResult;
import hudson.util.FormValidation;

import net.praqma.clearcase.util.ExceptionUtils;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.jenkins.configrotator.AbstractConfigurationRotatorSCM;
import net.praqma.jenkins.configrotator.ConfigurationRotator;
import net.praqma.jenkins.configrotator.ConfigurationRotatorBuildAction;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.jenkins.configrotator.ConfigurationRotatorSCMDescriptor;
import net.praqma.jenkins.utils.remoting.DetermineProject;
import net.praqma.jenkins.utils.remoting.GetBaselines;
import net.praqma.util.debug.Logger;
import net.sf.json.JSONObject;

public class ClearCaseUCM extends AbstractConfigurationRotatorSCM implements Serializable {
	
	private static Logger logger = Logger.getLogger();
	
	private ClearCaseUCMConfiguration projectConfiguration;
	
	public List<ClearCaseUCMTarget> targets;
	
	private PVob pvob;

	/**
	 * Version 0.1.0 constructor
	 * 
	 * Parse config
	 * Each line represents a {@link Component}, {@link Stream}, {@link Baseline} and a {@Plevel plevel}
	 * @param config
	 */
	@DataBoundConstructor
	public ClearCaseUCM( String pvobName ) {
		pvob = new PVob( pvobName );
	}
	
	public String getPvobName() {
		return pvob.toString();
	}

	@Override
	public String getName() {
		return "ClearCase UCM";
	}
	
	@Override
	public boolean wasReconfigured( AbstractProject<?, ?> project ) {
		ConfigurationRotatorBuildAction action = getLastResult( project, ClearCaseUCM.class );
		
		if( action == null ) {
			return true;
		}
		
		ClearCaseUCMConfiguration configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
		
		/* Check if the project configuration is even set */
		if( configuration == null ) {
			return true;
		}
		
		/* Check if the sizes are equal */
		if( targets.size() != configuration.getList().size() ) {
			return true;
		}
		
		/**/
		List<ClearCaseUCMTarget> list = getConfigurationAsTargets( configuration );
		for( int i = 0 ; i < targets.size() ; ++i ) {
			if( !targets.get( i ).equals( list.get( i ) ) ) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, boolean reconfigure ) throws IOException {
		PrintStream out = listener.getLogger();
		
		ConfigurationRotatorBuildAction action = getLastResult( build.getProject(), ClearCaseUCM.class );
		if( reconfigure ) {
			out.println( ConfigurationRotator.LOGGERNAME + "Job has been reconfigured" );
		}
		
		/* If there's no action, this is the first run */
		if( action == null || reconfigure ) {
			logger.debug( "Action was null(" + reconfigure + "), getting input as configuration" );
			
			/* Resolve the configuration */
			ClearCaseUCMConfiguration inputconfiguration = null;
			try {
				inputconfiguration = ClearCaseUCMConfiguration.getConfigurationFromTargets( getTargets(), workspace, listener );
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
		
		printConfiguration( out, projectConfiguration );
		
		/* Create the view */
		try {
			out.println( ConfigurationRotator.LOGGERNAME + "Creating view" );
			SnapshotView view = createView( listener, build, projectConfiguration, workspace, pvob );
			projectConfiguration.setView( view );
		} catch( Exception e ) {
			out.println( ConfigurationRotator.LOGGERNAME + "Unable to create view: " + e.getMessage() );
			ExceptionUtils.print( e, out, false );
			throw new AbortException();
		}
				
		/* Just try to save */
		logger.debug( "Adding action" );
		final ConfigurationRotatorBuildAction action1 = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, projectConfiguration );
		build.addAction( action1 );
		
		return true;
	}
	
	public void printConfiguration( PrintStream out, ClearCaseUCMConfiguration config ) {
		out.println( "The configuration is:" );
		for( ClearCaseUCMConfigurationComponent c : projectConfiguration.getList() ) {
			out.println( " * " + c.getBaseline().getComponent() + ", " + c.getBaseline().getStream() + ", " + c.getBaseline().getNormalizedName() );
		}
		out.println( "" );
	}
	
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
		project = workspace.act( new DetermineProject( Arrays.asList( new String[] { "jenkins", "Jenkins", "hudson", "Hudson" } ), pvob ) );
		
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
		if( projectConfiguration != null ) {
			return getConfigurationAsTargets( projectConfiguration );
		} else {
			return targets;
		}
	}
	
	private List<ClearCaseUCMTarget> getConfigurationAsTargets( ClearCaseUCMConfiguration config ) {
		List<ClearCaseUCMTarget> list = new ArrayList<ClearCaseUCMTarget>();
		if( config.getList() != null && config.getList().size() > 0 ) {
			for( ClearCaseUCMConfigurationComponent c : config.getList() ) {
				if( c != null ) {
					list.add( new ClearCaseUCMTarget( c.getBaseline().getNormalizedName() + ", " + c.getPlevel().toString() + ", " + c.isFixed() ) );
				} else {
					/* A null!? The list is corrupted, return targets */
					return targets;
				}
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
			project.save();
		} else {
			throw new AbortException( "Not a valid configuration" );
		}
	}

	@Override
	public PollingResult poll( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener ) throws IOException, InterruptedException {
		PrintStream out = listener.getLogger();
		out.println( ConfigurationRotator.LOGGERNAME + "Polling" );
		
		if( projectConfiguration == null ) {
			
		}
		
		ConfigurationRotatorBuildAction action = getLastResult( project, ClearCaseUCM.class );
		
		if( action == null ) {
			out.println( ConfigurationRotator.LOGGERNAME + "No previous actions, build now" );
			return PollingResult.BUILD_NOW;
		}
		
		ClearCaseUCMConfiguration configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
		
		if( configuration != null ) {
			out.println( ConfigurationRotator.LOGGERNAME + "Configuration is not null" );
			try {
				ClearCaseUCMConfiguration other;
				other = nextConfiguration( listener, configuration, workspace );
				if( other != null ) {
					printConfiguration( out, other );
					return PollingResult.BUILD_NOW;
				} else {
					out.println( ConfigurationRotator.LOGGERNAME + "No changes!" );
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
			ClearCaseUCM instance = (ClearCaseUCM)i;
			
			List<ClearCaseUCMTarget> targets = req.bindJSONToList( ClearCaseUCMTarget.class, formData.getJSONObject( "acrs" ).getJSONArray( "targets" ) );
			instance.targets = targets;
			
			save();
			return instance;
		}
		
		public List<ClearCaseUCMTarget> getTargets( ClearCaseUCM instance ) {
			if( instance == null ) {
				return new ArrayList<ClearCaseUCMTarget>();
			} else {
				return instance.getTargets();
			}
		}
		
	}
}
