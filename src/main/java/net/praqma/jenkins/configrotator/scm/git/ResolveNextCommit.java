package net.praqma.jenkins.configrotator.scm.git;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ResolveNextCommit implements FilePath.FileCallable<RevCommit> {

    private RevCommit commit;
    private String name;

    public ResolveNextCommit( String name, RevCommit commit ) {
        this.commit = commit;
        this.name = name;
    }

    @Override
    public RevCommit invoke( File workspace, VirtualChannel virtualChannel ) throws IOException, InterruptedException {

        Logger logger = Logger.getLogger( ResolveNextCommit.class.getName() );

        File local = new File( workspace, name );
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        logger.fine("Initializing repo");
        Repository repo = builder.setGitDir( new File( local, ".git" ) ).readEnvironment().findGitDir().build();
        org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git( repo );
        try {
            logger.fine("Pulling");
            git.pull().call();
        } catch( GitAPIException e ) {
            throw new IOException( e );
        }

        logger.fine( "Finding next commit" );
        RevWalk w = new RevWalk( repo );
        w.markStart( commit );

        return w.next();
    }
}
