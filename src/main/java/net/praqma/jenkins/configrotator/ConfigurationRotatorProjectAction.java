package net.praqma.jenkins.configrotator;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.ProminentProjectAction;

public class ConfigurationRotatorProjectAction extends Actionable implements ProminentProjectAction {

	AbstractProject<?, ?> project;
	
	public ConfigurationRotatorProjectAction( AbstractProject<?, ?> project ) {
		this.project = project;
	}
	
	@Override
	public String getIconFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplayName() {
		return "Config Rotator";
	}

	@Override
	public String getUrlName() {
		return "config-rotator";
	}

	@Override
	public String getSearchUrl() {
		return getUrlName();
	}
	
	public ConfigurationRotatorBuildAction getLastBuild( Class<? extends AbstractConfigurationRotatorSCM> clazz ) {
		for( AbstractBuild<?, ?> b = getLastBuildToBeConsidered(); b != null; b = b.getPreviousNotFailedBuild() ) {
			
			ConfigurationRotatorBuildAction r = b.getAction( ConfigurationRotatorBuildAction.class );
			if( r != null && r.isDetermined() && r.getClazz().equals( clazz ) ) {
				return r;
			}
		}
		
		return null;
	}
	
	private AbstractBuild<?, ?> getLastBuildToBeConsidered() {
		return project.getLastCompletedBuild();
	}
}
