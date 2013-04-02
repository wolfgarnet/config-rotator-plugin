package net.praqma.jenkins.configrotator.scm.git;

import net.praqma.jenkins.configrotator.ConfigurationRotatorFeedAction;

import java.util.logging.Logger;

public class GitFeedAction extends ConfigurationRotatorFeedAction {

    private static Logger logger = Logger.getLogger( GitFeedAction.class.getName() );

    @Override
    public String getUrlName() {
        return Git.class.getSimpleName();
    }

    @Override
    public String getComponentName() {
        return Git.class.getSimpleName();
    }
}
