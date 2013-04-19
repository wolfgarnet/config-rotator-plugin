package net.praqma.jenkins.configrotator.scm.mercurial;

import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSFeedAction;

/**
 * @author cwolfgang
 */
public class MercurialFeedAction extends BaseDVCSFeedAction {

    @Override
    public String getComponentName() {
        return Mercurial.class.getSimpleName();
    }

    @Override
    public String getUrlName() {
        return Mercurial.class.getSimpleName();
    }
}
