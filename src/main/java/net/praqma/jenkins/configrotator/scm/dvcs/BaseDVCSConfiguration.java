package net.praqma.jenkins.configrotator.scm.dvcs;

import hudson.FilePath;
import hudson.model.TaskListener;
import net.praqma.jenkins.configrotator.AbstractConfiguration;
import net.praqma.jenkins.configrotator.AbstractTarget;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogEntry;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public abstract class BaseDVCSConfiguration<T extends BaseDVCSConfigurationComponent, TARGET extends BaseDVCSTarget> extends AbstractConfiguration<T, TARGET> {

    private static Logger logger = Logger.getLogger( BaseDVCSConfiguration.class.getName() );

    public BaseDVCSConfiguration() {}

    public BaseDVCSConfiguration( List<TARGET> targets, FilePath workspace, TaskListener listener ) throws ConfigurationRotatorException {
        for( AbstractTarget t : targets ) {
            BaseDVCSTarget target = (BaseDVCSTarget)t;

            logger.fine("Getting component for " + target);
            T c = null;
            try {
                c = workspace.act( getConfigurationComponentResolver( listener, target.getName(), target.getRepository(), target.getBranch(), target.getCommitId(), target.getFixed() ) );
            } catch( Exception e ) {
                logger.log( Level.WARNING, "Whoops", e );
                throw new ConfigurationRotatorException( "Unable to get component for " + target, e );
            }

            logger.fine("Adding " + c);
            list.add( c );
        }
    }

    public abstract FilePath.FileCallable<T> getConfigurationComponentResolver( TaskListener listener, String name, String repository, String branch, String commitId, boolean fixed );

    public abstract void checkout( FilePath workspace, TaskListener listener ) throws IOException, InterruptedException;

    @Override
    public List<ConfigRotatorChangeLogEntry> difference( T component, T other ) throws ConfigurationRotatorException {
        return null;
    }

    @Override
    public String toHtml() {
        StringBuilder builder = new StringBuilder();
        return basicHtml( builder, "Repository", "Branch", "Commit", "Fixed" );
    }

    @Override
    public boolean equals( Object other ) {
        if( other == this ) {
            return true;
        }

        if( this.getClass().isInstance( other ) ) {
            BaseDVCSConfiguration<T, TARGET> o = (BaseDVCSConfiguration<T, TARGET>) other;
            /* Check size */
            if( o.getList().size() != list.size() ) {
                return false;
            }

            /* Check elements, the size is identical */
            for( int i = 0; i < list.size(); ++i ) {
                if( !o.list.get( i ).equals( list.get( i ) ) ) {
                    return false;
                }
            }

            /* Everything is ok */
            return true;
        } else {
            return true;
        }
    }
}
