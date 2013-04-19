package net.praqma.jenkins.configrotator.scm.mercurial;

import com.aragost.javahg.BaseRepository;
import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.LogCommand;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSCommit;
import net.praqma.jenkins.configrotator.scm.dvcs.NextCommitResolver;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class ResolveNextCommit extends NextCommitResolver<MercurialCommit> {

    public ResolveNextCommit( String name, String branchName, String commitOrigin ) {
        super( name, branchName, commitOrigin );
    }

    @Override
    public MercurialCommit resolve( File workspace ) {
        Logger logger = Logger.getLogger( ResolveNextCommit.class.getName() );

        logger.fine( "Create local repository object in " + workspace );
        BaseRepository hgRepo = Repository.open( workspace );

        List<Changeset> changesets = LogCommand.on( hgRepo ).rev( commitOrigin + ":" ).limit( 2 ).execute();
        logger.fine( "Change sets found: " + changesets );
        if( changesets.size() == 2 ) {
            return new MercurialCommit( changesets.get( 1 ) );
        } else {
            return null;
        }
    }

}
