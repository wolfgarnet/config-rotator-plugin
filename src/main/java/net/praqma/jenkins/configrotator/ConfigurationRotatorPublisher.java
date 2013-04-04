package net.praqma.jenkins.configrotator;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType;
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

	private static Logger logger = Logger.getLogger( ConfigurationRotatorPublisher.class.getName() );

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
			logger.fine( "SCM is part of ConfigRotator" );

			ConfigurationRotatorBuildAction action = build.getAction( ConfigurationRotatorBuildAction.class );
			logger.fine( "Action object is: " + action );
			if( action != null ) {
				
				if( build.getResult().isBetterOrEqualTo( Result.SUCCESS ) ) {
					action.setResult( ResultType.COMPATIBLE );
				} else {
					action.setResult( ResultType.INCOMPATIBLE );
				}

				out.println( ConfigurationRotator.LOGGERNAME + "Configuration is " + action.getResult() );
				
				return AbstractPostConfigurationRotator.doit( build.getWorkspace(), listener, action );

			} else {
                DiedBecauseAction da = build.getAction( DiedBecauseAction.class );
				out.println( ConfigurationRotator.LOGGERNAME + "Action was null, unable to set compatibility of configuration" );
                logger.fine( "Die action: " + da );
                if( da != null ) {
                    logger.fine( da.toString() );
                    if( !da.died() ) {
                        hadNothingToDo( build );
                    }
                }
			}
		} else {
			out.println( ConfigurationRotator.LOGGERNAME + "SCM not part of ConfigRotator" );
		}

		return true;
	}

    public void hadNothingToDo( AbstractBuild build ) throws IOException {
        String d = build.getDescription();
        if( d != null ) {
            build.setDescription( ( d.length() > 0 ? d + "<br/>" : "" ) + "Nothing to do" );
        } else {
            build.setDescription( "Nothing to do" );
        }

        build.setResult( Result.NOT_BUILT );
    }

	@Override
	public boolean needsToRunAfterFinalized() {
		return false;
	}

	@Override
	public Collection<Action> getProjectActions( AbstractProject<?, ?> project ) {
        return Collections.singleton( (Action)new ConfigurationRotatorProjectAction( project ) );
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
