package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.exceptions.*;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.jenkins.configrotator.*;

public class ClearCaseUCMConfiguration extends AbstractConfiguration<ClearCaseUCMConfigurationComponent> {

    private SnapshotView view;

    public ClearCaseUCMConfiguration() {
    }


    public ClearCaseUCMConfiguration clone() {
        ClearCaseUCMConfiguration n = new ClearCaseUCMConfiguration();
        n.view = this.view;

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

    /**
     * Parsing and loading the user-input config rotator configuration - targets in the GUI.
     * Returned configuration may not be valid for building, but the clear case components can load
     * Throws ConfigurationRotatorException if targets is not parsed correctly or can not be loaded.
     *
     * @param targets
     * @param workspace
     * @param listener
     * @return
     * @throws ConfigurationRotatorException if target can not be parsed, or if they can not be loaded with ClearCase
     * @throws IOException
     */
    public static ClearCaseUCMConfiguration getConfigurationFromTargets( List<ClearCaseUCMTarget> targets, FilePath workspace, TaskListener listener ) throws ConfigurationRotatorException {
        PrintStream out = listener.getLogger();

        /**/
        ClearCaseUCMConfiguration configuration = new ClearCaseUCMConfiguration();

        /* Each line is component, stream, baseline, plevel, type */
        for( ClearCaseUCMTarget target : targets ) {
            final String[] units = target.getComponent().split( "," );

            if( units.length == 3 ) {
                try {
                    ClearCaseUCMConfigurationComponent config = workspace.act( new GetConfiguration( units, listener ) );
                    configuration.list.add( config );
                    out.println( ConfigurationRotator.LOGGERNAME + "Parsed configuration: " + config );
                } catch( InterruptedException e ) {
                    out.println( ConfigurationRotator.LOGGERNAME + "Error parsing configuration - interrupted: " + e.getMessage() );
                    throw new ConfigurationRotatorException( "Unable parse configuration - interrupted", e );
                } catch( IOException ioe ) {
                    // The GetConfiguration above on the slave might throw three
                    // exception: IOException and InterruptedException.
                    // The third is ClearCase exception, implicit, as it is packed
                    // into the IOException as it was not serializeable.
                    try {
                        // Regardless of the exception, try get the cause
                        // to see if there is a ClearCase exception in it somewhere
                        Exception cce = (Exception) ioe.getCause();
                        throw cce;
                    } catch( ClearCaseException cce ) {
                        // yah, ...
                        out.println( ConfigurationRotator.LOGGERNAME + "Unable to load with ClearCase: " + cce.getMessage() );
                        throw new ConfigurationRotatorException( "Unable to load with ClearCase", cce );
                    } catch( Exception e2 ) {
                        // nah, just some other exception, but we should still fail
                        out.println( ConfigurationRotator.LOGGERNAME + "Error parsing configuration - io: " + e2.getMessage() );
                        throw new ConfigurationRotatorException( "Unable parse configuration - io", e2 );
                    }
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
            ClearCaseUCMConfiguration o = (ClearCaseUCMConfiguration) other;

            /* Check size */
            if( o.getList().size() != list.size() ) {
                return false;
            }

            /* Check elements, the size is identical */
            for( int i = 0; i < list.size(); ++i ) {
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

    @Override
    public String toHtml() {
        StringBuilder builder = new StringBuilder();

        builder.append( "<table border=\"0\" style=\"text-align:left;\">" );
        builder.append( "<thead>" );
        builder.append( "<th>" ).append( "Component" ).append( "</th>" );
        builder.append( "<th>" ).append( "Stream" ).append( "</th>" );
        builder.append( "<th>" ).append( "Baseline" ).append( "</th>" );
        builder.append( "<th>" ).append( "Promotion level" ).append( "</th>" );
        builder.append( "<th>" ).append( "Fixed" ).append( "</th>" );

        for( ClearCaseUCMConfigurationComponent comp : getList() ) {
            builder.append( comp.toHtml() );
        }

        builder.append( "</thead>" );
        builder.append( "</table>" );
        return builder.toString();
    }

    @Override
    public String getDescription( AbstractBuild<?, ?> build ) {
        /**
         * Ensure backwards compatability
         */
        if(description == null) {

            ConfigurationRotator rotator = (ConfigurationRotator)build.getProject().getScm();
            if(getChangedComponent() == null) {
                return "New Configuration - no changes yet";
            } else {
                int currentComponentIndex = getChangedComponentIndex();
                String currentBaseline = ((ClearCaseUCMConfigurationComponent)getChangedComponent()).getBaseline().getNormalizedName();
                ConfigurationRotatorBuildAction previous = rotator.getAcrs().getLastResult(build.getProject(), ClearCaseUCM.class);
                String previousBaseline = ((ClearCaseUCMConfiguration)previous.getConfiguration()).getList().get(currentComponentIndex).getBaseline().getNormalizedName();

                return String.format("Baseline changed from %s to %s", previousBaseline, currentBaseline);
            }
        }
        return description;
    }

    /**
     * Returns a list of files affected by the recent change.
     *
     * @param configuration
     * @return
     * @throws ConfigurationRotatorException
     */

    @Override
    public List<ClearCaseActivity> difference( AbstractConfiguration<ClearCaseUCMConfigurationComponent> configuration ) throws ConfigurationRotatorException {
        List<ClearCaseActivity> changes = new ArrayList<ClearCaseActivity>();
        List<ClearCaseUCMConfigurationComponent> components = getList();
        //Find changed component
        for( ClearCaseUCMConfigurationComponent comp : components ) {
            if( comp.isChangedLast() ) {
                try {
                    int index = components.indexOf( comp );
                    List<Activity> activities = Version.getBaselineDiff( configuration.getList().get( index ).getBaseline(), comp.getBaseline(), true, new File( getView().getPath() ) );
                    for( Activity a : activities ) {
                        ClearCaseActivity ccac = new ClearCaseActivity();
                        ccac.setAuthor( a.getUser() );
                        ccac.setActivityName( a.getShortname() );
                        for( Version v : a.changeset.versions ) {
                            ClearCaseVersion ccv = new ClearCaseVersion();
                            ccv.setFile( v.getSFile() );
                            ccv.setName( v.getVersion() );
                            ccv.setUser( v.blame() );
                            ccac.addVersion( ccv );
                        }
                        changes.add( ccac );
                    }
                } catch( UnableToCreateEntityException ex ) {
                    throw new ConfigurationRotatorException( "UnableToCreateEntityException.", ex );
                } catch( UnableToGetEntityException ex ) {
                    throw new ConfigurationRotatorException( "UnableToGetEntityException.", ex );
                } catch( NullPointerException nex ) {
                    throw new ConfigurationRotatorException( "Null pointer found.", nex );
                } catch( CleartoolException ex ) {
                    throw new ConfigurationRotatorException( "Cleartool error:", ex );
                } catch( UnableToLoadEntityException ex ) {
                    throw new ConfigurationRotatorException( "Cleartool error: unable to load entity", ex );
                } catch( UCMEntityNotFoundException ex ) {
                    throw new ConfigurationRotatorException( "Entity not found error:", ex );
                } catch( UnableToInitializeEntityException ex ) {
                    throw new ConfigurationRotatorException( "Unable to initalize entity error:", ex );
                }
            }
        }
        return changes;
    }
}