package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;
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
		
		/* Remove old view, if exists */
		if( viewroot.exists() ) {
			try {
				out.println( ConfigurationRotator.LOGGERNAME + "Determining old view" );
				SnapshotView oldview = null;
				try {
					oldview = SnapshotView.getSnapshotViewFromPath( viewroot );
				} catch( Exception e ) {
					out.println( "View root is not a ClearCase view?!" );
					/* This should indicate, that the view does not exist */
				}
				
				if( oldview != null ) {
					out.println( ConfigurationRotator.LOGGERNAME + "Removing old view" );
					oldview.end();
					oldview.remove();
					new FilePath( viewroot ).deleteRecursive();
				}
				
			} catch( ClearCaseException e ) {
				throw new IOException( "Unable to remove the old view", e );
			}
		}
			
		/* Remove old stream, if exists */
		String streamName = viewtag + "@" + project.getPVob();
		Stream devStream = null;
		try {
			devStream = Stream.get( streamName, true );
		} catch( ClearCaseException e ) {
			out.println( "No stream named " + streamName );
			e.print( out );
		}
		
		if( devStream != null && devStream.exists() ) {
			try {
				out.println( ConfigurationRotator.LOGGERNAME + "Removing old stream" );
				devStream.remove();
			} catch( ClearCaseException e ) {
				throw new IOException( "Could not load " + devStream, e );
			}
		}
		
		try {
			out.println( ConfigurationRotator.LOGGERNAME + "Creating new stream" );
			devStream = Stream.create( project.getIntegrationStream(), streamName, true, baselines );
		} catch( ClearCaseException e1 ) {
			throw new IOException( "Unable to create stream " + streamName, e1 );
		}
		


		try {
			view = ViewUtils.createView( out, devStream, "ALL", new File( workspace, "view" ), viewtag, true );
		} catch( ClearCaseException e ) {
			e.print( out );
		}
		
		return view;
	}

}
