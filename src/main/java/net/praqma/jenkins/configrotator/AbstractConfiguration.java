package net.praqma.jenkins.configrotator;

public abstract class AbstractConfiguration {

	public abstract void getConfiguration();

	public String getView( Class<?> clazz ) {
		return clazz.getName().replace( '.', '/' ).replace( '$', '/' ) + "/" + "cr.jelly";
	}

	public String toString() {
		return "This is just the configuration base class";
	}
}
