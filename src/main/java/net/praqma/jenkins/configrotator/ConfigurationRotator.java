package net.praqma.jenkins.configrotator;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.scm.SCM;
import hudson.tasks.Publisher;

public class ConfigurationRotator extends SCM {
	
	private AbstractConfigurationRotatorSCM acrs;
	
	public enum ResultType {
		COMPATIBLE,   /* Tested and configuration is compatible */
		INCOMPATIBLE, /* Tested and configuration is NOT compatible */
		FAILED,       /* The tests failed and was unable to determine compatibility */
		UNDETERMINED
	}
	
	public static final String NAME = "ConfigRotator";
	public static final String LOGGERNAME = "[" + NAME + "] ";
	
	public boolean justConfigured = false;
	
	/**
	 * Determines whether a new configuration has been entered.
	 * If true, the input is new.
	 */
	public boolean reconfigure;
	
	@DataBoundConstructor
	public ConfigurationRotator( AbstractConfigurationRotatorSCM acrs ) {
		this.acrs = acrs;
		this.justConfigured = true;
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
	
	@Override
	public SCMRevisionState calcRevisionsFromBuild( AbstractBuild<?, ?> arg0, Launcher arg1, TaskListener arg2 ) throws IOException, InterruptedException {
		if( !doReconfigure() ) {
			return new SCMRevisionState() {};
		} else {
			return null;
		}
	}

	@Override
	public boolean checkout( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File file ) throws IOException, InterruptedException {
		PrintStream out = listener.getLogger();
		
		out.println( LOGGERNAME + "Checking out" );
		
		/* Determine if the job was reconfigured */
		if( justConfigured ) {
			reconfigure = acrs.wasReconfigured( build.getProject() );
			out.println( "Was reconfigured: " + reconfigure );
		}
		
		try {
			acrs.perform( build, launcher, workspace, listener, reconfigure );
		} catch( AbortException e ) {
			out.println( LOGGERNAME + "Failed to check out" );
			throw e;
		}
		
		/* Config is not fresh anymore */
		reconfigure = false;
		justConfigured = false;
		build.getProject().save();
		
		/* If not aborted, add publisher */
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
	
	public void setConfigurationByAction( AbstractProject<?, ?> project, ConfigurationRotatorBuildAction action ) throws IOException {
		acrs.setConfigurationByAction( project, action );
		reconfigure = true;
	}

	@Override
	protected PollingResult compareRemoteRevisionWith( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState arg4 ) throws IOException, InterruptedException {
		return acrs.poll( project, launcher, workspace, listener );
	}

	@Override
	public ChangeLogParser createChangeLogParser() {
		// TODO Auto-generated method stub
		return null;
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
