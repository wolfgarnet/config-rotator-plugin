package net.praqma.jenkins.configrotator;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.praqma.jenkins.configrotator.functional.scm.ConfigRotatorChangeLogEntry;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.scm.SCM;
import jenkins.model.Jenkins;

import static java.lang.System.*;

public class ConfigurationRotator extends SCM {

    private AbstractConfigurationRotatorSCM acrs;

    private static Logger logger = Logger.getLogger( ConfigurationRotator.class.getName() );

    public enum ResultType {

        /*
           * Tested and configuration is compatible
           */
        COMPATIBLE,

        /*
           * Tested and configuration is NOT compatible
           */
        INCOMPATIBLE,

        FAILED,

        /*
           * The tests failed and was unable to determine compatibility
           */
        UNDETERMINED
    }

    public static final String URL_NAME = "config-rotator";
    public static final String NAME = "ConfigRotator";
    public static final String LOGGERNAME = "[" + NAME + "] ";
    public boolean justConfigured = false;

    public static final String SEPARATOR = getProperty( "file.separator" );
    public static final String FEED_DIR = "config-rotator-feeds" + SEPARATOR;

    public static File FEED_PATH;
    public static String VERSION = "Unresolved";

    static {
        if( Jenkins.getInstance() != null ) {
            FEED_PATH = new File( Jenkins.getInstance().getRootDir(), FEED_DIR );
            VERSION = Jenkins.getInstance().getPlugin( "config-rotator" ).getWrapper().getVersion();
        } else {

        }
    }

    /**
     * Determines whether a new configuration has been entered. If true, the
     * input is new.
     */
    public boolean reconfigure;

    @DataBoundConstructor
    public ConfigurationRotator( AbstractConfigurationRotatorSCM acrs ) {
        this.acrs = acrs;
        this.justConfigured = true;
    }

    public AbstractConfigurationRotatorSCM getAcrs() {
        return acrs;
    }

    public boolean doReconfigure() {
        return reconfigure;
    }

    public void setReconfigure( boolean reconfigure ) {
        this.reconfigure = reconfigure;
    }

    @Override
    public SCMRevisionState calcRevisionsFromBuild( AbstractBuild<?, ?> arg0, Launcher arg1, TaskListener arg2 ) throws IOException, InterruptedException {
        if( !doReconfigure() ) {
            return new SCMRevisionState() {
            };
        } else {
            return null;
        }
    }

    @Override
    public boolean checkout( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File file ) throws IOException, InterruptedException {
        PrintStream out = listener.getLogger();

        out.println( LOGGERNAME + "Version: " + VERSION );
        logger.fine( "Version: " + VERSION );

        /*
           * Determine if the job was reconfigured
           */
        if( justConfigured ) {
            reconfigure = acrs.wasReconfigured( build.getProject() );
            logger.fine( "Was reconfigured: " + reconfigure );
        }

        AbstractConfigurationRotatorSCM.Performer<AbstractConfiguration<?>> performer = acrs.getPerform( build, launcher, workspace, listener );
        ConfigurationRotatorBuildAction lastAction = acrs.getLastResult( build.getProject(), performer.getSCMClass() );
        AbstractConfiguration<?> configuration = null;

        /* Regarding JENKINS-14746 */
        ensurePublisher( build );

        boolean performResult = false;
        try {

            if( reconfigure || lastAction == null ) {
                out.println( LOGGERNAME + "Configuration from scratch" );
                configuration = performer.getInitialConfiguration();
            } else {
                out.println( LOGGERNAME + "Getting next configuration" );
                configuration = performer.getNextConfiguration( lastAction );
            }

            acrs.printConfiguration( out, configuration );

            if( configuration != null ) {
                out.println( LOGGERNAME + "Checking configuration(" + configuration.getClass() + ") " + configuration );
                performer.checkConfiguration( configuration );

                out.println( LOGGERNAME + "Creating workspace" );
                performer.createWorkspace( configuration );

                performer.save( configuration );

                performResult = true;

            }
        } catch( Exception e ) {
            logger.log( Level.SEVERE, "Unable to create configuration", e );
            e.printStackTrace( out );
            throw new AbortException( e.getMessage() );
        }

        if( !performResult ) {
            // ConfigurationRotator.perform will return false only if no new baselines found
            // We fail build if there is now new baseline.
            // An alternative would be to do like the CCUCM plugin and make the
            // build result "grey" with an comment "nothing to do".
            throw new AbortException( "Nothing new to rotate" );
        } else {

            /* Do the change log */
            AbstractConfigurationRotatorSCM.ChangeLogWriter clw = acrs.getChangeLogWriter(file, listener, build );

            try {
                List<ConfigRotatorChangeLogEntry> entries = null;
                if( clw != null ) {
                    if( lastAction == null || reconfigure ) {
                        entries = Collections.emptyList();
                    } else {
                        entries = clw.getChangeLogEntries( configuration );
                    }
                    clw.write( entries );
                } else {
                    logger.info( "Change log writer not implemented" );
                    out.println( LOGGERNAME + "Change log writer not implemented" );
                    entries = Collections.emptyList();
                }

            } catch( Exception e ) {
                /* The build must not be terminated because of the change log */
                logger.log( Level.WARNING, "Change log not generated", e );
                out.println( LOGGERNAME + "Change log not generated" );
            }

            /*
                * Config is not fresh anymore
                */
            reconfigure = false;
            justConfigured = false;

            build.getProject().save();

            return true;
        }
    }

