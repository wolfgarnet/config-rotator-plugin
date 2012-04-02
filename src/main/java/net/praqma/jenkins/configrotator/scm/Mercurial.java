package net.praqma.jenkins.configrotator.scm;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.PollingResult;
import net.praqma.jenkins.configrotator.AbstractConfiguration;
import net.praqma.jenkins.configrotator.AbstractConfigurationRotatorSCM;
import net.praqma.jenkins.configrotator.ConfigurationRotatorBuildAction;
import net.praqma.jenkins.configrotator.ConfigurationRotatorSCMDescriptor;

public class Mercurial extends AbstractConfigurationRotatorSCM {

	private String commit;
	
	@DataBoundConstructor
	public Mercurial( String commit ) {
		this.commit = commit;
	}
	
	public String getCommit() {
		return commit;
	}
	
	@Override
	public String getName() {
		return "Mercurial";
	}
	

	@Extension
	public static final class MercurialDescriptor extends ConfigurationRotatorSCMDescriptor<Mercurial> {

		@Override
		public String getDisplayName() {
			return "Mercurial";
		}
		
	}

	@Override
	public PollingResult poll( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener ) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConfigurationByAction( AbstractProject<?, ?> project, ConfigurationRotatorBuildAction action ) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean wasReconfigured( AbstractProject<?, ?> project ) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, boolean reconfigure ) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

}
