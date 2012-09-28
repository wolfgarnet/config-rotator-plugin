package net.praqma.jenkins.configrotator.scm.git;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ResolveNextCommit implements FilePath.FileCallable<RevCommit> {

    private String commitId;
    private String name;

    public ResolveNextCommit( String name, String commitId ) {
        this.commitId = commitId;
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

        RevWalk w = new RevWalk( repo );

        ObjectId ohead = repo.resolve( "HEAD" );
        ObjectId ostart = repo.resolve( commitId );
        RevCommit commithead = w.parseCommit( ohead );
        RevCommit commit = w.parseCommit( ostart );

        logger.fine( "Commit start: " + commitId );

        w.markStart( commithead );

        RevCommit next = null;

        for( RevCommit c : w ) {
            if( c != null && c.equals(commit) ) {
                break;
            }

            if( c == null ) {
                break;
            }

            next = c;
        }

        w.dispose();

        logger.fine( "Next is " + ( next == null ? "N/A" : next.getName() ) );

        return next;
    }

}