    public void ensurePublisher( AbstractBuild build ) throws IOException {
        Describable describable = build.getProject().getPublishersList().get( ConfigurationRotatorPublisher.class );
        if( describable == null ) {
            logger.info( "Adding publisher to project" );
            build.getProject().getPublishersList().add( new ConfigurationRotatorPublisher() );
        }
    }

    public void setConfigurationByAction( AbstractProject<?, ?> project, ConfigurationRotatorBuildAction action ) throws IOException {
        acrs.setConfigurationByAction( project, action );
        reconfigure = true;
    }

    @Override
    protected PollingResult compareRemoteRevisionWith( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState arg4 ) throws IOException, InterruptedException {
        PrintStream out = listener.getLogger();
        // This little check ensures changes are not found while building, as
        // this with many concurrent builds and polling leads to issues where
        // we saw a build was long in the queue, and when started, the polling found
        // changed a did schedule the same build with the same changes as last time
        // between the last one started and finished.
        //Basically this disables polling while the job has a build in the queue.
        if( project.isInQueue() ) {
            out.println( "A build already in queue - cancelling poll" );
            logger.fine( "A build already in queue - cancelling poll" );
            return PollingResult.NO_CHANGES;
        }

        logger.fine( "Version: " + Jenkins.getInstance().getPlugin( "config-rotator" ).getWrapper().getVersion() );

        /*
           * Determine if the job was reconfigured
           */
        if( justConfigured ) {
            reconfigure = acrs.wasReconfigured( project );
            logger.fine( "Was reconfigured: " + reconfigure );
        }

        AbstractConfigurationRotatorSCM.Poller poller = acrs.getPoller(project, launcher, workspace, listener );

        ConfigurationRotatorBuildAction lastAction = acrs.getLastResult( project, null );

        try {
            if( reconfigure || lastAction == null ) {
                logger.fine( "Reconfigured, build now!" );
                out.println( LOGGERNAME + "Configuration from scratch, build now!" );
                return PollingResult.BUILD_NOW;
            } else {
                logger.fine( "Do actual polling" );
                out.println( LOGGERNAME + "Getting next configuration" );
                return poller.poll( lastAction );
            }
        } catch( Exception e ) {
            logger.log( Level.SEVERE, "Unable to poll", e );
            throw new AbortException( e.getMessage() );
        }
    }

    /**
     * Delegate the change log parser to abstract subtypes.
     *
     * @return
     */
    @Override
    public ChangeLogParser createChangeLogParser() {
        logger.fine( "Creating change log parser" );
        return acrs.createChangeLogParser();
    }

    @Extension
    public static final class RotatorDescriptor extends SCMDescriptor<ConfigurationRotator> {

        public RotatorDescriptor() {
            super( ConfigurationRotator.class, null );
        }

        @Override
        public String getDisplayName() {
            return "Config rotator";
        }

        @Override
        public SCM newInstance( StaplerRequest req, JSONObject formData ) throws FormException {
            out.println( "FORM: " + formData.toString( 2 ) );
            ConfigurationRotator r = (ConfigurationRotator) super.newInstance( req, formData );
            ConfigurationRotatorSCMDescriptor<AbstractConfigurationRotatorSCM> d = (ConfigurationRotatorSCMDescriptor<AbstractConfigurationRotatorSCM>) r.getAcrs().getDescriptor();
            r.acrs = d.newInstance( req, formData, r.acrs );
            save();
            return r;
        }

        public List<ConfigurationRotatorSCMDescriptor<?>> getSCMs() {
            return AbstractConfigurationRotatorSCM.getDescriptors();
        }
    }
}
