package net.praqma.jenkins.configrotator;

import com.aragost.javahg.BaseRepository;
import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.AddCommand;
import com.aragost.javahg.commands.CommitCommand;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class MercurialRule extends DVCSRule<Changeset> {

    private static Logger logger = Logger.getLogger( MercurialRule.class.getName() );

    private File hgPath;
    private BaseRepository hgRepo;

    @Override
    public void initialize( File gitPath ) {
        this.hgPath = gitPath;

        hgRepo = Repository.create( gitPath );
    }

    @Override
    public Changeset createCommit( String filename, String content ) throws IOException {

        File file = new File( hgPath, filename );
        boolean create = !file.exists();
        FileUtils.write( file, content, false );

        AddCommand.on( hgRepo ).include( filename ).execute();
        return CommitCommand.on( hgRepo ).user( "Praqma" ).message( ( create ? "Creating " : "Updating" ) + " " + filename ).execute();
    }

    @Override
    public String getRepo() {
        return hgRepo.getDirectory().getAbsolutePath();
    }

    protected void listPath( File path ) {
        logger.info( "Listing " + path + "(" + path.exists() + ")" );
        for( String f : path.list() ) {
            logger.info( " * " + f );
        }
    }
}
