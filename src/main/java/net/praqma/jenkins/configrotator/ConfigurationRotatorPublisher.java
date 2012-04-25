package net.praqma.jenkins.configrotator;

import java.io.IOException;
import java.io.PrintStream;

import net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType;
import net.praqma.util.debug.Logger;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

public class ConfigurationRotatorPublisher extends Notifier {

	private static Logger logger = Logger.getLogger();

	public ConfigurationRotatorPublisher() {

	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Override
	public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener ) throws InterruptedException, IOException {
		PrintStream out = listener.getLogger();

		/* This must be ConfigRotator job */
		if( build.getProject().getScm() instanceof ConfigurationRotator ) {
			logger.debug( "SCM is part of ConfigRotator" );

			ConfigurationRotatorBuildAction action = build.getAction( ConfigurationRotatorBuildAction.class );
			logger.debug( "Action object is: " + action );
			if( action != null ) {
				
				if( build.getResult().isBetterOrEqualTo( Result.SUCCESS ) ) {
					action.setResult( ResultType.COMPATIBLE );
				} else {
					action.setResult( ResultType.INCOMPATIBLE );
				}

				out.println( ConfigurationRotator.LOGGERNAME + "Configuration is " + action.getResult() );
				
				return AbstractPostConfigurationRotator.doit( build.getWorkspace(), listener, action );

			} else {
				out.println( ConfigurationRotator.LOGGERNAME + "Action was null, unable to set compatability of configuration" );
			}
		} else {
			out.println( ConfigurationRotator.LOGGERNAME + "SCM not part of ConfigRotator" );
		}

		return true;
	}

	@Override
	public boolean needsToRunAfterFinalized() {
		return false;
	}

	@Override
	public Action getProjectAction( AbstractProject<?, ?> project ) {
		return new ConfigurationRotatorProjectAction( project );
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

        /**
         * Fix for save issues.
         * @param req
         * @param json
         * @return
         * @throws hudson.model.Descriptor.FormException 
         */
        
        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            save();
            return super.configure(req, json);
        }
        
        public DescriptorImpl() {
            super(ConfigurationRotatorPublisher.class);
            load();
        }
        
	}

}
