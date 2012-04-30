package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.TaskListener;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.taskdefs.Get;

import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.jenkins.configrotator.AbstractConfiguration;
import net.praqma.jenkins.configrotator.AbstractConfigurationComponent;
import net.praqma.jenkins.configrotator.ConfigurationRotator;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;

public class ClearCaseUCMConfiguration extends AbstractConfiguration {
	
	private List<ClearCaseUCMConfigurationComponent> list;
	private SnapshotView view;
	
	public ClearCaseUCMConfiguration() {
		list = new ArrayList<ClearCaseUCMConfigurationComponent>();
	}
	
	public ClearCaseUCMConfiguration clone() {
		ClearCaseUCMConfiguration n = new ClearCaseUCMConfiguration();
		n.view = this.view;
		
		n.list = new ArrayList<ClearCaseUCMConfigurationComponent>();
		//n.list.addAll( this.list );
		for( ClearCaseUCMConfigurationComponent cc : this.list ) {
			n.list.add( cc.clone() );
		}
		
		return n;		
	}
	
	public ClearCaseUCMConfiguration( List<ClearCaseUCMConfigurationComponent> list ) {
		this.list = list;
	}

	
	public void setView( SnapshotView view ) {
		this.view = view;
	}
	
	public SnapshotView getView() {
		return view;
	}
	
	public List<ClearCaseUCMConfigurationComponent> getList() {
		return list;
	}
	
	public static ClearCaseUCMConfiguration getConfigurationFromTargets( List<ClearCaseUCMTarget> targets, FilePath workspace, TaskListener listener ) throws ConfigurationRotatorException, IOException {
		PrintStream out = listener.getLogger();
		
		out.println( "Input: " + targets );
		
		/**/
		ClearCaseUCMConfiguration configuration = new ClearCaseUCMConfiguration();
		
		/* Each line is component, stream, baseline, plevel, type */
		for( ClearCaseUCMTarget target : targets ) {
			final String[] units = target.getComponent().split( "," );
			
			if( units.length == 3 ) {
				try {
					ClearCaseUCMConfigurationComponent config = workspace.act( new GetConfiguration( units, listener ) );
					out.println( ConfigurationRotator.LOGGERNAME + "Config: " + config );
					configuration.list.add( config );
				} catch( InterruptedException e ) {
					out.println( ConfigurationRotator.LOGGERNAME + "Error: " + e.getMessage() );
					
					throw new ConfigurationRotatorException( "Unable parse input", e );
				}
			} else {
				/* Do nothing */
				out.println( ConfigurationRotator.LOGGERNAME + "\"" + target.getComponent() + "\" was not correct" );
				throw new ConfigurationRotatorException( "Wrong input, length is " + units.length );
			}
		}
		
		return configuration;
	}
	
	public String toString() {
		return list.toString();
	}
	

	@Override
	public void getConfiguration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean equals( Object other ) {
		if( other == this ) {
			return true;
		}
		
		if( other instanceof ClearCaseUCMConfiguration ) {
			ClearCaseUCMConfiguration o = (ClearCaseUCMConfiguration)other;
			
			/* Check size */
			if( o.getList().size() != list.size() ) {
				return false;
			}
			
			/* Check elements, the size is identical */
			for( int i = 0 ; i < list.size() ; ++i ) {
				if( !o.list.get( i ).equals( list.get( i ) ) ) {
					return false;
				}
			}
			
			/* Everything is ok */
			return true;
		} else {
			/* Not same type */
			return false;
		}
		
		
	}

}
