package net.praqma.jenkins.configrotator.scm.mercurial;

import com.aragost.javahg.BaseRepository;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.PullCommand;
import com.aragost.javahg.commands.UpdateCommand;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

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
        Logger logger = Logger.getLogger( Checkout.class.getName() );

        File local = new File( workspace, name );

        /* Checkout configuration */
        logger.fine( "Create local repository object" );
        BaseRepository hgRepo = Repository.open( local );

        logger.fine( "Pulling" );
        PullCommand.on( hgRepo ).execute();

        logger.fine( "Updating to " + commitId );
        UpdateCommand.on( hgRepo ).rev( commitId ).execute();

        return true;
    }
}
