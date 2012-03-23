package net.praqma.jenkins.configrotator;

import jenkins.model.Jenkins;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.model.TaskListener;

public abstract class AbstractPostConfigurationRotator implements ExtensionPoint {

	public abstract boolean perform( FilePath workspace, TaskListener listener, ConfigurationRotatorBuildAction action );
	public abstract Class<? extends AbstractConfigurationRotatorSCM> tiedTo();

	public static boolean doit( FilePath workspace, TaskListener listener, ConfigurationRotatorBuildAction action ) {
		for( AbstractPostConfigurationRotator l : all() ) {
			if( l.tiedTo().equals( action.getClazz() ) ) {
				if( !l.perform( workspace, listener, action ) ) {
					return false;
				}
			}
		}
		
		return true;
	}

	public static ExtensionList<AbstractPostConfigurationRotator> all() {
		return Jenkins.getInstance().getExtensionList( AbstractPostConfigurationRotator.class );
	}

}
