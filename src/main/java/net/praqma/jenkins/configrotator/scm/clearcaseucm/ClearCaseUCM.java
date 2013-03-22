package net.praqma.jenkins.configrotator.scm.clearcaseucm;

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
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.jenkins.configrotator.*;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogParser;
import net.praqma.jenkins.utils.remoting.DetermineProject;
import net.praqma.jenkins.utils.remoting.GetBaselines;
import net.praqma.util.debug.Logger;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import java.util.HashSet;
import java.util.Set;
import net.praqma.clearcase.ucm.entities.Component;

public class ClearCaseUCM extends AbstractConfigurationRotatorSCM implements Serializable {
    
    private static Logger logger = Logger.getLogger();
    
    public ClearCaseUCMConfiguration projectConfiguration;
    
    public List<ClearCaseUCMTarget> targets;
    
    private PVob pvob;

    /**
     * Version 0.1.0 constructor
     * 
     * Parse config
     * Each line represents a {@link Component}, {@link Stream}, {@link Baseline} and a {@Plevel plevel}
     */
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
        return new ClearCaseUCMConfigRotatorChangeLogParser();
    }
    
    @Override
    public boolean wasReconfigured( AbstractProject<?, ?> project ) {
        ConfigurationRotatorBuildAction action = getLastResult( project, ClearCaseUCM.class );
        
        if( action == null ) {
            return true;
        }
        
        ClearCaseUCMConfiguration configuration = action.getConfiguration(ClearCaseUCMConfiguration.class);
        
        /* Check if the project configuration is even set */
        if( configuration == null ) {
            logger.debug(ConfigurationRotator.LOGGERNAME + "Configuration was null" );
            return true;
        }
        
        /* Check if the sizes are equal */
        if( targets.size() != configuration.getList().size() ) {
            logger.debug(ConfigurationRotator.LOGGERNAME + "Size was not equal" );
            return true;
        }
        
        /**/
        List<ClearCaseUCMTarget> list = getConfigurationAsTargets( configuration );
        for( int i = 0 ; i < targets.size() ; ++i ) {
            if( !targets.get( i ).equals( list.get( i ) ) ) {
                logger.debug(ConfigurationRotator.LOGGERNAME + "Configuration was not equal" );
                return true;
            }
        }
        
        return false;
    }
    

    @Override
    public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, boolean reconfigure ) throws IOException {
        PrintStream out = listener.getLogger();
        
        ConfigurationRotatorBuildAction action = getLastResult( build.getProject(), ClearCaseUCM.class );
        out.println(ConfigurationRotator.LOGGERNAME + "Getting configuration");
        logger.debug(ConfigurationRotator.LOGGERNAME + "Getting configuration");
        
        /* If there's no action, this is the first run */
        if( action == null || reconfigure ) {
            logger.debug(ConfigurationRotator.LOGGERNAME + "Action was NULL - first run");
            
            /* Resolve the configuration */
            ClearCaseUCMConfiguration inputconfiguration = null;
            logger.debug(ConfigurationRotator.LOGGERNAME + "Obtaining a configuration based on targets" );
            try {
                inputconfiguration = ClearCaseUCMConfiguration.getConfigurationFromTargets( getTargets(), workspace, listener );
            } catch( ConfigurationRotatorException e ) {
                out.println(ConfigurationRotator.LOGGERNAME + "Unable to parse or load configuration: " + e.getMessage() );
                logger.debug(ConfigurationRotator.LOGGERNAME + "Unable to parse or load configuration: " + e.getMessage());
                throw new AbortException();
            }
            
            projectConfiguration = inputconfiguration;
        } else {
            logger.debug(ConfigurationRotator.LOGGERNAME + "Action was NOT null" );
            /* Get the configuration from the action */
            ClearCaseUCMConfiguration oldconfiguration = action.getConfiguration(ClearCaseUCMConfiguration.class);
            /* Get next configuration */
            try {
                logger.debug(ConfigurationRotator.LOGGERNAME + "Obtaining new configuration based on old" );
                /* No new baselines */
                if( ( projectConfiguration = nextConfiguration( listener, oldconfiguration, workspace ) ) == null ) {
                    return false;
                }
            } catch( Exception e ) {
                out.println(ConfigurationRotator.LOGGERNAME + "Unable to get next configuration: " + e.getMessage());
                logger.debug(ConfigurationRotator.LOGGERNAME + "Unable to get next configuration: " + e.getMessage());
                 throw new AbortException();
            }
        }
        
        try
        {
            // This check is need to ensure some simple check that the configuration
            // is valid in Config Rotator context. Though the configuration above can be
            // loaded with clearcase and found, does not ensure it is valid for rotation.
            simpleCheckOfConfiguration(projectConfiguration);
            out.println(ConfigurationRotator.LOGGERNAME + "Found configuration - doing a simple check.");
            logger.debug(ConfigurationRotator.LOGGERNAME + "Found configuration - doing a simple check.");
        }
        catch (AbortException ae)
        {
            out.println(ConfigurationRotator.LOGGERNAME + "Simple check of configuration failed.");
            logger.debug(ConfigurationRotator.LOGGERNAME + "Simple check of configuration failed.");
            throw ae;
        }
        
        printConfiguration( out, projectConfiguration );
        
        /* Create the view */
        try {
            out.println(ConfigurationRotator.LOGGERNAME + "Creating view");
            logger.debug(ConfigurationRotator.LOGGERNAME + "Creating view");
            SnapshotView view = createView( listener, build, projectConfiguration, workspace, pvob );
            projectConfiguration.setView( view );
        } catch( Exception e ) {
            out.println(ConfigurationRotator.LOGGERNAME + "Unable to create view");
            logger.debug(ConfigurationRotator.LOGGERNAME + "Unable to create view, message is: " 
                    + e.getMessage() + ". Cause was: " + (e.getCause() == null ? "unknown" : e.getCause().getMessage()));
            throw new AbortException();
        }
                
        /* Just try to save */
        logger.debug(ConfigurationRotator.LOGGERNAME +  "Adding action" );
        final ConfigurationRotatorBuildAction action1 = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, projectConfiguration );
        build.addAction( action1 );
        
        return true;
    }
    
    /**
     * Reconfigure the project configuration given the targets from the configuration page
     * @param workspace A FilePath
     * @param listener A TaskListener
     * @throws IOException
     */
    public void reconfigure( FilePath workspace, TaskListener listener ) throws IOException {
        logger.debug( "Getting configuration" );
        PrintStream out = listener.getLogger();
        
        /* Resolve the configuration */
        ClearCaseUCMConfiguration inputconfiguration = null;
        try {
            inputconfiguration = ClearCaseUCMConfiguration.getConfigurationFromTargets( getTargets(), workspace, listener );
        } catch( ConfigurationRotatorException e ) {
            out.println(ConfigurationRotator.LOGGERNAME + "Unable to parse configuration: " + e.getMessage() );
            throw new AbortException();
        }
        
        projectConfiguration = inputconfiguration;
    }
    
    public void printConfiguration( PrintStream out, AbstractConfiguration cfg ) {
        out.println(ConfigurationRotator.LOGGERNAME +  "The configuration is:" );
        logger.debug(ConfigurationRotator.LOGGERNAME +  "The configuration is:" );
        if( cfg instanceof ClearCaseUCMConfiguration ) {
            ClearCaseUCMConfiguration config = (ClearCaseUCMConfiguration)cfg;
            for( ClearCaseUCMConfigurationComponent c : config.getList() ) {
                out.println(" * " + c.getBaseline().getComponent() + ", " + c.getBaseline().getStream() + ", " + c.getBaseline().getNormalizedName());
                logger.debug(" * " + c.getBaseline().getComponent() + ", " + c.getBaseline().getStream() + ", " + c.getBaseline().getNormalizedName());
            }
            out.println("");
            logger.debug("");
        }
        }

    
    /**
     * Does a simple check of the config-rotator configuration.
     * We do implicitly assume the configuration can be loaded and clear case objects
     * exists. The checks is done only with regards to configuration rotation, eg.
     * not using the same component twice.
     * 1) is a Clear Case UCM component used more than once in the configuration?
     * @param cfg config rotator configuration
     * @throws AbortException 
     */
    public void simpleCheckOfConfiguration(AbstractConfiguration cfg ) throws AbortException {
        if( cfg instanceof ClearCaseUCMConfiguration ) {
            ClearCaseUCMConfiguration config = (ClearCaseUCMConfiguration)cfg;
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
                        if (!ccucmcfgset.contains(currentClearCaseComponent))
                        {
                            ccucmcfgset.add(currentClearCaseComponent);
                        }
                        else
                        {
                            String errorMessage = ConfigurationRotator.LOGGERNAME +"Simple check of configuration failed because component used more than once in configuration. Component is: \n";
                            errorMessage += " * " + c.getBaseline().getComponent() + ", " + c.getBaseline().getStream() + ", " + c.getBaseline().getNormalizedName();
                            throw new AbortException(errorMessage);
                        }
            }
        }
        else
            throw new AbortException(ConfigurationRotator.LOGGERNAME + "simpleCheckOfconfiguration was not passed an instance of a ClearCaseUCMConfiguration");
    }
    
    
    public ClearCaseUCMConfiguration nextConfiguration( TaskListener listener, ClearCaseUCMConfiguration configuration, FilePath workspace ) throws IOException, InterruptedException, ConfigurationRotatorException {
        
        Baseline oldest = null, current;
        ClearCaseUCMConfigurationComponent chosen = null;
        
        ClearCaseUCMConfiguration nconfig = configuration.clone();
        
        logger.debug( "Foreach configuration component" );
        for( ClearCaseUCMConfigurationComponent config : nconfig.getList() ) {
            logger.debug(ConfigurationRotator.LOGGERNAME + " * " + config );
            /* This configuration is not fixed */
            if( !config.isFixed() ) {
                logger.debug(ConfigurationRotator.LOGGERNAME + "Wasn't fixed: " + config.getBaseline().getNormalizedName() );
               
                try {
                    //current = workspace.act( new GetBaselines( listener, config.getBaseline().getComponent(), config.getBaseline().getStream(), config.getPlevel(), 1, config.getBaseline() ) ).get( 0 ); //.get(0) newest baseline, they are sorted!
                    current = workspace.act( new NextBaseline( config.getBaseline().getStream(), config.getBaseline().getComponent(), config.getPlevel(), config.getBaseline() ) );
                    if( oldest == null || current.getDate().before( oldest.getDate() ) )  {
                        logger.debug(ConfigurationRotator.LOGGERNAME + "Was older: " + current );
                        oldest = current;
                        chosen = config;
                    }
                    
                    /* Reset */
                    config.setChangedLast( false );

                } catch( Exception e ) {
                    /* No baselines found .get(0) above throws exception if no new baselines*/
                    logger.debug(ConfigurationRotator.LOGGERNAME + "No baselines found: " + e.getMessage() );
                }
                
            }
        }
        
        /**/
        logger.debug(ConfigurationRotator.LOGGERNAME + "chosen: " + chosen );
        logger.debug(ConfigurationRotator.LOGGERNAME + "oldest: " + oldest );
        if( chosen != null && oldest != null ) {
            logger.debug(ConfigurationRotator.LOGGERNAME + "There was a new baseline: " + oldest );
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

        logger.debug(ConfigurationRotator.LOGGERNAME + "Getting project" );
        project = workspace.act( new DetermineProject( Arrays.asList( new String[] { "jenkins", "Jenkins", "hudson", "Hudson" } ), pvob ) );
        
        logger.debug(ConfigurationRotator.LOGGERNAME + "Project is " + project );
        
        /* Create baselines list */
        List<Baseline> selectedBaselines = new ArrayList<Baseline>();
        logger.debug(ConfigurationRotator.LOGGERNAME + "Selected baselines:" );
        for( ClearCaseUCMConfigurationComponent config : configuration.getList() ) {
            logger.debug(ConfigurationRotator.LOGGERNAME + config.getBaseline().getNormalizedName() );
            selectedBaselines.add( config.getBaseline() );
        }
        
        /* Make a view tag*/
        String viewtag = "cr-" + build.getProject().getDisplayName().replaceAll( "\\s", "_" ) + "-" + System.getenv( "COMPUTERNAME" );
        
        return workspace.act( new PrepareWorkspace( project, selectedBaselines, viewtag, listener ) );
        
    }
    
    /**
     * Get the configuration as targets. If the project configuration is null, the last targets defined by the configuration page is returned otherwise the current project configuration is returned as targets
     * @return A list of targets
     */
    public List<ClearCaseUCMTarget> getTargets() {
        if( projectConfiguration != null ) {
            return getConfigurationAsTargets( projectConfiguration );
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
                    list.add(new ClearCaseUCMTarget(c.getBaseline().getNormalizedName(), c.getPlevel(), c.isFixed()));
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
        ClearCaseUCMConfiguration c = action.getConfiguration(ClearCaseUCMConfiguration.class);
        if(c == null) {
            throw new AbortException(ConfigurationRotator.LOGGERNAME + "Not a valid configuration" );
        } else {
            this.projectConfiguration = c;
            project.save();
        }
    }

    /**
     * Polling functionality - done on slave, need ClearCase.
     * Design note: The polling may fail in several ways,  but if we can not recover we 
     * throw an abortexception. It should be noted, that if it a new configuration (first time polling
     * or a reconfiguration), an abort is done if configuration is invalid and can not be loaded with
     * Clear Case. In every other case, we return a polling result.
     * @param project
     * @param launcher
     * @param workspace
     * @param listener
     * @param reconfigure
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    @Override
    public PollingResult poll( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, boolean reconfigure ) throws IOException, InterruptedException {
        PrintStream out = listener.getLogger();
        out.println( ConfigurationRotator.LOGGERNAME + "Polling started" );
        logger.debug( ConfigurationRotator.LOGGERNAME + "Polling started" );

        ClearCaseUCMConfiguration configuration = null;
        if( projectConfiguration == null ) {
            if( reconfigure ) {
                try {
                    out.println( ConfigurationRotator.LOGGERNAME + "Project is reconfigured" );
                    logger.debug( ConfigurationRotator.LOGGERNAME + "Project is reconfigured" );
                    configuration = ClearCaseUCMConfiguration.getConfigurationFromTargets( getTargets(), workspace, listener );
                } catch( ConfigurationRotatorException e ) {
                    logger.debug("Unable to get configurations from targets: Exception message: ", e.getMessage());
                    throw new AbortException(ConfigurationRotator.LOGGERNAME + "Unable to get configurations from targets. " + e.getMessage());
                }
            } else {
                out.println( ConfigurationRotator.LOGGERNAME + "Project has no configuration, using configuration from last result" );
                logger.debug( ConfigurationRotator.LOGGERNAME + "Project has no configuration, using configuration from last result" );
                ConfigurationRotatorBuildAction action = getLastResult( project, ClearCaseUCM.class );
                
                if( action == null ) {
                    out.println( ConfigurationRotator.LOGGERNAME + "No last result, build now" );
                    logger.debug( ConfigurationRotator.LOGGERNAME + "No last result, build now" );
                    return PollingResult.BUILD_NOW;
                }
                
                configuration = action.getConfiguration(ClearCaseUCMConfiguration.class);
            }
        } else {
            out.println( ConfigurationRotator.LOGGERNAME + "Project configuration found" );
            logger.debug( ConfigurationRotator.LOGGERNAME + "Project configuration found" );
            configuration = this.projectConfiguration;
        }    

        /* Only look ahead if the build was NOT reconfigured */
        if( configuration != null && !reconfigure ) {
            out.println( ConfigurationRotator.LOGGERNAME + "Looking for changes" );
            logger.debug( ConfigurationRotator.LOGGERNAME + "Looking for changes" );
            try {
                ClearCaseUCMConfiguration other;
                other = nextConfiguration( listener, configuration, workspace );
                if( other != null ) {
                    out.println( ConfigurationRotator.LOGGERNAME + "Found changes" );
                    logger.debug( ConfigurationRotator.LOGGERNAME + "Found changes" );
                    printConfiguration( out, other );
                    return PollingResult.BUILD_NOW;
                } else {
                    out.println( ConfigurationRotator.LOGGERNAME + "No changes!" );
                    logger.debug( ConfigurationRotator.LOGGERNAME + "No changes!" );
                    return PollingResult.NO_CHANGES;
                }
            } catch( ConfigurationRotatorException e ) {
                logger.debug("Unable to poll, exception message: ", e.getMessage());
                throw new AbortException(ConfigurationRotator.LOGGERNAME + "Unable to poll: " + e.getMessage());
            } catch (Exception e) {
                out.println(ConfigurationRotator.LOGGERNAME + "Polling caught unhandled exception. Message was: " + e.getMessage());
                logger.debug(ConfigurationRotator.LOGGERNAME + "Polling caught unhandled exception. Message was: " + e.getMessage());
                throw new AbortException(ConfigurationRotator.LOGGERNAME + "Polling caught unhandled exception! Message was: " + e.getMessage());
            }
        }
        else
        {
        out.println( ConfigurationRotator.LOGGERNAME + "Starting first build" );
        logger.debug( ConfigurationRotator.LOGGERNAME + "Starting first build" );
        return PollingResult.BUILD_NOW;
        }
    }

    @Override
    public void writeChangeLog(File f, BuildListener listener, AbstractBuild<?, ?> build) throws IOException, ConfigurationRotatorException, InterruptedException {
        PrintWriter writer = null;
        List<ClearCaseActivity> changes = new ArrayList<ClearCaseActivity>();
        //First obtain last succesful result
        ConfigurationRotatorBuildAction crbac = getLastResult(build.getProject(), this.getClass());
        
        //Special case: This is the first build
         if(crbac == null) {
            
        } else {
             List<AbstractConfigurationComponent> currentComponentList = null;
             ConfigurationRotatorBuildAction current = build.getAction(ConfigurationRotatorBuildAction.class);
             if(current != null)
                 currentComponentList = current.getConfiguration().getList();
             
             int compareIndex = -1;
             
             if(currentComponentList != null) {
                for(AbstractConfigurationComponent acc : currentComponentList) {
                    if(acc.isChangedLast()) {
                        compareIndex = currentComponentList.indexOf(acc);
                        break;
                    }
                }
             }
             
             //The compare is totally new. Else compare the previous component
             if(compareIndex == -1) {
                 
             } else {
                 if(currentComponentList.get(compareIndex) instanceof ClearCaseUCMConfigurationComponent) {
                    changes = build.getWorkspace().act(new ClearCaseGetBaseLineCompare(listener, current.getConfiguration(ClearCaseUCMConfiguration.class), crbac.getConfiguration(ClearCaseUCMConfiguration.class)));
                 }
             }
        }
        
        try {
            
            writer = new PrintWriter(new FileWriter(f));
            
            
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<changelog>");
            
            for(ClearCaseActivity a : changes) {
                writer.println("<activity>");
                writer.println(String.format("<user>%s</user>", a.getAuthor()));
                writer.println(String.format("<activityName>%s</activityName>", a.getActivityName()));
                writer.println(String.format("<activityHeadline>%s</activityHeadline>", a.getActivityHeadline()));
                writer.println("<versions>");
                for(ClearCaseVersion v : a.getVersions()) {
                    writer.println("<version>");
                    writer.println(String.format("<name>%s</name>", v.getName()));
                    writer.println(String.format("<file>%s</file>", v.getFile()));
                    writer.println(String.format("<user>%s</user>", v.getUser()));
                    writer.println(String.format("<fullyQualifiedVersionName>%s</fullyQualifiedVersionName>", v.getFullyQualifiedVersionName()));
                    writer.println("</version>");
                }
                writer.println("</versions>");
                writer.print("</activity>");
                
                
            }
            
            writer.println("</changelog>");
            
        
        } catch (IOException e) {
            listener.getLogger().println("Unable to create change log!" +e);
        } finally {
            writer.close();
        }
    }
    
    @Extension
    public static final class DescriptorImpl extends ConfigurationRotatorSCMDescriptor<ClearCaseUCM> {

        @Override
        public String getDisplayName() {
            return "ClearCase UCM";
        }
        
        public FormValidation doTest(  ) throws IOException, ServletException {
            return FormValidation.ok();
        }
         
        @Override
        public AbstractConfigurationRotatorSCM newInstance( StaplerRequest req, JSONObject formData, AbstractConfigurationRotatorSCM i ) throws FormException {
            ClearCaseUCM instance = (ClearCaseUCM)i;
            //Default to an empty configuration. When the plugin is first started this should be an empty list
            List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
            
            
            try {                
                JSONArray obj = formData.getJSONObject( "acrs" ).getJSONArray( "targets" );
                targets = req.bindJSONToList( ClearCaseUCMTarget.class, obj );
            } catch (net.sf.json.JSONException jasonEx) {
                //This happens if the targets is not an array!
                JSONObject obj = formData.getJSONObject( "acrs" ).getJSONObject( "targets" );
                if(obj != null) {
                    ClearCaseUCMTarget target = req.bindJSON(ClearCaseUCMTarget.class, obj);
                    if(target != null && target.getBaselineName() != null && !target.getBaselineName().equals("")) {
                        targets.add(target);
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