package net.praqma.jenkins.configrotator.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class GitTest {

    @Test
    public void testing() throws IOException, GitAPIException {
        //String commitId = "8ecfebfbda136914da727334c6fc94ab56a05ac5";
        String commitId = "8ecfebfbda136914da727334c6fc94ab56a05ac5";
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

        ObjectId head = repo.resolve("HEAD");
        System.out.println( "OBEJCTID: " + head );

        ObjectId o = repo.resolve( commitId );
        System.out.println( "OBEJCTID: " + o.getName() );

        Git git = new Git( repo );
        Iterable<RevCommit> logs = git.log().add( o ).call();
        System.out.println( "COMMIT: " + logs );

    }
}
