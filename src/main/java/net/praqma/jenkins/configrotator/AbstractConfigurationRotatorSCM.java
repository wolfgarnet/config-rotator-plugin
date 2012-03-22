package net.praqma.jenkins.configrotator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import net.praqma.util.debug.Logger;

import jenkins.model.Jenkins;
import hudson.DescriptorExtensionList;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.Saveable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;

public abstract class AbstractConfigurationRotatorSCM implements Describable<AbstractConfigurationRotatorSCM>, ExtensionPoint {
	
	private static Logger logger = Logger.getLogger();
	
	/**
	 * Determines whether a new configuration has been entered.
	 * If true, the input is new.
	 */
	public boolean fresh;

	public abstract String getName();
	
	public abstract boolean perform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) throws IOException;

	public boolean isFresh() {
		return fresh;
	}
	
	public void setFresh( AbstractProject<?, ?> project, boolean fresh ) throws IOException {
		this.fresh = fresh;
		project.save();
	}
		
	/*
	public void doReset( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
		fresh = true;
		rsp.forwardToPreviousPage( req );
	}
	*/
	
	
	
	@Override
	public Descriptor<AbstractConfigurationRotatorSCM> getDescriptor() {
		return (ConfigurationRotatorSCMDescriptor<?>) Jenkins.getInstance().getDescriptorOrDie( getClass() );
	}

	/**
	 * All registered {@link AbstractConfigurationRotatorSCM}s.
	 */
	public static DescriptorExtensionList<AbstractConfigurationRotatorSCM, ConfigurationRotatorSCMDescriptor<AbstractConfigurationRotatorSCM>> all() {
		return Jenkins.getInstance().<AbstractConfigurationRotatorSCM, ConfigurationRotatorSCMDescriptor<AbstractConfigurationRotatorSCM>> getDescriptorList( AbstractConfigurationRotatorSCM.class );
	}
	
	public static List<ConfigurationRotatorSCMDescriptor<?>> getDescriptors() {
		List<ConfigurationRotatorSCMDescriptor<?>> list = new ArrayList<ConfigurationRotatorSCMDescriptor<?>>();
		for( ConfigurationRotatorSCMDescriptor<?> d : all() ) {
			list.add( d );
		}
		
		return list;
	}
	
	public ConfigurationRotatorBuildAction getLastResult( AbstractProject<?, ?> project, Class<? extends AbstractConfigurationRotatorSCM> clazz ) {
		logger.debug( "Getting last result" );
		
		for( AbstractBuild<?, ?> b = getLastBuildToBeConsidered( project ); b != null; b = b.getPreviousNotFailedBuild() ) {
			logger.debug( "b: " + b.getFullDisplayName() );
			ConfigurationRotatorBuildAction r = b.getAction( ConfigurationRotatorBuildAction.class );
			if( r != null ) {
				logger.debug( "r: " + r.getResult() );
				if( r.isDetermined() && ( clazz == null || r.getClazz().equals( clazz ) ) ) {
					logger.debug( "I got one" );
					return r;
				}
			}
		}
		
		return null;
	}
	
	private AbstractBuild<?, ?> getLastBuildToBeConsidered( AbstractProject<?, ?> project ) {
		return project.getLastCompletedBuild();
	}
}
