package net.praqma.jenkins.configrotator.scm.mercurial;

import com.aragost.javahg.Changeset;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSCommit;

/**
 * @author cwolfgang
 */
public class MercurialCommit extends BaseDVCSCommit {

    private long time;
    private String user;

    /**
     * Typically the revision in the for of a number or a SHA
     */
    private String name;

    public MercurialCommit( Changeset changeset ) {
        this.user = changeset.getUser();
        this.time = changeset.getTimestamp().getDate().getTime();
        this.name = String.valueOf( changeset.getRevision() );
    }

    @Override
    public long getCommitTime() {
        return time;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUser() {
        return user;
    }
}
