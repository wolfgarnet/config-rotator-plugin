package net.praqma.jenkins.configrotator;

import java.util.List;

public abstract class AbstractConfigurationActionElement {
	private List<AbstractComponentConfiguration> configuration;
	
	public AbstractConfigurationActionElement( List<AbstractComponentConfiguration> configuration ) {
		this.configuration = configuration;
	}
	
	public List<AbstractComponentConfiguration> getList() {
		return configuration;
	}
}
