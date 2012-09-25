package net.praqma.jenkins.configrotator;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;

public abstract class ConfigurationRotatorSCMDescriptor<CC extends AbstractConfigurationComponent, C extends AbstractConfiguration, T extends AbstractConfigurationRotatorSCM<CC, C>> extends Descriptor<AbstractConfigurationRotatorSCM<CC, C>> {

	public AbstractConfigurationRotatorSCM newInstance( StaplerRequest req, JSONObject formData, AbstractConfigurationRotatorSCM instance ) throws FormException {
		return super.newInstance( req, formData );
	}
}
