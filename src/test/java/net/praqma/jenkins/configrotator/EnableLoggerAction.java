package net.praqma.jenkins.configrotator;

import hudson.model.Action;

import java.io.File;

/**
 * @author cwolfgang
 */
public class EnableLoggerAction implements Action {

    private File outputDir;

    public EnableLoggerAction( File outputDir ) {
        this.outputDir = outputDir;
    }

    public File getOutputDir() {
        return outputDir;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }
}
