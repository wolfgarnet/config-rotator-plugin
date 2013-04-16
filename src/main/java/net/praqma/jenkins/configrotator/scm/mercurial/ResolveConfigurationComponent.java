package net.praqma.jenkins.configrotator.scm.mercurial;

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

    public ResolveConfigurationComponent( TaskListener listener, String name, String repository, String branch, String commitId, boolean fixed ) {
        super( listener, name, repository, branch, commitId, fixed );
    }

    @Override
    public String getFixedName() {
        String name = repository.substring( repository.lastIndexOf( "/" ) );

        if( name.matches( ".*?\\.git$" ) ) {
            name = name.substring( 0, name.length() - 4 );
        }

        if( name.startsWith( "/" ) ) {
            name = name.substring( 1 );
        }

        return name;
    }

    @Override
    public void cloneRepository( File workspace ) {
    }

    @Override
    public void ensureBranch( String branchName ) {
    }

    @Override
    public void pull() {
    }

    @Override
    public MercurialConfigurationComponent getConfigurationComponent() {
        return null;
    }
}
