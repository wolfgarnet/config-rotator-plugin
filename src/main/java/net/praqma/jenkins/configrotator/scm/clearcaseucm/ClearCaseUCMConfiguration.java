package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.FilePath;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.praqma.clearcase.exceptions.*;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.jenkins.configrotator.AbstractConfiguration;
import net.praqma.jenkins.configrotator.AbstractConfigurationComponent;
import net.praqma.jenkins.configrotator.ConfigurationRotator;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;

public class ClearCaseUCMConfiguration extends AbstractConfiguration<ClearCaseUCMConfigurationComponent> {

	private SnapshotView view;
	
	public ClearCaseUCMConfiguration() {
		list = new ArrayList<ClearCaseUCMConfigurationComponent>();
	}
    
    /**
     * Gets the changed component
     * 
     */ 
    public ClearCaseUCMConfigurationComponent getChangedComponent() {
        for(AbstractConfigurationComponent configuration : this.getList()) {
            if(configuration.isChangedLast()) {
                return (ClearCaseUCMConfigurationComponent)configuration;
            }
        }
        return null;
    }
    
    /**
     * Gets the index of the changed component.
     * @return the index of the changed component. If there is no changed component default return value is -1
     */
    public int getChangedComponentIndex() {
        int index = -1;
        
        for(AbstractConfigurationComponent configuration : this.getList()) {
            if(configuration.isChangedLast()) {
                index = getList().indexOf(configuration);
            }
        }
        
        return index;
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
        
        builder.append("<table border=\"0\" style=\"text-align:left;\">");
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
    
    /**
     * Returns a list of files affected by the recent change.
     * @param configuration
     * @return
     * @throws ConfigurationRotatorException 
     */
    
    @Override
    public List<ClearCaseActivity> difference(AbstractConfiguration<ClearCaseUCMConfigurationComponent> configuration) throws ConfigurationRotatorException {
        List<ClearCaseActivity> changes = new ArrayList<ClearCaseActivity>();
        List<ClearCaseUCMConfigurationComponent> components = getList();
        //Find changed component
        for(ClearCaseUCMConfigurationComponent comp : components) {
            if(comp.isChangedLast()) {
                try {
                    int index = components.indexOf(comp);                    
                    List<Activity> activities = Version.getBaselineDiff(configuration.getList().get(index).getBaseline(),comp.getBaseline(), true, new File(getView().getPath()));                    
                    for(Activity a : activities) {
                        ClearCaseActivity ccac = new ClearCaseActivity();
                        ccac.setAuthor(a.getUser());
                        ccac.setActivityName(a.getShortname());
                        for(Version v: a.changeset.versions) {
                            ClearCaseVersion ccv = new ClearCaseVersion();
                            ccv.setFile(v.getSFile());
                            ccv.setName(v.getVersion());
                            ccv.setUser(v.blame());
                            ccac.addVersion(ccv);
                        }
                        changes.add(ccac);
                    }
                } catch (UnableToCreateEntityException ex) {
                    throw new ConfigurationRotatorException("UnableToCreateEntityException.", ex);
                } catch (UnableToGetEntityException ex) {
                    throw new ConfigurationRotatorException("UnableToGetEntityException.", ex);
                } catch (NullPointerException nex) {
                    throw new ConfigurationRotatorException("Null pointer found.", nex);
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