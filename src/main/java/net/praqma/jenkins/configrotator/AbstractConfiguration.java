package net.praqma.jenkins.configrotator;

import hudson.model.AbstractBuild;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractConfiguration<T extends AbstractConfigurationComponent> implements Serializable {   
    public abstract List<? extends Serializable> difference(AbstractConfiguration<T> configuration) throws ConfigurationRotatorException;
    protected List<T> list = new ArrayList<T>();

    protected String description = null;

	public String getView( Class<?> clazz ) {
		return clazz.getName().replace( '.', '/' ).replace( '$', '/' ) + "/" + "cr.jelly";
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

    public abstract String toHtml();

    public String getDescription( AbstractBuild<?, ?> build ) {
        return "Result from " + build;
    }
}
