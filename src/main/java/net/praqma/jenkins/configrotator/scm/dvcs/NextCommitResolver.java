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
    protected String commitOrigin;
    protected String branchName;

    public NextCommitResolver( String name, String branchName, String commitOrigin ) {
        this.name = name;
        this.commitOrigin = commitOrigin;
        this.branchName = branchName;
    }

    @Override
    public T invoke( File f, VirtualChannel channel ) throws IOException, InterruptedException {
        File workspace = new File( f, name );
        return resolve( workspace );
    }

    public abstract T resolve( File workspace );
}
