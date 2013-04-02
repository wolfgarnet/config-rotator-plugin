package net.praqma.jenkins.configrotator.functional.scm.clearcaseucm;

import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import net.praqma.clearcase.Rebase;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.jenkins.configrotator.ConfigurationRotator;
import net.praqma.jenkins.utils.ViewUtils;

public class PrepareWorkspace implements FileCallable<SnapshotView> {

	private Project project;
	private TaskListener listener;
	private String viewtag;
	private List<Baseline> baselines;
	

	public PrepareWorkspace( Project project, List<Baseline> baselines, String viewtag, TaskListener listener ) {
		this.project = project;
		this.viewtag = viewtag;
		this.listener = listener;
		this.baselines = baselines;
		
	}

	@Override
	public SnapshotView invoke( File workspace, VirtualChannel channel ) throws IOException, InterruptedException {
		PrintStream out = listener.getLogger();
		SnapshotView view = null;
		File viewroot = new File( workspace, "view" );
		
					
		/* Changle stream, if exists */
		String streamName = viewtag + "@" + project.getPVob();
		Stream devStream;
		try {
			devStream = Stream.get( streamName );
		} catch( UnableToInitializeEntityException e ) {
			throw new IOException( "No entity", e );

		}
		
		/* If the stream exists, change it */
		if( devStream.exists() ) {
			out.println( ConfigurationRotator.LOGGERNAME + "Stream exists" );
			
			/* First we need the view */
			try {
				view = ViewUtils.createView( out, devStream, "ALL", new File( workspace, "view" ), viewtag, true );
			} catch( ClearCaseException e ) {
				throw new IOException( "Unable to create view", e );
			}
			
			try {
				out.println( ConfigurationRotator.LOGGERNAME + "Rebasing stream to " + devStream.getNormalizedName() );
				Rebase rebase = new Rebase( devStream, view, baselines );
				rebase.rebase( true );
			} catch( ClearCaseException e ) {
				throw new IOException( "Could not load " + devStream, e );
			}
		} else {
			/* Create new */
			
			out.println( ConfigurationRotator.LOGGERNAME + "Creating a new environment" );
			
			try {
				out.println( ConfigurationRotator.LOGGERNAME + "Creating new stream" );
				devStream = Stream.create( project.getIntegrationStream(), streamName, true, baselines );
			} catch( ClearCaseException e1 ) {
				throw new IOException( "Unable to create stream " + streamName, e1 );
			}
			
			try {
				view = ViewUtils.createView( out, devStream, "ALL", new File( workspace, "view" ), viewtag, true );
			} catch( ClearCaseException e ) {
				throw new IOException( "Unable to create view", e );
			}
		}
		
		return view;
	}

}
