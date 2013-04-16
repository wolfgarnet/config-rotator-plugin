package net.praqma.jenkins.configrotator.scm.dvcs;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import net.praqma.jenkins.configrotator.ConfigurationRotator;
import net.praqma.jenkins.configrotator.scm.git.GitConfigurationComponent;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * This involves cloning the repository
 */
public abstract class BaseConfigurationComponentResolver<T extends BaseDVCSConfigurationComponent> implements FilePath.FileCallable<T> {

    protected String name;
    protected String repository;
    protected String branch;
    protected String commitId;
    protected boolean fixed;

    protected TaskListener listener;

    public BaseConfigurationComponentResolver( TaskListener listener, String name, String repository, String branch, String commitId, boolean fixed ) {
        this.name = name;
        this.repository = repository;
        this.branch = branch;
        this.commitId = commitId;
        this.fixed = fixed;

        this.listener = listener;
    }

    public abstract String getFixedName();
    public abstract void cloneRepository( File workspace );
    public abstract void ensureBranch( String branchName );
    public abstract void pull();
    public abstract T getConfigurationComponent();

    @Override
    public T invoke( File workspace, VirtualChannel channel ) throws IOException, InterruptedException {
        Logger logger = Logger.getLogger( BaseConfigurationComponentResolver.class.getName() );

        /* fixing name */
        if( name == null || name.isEmpty() ) {
            name = getFixedName();
        }

        logger.fine( "Name: " + name );

        /* Fixing branch */
        if( branch == null || branch.equals( "" ) ) {
            branch = "master";
        }

        File local = new File( workspace, name );

        logger.fine( "Cloning repository from " + repository );
        cloneRepository( local );

        logger.fine( "Creating branch " + branch );
        ensureBranch( branch );

        logger.fine( "Pulling" );
        pull();

        logger.fine( "Getting configuration component" );
        return getConfigurationComponent();

    }

    private void listPath( PrintStream logger, File path ) {
        logger.println( "PATH: " + path );
        for( String f : path.list() ) {
            logger.println( " * " + f );
        }
    }
}
