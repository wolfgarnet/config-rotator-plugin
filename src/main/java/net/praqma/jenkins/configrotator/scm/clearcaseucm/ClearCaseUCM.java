package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.util.FormValidation;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.jenkins.configrotator.*;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogEntry;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogParser;
import net.praqma.jenkins.utils.remoting.DetermineProject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import net.praqma.clearcase.ucm.entities.Component;

public class ClearCaseUCM extends AbstractConfigurationRotatorSCM implements Serializable {

    private static Logger logger = Logger.getLogger( ClearCaseUCM.class.getName() );

    public List<ClearCaseUCMTarget> targets;

    private PVob pvob;

    public ClearCaseUCM( PVob pvob ) {
        this.pvob = pvob;
    }

    @DataBoundConstructor
    public ClearCaseUCM( String pvobName ) {
        pvob = new PVob( pvobName );
    }

    public String getPvobName() {
        return pvob.toString();
    }

    @Override
    public String getName() {
        return "ClearCase UCM";
    }

    @Override
    public ConfigRotatorChangeLogParser createChangeLogParser() {
        return new ConfigRotatorChangeLogParser();
    }

    @Override
    public boolean wasReconfigured( AbstractProject<?, ?> project ) {
        ConfigurationRotatorBuildAction action = getLastResult( project, ClearCaseUCM.class );

        if( action == null ) {
            return true;
        }

        ClearCaseUCMConfiguration configuration = action.getConfiguration();

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
        List<ClearCaseUCMTarget> list = getConfigurationAsTargets( configuration );
        for( int i = 0; i < targets.size(); ++i ) {
            if( !targets.get( i ).equals( list.get( i ) ) ) {
                logger.fine( "Configuration was not equal" );
                return true;
            }
        }

        return false;
    }

    @Override
    public Poller getPoller( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener ) {
        return new Poller(project, launcher, workspace, listener );
    }


