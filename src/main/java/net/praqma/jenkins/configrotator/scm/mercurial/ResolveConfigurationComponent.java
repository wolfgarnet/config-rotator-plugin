package net.praqma.jenkins.configrotator.scm.mercurial;

import com.aragost.javahg.BaseRepository;
import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.CloneCommand;
import com.aragost.javahg.commands.LogCommand;
import com.aragost.javahg.commands.UpdateCommand;
import com.aragost.javahg.commands.UpdateResult;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import net.praqma.jenkins.configrotator.ConfigurationRotator;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseConfigurationComponentResolver;
import net.praqma.jenkins.configrotator.scm.git.GitConfigurationComponent;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * This involves cloning the repository
 */
public class ResolveConfigurationComponent extends BaseConfigurationComponentResolver<MercurialConfigurationComponent> {

    private static Logger logger = Logger.getLogger( ResolveConfigurationComponent.class.getName() );

    private BaseRepository hgRepo;

    public ResolveConfigurationComponent( TaskListener listener, String name, String repository, String branch, String commitId, boolean fixed ) {
        super( listener, name, repository, branch, commitId, fixed );
    }

    @Override
    public String getDefaultBranchName() {
        return "default";
    }

    @Override
    public String getFixedName() {
        String name = repository.substring( repository.lastIndexOf( "/" ) );

        if( name.startsWith( "/" ) ) {
            name = name.substring( 1 );
        }

        return name;
    }

    @Override
    public void cloneRepository( File workspace ) {
        try {
            hgRepo = Repository.clone( workspace, this.repository );
        } catch( RuntimeException e ) {
            logger.fine( e.getMessage() );

            /* The repo was not empty, let's try to use it */
            hgRepo = Repository.open( workspace );
        }
    }

    @Override
    public void ensureBranch( String branchName ) {
        /* No op */
    }

    @Override
    public void pull() {
        /* No op */
    }

    @Override
    public MercurialConfigurationComponent getConfigurationComponent() {
        Changeset cs = LogCommand.on( hgRepo ).rev( commitId ).branch( branch ).single();
        logger.fine( "Change set node: " + cs.getNode() + ", " + cs.getRevision() );
        return new MercurialConfigurationComponent( name, repository, branch, cs, fixed );
    }
}
