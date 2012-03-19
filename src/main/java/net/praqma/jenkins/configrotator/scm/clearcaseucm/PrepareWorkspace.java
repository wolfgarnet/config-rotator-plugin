package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.jenkins.utils.ViewUtils;

public class PrepareWorkspace implements FileCallable<SnapshotView> {

	private Project project;
	private TaskListener listener;
	private String viewtag;

	public PrepareWorkspace( Project project, String viewtag, TaskListener listener ) {
		this.project = project;
		this.viewtag = viewtag;
		this.listener = listener;
	}

	@Override
	public SnapshotView invoke( File workspace, VirtualChannel channel ) throws IOException, InterruptedException {
		PrintStream out = listener.getLogger();
		SnapshotView view = null;
			
		String streamName = viewtag + "@" + project.getPVob();
		try {
			Stream devStream = Stream.create( project.getIntegrationStream(), streamName, true, (Baseline)null );
		} catch( ClearCaseException e1 ) {
			e1.print( out );
			throw new IOException( "Unable to create stream " + streamName, e1 );
		}

		try {
			view = ViewUtils.createView( out, project.getIntegrationStream(), "ALL", new File( workspace, "view" ), "", true );
		} catch( ClearCaseException e ) {
			e.print( out );
		}
		
		return view;
	}

}
