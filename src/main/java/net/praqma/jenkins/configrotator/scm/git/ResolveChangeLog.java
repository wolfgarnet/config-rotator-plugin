package net.praqma.jenkins.configrotator.scm.git;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogEntry;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorVersion;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ResolveChangeLog implements FilePath.FileCallable<ConfigRotatorChangeLogEntry> {

    private String commitId;
    private String name;

    public ResolveChangeLog( String name, String commitId ) {
        this.commitId = commitId;
        this.name = name;
    }

    @Override
    public ConfigRotatorChangeLogEntry invoke( File workspace, VirtualChannel virtualChannel ) throws IOException, InterruptedException {

        Logger logger = Logger.getLogger( ResolveChangeLog.class.getName() );

        File local = new File( workspace, name );
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder.setGitDir( new File( local, ".git" ) ).readEnvironment().findGitDir().build();

        RevWalk w = new RevWalk( repo );

        ObjectId o = repo.resolve( commitId );
        RevCommit commit = w.parseCommit( o );

        RevCommit parent = w.parseCommit( commit.getParent( 0 ).getId() );

        logger.fine("Diffing " + commit.getName() + " -> " + parent.getName() );

        DiffFormatter df = new DiffFormatter( DisabledOutputStream.INSTANCE );
        df.setRepository( repo );
        df.setDiffComparator( RawTextComparator.DEFAULT );
        df.setDetectRenames( true );

        List<DiffEntry> diffs = df.scan( parent.getTree(), commit.getTree() );
        ConfigRotatorChangeLogEntry entry = new ConfigRotatorChangeLogEntry( commit.getFullMessage(), commit.getAuthorIdent().getName(), new ArrayList<ConfigRotatorVersion>());
        for( DiffEntry diff : diffs ) {
            entry.addVersion( new ConfigRotatorVersion( diff.getNewPath(), "", commit.getAuthorIdent().getName() ) );
        }

        logger.fine("ENTRY: " + entry);

        return entry;
    }

}
