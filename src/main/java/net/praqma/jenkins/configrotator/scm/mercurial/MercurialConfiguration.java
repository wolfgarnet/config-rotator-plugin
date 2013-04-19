package net.praqma.jenkins.configrotator.scm.mercurial;

import hudson.FilePath;
import hudson.model.TaskListener;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSConfiguration;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSTarget;

import java.io.IOException;
import java.util.List;

/**
 * @author cwolfgang
 */
public class MercurialConfiguration extends BaseDVCSConfiguration<MercurialConfigurationComponent, MercurialTarget> {

    public MercurialConfiguration() {}

    public MercurialConfiguration( List<MercurialTarget> targets, FilePath workspace, TaskListener listener ) throws ConfigurationRotatorException {
        super( targets, workspace, listener );
    }

    @Override
    public FilePath.FileCallable<MercurialConfigurationComponent> getConfigurationComponentResolver( TaskListener listener, String name, String repository, String branch, String commitId, boolean fixed ) {
        return new ResolveConfigurationComponent( listener, name, repository, branch, commitId, fixed );
    }

    @Override
    public void checkout( FilePath workspace, TaskListener listener ) throws IOException, InterruptedException {
        for( MercurialConfigurationComponent c : getList() ) {
            c.checkout( workspace, listener );
        }
    }

    @Override
    public MercurialConfiguration clone() {
        MercurialConfiguration n = new MercurialConfiguration();

        for( MercurialConfigurationComponent mc : this.list ) {
            n.list.add( (MercurialConfigurationComponent) mc.clone() );
        }

        return n;
    }
}
