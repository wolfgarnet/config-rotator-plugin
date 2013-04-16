package net.praqma.jenkins.configrotator.scm.git;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

/**
 * @author cwolfgang
 */
public class Checkout implements FilePath.FileCallable<Boolean> {

    private String commitId;
    private String name;
    private String branch;

    public Checkout( String name, String branch, String commitId ) {
        this.commitId = commitId;
        this.name = name;
        this.branch = branch;
    }

    @Override
    public Boolean invoke( File workspace, VirtualChannel channel ) throws IOException, InterruptedException {

        File local = new File( workspace, name );

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder.setGitDir( new File( local, ".git" ) ).readEnvironment().findGitDir().build();
        org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git( repo );

        try {
            git.checkout().setName( branch ).setAllPaths( true ).setForce( true ).setStartPoint( commitId ).call();
        } catch( GitAPIException e ) {
            throw new IOException( e );
        }

        return true;
    }
}
