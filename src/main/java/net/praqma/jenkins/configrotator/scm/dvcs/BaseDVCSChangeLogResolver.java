package net.praqma.jenkins.configrotator.scm.dvcs;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogEntry;

import java.io.File;
import java.io.IOException;

/**
 * @author cwolfgang
 */
public abstract class BaseDVCSChangeLogResolver implements FilePath.FileCallable<ConfigRotatorChangeLogEntry> {

    protected String commitId;
    protected String name;
    protected String branchName;

    public BaseDVCSChangeLogResolver( String name, String commitId, String branchName ) {
        this.commitId = commitId;
        this.name = name;
        this.branchName = branchName;
    }

    @Override
    public ConfigRotatorChangeLogEntry invoke( File f, VirtualChannel channel ) throws IOException, InterruptedException {
        return getChangeLogEntry( f );
    }

    protected abstract ConfigRotatorChangeLogEntry getChangeLogEntry( File workspace );
}
