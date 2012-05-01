package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import java.io.File;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Version;

import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.jenkins.configrotator.AbstractConfiguration;
import net.praqma.jenkins.configrotator.ConfigurationRotator;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.PraqmaLogger;

public class ClearCaseUCMConfiguration extends AbstractConfiguration<ClearCaseUCMConfigurationComponent> {

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
    
	@Override
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
    
    public String toHtml() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("<table border=\"1\">");
        builder.append("<thead>");
        builder.append("<th>").append("Component").append("</th>");
        builder.append("<th>").append("Stream").append("</th>");
        builder.append("<th>").append("Baseline").append("</th>");
        builder.append("<th>").append("Promotion level").append("</th>");
        builder.append("<th>").append("Fixed").append("</th>");
        
        for(ClearCaseUCMConfigurationComponent comp : getList()) {
            builder.append(comp.toHtml());
        }
        
        builder.append("</thead>");
        builder.append("</table>");
        return builder.toString();
    }
    
    @Override
    public List<String> difference(AbstractConfiguration<ClearCaseUCMConfigurationComponent> configuration) throws ConfigurationRotatorException {
        List<String> changes = new ArrayList<String>();
        List<ClearCaseUCMConfigurationComponent> components = getList();
        //Find changed component
        for(ClearCaseUCMConfigurationComponent comp : components) {
            if(comp.isChangedLast()) {
                try {
                    int index = components.indexOf(comp);
                    
                    List<Activity> activities = Version.getBaselineDiff(configuration.getList().get(index).getBaseline(),comp.getBaseline(), true, new File(getView().getPath()));
                    
                    Logger.getLogger().debug("Printing list of activities:" +activities);
                    for(Activity a : activities) {
                        changes.add(a.toString());
                    }

                } catch (CleartoolException ex) {
                    throw new ConfigurationRotatorException("Cleartool error:", ex);
                } catch (UnableToLoadEntityException ex) {
                    throw new ConfigurationRotatorException("Cleartool error: unable to load entity", ex);
                } catch (UCMEntityNotFoundException ex) {
                    throw new ConfigurationRotatorException("Entity not found error:", ex);
                } catch (UnableToInitializeEntityException ex) {
                    throw new ConfigurationRotatorException("Unable to initalize entity error:", ex);
                }
            }
        }       
        return changes;
    }
}
