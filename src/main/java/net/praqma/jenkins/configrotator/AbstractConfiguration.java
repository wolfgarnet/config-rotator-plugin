package net.praqma.jenkins.configrotator;

import hudson.model.AbstractBuild;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractConfiguration<T extends AbstractConfigurationComponent, TARGET extends AbstractTarget> implements Serializable {

    private static Logger logger = Logger.getLogger( AbstractConfiguration.class.getName() );

    public abstract List<ConfigRotatorChangeLogEntry> difference( T component, T other ) throws ConfigurationRotatorException;

    protected List<T> list = new ArrayList<T>();

    protected String description = null;


    public String getView( Class<?> clazz ) {
        return clazz.getName().replace( '.', '/' ).replace( '$', '/' ) + "/" + "cr.jelly";
    }

    public String getViewPage( Class<?> clazz, String pageName ) {
        String origin = clazz.getName();

        while( clazz != Object.class && clazz != null ) {
            String name = clazz.getName().replace( '.', '/' ).replace( '$', '/' ) + "/" + pageName;

            if( clazz.getClassLoader().getResource( name ) != null ) {
                return '/' + name;
            }
            clazz = clazz.getSuperclass();
        }

        throw new IllegalStateException( origin + " does not have " + pageName );
    }

    public AbstractConfigurationComponent getChangedComponent() {
        for( AbstractConfigurationComponent configuration : this.getList() ) {
            if( configuration.isChangedLast() ) {
                return (AbstractConfigurationComponent) configuration;
            }
        }
        return null;
    }

    /**
     * Gets the index of the changed component.
     *
     * @return the index of the changed component. If there is no changed component default return value is -1
     */
    public int getChangedComponentIndex() {
        int index = -1;

        for( AbstractConfigurationComponent configuration : this.getList() ) {
            if( configuration.isChangedLast() ) {
                index = getList().indexOf( configuration );
            }
        }

        return index;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + list + "]";
    }

    public List<T> getList() {
        return list;
    }

    public abstract <T extends AbstractConfiguration> T clone();

    public abstract String toHtml();

    public String getDescription( AbstractBuild<?, ?> build ) {
        if( description == null ) {
            ConfigurationRotator rotator = (ConfigurationRotator) build.getProject().getScm();
            if( getChangedComponent() == null ) {
                return "New Configuration - no changes yet";
            } else {
                ConfigurationRotatorBuildAction previous = rotator.getAcrs().getPreviousResult( build, null );

                //return String.format( "Commit changed:<br/>%s<br/>%s", previous.getConfigurationWithOutCast().getList().get( getChangedComponentIndex() ).prettyPrint(), getChangedComponent().prettyPrint() );
                return String.format( "%s<br/>%s", ((T)previous.getConfigurationWithOutCast().getList().get( getChangedComponentIndex() ) ).prettyPrint(), getChangedComponent().prettyPrint() );
            }
        }

        return description;
    }


    public String basicHtml( StringBuilder builder, String ... titles ) {

        builder.append( "<table style=\"text-align:left;border-solid:hidden;border-collapse:collapse;\">" );
        builder.append( "<thead>" );
        for( String title : titles ) {
            builder.append( "<th style=\"padding-right:15px\">" ).append( title ).append( "</th>" );
        }
        builder.append( "</thead>" );

        builder.append( "<tbody>" );
        for( T comp : getList() ) {
            builder.append( comp.toHtml() );
        }
        builder.append( "</tbody>" );

        builder.append( "</table>" );
        return builder.toString();
    }
}
