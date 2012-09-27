package net.praqma.jenkins.configrotator.scm.git;

import net.praqma.jenkins.configrotator.AbstractConfigurationComponent;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.logging.Logger;

public class GitConfigurationComponent extends AbstractConfigurationComponent {

    private static Logger logger = Logger.getLogger( GitConfigurationComponent.class.getName() );

    private transient RevCommit commit;
    private String commitId;
    private String name;
    private String branch;
    private String repository;

    private GitConfigurationComponent( String name, String repository, String branch, String commitId, boolean fixed ) {
        super( fixed );
        this.name = name;
        this.repository = repository;
        this.branch = branch;
        this.commitId = commitId;
    }

    public GitConfigurationComponent( String name, String repository, String branch, RevCommit commit, boolean fixed ) {
        super( fixed );
        this.commit = commit;
        if( commit != null ) {
            this.commitId = commit.getName();
        }
        this.name = name;
        this.branch = branch;
        this.repository = repository;
    }

    public String getBranch() {
        return branch;
    }

    public String getRepository() {
        return repository;
    }

    public String getName() {
        return name;
    }

    public RevCommit getCommit() {
        return commit;
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
    protected Object clone() throws CloneNotSupportedException {
        GitConfigurationComponent gcc = new GitConfigurationComponent( name, repository, branch, commitId, fixed );
        return  gcc;
    }

    @Override
    public String getFeedId() {
        return repository;
    }

    @Override
    public String getFeedName() {
        return repository;
    }

    @Override
    public String toString() {
        return "GitComponent[" + name + ": " + repository + ", " + branch + ", " + commitId + "]";
    }

    @Override
    public String prettyPrint() {
        return name + ": " + repository + ", " + branch + ", " + commitId;
    }

    @Override
    public String toHtml() {
        StringBuilder builder = new StringBuilder();
        builder.append( "<tr>" );
        builder.append( "<td style=\"padding:5px 10px;\">" ).append( repository ).append( "</td>" );
        builder.append( "<td style=\"padding:5px 10px;\">" ).append( branch ).append( "</td>" );
        if( isChangedLast() ) {
            builder.append( "<td style=\"font-weight:bold;color:#FF6633;padding:5px 10px;\">" ).append( commitId ).append( "</td>" );
        } else {
            builder.append( "<td style=\"padding:5px 10px;\">" ).append( commitId ).append( "</td>" );
        }
        builder.append( "<td style=\"padding:5px 10px;\">" ).append( fixed ).append( "</td>" ).append( "</tr>" );
        return builder.toString();
    }

}
