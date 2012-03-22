package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.Extension;
import hudson.model.TaskListener;
import net.praqma.jenkins.configrotator.AbstractConfigurationRotatorSCM;
import net.praqma.jenkins.configrotator.AbstractPostConfigurationRotator;
import net.praqma.jenkins.configrotator.ConfigurationRotatorBuildAction;

@Extension
public class ClearCaseUCMPostBuild extends AbstractPostConfigurationRotator {

	@Override
	public boolean perform( TaskListener listener, ConfigurationRotatorBuildAction action ) {
		listener.getLogger().println( "In post build" );
		return true;
	}

	@Override
	public Class<? extends AbstractConfigurationRotatorSCM> tiedTo() {
		return ClearCaseUCM.class;
	}

}
