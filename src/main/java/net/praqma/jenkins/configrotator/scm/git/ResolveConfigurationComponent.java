package net.praqma.jenkins.configrotator.scm.git;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * This involves cloning the repository
 */
public class ResolveConfigurationComponent implements FilePath.FileCallable<GitConfigurationComponent> {

    private String repository;
    private String branch;
    private String commitId;
    private boolean fixed;

    public ResolveConfigurationComponent( String repository, String branch, String commitId, boolean fixed ) {
        this.repository = repository;
        this.branch = branch;
        this.commitId = commitId;
        this.fixed = fixed;
    }

    @Override
    public GitConfigurationComponent invoke( File workspace, VirtualChannel channel ) throws IOException, InterruptedException {
        Logger logger = Logger.getLogger( ResolveConfigurationComponent.class.getName() );
        String name = repository.substring( repository.lastIndexOf( "/" ) );

        logger.fine("NAME1: " + name);

        if( name.matches( ".*?\\.git$" ) ) {
            name = name.substring( 0, name.length() - 4 );
        }

        if( name.startsWith( "/" ) ) {
            name = name.substring( 1 );
        }

        logger.fine("NAME2: " + name);

        File local = new File( workspace, name);

        try {
            logger.fine("Cloning repo from " + repository);
            try {
                org.eclipse.jgit.api.Git.cloneRepository().setURI( repository ).setDirectory( local ).call();
            } catch( JGitInternalException e ) {
                logger.warning(e.getMessage());
            }

            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repo = builder.setGitDir(new File( local, ".git" ) ).readEnvironment().findGitDir().build();

            logger.fine("Updating to " + branch);
            repo.updateRef( branch );
            RevWalk w = new RevWalk( repo );

            if( commitId == null ) {
                commitId = "HEAD";
            }

            logger.fine("Getting commit " + commitId);
            ObjectId o = repo.resolve( commitId );
            RevCommit commit = w.parseCommit( o );

            return new GitConfigurationComponent( name, branch, commit, fixed );

        } catch( GitAPIException e ) {
            throw new IOException( e );
        }
    }
}
