package net.praqma.jenkins.configrotator;

import hudson.model.Action;

public abstract class FeedAction implements Action {

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }
}
