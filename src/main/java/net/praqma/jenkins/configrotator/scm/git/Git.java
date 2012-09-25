package net.praqma.jenkins.configrotator.scm.git;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.scm.PollingResult;
import hudson.util.FormValidation;
import net.praqma.jenkins.configrotator.*;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogParser;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Git extends AbstractConfigurationRotatorSCM<GitConfiguration> implements Serializable {

    private static Logger logger = Logger.getLogger( Git.class.getName() );

    private List<GitTarget> targets = new ArrayList<GitTarget>();
    public GitConfiguration projectConfiguration;

    @DataBoundConstructor
    public Git() {
    }

    @Override
    public String getName() {
        return "Git repository";
    }

    @Override
    public PollingResult poll( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, boolean reconfigure ) throws IOException, InterruptedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Performer getPerform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) throws IOException {
        return new GitPerformer(build, launcher, workspace, listener);
    }

    public class GitPerformer extends Performer<GitConfiguration> {

        public GitPerformer( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) {
            super( build, launcher, workspace, listener );
        }

        @Override
        public GitConfiguration getInitialConfiguration() throws AbortException {
            GitConfiguration inputconfiguration = null;
            try {
                inputconfiguration = GitConfiguration.getConfigurationFromTargets( getTargets(), workspace, listener );
            } catch( Exception e ) {
                out.println( ConfigurationRotator.LOGGERNAME + "Unable to parse or load configuration: " + e.getMessage() );
                logger.log( Level.WARNING, "Unable to parse or load configuration" + e );
                throw new AbortException();
            }

            return inputconfiguration;
        }

        @Override
        public GitConfiguration getNextConfiguration( ConfigurationRotatorBuildAction action ) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean checkConfiguration( GitConfiguration configuration ) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void createWorkspace( GitConfiguration configuration ) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    @Override
    public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, boolean reconfigure ) throws IOException {
        logger.fine("Perform");

        PrintStream out = listener.getLogger();

        ConfigurationRotatorBuildAction action = getLastResult( build.getProject(), Git.class );
        out.println( ConfigurationRotator.LOGGERNAME + "Getting configuration" );
        logger.fine( "Getting configuration" );

        /* If there's no action, this is the first run */
        if( action == null || reconfigure ) {
            GitConfiguration inputconfiguration = null;
            try {
                inputconfiguration = GitConfiguration.getConfigurationFromTargets( getTargets(), workspace, listener );
            } catch( ConfigurationRotatorException e ) {
                out.println(ConfigurationRotator.LOGGERNAME + "Unable to parse or load configuration: " + e.getMessage() );
                logger.log( Level.WARNING, "Unable to parse or load configuration" + e );
                throw new AbortException();
            }

            projectConfiguration = inputconfiguration;
        } else { // There is a configuration, ROTATE!
            GitConfiguration oldconfiguration = action.getConfiguration(GitConfiguration.class);
        }

        logger.fine( "Adding action" );
        final ConfigurationRotatorBuildAction action1 = new ConfigurationRotatorBuildAction( build, Git.class, projectConfiguration );
        build.addAction( action1 );

        return true;
    }

    @Override
    public void setConfigurationByAction( AbstractProject<?, ?> project, ConfigurationRotatorBuildAction action ) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean wasReconfigured( AbstractProject<?, ?> project ) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ConfigRotatorChangeLogParser createChangeLogParser() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void writeChangeLog( File changeLogFile, BuildListener listener, AbstractBuild<?, ?> build ) throws IOException, ConfigurationRotatorException, InterruptedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public GitConfiguration nextConfiguration( TaskListener listener, GitConfiguration configuration, FilePath workspace ) throws ConfigurationRotatorException {

        RevCommit oldest = null;
        GitConfigurationComponent chosen = null;
        GitConfiguration nconfig = null;
        try {
            nconfig = (GitConfiguration) configuration.clone();
        } catch( CloneNotSupportedException e ) {
            throw new ConfigurationRotatorException( e );
        }

        /* Find oldest commit, newer than current */
        for( GitConfigurationComponent config : nconfig.getList() ) {
            if( !config.isFixed() ) {
                try {
                    RevCommit commit = workspace.act( new ResolveNextCommit(config.getName(), config.getCommit() ) );
                    if( oldest != null && commit.getCommitTime() < oldest.getCommitTime() ) {
                        oldest = commit;
                        chosen = config;
                    }

                    config.setChangedLast( false );

                } catch( Exception e ) {
                    logger.log( Level.FINE, "No commit found", e );
                }

            }
        }

        logger.fine( "chosen: " + chosen );
        logger.fine( "oldest: " + oldest );
        if( chosen != null && oldest != null ) {
            logger.fine( "There was a new commit: " + oldest );
            listener.getLogger().println( "Next commit: " + oldest );
            chosen.setCommit( oldest );
            chosen.setChangedLast( true );
        } else {
            listener.getLogger().println( "No new commits" );
            return null;
        }

        return nconfig;
    }


    public List<GitTarget> getTargets() {
        return targets;
    }


    @Extension
    public static final class DescriptorImpl extends ConfigurationRotatorSCMDescriptor<Git> {

        @Override
        public String getDisplayName() {
            return "Git repositories";
        }

        public FormValidation doTest(  ) throws IOException, ServletException {
            return FormValidation.ok();
        }

        @Override
        public AbstractConfigurationRotatorSCM newInstance( StaplerRequest req, JSONObject formData, AbstractConfigurationRotatorSCM i ) throws FormException {
            Git instance = (Git)i;
            //Default to an empty configuration. When the plugin is first started this should be an empty list
            List<GitTarget> targets = new ArrayList<GitTarget>();


            try {
                JSONArray obj = formData.getJSONObject( "acrs" ).getJSONArray( "targets" );
                targets = req.bindJSONToList( GitTarget.class, obj );
            } catch (net.sf.json.JSONException jasonEx) {
                //This happens if the targets is not an array!
                JSONObject obj = formData.getJSONObject( "acrs" ).getJSONObject( "targets" );
                if(obj != null) {
                    GitTarget target = req.bindJSON(GitTarget.class, obj);
                    if(target != null && target.getRepository() != null && !target.getRepository().equals("")) {
                        targets.add(target);
                    }
                }
            }
            instance.targets = targets;

            save();
            return instance;
        }

        public List<GitTarget> getTargets( Git instance ) {
            if( instance == null ) {
                return new ArrayList<GitTarget>();
            } else {
                return instance.getTargets();
            }
        }
    }
}
