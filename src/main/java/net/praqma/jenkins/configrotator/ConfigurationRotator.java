package net.praqma.jenkins.configrotator;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.List;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
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
	
	@DataBoundConstructor
	public ConfigurationRotator( AbstractConfigurationRotatorSCM acrs ) {
		this.acrs = acrs;
	}
	
	public AbstractConfigurationRotatorSCM getAcrs() {
		return acrs;
	}

	@Override
	public SCMRevisionState calcRevisionsFromBuild( AbstractBuild<?, ?> arg0, Launcher arg1, TaskListener arg2 ) throws IOException, InterruptedException {
		if( !acrs.isFresh() ) {
			return new SCMRevisionState() {};
		} else {
			return null;
		}
	}

	@Override
	public boolean checkout( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File file ) throws IOException, InterruptedException {
		PrintStream out = listener.getLogger();
		
		out.println( LOGGERNAME + "Check out" );
		
		try {
			acrs.perform( build, launcher, workspace, listener );
		} catch( AbortException e ) {
			out.println( LOGGERNAME + "Failed to check out" );
			throw e;
		}
		
		/* If not aborted, add publisher */
		out.println( LOGGERNAME + "Adding publisher" );
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
		
		/*
		@Override
		public SCM newInstance(StaplerRequest req, JSONObject formData)	throws FormException {
			System.out.println( formData.toString( 2 ) );
			return super.newInstance(req, formData);
		}
		*/


		/*
		@Override
		public ConfigurationRotator newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			System.out.println( formData.toString( 2 ) );
			
			Class<?> cl = null;
			JSONObject acrs = (JSONObject)formData.get( "acrs" );
			try {
				cl = Class.forName( (String) acrs.get( "stapler-class" ) );
			} catch( ClassNotFoundException e ) {
				throw new FormException( "WHAT?", "THE?!" );
			}
			System.out.println( "CLASS: " + cl );
			AbstractConfigurationRotatorSCM scm = (AbstractConfigurationRotatorSCM) req.bindJSON( cl, acrs );
			//AbstractConfigurationRotatorSCM scm = cl.;
			
			return new ConfigurationRotator( scm );
			//return super.newInstance(req, formData);
		}
		*/

		@Override
		public SCM newInstance( StaplerRequest req, JSONObject formData ) throws FormException {
			System.out.println( "FORM: " + formData.toString( 2 ) );
			ConfigurationRotator r = (ConfigurationRotator) super.newInstance( req, formData );
			ConfigurationRotatorSCMDescriptor<AbstractConfigurationRotatorSCM> d = (ConfigurationRotatorSCMDescriptor<AbstractConfigurationRotatorSCM>) r.getAcrs().getDescriptor();
			r.acrs = d.newInstance( req, formData, r.acrs );
			save();
			return r;
		}

		/*
		@Override
		public SCM newInstance( StaplerRequest req, JSONObject formData ) throws FormException {
			System.out.println( formData.toString( 2 ) );
			SCM instance =  super.newInstance( req, formData );
			//Class.forName( (String) formData.get( "stapler-class" ) );
			
			//ConfigurationRotator instance = req.bindJSON( ConfigurationRotator.class, formData );
			System.out.println( "---->"  + instance );
			return instance;
		}
		*/

		public List<ConfigurationRotatorSCMDescriptor<?>> getSCMs() {
			return AbstractConfigurationRotatorSCM.getDescriptors();
		}
		
	}

}
