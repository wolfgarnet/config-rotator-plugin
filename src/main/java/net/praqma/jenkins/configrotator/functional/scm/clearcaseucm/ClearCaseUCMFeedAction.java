package net.praqma.jenkins.configrotator.functional.scm.clearcaseucm;

import net.praqma.jenkins.configrotator.ConfigurationRotatorFeedAction;

public class ClearCaseUCMFeedAction extends ConfigurationRotatorFeedAction {
    @Override
    public String getComponentName() {
        return ClearCaseUCM.class.getSimpleName();
    }

    @Override
    public String getUrlName() {
        return ClearCaseUCM.class.getSimpleName();
    }
}
