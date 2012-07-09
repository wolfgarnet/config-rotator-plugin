package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import net.praqma.clearcase.exceptions.ClearCaseException;
import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

public class GetConfiguration implements FileCallable<ClearCaseUCMConfigurationComponent> {
	
	private String[] units;
	private TaskListener listener;
	
	public GetConfiguration( String[] units, TaskListener listener ) {
		this.units = units;
		this.listener = listener;
	}
	
        @Override
	public ClearCaseUCMConfigurationComponent invoke( File f, VirtualChannel channel ) throws IOException, InterruptedException {
		//PrintStream out = listener.getLogger();
		
		try {
			return new ClearCaseUCMConfigurationComponent( units[0].trim(), units[1].trim(), units[2].trim() );
		} catch( ClearCaseException e ) {
                    // ClearCaseException can not be passed through from slave to master
                    // but IOException can, so using that one, and packing out later
                        IOException ioe = new IOException(e);
                        throw ioe;
		}
	}
}
