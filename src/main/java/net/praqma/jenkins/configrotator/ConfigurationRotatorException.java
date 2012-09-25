package net.praqma.jenkins.configrotator;

public class ConfigurationRotatorException extends Exception {
	public ConfigurationRotatorException( String msg ) {
		super( msg );
	}

    public ConfigurationRotatorException( Exception e ) {
        super( e );
    }
	
	public ConfigurationRotatorException( String msg, Exception e ) {
		super( msg, e );
	}
}
