package net.praqma.jenkins.configrotator;

public abstract class AbstractConfiguration {
	
	public abstract void getConfiguration();
	
	public String toString() {
		return "This is just the configuration base class";
	}
}
