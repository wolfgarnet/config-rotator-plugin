package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
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

	@Extension
	public static final class ClearCaseUCMDescriptor extends ConfigurationRotatorSCMDescriptor<ClearCaseUCM> {

		@Override
		public String getDisplayName() {
			return "ClearCase UCM";
		}
		
	}
}
