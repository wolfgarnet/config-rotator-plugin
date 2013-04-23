package net.praqma.jenkins.configrotator.scm.dvcs;

import hudson.AbortException;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import net.praqma.jenkins.configrotator.*;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogEntry;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogParser;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.ObjectUtils;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public abstract class BaseDVCS<COMPONENT extends BaseDVCSConfigurationComponent, TARGET extends BaseDVCSTarget, CONFIG extends BaseDVCSConfiguration<COMPONENT, TARGET>> extends AbstractConfigurationRotatorSCM {

    private static Logger logger = Logger.getLogger( BaseDVCS.class.getName() );

    protected List<TARGET> targets = new ArrayList<TARGET>();

    @Override
    public void setConfigurationByAction( AbstractProject<?, ?> project, ConfigurationRotatorBuildAction action ) throws IOException {
        CONFIG c = action.getConfiguration();
        if( c == null ) {
            throw new AbortException( ConfigurationRotator.LOGGERNAME + "Not a valid configuration" );
        } else {
            this.projectConfiguration = c;
            project.save();
        }
    }

    @Override
    public boolean wasReconfigured( AbstractProject<?, ?> project ) {
        ConfigurationRotatorBuildAction action = getLastResult( project, this.getClass() );

        if( action == null ) {
            return true;
        }

        CONFIG configuration = action.getConfiguration();

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
            TARGET t = targets.get( i );
            COMPONENT c = configuration.getList().get( i );
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
        return new ConfigRotatorChangeLogParser();
    }

    @Override
    public NextConfigurationResolver getNextConfigurationResolver() {
        return new NextDVCSConfigurationResolver();
    }

    public class NextDVCSConfigurationResolver implements NextConfigurationResolver<COMPONENT, TARGET, CONFIG> {

        public BaseDVCSCommit getCommit( FilePath workspace, String name, String branchName, String commitId ) throws IOException, InterruptedException {
            return workspace.act( getNextCommitResolver( name, branchName, commitId ) );
        }

        @Override
        public CONFIG resolve( TaskListener listener, CONFIG configuration, FilePath workspace ) throws ConfigurationRotatorException {
            logger.fine( "Getting next configuration: " + configuration );

            BaseDVCSCommit oldestCommit = null;
            COMPONENT chosenComponent = null;
            CONFIG nconfig = configuration.clone();

            /* Find oldest commit, newer than current */
            for( COMPONENT currentComponent : nconfig.getList() ) {
                if( !currentComponent.isFixed() ) {
                    try {
                        logger.fine( "Configuration: " + currentComponent );
                        BaseDVCSCommit nextCommit = getCommit( workspace, currentComponent.getName(), currentComponent.getBranch(), currentComponent.getCommitId() );
                        if( nextCommit != null ) {
                            logger.fine( "Current commit: " + nextCommit.getName() + ", " + nextCommit.getCommitTime() );
                            if( oldestCommit != null ) {
                                logger.fine( "Oldest commit: " + oldestCommit.getName() + ", " + oldestCommit.getCommitTime() );
                            }
                            if( oldestCommit == null || nextCommit.getCommitTime() < oldestCommit.getCommitTime() ) {
                                oldestCommit = nextCommit;
                                chosenComponent = currentComponent;
                            }

                            currentComponent.setChangedLast( false );
                        }

                    } catch( Exception e ) {
                        logger.log( Level.FINE, "No commit found", e );
                    }

                }
            }

            logger.fine( "Configuration component: " + chosenComponent );
            logger.fine( "Oldest valid commit: " + oldestCommit );
            if( chosenComponent != null && oldestCommit != null ) {
                logger.fine( "There was a new commit: " + oldestCommit );
                listener.getLogger().println( ConfigurationRotator.LOGGERNAME + "Component to rotate: " + chosenComponent );
                chosenComponent.setCommitId( oldestCommit.getName() );
                chosenComponent.setChangedLast( true );
            } else {
                listener.getLogger().println( ConfigurationRotator.LOGGERNAME + "No new commits to rotate" );
                logger.fine( "No new commits" );
                return null;
            }

            return nconfig;
        }
    }

    public abstract <CT extends BaseDVCSCommit> NextCommitResolver<CT> getNextCommitResolver( String name, String branchName, String commitId );

    public abstract TARGET createTarget( String name, String repository, String branch, String commitId, boolean fixed );

    private List<TARGET> getConfigurationAsTargets( CONFIG config ) {
        List<TARGET> list = new ArrayList<TARGET>();
        if( config.getList() != null && config.getList().size() > 0 ) {
            for( COMPONENT c : config.getList() ) {
                if( c != null ) {
                    list.add(createTarget( c.getName(), c.getRepository(), c.getBranch(), c.getCommitId(), c.isFixed() ) );
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

    @Override
    public <TT extends AbstractTarget> void setTargets( List<TT> targets ) {
        this.targets = (List<TARGET>) targets;
    }

    public List<TARGET> getTargets() {
        if( projectConfiguration != null ) {
            return getConfigurationAsTargets( (CONFIG) projectConfiguration );
        } else {
            return targets;
        }
    }




    public abstract static class DVCSDescriptor<TARGET extends BaseDVCSTarget, VCS extends BaseDVCS> extends ConfigurationRotatorSCMDescriptor<VCS> {

        @Override
        public String getFeedComponentName() {
            return this.getClass().getEnclosingClass().getSimpleName();
        }

        public FormValidation doTest(  ) throws IOException, ServletException {
            return FormValidation.ok();
        }

        public abstract Class<TARGET> getTargetClass();

        @Override
        public AbstractConfigurationRotatorSCM newInstance( StaplerRequest req, JSONObject formData, VCS i ) throws FormException {
            VCS instance = (VCS) i;
            //Default to an empty configuration. When the plugin is first started this should be an empty list
            List<TARGET> targets = new ArrayList<TARGET>();


            try {
                JSONArray obj = formData.getJSONObject( "acrs" ).getJSONArray( "targets" );
                targets = req.bindJSONToList( getTargetClass(), obj );
            } catch (net.sf.json.JSONException jasonEx) {
                //This happens if the targets is not an array!
                JSONObject obj = formData.getJSONObject( "acrs" ).getJSONObject( "targets" );
                if(obj != null) {
                    TARGET target = req.bindJSON( getTargetClass(), obj );
                    if(target != null && target.getRepository() != null && !target.getRepository().equals("")) {
                        targets.add( target );
                    }
                }
            }
            instance.targets = targets;

            save();
            return instance;
        }

        public List<BaseDVCSTarget> getTargets( VCS instance ) {
            if( instance == null ) {
                return new ArrayList<BaseDVCSTarget>();
            } else {
                return instance.getTargets();
            }
        }
    }

    @Override
    public ChangeLogWriter getChangeLogWriter( File changeLogFile, BuildListener listener, AbstractBuild<?, ?> build ) {
        return new DVCSChangeLogWriter( changeLogFile, listener, build );
    }

    public class DVCSChangeLogWriter extends ChangeLogWriter<TARGET, COMPONENT, CONFIG> {

        public DVCSChangeLogWriter( File changeLogFile, BuildListener listener, AbstractBuild<?, ?> build ) {
            super( changeLogFile, listener, build );
        }

        @Override
        protected List<ConfigRotatorChangeLogEntry> getChangeLogEntries( CONFIG configuration, COMPONENT configurationComponent ) throws ConfigurationRotatorException {
            logger.fine( "Change log entry, " + configurationComponent );
            try {
                ConfigRotatorChangeLogEntry entry = build.getWorkspace().act( getChangelogResolver( configurationComponent.getName(), configurationComponent.getCommitId(), configurationComponent.getBranch() ) );
                logger.fine("ENTRY: " + entry);
                return Collections.singletonList( entry );
            } catch( Exception e ) {
                throw new ConfigurationRotatorException( "Unable to resolve changelog " + configurationComponent.getCommitId(), e );
            }
        }
    }

    protected abstract BaseDVCSChangeLogResolver getChangelogResolver( String name, String commitId, String branchName );
}
