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
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import jenkins.model.Jenkins;

// FIXME - RootAction - needs login ? Should it be public?
@Extension
public class ConfigurationRotatorReport extends AbstractModelObject implements UnprotectedRootAction {
    
    private static final String XML_EXTENSION = ".xml";
     
	@Override
	public String getIconFileName() {
		return "/plugin/config-rotator/images/rotate.png";
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
    
    public String getFeedUrl(String componentXmlFile) {
        String actionLink = "/"+getUrlName()+"/feed/?component="+getComponentName(componentXmlFile);
        return actionLink;
    }
    
    public String getComponentName(String componentXmlFile) {
        return componentXmlFile.substring(0,componentXmlFile.indexOf("."));
    } 
            
    
    /**
     * 
     * @return a list of available feeds in link format. 
     */
    public ArrayList<String> listAvailableFeeds() {
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(new File(ConfigurationRotator.FEED_FULL_PATH).list()));
        return list;
    }
    
    

	// yourhost/config-rotator/feed/
	// eg. http://localhost:8080/config-rotator/feed/?component=hest2
    // TODO: input parameter check and validation
	public HttpResponse doFeed( @QueryParameter( required = true ) String component) throws ServletException, IOException {
		final String mycomp = component+XML_EXTENSION;
        final String fullComponentFeedPath = ConfigurationRotator.FEED_FULL_PATH+mycomp;
		return new HttpResponse() {

			@Override
			public void generateResponse( StaplerRequest req, StaplerResponse rsp, Object node ) throws IOException, ServletException {
				rsp.setStatus( StaplerResponse.SC_OK );
                
                rsp.setContentType("application/atom+xml");

                FileInputStream fis = null;
                InputStreamReader isr = null;
                PrintWriter writer = null;
                BufferedReader reader = null;
				
                //Open file to begin reading file. 
                try {
                    fis = new FileInputStream(new File(fullComponentFeedPath));
                    isr = new InputStreamReader(fis);
                
                    reader = new BufferedReader(isr);
                    writer = rsp.getWriter();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                    }
                    } catch (IOException ex) {
                        throw ex;
                    } finally {
                        if(writer != null)
                            writer.close();
                        if(reader != null)
                            reader.close();
                    }
				
			}			
		};
	}
}
