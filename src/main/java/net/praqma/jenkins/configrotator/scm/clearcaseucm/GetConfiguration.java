package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import net.praqma.clearcase.exceptions.ClearCaseException;
import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

public class GetConfiguration implements FileCallable<ClearCaseUCMComponentConfiguration> {
	
	private String[] units;
	private TaskListener listener;
	
	public GetConfiguration( String[] units, TaskListener listener ) {
		this.units = units;
		this.listener = listener;
	}
	
	public ClearCaseUCMComponentConfiguration invoke( File f, VirtualChannel channel ) throws IOException, InterruptedException {
		PrintStream out = listener.getLogger();
		
		try {
			return new ClearCaseUCMComponentConfiguration( units[0].trim(), units[1].trim(), units[2].trim(), units[3].trim(), units[4].trim() );
		} catch( ClearCaseException e ) {
			e.print( out );
			return null;
		}
	}
}
