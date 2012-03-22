package net.praqma.jenkins.configrotator;

import net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType;
import hudson.model.AbstractBuild;
import hudson.model.Action;

public class ConfigurationRotatorBuildAction implements Action {
	
	private AbstractBuild<?, ?> build;
	private Class<? extends AbstractConfigurationRotatorSCM> clazz;
	private ResultType result = ResultType.UNDETERMINED;
	private AbstractConfiguration configuration;
	
	public ConfigurationRotatorBuildAction( AbstractBuild<?, ?> build, Class<? extends AbstractConfigurationRotatorSCM> clazz, AbstractConfiguration configuration ) {
		this.build = build;
		this.clazz = clazz;
		this.configuration = configuration;
	}
	
	public Class<?> getClazz() {
		return clazz;
	}
	
	public void setResult( ResultType result ) {
		this.result = result;
	}
	
	public boolean isDetermined() {
		return result.equals( ResultType.COMPATIBLE ) || result.equals( ResultType.INCOMPATIBLE );
	}
	
	public ResultType getResult() {
		return result;
	}

	@Override
	public String getIconFileName() {
		return "graph.gif";
	}

	@Override
	public String getDisplayName() {
		return "Config Rotator";
	}

	@Override
	public String getUrlName() {
		return "config-rotator";
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
	}
	
	public AbstractConfiguration getConfiguration() {
		return configuration;
	}
	
	public String toString() {
		return configuration.toString();
	}
}
