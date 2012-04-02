package net.praqma.jenkins.configrotator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.praqma.util.debug.Logger;

import jenkins.model.Jenkins;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.TaskListener;
import hudson.model.Descriptor;
import hudson.scm.PollingResult;

public abstract class AbstractConfigurationRotatorSCM implements Describable<AbstractConfigurationRotatorSCM>, ExtensionPoint {
	
	private static Logger logger = Logger.getLogger();
	
	public abstract String getName();
	
	public abstract PollingResult poll( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener ) throws IOException, InterruptedException;
	
	public abstract boolean perform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, boolean reconfigure ) throws IOException;
	
	public abstract void setConfigurationByAction( AbstractProject<?, ?> project, ConfigurationRotatorBuildAction action ) throws IOException;
	
	public abstract boolean wasReconfigured( AbstractProject<?, ?> project );
		
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
			ConfigurationRotatorBuildAction r = b.getAction( ConfigurationRotatorBuildAction.class );
			if( r != null ) {
				if( r.isDetermined() && ( clazz == null || r.getClazz().equals( clazz ) ) ) {
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
