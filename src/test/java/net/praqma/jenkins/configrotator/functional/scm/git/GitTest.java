package net.praqma.jenkins.configrotator.functional.scm.git;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class GitTest {

    @Test
    public void testing() throws IOException, GitAPIException {

        String commitId = "HEAD";
        //String commitId = "8ecfebfbda136914da727334c6fc94ab56a05ac5";
        //String commitId = "6035e2b582d5547c250dd398d4edc5fedfc57dd8";
        //String commitId = "1d5e0bcaa5b94359fac20e642e3b757989270859";

        String repository = "http://github.com/praqma-test/module1.git";

        File local = new File( "C:\\temp\\configrotater\\test" );

        try {
            org.eclipse.jgit.api.Git.cloneRepository().setURI( repository ).setDirectory( local ).setBare(true).call();
        } catch( JGitInternalException e ) {
            System.out.println( e.getMessage() );
        }

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder.setGitDir(local).readEnvironment().findGitDir().build();
        repo.updateRef("master");
        System.out.println( "REPO: " + repo.getObjectDatabase() );

        ObjectId ohead = repo.resolve( commitId );
        ObjectId o1 = repo.resolve( "8ecfebfbda136914da727334c6fc94ab56a05ac5" );
        ObjectId o2 = repo.resolve( "6035e2b582d5547c250dd398d4edc5fedfc57dd8" );
        ObjectId o3 = repo.resolve( "1d5e0bcaa5b94359fac20e642e3b757989270859" );
        System.out.println( "OBEJCTID: " + ohead.getName() );

        /*
        Git git = new Git( repo );
        Iterable<RevCommit> logs = git.log().add( o ).call();
        System.out.println( "COMMIT: " + logs );
        */

        RevWalk w = new RevWalk( repo );

        RevCommit commithead = w.parseCommit( ohead );
        RevCommit commit1 = w.parseCommit( o1 );
        RevCommit commit2 = w.parseCommit( o2 );
        RevCommit commit3 = w.parseCommit( o3 );

        System.out.println( "1" );
        //w.sort( RevSort.COMMIT_TIME_DESC );
        w.sort( RevSort.REVERSE );
        System.out.println( "2" );
        w.markStart( commithead );
        w.markUninteresting( commit1 );
        System.out.println( "3" );

        System.out.println( "START: " + commit1 );

        /*
        for( RevCommit c : w ) {
            System.out.println( "::: " + c );
        }
        */

        System.out.println( "next: " + w.next() );
    }
}
