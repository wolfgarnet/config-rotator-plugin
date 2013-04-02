package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.view.SnapshotView;

public class EndView implements FileCallable<Boolean> {

	private SnapshotView view;
	private TaskListener listener;

	public EndView( SnapshotView view, TaskListener listener ) {
		this.view = view;
		this.listener = listener;
	}

	@Override
	public Boolean invoke( File workspace, VirtualChannel channel ) throws IOException, InterruptedException {
		PrintStream out = listener.getLogger();

		try {
			view.end();
			view.remove();
		} catch( ViewException e ) {
			throw new IOException( "Unable to end view", e );
		}
		
		
		return true;
	}

}
