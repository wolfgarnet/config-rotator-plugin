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
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.jenkins.configrotator.AbstractComponentConfiguration;
import net.praqma.jenkins.configrotator.AbstractConfigurationRotatorSCM;
import net.praqma.jenkins.configrotator.ConfigurationRotatorBuildAction;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.jenkins.configrotator.ConfigurationRotatorSCMDescriptor;
import net.praqma.jenkins.utils.remoting.DetermineProject;
import net.praqma.jenkins.utils.remoting.LoadEntity;
import net.praqma.util.debug.Logger;

public class ClearCaseUCM extends AbstractConfigurationRotatorSCM implements Serializable {
	
	private static Logger logger = Logger.getLogger();

	private String config;
	private List<AbstractComponentConfiguration> list = new ArrayList<AbstractComponentConfiguration>();

	
	private String streamName;
	private Stream stream;
	
	private String UCMProject;

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

	@Override
	public String getName() {
		return "ClearCase UCM";
	}

	@Override
	public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) throws IOException {
		PrintStream out = listener.getLogger();
		
		/* Resolve streamName */
		try {
			stream = (Stream) workspace.act( new LoadEntity( Stream.get( streamName, true ) ) );
		} catch( ClearCaseException e ) {
			e.print( out );
			return false;
		} catch( InterruptedException e ) {
			out.println( "Connection failed while loading + " + streamName + ": " + e.getMessage() );
			return false;
		}
		
		ConfigurationRotatorBuildAction action = getLastResult( build.getProject(), ClearCaseUCM.class );
		if( action == null ) {
			try {
				list = inputToConfiguration( workspace, listener );
			} catch( ConfigurationRotatorException e ) {
				out.println( "Unable to parse input: " + e.getMessage() );
			}
		} else {
			list = action.getConfiguration().getList();
			/* Get next configuration */
			try {
				nextConfiguration( listener, list, workspace, stream.getPVob() );
			} catch( Exception e ) {
				out.println( "Unable to get next configuration: " + e.getMessage() );
				return false;
			}
		}
		
		
		/* Just try to save */
		ConfigurationRotatorBuildAction action1 = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, new ClearCaseUCMConfigurationAction( list ) );
		build.addAction( action1 );
		
		return true;
	}
	
	private void nextConfiguration( TaskListener listener, List<AbstractComponentConfiguration> list, FilePath workspace, PVob pvob ) throws IOException, InterruptedException {
		Project project = null;

		project = workspace.act( new DetermineProject( Arrays.asList( new String[] { "jenkins" } ), pvob ) );
		
		SnapshotView view = workspace.act( new PrepareWorkspace( project, listener ) );
	}
	
	private List<AbstractComponentConfiguration> inputToConfiguration( FilePath workspace, BuildListener listener ) throws ConfigurationRotatorException, IOException {
		PrintStream out = listener.getLogger();
		/* v 1 parsing */
		
		out.println( "CONFIG: " + config );
		
		/* Parse config */
		String[] parts = config.split( "\\n" );
		
		/**/
		List<AbstractComponentConfiguration> list = new ArrayList<AbstractComponentConfiguration>();
		
		/* Each line is component, stream, baseline, plevel, type */
		for( String part : parts ) {
			final String[] units = part.split( "," );
			
			if( units.length == 4 ) {
				try {
					ClearCaseUCMComponentConfiguration config = workspace.act( new GetConfiguration( units, listener ) );
					out.println( "[Rotator] Config: " + config );
					list.add( config );
				} catch( InterruptedException e ) {
					out.println( "[Rotator] Error: " + e.getMessage() );
					
					throw new ConfigurationRotatorException( "Unable parse input", e );
				}
			} else {
				/* Do nothing */
				out.println( "[Rotator] \"" + part + "\" was not correct" );
				throw new ConfigurationRotatorException( "Wrong input, length is " + units.length );
			}
		}
		
		return list;
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
