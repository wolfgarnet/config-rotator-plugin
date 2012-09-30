package net.praqma.jenkins.configrotator.scm.git;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RefSpec;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * This involves cloning the repository
 */
public class ResolveConfigurationComponent implements FilePath.FileCallable<GitConfigurationComponent> {

    private String name;
    private String repository;
    private String branch;
    private String commitId;
    private boolean fixed;

    public ResolveConfigurationComponent( String name, String repository, String branch, String commitId, boolean fixed ) {
        this.name = name;
        this.repository = repository;
        this.branch = branch;
        this.commitId = commitId;
        this.fixed = fixed;
    }

    @Override
    public GitConfigurationComponent invoke( File workspace, VirtualChannel channel ) throws IOException, InterruptedException {
        Logger logger = Logger.getLogger( ResolveConfigurationComponent.class.getName() );

        if( name == null || name.equals("") ) {
            name = repository.substring( repository.lastIndexOf( "/" ) );

            if( name.matches( ".*?\\.git$" ) ) {
                name = name.substring( 0, name.length() - 4 );
            }

            if( name.startsWith( "/" ) ) {
                name = name.substring( 1 );
            }
        }

        logger.fine("Name: " + name);

        File local = new File( workspace, name);

        try {
            logger.fine("Cloning repo from " + repository);
            try {
                org.eclipse.jgit.api.Git.cloneRepository().setURI( repository ).setDirectory( local ).setCloneAllBranches( true ).call();
            } catch( JGitInternalException e ) {
                logger.warning(e.getMessage());
            }

            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repo = builder.setGitDir(new File( local, ".git" ) ).readEnvironment().findGitDir().build();
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git( repo );

            logger.fine("Updating to " + branch);
            git.branchCreate().setUpstreamMode( CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).setName(branch).setStartPoint("origin/"+branch).call();

            try {
                logger.fine("Pulling");
                git.pull().call();
                //git.fetch().setRefSpecs( new RefSpec( "refs/heads/*" ).setForceUpdate( true ) ).call();
            } catch( GitAPIException e ) {
                throw new IOException( e );
            }


            //repo.updateRef( branch );
            git.checkout().setName( branch ).call();
            logger.fine("BRANCH: " + repo.getBranch());
            RevWalk w = new RevWalk( repo );

            if( commitId == null ) {
                commitId = "HEAD";
            }

            logger.fine("Getting commit \"" + commitId +"\"");
            ObjectId o = repo.resolve( commitId );
            RevCommit commit = w.parseCommit( o );
            logger.fine("RevCommit: " + commit);

            return new GitConfigurationComponent( name, repository, branch, commit, fixed );

        } catch( GitAPIException e ) {
            throw new IOException( e );
        }
    }
}
