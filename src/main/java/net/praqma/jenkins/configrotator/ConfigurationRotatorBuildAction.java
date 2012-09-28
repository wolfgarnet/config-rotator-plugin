package net.praqma.jenkins.configrotator;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.scm.SCM;
import java.io.IOException;
import javax.servlet.ServletException;
import net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class ConfigurationRotatorBuildAction implements Action {
	
    private String description;
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

	
	
	public void doReset( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
		SCM scm = build.getProject().getScm();
		if( scm instanceof ConfigurationRotator ) {
			//((ConfigurationRotator)scm).setFresh( build.getProject(), true );
			((ConfigurationRotator)scm).setConfigurationByAction( build.getProject(), this );
			//rsp.forwardToPreviousPage( req. );
			rsp.sendRedirect( "../../" );
		} else {
			rsp.sendError( StaplerResponse.SC_BAD_REQUEST, "Not a Configuration Rotator job" );
		}
	}
	
	public void setResult( ResultType result ) {
		this.result = result;
	}
	
	public boolean isDetermined() {
		return result.equals( ResultType.COMPATIBLE ) || result.equals( ResultType.INCOMPATIBLE );
	}
	
	public boolean isCompatible() {
		return result.equals( ResultType.COMPATIBLE );
	}
	
	public ResultType getResult() {
		return result;
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public String getUrlName() {
		return "config-rotator";
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
	}
	
	public <T extends AbstractConfiguration<? extends AbstractConfigurationComponent>> T getConfiguration() {
		return (T)configuration;
	}

    public AbstractConfiguration getConfigurationWithOutCast() {
        return configuration;
    }
    
	@Override
	public String toString() {
		return "Build action: " + configuration;
	}

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
