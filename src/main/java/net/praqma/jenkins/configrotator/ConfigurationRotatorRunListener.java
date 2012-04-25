/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.model.listeners.RunListener;

@Extension
public class ConfigurationRotatorRunListener extends RunListener<Run> {

    public ConfigurationRotatorRunListener() {
        super(Run.class);

    }

    @Override
    public void onCompleted(Run run, TaskListener listener) {
        /*
         * FIXME Test for MatrixBuild and add to context
         */
        AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) run;

        if (build.getProject().getScm() instanceof ConfigurationRotator) {
            System.out.println("onCompleted runlistener - we should write xml here");
            //build.getResult().isBetterThan(Result.SUCCESS);
        } else {
            System.out.println("onCompleted runlistener - was not a ConfigurationRotator");
        }
    }

}