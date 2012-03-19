package net.praqma.jenkins.configrotator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;

public abstract class AbstractConfigurationRotatorSCM implements Describable<AbstractConfigurationRotatorSCM>, ExtensionPoint {

	public abstract String getName();
	
	public abstract boolean perform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) throws IOException;

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
		for( AbstractBuild<?, ?> b = getLastBuildToBeConsidered( project ); b != null; b = b.getPreviousNotFailedBuild() ) {
			
			ConfigurationRotatorBuildAction r = b.getAction( ConfigurationRotatorBuildAction.class );
			if( r != null && r.isDetermined() && r.getClazz().equals( clazz ) ) {
				return r;
			}
		}
		
		return null;
	}
	
	private AbstractBuild<?, ?> getLastBuildToBeConsidered( AbstractProject<?, ?> project ) {
		return project.getLastCompletedBuild();
	}
}
