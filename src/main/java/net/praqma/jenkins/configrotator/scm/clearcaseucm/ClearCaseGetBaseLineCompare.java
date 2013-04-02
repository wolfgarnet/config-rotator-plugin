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
import java.util.logging.Level;
import java.util.logging.Logger;

import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogEntry;

/**
 *
 * @author Praqma
 */
public class ClearCaseGetBaseLineCompare implements FilePath.FileCallable<List<ConfigRotatorChangeLogEntry>> {
    private ClearCaseUCMConfigurationComponent component;
    private ClearCaseUCMConfiguration configuration;
    private BuildListener listener;

    public ClearCaseGetBaseLineCompare(BuildListener listener, ClearCaseUCMConfiguration configuration, ClearCaseUCMConfigurationComponent component) {
        this.configuration = configuration;
        this.component = component;
        this.listener = listener;
    }

    @Override
    public List<ConfigRotatorChangeLogEntry> invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
        Logger logger = Logger.getLogger( ClearCaseGetBaseLineCompare.class.getName() );
        try {
            return configuration.difference( component, null );
        } catch (ConfigurationRotatorException ex) {
            logger.log( Level.WARNING, "Unable to get differences for " + component, ex );
            throw new IOException( "Unable to get differences for " + component, ex );
        }
    }

}
