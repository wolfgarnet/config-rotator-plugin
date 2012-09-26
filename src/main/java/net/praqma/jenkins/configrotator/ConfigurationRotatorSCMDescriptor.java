package net.praqma.jenkins.configrotator;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;

public abstract class ConfigurationRotatorSCMDescriptor<T extends AbstractConfigurationRotatorSCM> extends Descriptor<AbstractConfigurationRotatorSCM> {

	public AbstractConfigurationRotatorSCM newInstance( StaplerRequest req, JSONObject formData, AbstractConfigurationRotatorSCM instance ) throws FormException {
		return super.newInstance( req, formData );
	}

    public abstract String getFeedModuleName();
}
