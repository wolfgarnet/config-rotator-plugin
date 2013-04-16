package net.praqma.jenkins.configrotator.scm.mercurial;

import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSCommit;

/**
 * @author cwolfgang
 */
public class MercurialCommit extends BaseDVCSCommit {

    @Override
    public int getCommitTime() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }
}
