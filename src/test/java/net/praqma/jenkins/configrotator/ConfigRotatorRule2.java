package net.praqma.jenkins.configrotator;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Slave;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class ConfigRotatorRule2 extends JenkinsRule {

    private File outputDir;

    public ConfigRotatorRule2() {

        //System.out.println( "ENVS: " +  System.getenv() );

        if( System.getenv().containsKey( "BUILD_NUMBER" ) ) {
            String bname = System.getenv( "JOB_NAME" );
            Integer number = new Integer( System.getenv( "BUILD_NUMBER" ) );

            this.outputDir = new File( new File( new File( System.getProperty( "user.dir" ) ), "test-logs" ), number.toString() );
        } else {
            this.outputDir = new File( new File( System.getProperty( "user.dir" ) ), "runs" );
        }

        this.outputDir.mkdirs();
    }

    private static Logger logger = Logger.getLogger( ConfigRotatorRule2.class.getName() );

    public AbstractBuild<?, ?> buildProject( AbstractProject<?, ?> project, boolean fail, Slave slave ) throws IOException {

        if( slave != null ) {
            logger.fine( "Running on " + slave );
            project.setAssignedNode( slave );
        }

        AbstractBuild<?, ?> build = null;
        try {
            EnableLoggerAction action = new EnableLoggerAction( outputDir );
            build = project.scheduleBuild2( 0, new Cause.UserCause(), action ).get();
        } catch( Exception e ) {
            logger.info( "Build failed(" + (fail?"on purpose":"it should not?") + "): " + e.getMessage() );
        }

        logger.info( "Build info for: " + build );

        logger.info( "Workspace: " + build.getWorkspace() );

        logger.info( "Logfile: " + build.getLogFile() );

        logger.info( "DESCRIPTION: " + build.getDescription() );

        logger.info( "-------------------------------------------------\nJENKINS LOG: " );
        logger.info( getLog( build ) );
        logger.info( "\n-------------------------------------------------\n" );

        return build;
    }
}
