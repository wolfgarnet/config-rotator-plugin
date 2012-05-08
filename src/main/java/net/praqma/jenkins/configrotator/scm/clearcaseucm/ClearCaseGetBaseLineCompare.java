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
import java.util.ArrayList;
import java.util.List;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;

/**
 *
 * @author Praqma
 */
public class ClearCaseGetBaseLineCompare implements FilePath.FileCallable<List<ClearCaseActivity>> {
    private ClearCaseUCMConfiguration current;
    private ClearCaseUCMConfiguration compareto;
    private BuildListener listener;

    public ClearCaseGetBaseLineCompare(BuildListener listener, ClearCaseUCMConfiguration current, ClearCaseUCMConfiguration compareto) {
        this.current = current;
        this.compareto = compareto;
        this.listener = listener;
    }

    @Override
    public List<ClearCaseActivity> invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
        List<ClearCaseActivity> changes = new ArrayList<ClearCaseActivity>();
        try {
            changes = current.difference(compareto);          
        } catch (ConfigurationRotatorException ex) {
            listener.getLogger().println("Caught exception executeing remote ClearCaseBaseLineCompare: "+ex);
        }
        return changes;
    }

}
