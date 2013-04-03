package net.praqma.jenkins.configrotator;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Slave;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class ConfigRotatorRule2 extends JenkinsRule {

    private static Logger logger = Logger.getLogger( ConfigRotatorRule2.class.getName() );

    public AbstractBuild<?, ?> buildProject( AbstractProject<?, ?> project, boolean fail, Slave slave ) throws IOException {

        if( slave != null ) {
            logger.fine( "Running on " + slave );
            project.setAssignedNode( slave );
        }

        AbstractBuild<?, ?> build = null;
        try {
            build = project.scheduleBuild2( 0, new Cause.UserCause() ).get();
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
