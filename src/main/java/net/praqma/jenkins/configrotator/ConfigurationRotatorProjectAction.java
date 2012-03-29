package net.praqma.jenkins.configrotator;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.ProminentProjectAction;
import hudson.scm.SCM;

public class ConfigurationRotatorProjectAction extends Actionable implements ProminentProjectAction {

	AbstractProject<?, ?> project;
	
	public ConfigurationRotatorProjectAction( AbstractProject<?, ?> project ) {
		this.project = project;
	}
	
	@Override
	public String getIconFileName() {
		return "graph.gif";
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
	
	/*
	public void doReset( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
		SCM scm = project.getScm();
		if( scm instanceof ConfigurationRotator ) {
			((ConfigurationRotator)scm).setFresh( project, true );
			rsp.forwardToPreviousPage( req );
		} else {
			rsp.sendError( StaplerResponse.SC_BAD_REQUEST, "Not Configuration Rotator job" );
		}
	}
	*/
	
	public ConfigurationRotatorBuildAction getLastAction() {
		SCM scm = project.getScm();
		if( scm instanceof ConfigurationRotator ) {
			return ((ConfigurationRotator)scm).getAcrs().getLastResult( project, null );
		} else {
			return null;
		}
	}
	
}
