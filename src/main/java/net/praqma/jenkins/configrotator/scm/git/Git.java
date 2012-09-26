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
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfiguration;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfigurationComponent;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMTarget;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Git extends AbstractConfigurationRotatorSCM implements Serializable {

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
        public GitConfiguration getInitialConfiguration() throws ConfigurationRotatorException {
            return GitConfiguration.getConfigurationFromTargets( getTargets(), workspace, listener );
        }

        @Override
        public GitConfiguration getNextConfiguration( ConfigurationRotatorBuildAction action ) throws ConfigurationRotatorException {
            GitConfiguration oldconfiguration = action.getConfiguration( GitConfiguration.class );
            return nextConfiguration(listener, oldconfiguration, workspace );
        }

        @Override
        public void checkConfiguration( GitConfiguration configuration ) {
            projectConfiguration = configuration;
            /* TODO: implement */
        }

        @Override
        public void createWorkspace( GitConfiguration configuration ) {
            /* No need */
        }

        @Override
        public void print( GitConfiguration configuration ) {
            /* TODO: implement */
        }
    }


    @Override
    public void setConfigurationByAction( AbstractProject<?, ?> project, ConfigurationRotatorBuildAction action ) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean wasReconfigured( AbstractProject<?, ?> project ) {
        ConfigurationRotatorBuildAction action = getLastResult( project, Git.class );

        if( action == null ) {
            return true;
        }

        GitConfiguration configuration = action.getConfiguration( GitConfiguration.class );

        /* Check if the project configuration is even set */
        if( configuration == null ) {
            logger.fine( "Configuration was null" );
            return true;
        }

        /* Check if the sizes are equal */
        if( targets.size() != configuration.getList().size() ) {
            logger.fine( "Size was not equal" );
            return true;
        }

        /**/
        for( int i = 0; i < targets.size(); ++i ) {
            GitTarget t = targets.get( i );
            GitConfigurationComponent c = configuration.getList().get( i );
            if( !t.getBranch().equals( c.getBranch()) ||
                !t.getRepository().equals( c.getRepository() ) ||
                !t.getCommitId().equals( c.getCommitId() )) {
                logger.finer( "Configuration was not equal" );
                return true;
            }
        }

        return false;
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
        logger.fine("Getting next Git configuration: " + configuration);

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
                    logger.fine("Config: " + config);
                    RevCommit commit = workspace.act( new ResolveNextCommit( config.getName(), config.getCommitId() ) );
                    logger.fine( "Commit1: " + commit );
                    logger.fine( "Commit2: " + config.getCommitId() );
                    if( oldest == null || commit.getCommitTime() < oldest.getCommitTime() ) {
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
            chosen.setCommitId( oldest.getName() );
            chosen.setChangedLast( true );
        } else {
            listener.getLogger().println( "No new commits" );
            return null;
        }

        return nconfig;
    }


    private List<GitTarget> getConfigurationAsTargets( GitConfiguration config ) {
        List<GitTarget> list = new ArrayList<GitTarget>();
        if( config.getList() != null && config.getList().size() > 0 ) {
            for( GitConfigurationComponent c : config.getList() ) {
                if( c != null ) {
                    //list.add( new ClearCaseUCMTarget( c.getBaseline().getNormalizedName() + ", " + c.getPlevel().toString() + ", " + c.isFixed() ) );
                    list.add( new GitTarget( c.getRepository(), c.getBranch(), c.getCommitId(), c.isFixed() ) );
                } else {
                    /* A null!? The list is corrupted, return targets */
                    return targets;
                }
            }

            return list;
        } else {
            return targets;
        }
    }

    public List<GitTarget> getTargets() {
        if( projectConfiguration != null ) {
            return getConfigurationAsTargets( projectConfiguration );
        } else {
            return targets;
        }

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
