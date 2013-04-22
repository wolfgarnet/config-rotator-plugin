package net.praqma.jenkins.configrotator.scm.mercurial;

import com.aragost.javahg.BaseRepository;
import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.LogCommand;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogEntry;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorVersion;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSChangeLogResolver;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class MercurialChangelogResolver extends BaseDVCSChangeLogResolver {

    public MercurialChangelogResolver( String name, String commitId, String branchName ) {
        super( name, commitId, branchName );
    }

    @Override
    protected ConfigRotatorChangeLogEntry getChangeLogEntry( File workspace ) {
        Logger logger = Logger.getLogger( MercurialChangelogResolver.class.getName() );

        File local = new File( workspace, name );

        logger.fine( "Create local repository object" );
        BaseRepository hgRepo = Repository.open( local );

        Changeset res = LogCommand.on( hgRepo ).branch( branchName ).rev( commitId ).single();

        String user = StringEscapeUtils.escapeHtml( res.getUser() );
        ConfigRotatorChangeLogEntry entry = new ConfigRotatorChangeLogEntry( res.getMessage(), user, new ArrayList<ConfigRotatorVersion>() );
        for( String file : res.getModifiedFiles() ) {
            entry.addVersion( new ConfigRotatorVersion( file, commitId, user ) );
        }

        logger.fine( "ENTRY: " + entry );

        return entry;
    }
}
