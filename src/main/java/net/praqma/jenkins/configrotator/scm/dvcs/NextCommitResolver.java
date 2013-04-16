package net.praqma.jenkins.configrotator.scm.dvcs;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;

/**
 * @author cwolfgang
 */
public abstract class NextCommitResolver<T extends BaseDVCSCommit> implements FilePath.FileCallable<T> {

    protected String name;
    protected String commitId;

    public NextCommitResolver( String name, String commitId ) {
        this.name = name;
        this.commitId = commitId;
    }

    @Override
    public T invoke( File f, VirtualChannel channel ) throws IOException, InterruptedException {
        return resolve();
    }

    public abstract T resolve();
}
