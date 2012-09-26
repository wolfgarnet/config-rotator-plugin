package net.praqma.jenkins.configrotator.scm.git;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import net.praqma.jenkins.configrotator.AbstractConfiguration;
import net.praqma.jenkins.configrotator.ConfigurationRotator;
import net.praqma.jenkins.configrotator.ConfigurationRotatorBuildAction;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitConfiguration extends AbstractConfiguration<GitConfigurationComponent> {

    private static Logger logger = Logger.getLogger( GitConfiguration.class.getName() );

    @Override
    public List<? extends Serializable> difference( AbstractConfiguration<GitConfigurationComponent> configuration ) throws ConfigurationRotatorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static GitConfiguration getConfigurationFromTargets( List<GitTarget> targets, FilePath workspace, TaskListener listener ) throws ConfigurationRotatorException {
        logger.finest( "Getting configurations from targets: " + targets );
        PrintStream out = listener.getLogger();

        GitConfiguration config = new GitConfiguration();

        for( GitTarget target : targets ) {

            logger.fine("Getting component for " + target);
            GitConfigurationComponent c = null;
            try {
                c = workspace.act( new ResolveConfigurationComponent( target.getRepository(), target.getBranch(), target.getCommitId(), target.getFixed() ) );
            } catch( Exception e ) {
                logger.log( Level.WARNING, "Whoops", e );
                throw new ConfigurationRotatorException( "Unable to get component for " + target, e );
            }

            logger.fine("Adding " + c);
            config.list.add( c );
        }

        return config;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        GitConfiguration n = new GitConfiguration();

        for( GitConfigurationComponent gc : this.list ) {
            n.list.add( (GitConfigurationComponent) gc.clone() );
        }

        return n;
    }

    @Override
    public String toHtml() {
        return "";
    }
}
