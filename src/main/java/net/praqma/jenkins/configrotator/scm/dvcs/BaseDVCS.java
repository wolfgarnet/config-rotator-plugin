package net.praqma.jenkins.configrotator.scm.dvcs;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import net.praqma.jenkins.configrotator.*;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogParser;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
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

        @Override
        public CONFIG resolve( TaskListener listener, CONFIG configuration, FilePath workspace ) throws ConfigurationRotatorException {
            logger.fine( "Getting next Git configuration: " + configuration);

            BaseDVCSCommit oldest = null;
            COMPONENT chosen = null;
            CONFIG nconfig = configuration.clone();

            /* Find oldest commit, newer than current */
            for( COMPONENT config : nconfig.getList() ) {
                if( !config.isFixed() ) {
                    try {
                        logger.fine("Config: " + config);
                        BaseDVCSCommit commit = workspace.act( getNextCommitResolver( config.getName(), config.getBranch(), config.getCommitId() ) );
                        if( commit != null ) {
                            logger.fine( "Current commit: " + commit.getName() );
                            logger.fine( "Current commit: " + commit.getCommitTime() );
                            if( oldest != null ) {
                                logger.fine( "Oldest  commit: " + oldest.getName() );
                                logger.fine( "Oldest  commit: " + oldest.getCommitTime() );
                            }
                            if( oldest == null || commit.getCommitTime() < oldest.getCommitTime() ) {
                                oldest = commit;
                                chosen = config;
                            }

                            config.setChangedLast( false );
                        }

                    } catch( Exception e ) {
                        logger.log( Level.FINE, "No commit found", e );
                    }

                }
            }

            logger.fine( "Configuration component: " + chosen );
            logger.fine( "Oldest valid commit: " + oldest );
            if( chosen != null && oldest != null ) {
                logger.fine( "There was a new commit: " + oldest );
                listener.getLogger().println( ConfigurationRotator.LOGGERNAME + "Component to rotate: " + chosen );
                chosen.setCommitId( oldest.getName() );
                chosen.setChangedLast( true );
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
            return this.getClass().getSimpleName();
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
}
