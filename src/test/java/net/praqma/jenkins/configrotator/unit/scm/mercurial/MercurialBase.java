package net.praqma.jenkins.configrotator.unit.scm.mercurial;

import net.praqma.jenkins.configrotator.scm.mercurial.MercurialCommit;
import net.praqma.jenkins.configrotator.scm.mercurial.MercurialConfiguration;
import net.praqma.jenkins.configrotator.scm.mercurial.MercurialConfigurationComponent;
import net.praqma.jenkins.configrotator.scm.mercurial.MercurialTarget;
import net.praqma.jenkins.configrotator.unit.scm.dvcs.BaseUnitTest;

/**
 * @author cwolfgang
 */
public abstract class MercurialBase extends BaseUnitTest<MercurialCommit, MercurialConfigurationComponent, MercurialTarget, MercurialConfiguration> {

    @Override
    public MercurialConfiguration getDefaultConfiguration() {
        return new MercurialConfiguration();
    }

    @Override
    public Class<MercurialCommit> getCommitClass() {
        return MercurialCommit.class;
    }

    @Override
    public Class<MercurialConfigurationComponent> getComponentClass() {
        return MercurialConfigurationComponent.class;
    }

    @Override
    public Class<MercurialConfiguration> getConfigurationClass() {
        return MercurialConfiguration.class;
    }

}
