package net.praqma.jenkins.configrotator.functional.scm.git;

import hudson.FilePath;
import hudson.model.TaskListener;
import net.praqma.jenkins.configrotator.*;
import net.praqma.jenkins.configrotator.functional.scm.ConfigRotatorChangeLogEntry;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitConfiguration extends AbstractConfiguration<GitConfigurationComponent> {

    private static Logger logger = Logger.getLogger( GitConfiguration.class.getName() );

    private GitConfiguration() {}

    public GitConfiguration( List<GitTarget> targets, FilePath workspace, TaskListener listener ) throws ConfigurationRotatorException {
        for( AbstractTarget t : targets ) {
            GitTarget target = (GitTarget)t;

            logger.fine("Getting component for " + target);
            GitConfigurationComponent c = null;
            try {
                c = workspace.act( new ResolveConfigurationComponent( listener, target.getName(), target.getRepository(), target.getBranch(), target.getCommitId(), target.getFixed() ) );
            } catch( Exception e ) {
                logger.log( Level.WARNING, "Whoops", e );
                throw new ConfigurationRotatorException( "Unable to get component for " + target, e );
            }

            logger.fine("Adding " + c);
            list.add( c );
        }
    }

    @Override
    public List<ConfigRotatorChangeLogEntry> difference( GitConfigurationComponent component, GitConfigurationComponent other ) throws ConfigurationRotatorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GitConfiguration clone() {
        GitConfiguration n = new GitConfiguration();

        for( GitConfigurationComponent gc : this.list ) {
            n.list.add( (GitConfigurationComponent) gc.clone() );
        }

        return n;
    }

    @Override
    public String toHtml() {
        StringBuilder builder = new StringBuilder();
        return basicHtml( builder, "Repository", "Branch", "Commit", "Fixed" );
    }
}
