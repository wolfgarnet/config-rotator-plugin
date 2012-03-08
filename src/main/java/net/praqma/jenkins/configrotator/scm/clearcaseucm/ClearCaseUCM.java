package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.FormValidation;
import net.praqma.jenkins.configrotator.AbstractConfigurationRotatorSCM;
import net.praqma.jenkins.configrotator.ConfigurationRotatorSCMDescriptor;

public class ClearCaseUCM extends AbstractConfigurationRotatorSCM {

	private String baseline;

	@DataBoundConstructor
	public ClearCaseUCM( String baseline ) {
		this.baseline = baseline;
	}

	public String getBaseline() {
		return baseline;
	}

	@Override
	public String getName() {
		return "ClearCase UCM";
	}

	@Override
	public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Extension
	public static final class ClearCaseUCMDescriptor extends ConfigurationRotatorSCMDescriptor<ClearCaseUCM> {

		@Override
		public String getDisplayName() {
			return "ClearCase UCM";
		}
		
		public List<String> getConfigurations() {
			String[] t = { "1", "2", "3" };
			return Arrays.asList( t );
		}

		public FormValidation doTest(  ) throws IOException, ServletException {
			return FormValidation.ok();
		}

	}
}
