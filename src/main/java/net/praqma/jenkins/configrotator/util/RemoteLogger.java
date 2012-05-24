package net.praqma.jenkins.configrotator.util;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

import net.praqma.util.debug.Logger;
import net.praqma.util.debug.LoggerSetting;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.StreamAppender;

import hudson.FilePath;
import hudson.remoting.Pipe;

public class RemoteLogger implements Serializable {

	private Pipe pipeToMaster;
	private PrintStream streamToMaster;
	private LoggerSetting loggerSetting;
	
	private transient FilePath workspace;
	private transient PipedInputStream inputStream;
	
	private transient Appender appender;
	
	public RemoteLogger( FilePath workspace, LoggerSetting loggerSetting ) {
		this.workspace = workspace;
		this.loggerSetting = loggerSetting;
	}
	
	/**
	 * This method should be called on the master, before called RPC
	 * @throws IOException 
	 */
	public void prepare() throws IOException {
		if( workspace.isRemote() ) {
			pipeToMaster = Pipe.createRemoteToLocal();
		} else {
			PipedInputStream in = new PipedInputStream();
			PipedOutputStream out = new PipedOutputStream( in );
			streamToMaster = new PrintStream( out );
		}
	}
	
	/**
	 * This should be called once in the slave code to get the Appender.
	 * @return
	 */
	public Appender establish() {
    	if( pipeToMaster != null ) {
	    	PrintStream toMaster = new PrintStream( pipeToMaster.getOut() );	    	
	    	appender = new StreamAppender( toMaster );
	    	appender.lockToCurrentThread();
	    	Logger.addAppender( appender );
	    	appender.setSettings( loggerSetting );
    	} else if( streamToMaster != null ) {
	    	appender = new StreamAppender( streamToMaster );
	    	appender.lockToCurrentThread();
	    	Logger.addAppender( appender );
	    	appender.setSettings( loggerSetting );
    	}
    	
    	return appender;
	}
	
	public void end() {
		Logger.removeAppender( appender );
	}
	
	/**
	 * This method is called on the master after the remote method is called asynchronously. All logs on the remote is piped to the given appender.
	 * @param appender
	 */
	public void writeLog( Appender appender ) {
		if( workspace.isRemote() ) {
			appender.write( pipeToMaster.getIn() );
		} else {
			appender.write( inputStream );
		}
	}
}
