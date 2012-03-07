package net.praqma.jenkins.configrotator;

import java.util.ArrayList;
import java.util.List;

import jenkins.model.Jenkins;
import hudson.DescriptorExtensionList;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;

public abstract class ConfigurationRotatorSCM extends SCM implements Interface {

	public abstract String getName();

	@Override
	public SCMDescriptor<?> getDescriptor() {
		return (SCMDescriptor<?>) Jenkins.getInstance().getDescriptorOrDie( getClass() );
	}

	
	public static List<SCMDescriptor<ConfigurationRotatorSCM>> getDescriptors() {
		List<SCMDescriptor<ConfigurationRotatorSCM>> list = new ArrayList<SCMDescriptor<ConfigurationRotatorSCM>>();
		for( SCMDescriptor<?> d : all() ) {
			
			if( d.getKlass().equals( ConfigurationRotatorSCM.class ) ) {
				list.add( (SCMDescriptor<ConfigurationRotatorSCM>)d );
			}
		}
		
		return list;
	}
}
