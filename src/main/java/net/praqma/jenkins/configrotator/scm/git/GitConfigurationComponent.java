package net.praqma.jenkins.configrotator.scm.git;

import net.praqma.jenkins.configrotator.AbstractConfigurationComponent;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;

public class GitConfigurationComponent extends AbstractConfigurationComponent {
    private RevCommit commit;
    private String name;
    private String branch;

    public GitConfigurationComponent( String name, String branch, RevCommit commit, boolean fixed ) {
        super( fixed );
        this.commit = commit;
        this.name = name;
        this.branch = branch;
    }

    public void setCommit( RevCommit commit ) {
        this.commit = commit;
    }

    public String getName() {
        return name;
    }

    public RevCommit getCommit() {
        return commit;
    }

    @Override
    public File getFeedFile( File path ) {
        return new File( new File( path, name ), branch );
    }
}
