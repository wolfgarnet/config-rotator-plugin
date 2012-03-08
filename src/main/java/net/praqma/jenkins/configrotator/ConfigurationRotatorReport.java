package net.praqma.jenkins.configrotator;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.model.AbstractModelObject;

@Extension
public class ConfigurationRotatorReport extends AbstractModelObject implements UnprotectedRootAction {

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return "Config Rotator";
	}

	@Override
	public String getUrlName() {
		return "config-rotator";
	}

	@Override
	public String getSearchUrl() {
		return getUrlName();
	}

	public HttpResponse doFeed( @QueryParameter( required = true ) String component, @QueryParameter( required = false ) String type ) throws ServletException, IOException {

		return new HttpResponse() {

			@Override
			public void generateResponse( StaplerRequest req, StaplerResponse rsp, Object node ) throws IOException, ServletException {
				rsp.setStatus( StaplerResponse.SC_OK );
				rsp.setContentType( "text/plain" );
				
				PrintWriter writer = rsp.getWriter();
				
				writer.write( "Was here!" );
			}

		};
	}

}
