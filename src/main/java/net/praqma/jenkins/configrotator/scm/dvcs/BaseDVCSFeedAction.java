package net.praqma.jenkins.configrotator.scm.dvcs;

import net.praqma.jenkins.configrotator.ConfigurationRotatorFeedAction;
import net.praqma.jenkins.configrotator.scm.mercurial.Mercurial;

/**
 * @author cwolfgang
 */
public class BaseDVCSFeedAction extends ConfigurationRotatorFeedAction {

    @Override
    public String getComponentName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getUrlName() {
        return this.getClass().getSimpleName();
    }
}
