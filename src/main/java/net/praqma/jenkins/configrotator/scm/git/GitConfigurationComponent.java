package net.praqma.jenkins.configrotator.scm.git;

import net.praqma.jenkins.configrotator.AbstractConfigurationComponent;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitConfigurationComponent extends AbstractConfigurationComponent {
    private RevCommit commit;
    private String name;

    public GitConfigurationComponent( String name, RevCommit commit, boolean fixed ) {
        super( fixed );
        this.commit = commit;
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
}
