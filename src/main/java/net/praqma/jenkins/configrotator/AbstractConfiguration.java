package net.praqma.jenkins.configrotator;

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

    @Override
	public String toString() {
		return getClass().getSimpleName() + "[" + list + "]";
	}
    
    public List<T> getList() {
        return list;
    }

    public abstract String toHtml();

    public String getDescription() {
        return "Blaha";
    }
}
