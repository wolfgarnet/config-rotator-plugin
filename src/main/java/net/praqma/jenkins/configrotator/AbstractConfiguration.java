package net.praqma.jenkins.configrotator;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractConfiguration<T extends AbstractConfigurationComponent> implements Serializable {    
	public abstract void getConfiguration();
    public abstract List<String> difference(AbstractConfiguration<T> configuration) throws ConfigurationRotatorException;
    protected List<T> list;

	public String getView( Class<?> clazz ) {
		return clazz.getName().replace( '.', '/' ).replace( '$', '/' ) + "/" + "cr.jelly";
	}

    @Override
	public String toString() {
		return "This is just the configuration base class";
	}
    
    public List<T> getList() {
        return list;
    }
}
