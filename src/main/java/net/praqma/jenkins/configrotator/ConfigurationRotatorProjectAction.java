package net.praqma.jenkins.configrotator;

import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.ProminentProjectAction;
import hudson.scm.SCM;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.logging.Logger;

public class ConfigurationRotatorProjectAction implements ProminentProjectAction {

    private static Logger logger = Logger.getLogger( ConfigurationRotatorProjectAction.class.getName() );

	private final AbstractProject<?, ?> project;
	
	public ConfigurationRotatorProjectAction( AbstractProject<?, ?> project ) {
        logger.fine( "Creating a project action for " + project );
		this.project = project;
	}
	
	@Override
	public String getIconFileName() {
		return "/plugin/config-rotator/images/rotate.png";
	}

	@Override
	public String getDisplayName() {
		return "Config Rotator";
	}

	@Override
	public String getUrlName() {
		return "config-rotator";
	}

    /**
     * New method extracts the latest builds.
     * @param number
     * @return 
     */
    public ArrayList<ConfigurationRotatorBuildAction> getLastActions(int number) {
        SCM scm = project.getScm();
        if( scm instanceof ConfigurationRotator ) {
			return ((ConfigurationRotator)scm).getAcrs().getLastResults( project, null, number );
		} else {
			return null;
		}
    }
	
	public ConfigurationRotatorBuildAction getLastAction() {
		SCM scm = project.getScm();
		if( scm instanceof ConfigurationRotator ) {
			return ((ConfigurationRotator)scm).getAcrs().getLastResult( project, null );
		} else {
			return null;
		}
	}
	
}
