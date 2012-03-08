package net.praqma.jenkins.configrotator.scm;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import net.praqma.jenkins.configrotator.AbstractConfigurationRotatorSCM;
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
	

	@Override
	public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Extension
	public static final class MercurialDescriptor extends ConfigurationRotatorSCMDescriptor<Mercurial> {

		@Override
		public String getDisplayName() {
			return "Mercurial";
		}
		
	}

}
