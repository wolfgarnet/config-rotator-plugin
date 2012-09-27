package net.praqma.jenkins.configrotator.scm.git;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import net.praqma.jenkins.configrotator.*;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
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
                c = workspace.act( new ResolveConfigurationComponent( target.getName(), target.getRepository(), target.getBranch(), target.getCommitId(), target.getFixed() ) );
            } catch( Exception e ) {
                logger.log( Level.WARNING, "Whoops", e );
                throw new ConfigurationRotatorException( "Unable to get component for " + target, e );
            }

            logger.fine("Adding " + c);
            list.add( c );
        }
    }

    @Override
    public List<? extends Serializable> difference( AbstractConfiguration<GitConfigurationComponent> configuration ) throws ConfigurationRotatorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GitConfiguration clone() throws CloneNotSupportedException {
        GitConfiguration n = new GitConfiguration();

        for( GitConfigurationComponent gc : this.list ) {
            n.list.add( (GitConfigurationComponent) gc.clone() );
        }

        return n;
    }

    @Override
    public String toHtml() {
        StringBuilder builder = new StringBuilder();

        builder.append( "<table border=\"0\" style=\"text-align:left;\">" );
        builder.append( "<thead>" );
        builder.append( "<th>" ).append( "Repository" ).append( "</th>" );
        builder.append( "<th>" ).append( "Branch" ).append( "</th>" );
        builder.append( "<th>" ).append( "Commit" ).append( "</th>" );
        builder.append( "<th>" ).append( "Fixed" ).append( "</th>" );

        for( GitConfigurationComponent comp : getList() ) {
            builder.append( comp.toHtml() );
        }

        builder.append( "</thead>" );
        builder.append( "</table>" );
        return builder.toString();
    }
}
