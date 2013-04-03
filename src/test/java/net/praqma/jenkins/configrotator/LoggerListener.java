package net.praqma.jenkins.configrotator;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import net.praqma.logging.LoggingUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author cwolfgang
 */
@Extension
public class LoggerListener extends RunListener<AbstractBuild> {

    public LoggerListener() {
        super( AbstractBuild.class );
    }

    @Override
    public void onStarted( AbstractBuild run, TaskListener listener ) {
        EnableLoggerAction action = run.getAction( EnableLoggerAction.class );

        if( action != null ) {
            File output = new File( action.getOutputDir(), run.getProject().getDisplayName().replace( "[\\\\~#%&*{}/:<>?|\\\"-]", "_" ) + "." + run.getNumber() + ".log" );
            List<String> loggers = new ArrayList<String>(2);
            loggers.add( "net.praqma" );
            try {
                LoggingUtil.setPraqmaticHandler( Level.ALL, loggers, output );
            } catch( FileNotFoundException e ) {
                e.printStackTrace();
            }
        }
    }
}
