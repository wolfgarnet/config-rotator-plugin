package net.praqma.jenkins.configrotator.scm.dvcs;

import hudson.FilePath;
import hudson.model.TaskListener;
import net.praqma.jenkins.configrotator.AbstractConfigurationComponent;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public abstract class BaseDVCSConfigurationComponent extends AbstractConfigurationComponent {

    private static Logger logger = Logger.getLogger( BaseDVCSConfigurationComponent.class.getName() );

    protected String commitId;
    protected String name;
    protected String branch;
    protected String repository;

    public BaseDVCSConfigurationComponent( String name, String repository, String branch, String commitId, boolean fixed ) {
        super( fixed );
        this.name = name;
        this.repository = repository;
        this.branch = branch;
        this.commitId = commitId;
    }

    public abstract void checkout( FilePath workspace, TaskListener listener ) throws IOException, InterruptedException;

    public String getBranch() {
        return branch;
    }

    public String getRepository() {
        return repository;
    }

    public String getName() {
        return name;
    }

    public void setCommitId( String commitId ) {
        this.commitId = commitId;
    }

    public String getCommitId() {
        return commitId;
    }

    @Override
    public String getComponentName() {
        return repository;
    }

    @Override
    public String prettyPrint() {
        return name + ": " + repository + ", " + branch + ", " + commitId;
    }

    @Override
    public String getFeedName() {
        return repository;
    }

    @Override
    public String getFeedId() {
        return repository;
    }

    @Override
    public String toHtml() {
        StringBuilder builder = new StringBuilder();

        return getBasicHtml( builder, new Element( repository, isChangedLast() ), new Element( branch, isChangedLast() ), new Element( commitId, isChangedLast() ), new Element( fixed+"", isChangedLast() ) );
    }

    @Override
    public boolean equals( Object other ) {
        if( other == this ) {
            return true;
        }

        if( this.getClass().isInstance( other ) ) {
            BaseDVCSConfigurationComponent o = (BaseDVCSConfigurationComponent) other;

            logger.finest( "Other: " + o.commitId + " == " + commitId );

            return ( o.commitId.equals( commitId ) && ( o.isFixed() == fixed ) );
        } else {
            return false;
        }
    }
}