    @Override
    public Performer<ClearCaseUCMConfiguration> getPerform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) throws IOException {
        return new UCMPerformer(build, launcher, workspace, listener);
    }

    public class UCMPerformer extends Performer<ClearCaseUCMConfiguration> {

        public UCMPerformer( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) {
            super( build, launcher, workspace, listener );
        }

        @Override
        public ClearCaseUCMConfiguration getInitialConfiguration() throws IOException, ConfigurationRotatorException {
            return ClearCaseUCMConfiguration.getConfigurationFromTargets( getTargets(), workspace, listener );
        }

        @Override
        public ClearCaseUCMConfiguration getNextConfiguration( ConfigurationRotatorBuildAction action ) throws ConfigurationRotatorException {
            ClearCaseUCMConfiguration oldconfiguration = action.getConfiguration();
            return (ClearCaseUCMConfiguration) nextConfiguration( listener, oldconfiguration, workspace );
        }

        @Override
        public void checkConfiguration( ClearCaseUCMConfiguration configuration ) throws ConfigurationRotatorException {
               simpleCheckOfConfiguration( configuration );

        }

        @Override
        public void createWorkspace( ClearCaseUCMConfiguration configuration ) throws ConfigurationRotatorException {
            try {
                out.println( ConfigurationRotator.LOGGERNAME + "Creating view" );
                logger.fine( "Creating view" );
                SnapshotView view = createView( listener, build, configuration, workspace, pvob );
                configuration.setView( view );
            } catch( Exception e ) {
                out.println( ConfigurationRotator.LOGGERNAME + "Unable to create view" );
                logger.fine( ConfigurationRotator.LOGGERNAME + "Unable to create view, message is: "
                        + e.getMessage() + ". Cause was: " + ( e.getCause() == null ? "unknown" : e.getCause().getMessage() ) );
                throw new ConfigurationRotatorException( "Unable to create view", e );
            }
        }

        @Override
        public void print( ClearCaseUCMConfiguration configuration ) {
            printConfiguration( out, configuration );
        }
    }

    /**
     * Reconfigure the project configuration given the targets from the configuration page
     *
     * @param workspace A FilePath
     * @param listener  A TaskListener
     * @throws IOException
     */
    public void reconfigure( FilePath workspace, TaskListener listener ) throws IOException {
        logger.fine( "Getting configuration" );
        PrintStream out = listener.getLogger();

        /* Resolve the configuration */
        ClearCaseUCMConfiguration inputconfiguration = null;
        try {
            inputconfiguration = ClearCaseUCMConfiguration.getConfigurationFromTargets( getTargets(), workspace, listener );
        } catch( ConfigurationRotatorException e ) {
            out.println( ConfigurationRotator.LOGGERNAME + "Unable to parse configuration: " + e.getMessage() );
            throw new AbortException();
        }

        projectConfiguration = inputconfiguration;
    }

    @Override
    public void printConfiguration( PrintStream out, AbstractConfiguration cfg ) {
        out.println( ConfigurationRotator.LOGGERNAME + "The configuration is:" );
        logger.fine( ConfigurationRotator.LOGGERNAME + "The configuration is:" );
        if( cfg instanceof ClearCaseUCMConfiguration ) {
            ClearCaseUCMConfiguration config = (ClearCaseUCMConfiguration) cfg;
            for( ClearCaseUCMConfigurationComponent c : config.getList() ) {
                out.println( " * " + c.getBaseline().getComponent() + ", " + c.getBaseline().getStream() + ", " + c.getBaseline().getNormalizedName() );
                logger.fine( " * " + c.getBaseline().getComponent() + ", " + c.getBaseline().getStream() + ", " + c.getBaseline().getNormalizedName() );
            }
            out.println( "" );
            logger.fine( "" );
        }
    }


    /**
     * Does a simple check of the config-rotator configuration.
     * We do implicitly assume the configuration can be loaded and clear case objects
     * exists. The checks is done only with regards to configuration rotation, eg.
     * not using the same component twice.
     * 1) is a Clear Case UCM component used more than once in the configuration?
     *
     * @param cfg config rotator configuration
     * @throws AbortException
     */
    public void simpleCheckOfConfiguration( AbstractConfiguration cfg ) throws ConfigurationRotatorException {
        logger.finer( "Checking configuration " + cfg );
        if( cfg instanceof ClearCaseUCMConfiguration ) {
            ClearCaseUCMConfiguration config = (ClearCaseUCMConfiguration) cfg;
            Set<Component> ccucmcfgset = new HashSet<Component>();

            // loops iterates over clear case component which must have unique 
            // hash representation
            // Notice: we should throw abort exception that is catched by jenkins
            // and message printed to the console by Jenkins.
            // Therefore we like it to be descriptive.
            for( ClearCaseUCMConfigurationComponent c : config.getList() ) {
                // check 1) is a component more than once in the configuration?
                // as baselines are part of component, this also ensure no two baseline
                // for the same component are used.
                Component currentClearCaseComponent = c.getBaseline().getComponent();
                if( !ccucmcfgset.contains( currentClearCaseComponent ) ) {
                    ccucmcfgset.add( currentClearCaseComponent );
                } else {
                    String errorMessage = ConfigurationRotator.LOGGERNAME + "Simple check of configuration failed because component used more than once in configuration. Component is: \n";
                    errorMessage += " * " + c.getBaseline().getComponent() + ", " + c.getBaseline().getStream() + ", " + c.getBaseline().getNormalizedName();
                    throw new ConfigurationRotatorException( errorMessage );
                }
            }
        } else {
            throw new ConfigurationRotatorException( "simpleCheckOfconfiguration failed " + cfg );
        }
    }


    @Override
    public AbstractConfiguration nextConfiguration( TaskListener listener, AbstractConfiguration configuration, FilePath workspace ) throws ConfigurationRotatorException {

        Baseline oldest = null, current;
        ClearCaseUCMConfigurationComponent chosen = null;

        ClearCaseUCMConfiguration nconfig = (ClearCaseUCMConfiguration) configuration.clone();

        logger.fine( "Foreach configuration component" );
        for( ClearCaseUCMConfigurationComponent config : nconfig.getList() ) {
            logger.fine( ConfigurationRotator.LOGGERNAME + " * " + config );
            /* This configuration is not fixed */
            if( !config.isFixed() ) {
                logger.fine( ConfigurationRotator.LOGGERNAME + "Wasn't fixed: " + config.getBaseline().getNormalizedName() );

                try {
                    //current = workspace.act( new GetBaselines( listener, config.getBaseline().getComponent(), config.getBaseline().getStream(), config.getPlevel(), 1, config.getBaseline() ) ).get( 0 ); //.get(0) newest baseline, they are sorted!
                    current = workspace.act( new NextBaseline( config.getBaseline().getStream(), config.getBaseline().getComponent(), config.getPlevel(), config.getBaseline() ) );
                    if( oldest == null || current.getDate().before( oldest.getDate() ) ) {
                        logger.fine( ConfigurationRotator.LOGGERNAME + "Was older: " + current );
                        oldest = current;
                        chosen = config;
                    }

                    /* Reset */
                    config.setChangedLast( false );

                } catch( Exception e ) {
                    /* No baselines found .get(0) above throws exception if no new baselines*/
                    logger.fine( ConfigurationRotator.LOGGERNAME + "No baselines found: " + e.getMessage() );
                }

            }
        }

        /**/
        logger.fine( ConfigurationRotator.LOGGERNAME + "chosen: " + chosen );
        logger.fine( ConfigurationRotator.LOGGERNAME + "oldest: " + oldest );
        if( chosen != null && oldest != null ) {
            logger.fine( ConfigurationRotator.LOGGERNAME + "There was a new baseline: " + oldest );
            listener.getLogger().println( ConfigurationRotator.LOGGERNAME + "There was a new baseline: " + oldest );
            chosen.setBaseline( oldest );
            chosen.setChangedLast( true );
        } else {
            listener.getLogger().println( ConfigurationRotator.LOGGERNAME + "No new baselines" );
            return null;
        }

        return nconfig;
    }

    public SnapshotView createView( TaskListener listener, AbstractBuild<?, ?> build, ClearCaseUCMConfiguration configuration, FilePath workspace, PVob pvob ) throws IOException, InterruptedException {
        Project project = null;

        logger.fine( "Getting project" );
        project = workspace.act( new DetermineProject( Arrays.asList( new String[]{ "jenkins", "Jenkins", "hudson", "Hudson" } ), pvob ) );

        logger.fine( "Project is " + project );

        /* Create baselines list */
        List<Baseline> selectedBaselines = new ArrayList<Baseline>();
        logger.fine( "Selected baselines:" );
        listener.getLogger().println( "Configratioanmwd: " + configuration );
        for( ClearCaseUCMConfigurationComponent config : configuration.getList() ) {
            listener.getLogger().println( "Component: " + config );
            logger.fine( ConfigurationRotator.LOGGERNAME + config.getBaseline().getNormalizedName() );
            selectedBaselines.add( config.getBaseline() );
        }

        /* Make a view tag*/
        String viewtag = "cr-" + build.getProject().getDisplayName().replaceAll( "\\s", "_" ) + "-" + System.getenv( "COMPUTERNAME" );

        return workspace.act( new PrepareWorkspace( project, selectedBaselines, viewtag, listener ) );

    }

    /*
    @Override
    public void setTargets( List<AbstractTarget> targets ) {
        this.targets = (List<ClearCaseUCMTarget>) targets;
    }
    */

    @Override
    public <TT extends AbstractTarget> void setTargets( List<TT> targets ) {
        this.targets = (List<ClearCaseUCMTarget>) targets;
    }

    /**
     * Get the configuration as targets. If the project configuration is null, the last targets defined by the configuration page is returned otherwise the current project configuration is returned as targets
     *
     * @return A list of targets
     */
    public List<ClearCaseUCMTarget> getTargets() {
        if( projectConfiguration != null ) {
            return getConfigurationAsTargets( (ClearCaseUCMConfiguration) projectConfiguration );
        } else {
            return targets;
        }
    }

    private List<ClearCaseUCMTarget> getConfigurationAsTargets( ClearCaseUCMConfiguration config ) {
        List<ClearCaseUCMTarget> list = new ArrayList<ClearCaseUCMTarget>();
        if( config.getList() != null && config.getList().size() > 0 ) {
            for( ClearCaseUCMConfigurationComponent c : config.getList() ) {
                if( c != null ) {
                    //list.add( new ClearCaseUCMTarget( c.getBaseline().getNormalizedName() + ", " + c.getPlevel().toString() + ", " + c.isFixed() ) );
                    list.add( new ClearCaseUCMTarget( c.getBaseline().getNormalizedName(), c.getPlevel(), c.isFixed() ) );
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
    public void setConfigurationByAction( AbstractProject<?, ?> project, ConfigurationRotatorBuildAction action ) throws IOException {
        ClearCaseUCMConfiguration c = action.getConfiguration();
        if( c == null ) {
            throw new AbortException( ConfigurationRotator.LOGGERNAME + "Not a valid configuration" );
        } else {
            this.projectConfiguration = c;
            project.save();
        }
    }

    @Override
    public ChangeLogWriter getChangeLogWriter( File changeLogFile, BuildListener listener, AbstractBuild<?, ?> build ) {
        return new UCMChangeLogWriter( changeLogFile, listener, build );
    }

    public class UCMChangeLogWriter extends ChangeLogWriter<ClearCaseUCMConfigurationComponent, ClearCaseUCMConfiguration> {

        public UCMChangeLogWriter( File changeLogFile, BuildListener listener, AbstractBuild<?, ?> build ) {
            super( changeLogFile, listener, build );
        }

        @Override
        protected List<ConfigRotatorChangeLogEntry> getChangeLogEntries( ClearCaseUCMConfiguration configuration, ClearCaseUCMConfigurationComponent Component ) throws ConfigurationRotatorException {
            logger.fine( "Change log entry, " + Component );
            try {
                return build.getWorkspace().act( new ClearCaseGetBaseLineCompare(listener, configuration, Component ) );
            } catch( Exception e ) {
                throw new ConfigurationRotatorException( e );
            }
        }
    }

    @Extension
    public static final class DescriptorImpl extends ConfigurationRotatorSCMDescriptor<ClearCaseUCM> {

        @Override
        public String getDisplayName() {
            return "ClearCase UCM Components";
        }

        @Override
        public String getFeedComponentName() {
            return ClearCaseUCM.class.getSimpleName();
        }

        public FormValidation doTest() throws IOException, ServletException {
            return FormValidation.ok();
        }

        @Override
        public AbstractConfigurationRotatorSCM newInstance( StaplerRequest req, JSONObject formData, AbstractConfigurationRotatorSCM i ) throws FormException {
            ClearCaseUCM instance = (ClearCaseUCM) i;
            //Default to an empty configuration. When the plugin is first started this should be an empty list
            List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();


            try {
                JSONArray obj = formData.getJSONObject( "acrs" ).getJSONArray( "targets" );
                targets = req.bindJSONToList( ClearCaseUCMTarget.class, obj );
            } catch( net.sf.json.JSONException jasonEx ) {
                //This happens if the targets is not an array!
                JSONObject obj = formData.getJSONObject( "acrs" ).getJSONObject( "targets" );
                if( obj != null ) {
                    ClearCaseUCMTarget target = req.bindJSON( ClearCaseUCMTarget.class, obj );
                    if( target != null && target.getBaselineName() != null && !target.getBaselineName().equals( "" ) ) {
                        targets.add( target );
                    }
                }
            }
            instance.targets = targets;

            save();
            return instance;
        }

        public List<ClearCaseUCMTarget> getTargets( ClearCaseUCM instance ) {
            if( instance == null ) {
                return new ArrayList<ClearCaseUCMTarget>();
            } else {
                return instance.getTargets();
            }
        }

        public Project.PromotionLevel[] getPromotionLevels() {
            return Project.PromotionLevel.values();
        }


    }
}