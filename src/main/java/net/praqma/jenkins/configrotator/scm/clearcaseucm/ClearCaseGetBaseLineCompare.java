/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import java.util.List;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.appenders.StreamAppender;

/**
 *
 * @author Praqma
 */
public class ClearCaseGetBaseLineCompare implements FilePath.FileCallable<List<String>> {
    private ClearCaseUCMConfiguration current;
    private ClearCaseUCMConfiguration compareto;
    private BuildListener listener;

    public ClearCaseGetBaseLineCompare(BuildListener listener, ClearCaseUCMConfiguration current, ClearCaseUCMConfiguration compareto) {
        this.current = current;
        this.compareto = compareto;
        this.listener = listener;
    }

    @Override
    public List<String> invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
        List<String> changes = null;
        StreamAppender app = new StreamAppender(listener.getLogger());
        app.setMinimumLevel(Logger.LogLevel.DEBUG);
        Logger.addAppender(app);
        try {
            changes = current.difference(compareto);          
        } catch (ConfigurationRotatorException ex) {
            listener.getLogger().println("Caught exception executeing remote ClearCaseBaseLineCompare: "+ex);
        }
        return changes;
    }

}
