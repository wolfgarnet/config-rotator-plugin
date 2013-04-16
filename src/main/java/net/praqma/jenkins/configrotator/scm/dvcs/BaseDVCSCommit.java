package net.praqma.jenkins.configrotator.scm.dvcs;

/**
 * @author cwolfgang
 */
public abstract class BaseDVCSCommit {

    public abstract int getCommitTime();

    public abstract String getName();
}
