package net.praqma.jenkins.configrotator;

import java.io.IOException;

import hudson.model.FreeStyleProject;

import org.jvnet.hudson.test.JenkinsRule;

public class ConfigRotatorRule extends JenkinsRule {

	public FreeStyleProject createProject( String title ) throws IOException {
		return createFreeStyleProject( title );
	}
}
