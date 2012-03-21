package net.praqma.jenkins.configrotator;

import java.io.IOException;
import java.io.PrintStream;

import net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

public class ConfigurationRotatorPublisher extends Notifier {
	
	public ConfigurationRotatorPublisher() {
		
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}
	
	@Override
	public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener ) throws InterruptedException, IOException {
		PrintStream out = listener.getLogger();
		out.println( "I am here" );
		
		/* This must be ConfigRotator job */
		if( build.getProject().getScm() instanceof ConfigurationRotator ) {
			out.println( "IT IS" );
			
			ConfigurationRotatorBuildAction action = build.getAction( ConfigurationRotatorBuildAction.class );
			out.println( "Action: " + action );
			out.println( "Action: " + action.getResult() );
			if( !action.getResult().equals( ResultType.FAILED ) ) {
				if( build.getResult().isBetterOrEqualTo( Result.SUCCESS ) ) {
					action.setResult( ResultType.COMPATIBLE );
				} else {
					action.setResult( ResultType.INCOMPATIBLE );
				}
			}
		} else {
			out.println( "IT IS NOT" );
		}
		
		return true;
	}
	
	@Override
	public boolean needsToRunAfterFinalized() {
		return true;
	}
	
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		
		@Override
		public Notifier newInstance( StaplerRequest req, JSONObject formData ) throws FormException {
			return new ConfigurationRotatorPublisher();
		}

		@Override
		public boolean isApplicable( Class<? extends AbstractProject> jobType ) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Configuration Rotator Publisher";
		}
		
	}

}
