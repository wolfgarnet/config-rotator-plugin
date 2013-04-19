package net.praqma.jenkins.configrotator.scm.dvcs;

import java.io.Serializable;

/**
 * @author cwolfgang
 */
public abstract class BaseDVCSCommit implements Serializable {

    public abstract long getCommitTime();

    public abstract String getName();

    public abstract String getUser();
}
