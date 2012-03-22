package net.praqma.jenkins.configrotator;

import jenkins.model.Jenkins;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.TaskListener;

public abstract class AbstractPostConfigurationRotator implements ExtensionPoint {

	public abstract boolean perform( TaskListener listener, ConfigurationRotatorBuildAction action );
	public abstract Class<? extends AbstractConfigurationRotatorSCM> tiedTo();

	public static boolean doit( TaskListener listener, ConfigurationRotatorBuildAction action ) {
		for( AbstractPostConfigurationRotator l : all() ) {
			if( l.tiedTo().equals( action.getClazz() ) ) {
				if( !l.perform( listener, action ) ) {
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
