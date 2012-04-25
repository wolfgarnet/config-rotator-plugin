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
import hudson.model.listeners.RunListener;

// FIXME - RootAction - needs login ? Should it be public?
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

	@Override	public String getUrlName() {		return "config-rotator";
	}

	@Override
	public String getSearchUrl() {
		return getUrlName();
	}

	// yourhost/config-rotator/feed/
	// eg. http://localhost:8080/config-rotator/feed/?component=hest2
  // TODO: input parameter check and validation
	public HttpResponse doFeed( @QueryParameter( required = true ) String component) throws ServletException, IOException {
	//	public HttpResponse doFeed( @QueryParameter( required = true ) String component, @QueryParameter( required = false ) String type ) throws ServletException, IOException {

		final String mycomp = component; 
		return new HttpResponse() {

			@Override
			public void generateResponse( StaplerRequest req, StaplerResponse rsp, Object node ) throws IOException, ServletException {
				rsp.setStatus( StaplerResponse.SC_OK );
				rsp.setContentType( "text/plain" );
				
				PrintWriter writer = rsp.getWriter();
				
				writer.write( "Was here!: paramater was: " + mycomp);
				writer.close();
		
			}
			
		};
	}

}
