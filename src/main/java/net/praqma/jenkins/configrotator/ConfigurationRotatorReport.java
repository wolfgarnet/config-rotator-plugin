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
import java.util.List;
import javax.security.auth.login.Configuration;
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
    
    /**
     * Factory to create url when subscribing to feeds
     * 
     */ 
    public static String CreateFeedUrl(String vobname, String componentName) {
        String actionLink = "/"+ConfigurationRotator.NAME+"/feed/?component="+componentName+"&pvob="+vobname;
        return actionLink;
    }
    
    public String getFeedUrl(String vob, String componentName) {
        String actionLink = "/"+getUrlName()+"/feed/?component="+componentName+"&pvob="+vob;
        return actionLink;
    }
    
    public String getComponentName(String componentXmlFile) {
        return componentXmlFile.substring(0,componentXmlFile.indexOf("."));
    }
    
    public String getVobName(File f) {
        return f.getName();
    }
    
    /**
     * 
     * @return a list of available feeds in link format. 
     */
    public ArrayList<String> listAvailableFeeds() {
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(ConfigurationRotator.FEED_DIRFILE.list()));
        return list;
    }
    
    public ArrayList<String> listAvailableFeeds(List<String> vobs) {
        ArrayList<String> list = new ArrayList<String>();
        for(String s: vobs) {
            
        }
        
        return list;
    }
    
    /**
     * Extracts vobs. Use this as an argument to listAvailableFeeds
     * 
     */ 
    public ArrayList<File> listVobs() {
        FileFilter listDirsFilter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };
        
        ArrayList<File> list = new ArrayList<File>();
        
        for(File f: ConfigurationRotator.FEED_DIRFILE.listFiles(listDirsFilter)) {
            list.add(f);
        }
        
        return list;
    }
    
    public ArrayList<String> listComponents(File f) {
        ArrayList<String> components = new ArrayList<String>();
        components.addAll(Arrays.asList(f.list()));
        return components;
    }
    
    
    
    
    

	// yourhost/config-rotator/feed/
	// eg. http://localhost:8080/config-rotator/feed/?component=hest2
    // TODO: input parameter check and validation
	public HttpResponse doFeed( @QueryParameter( required = true ) String component, @QueryParameter( required = true ) String pvob) throws ServletException, IOException {
		final String mycomp = pvob+ConfigurationRotator.SEPARATOR+component+XML_EXTENSION;
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
                    File feedFile = new File(fullComponentFeedPath);
                    if(feedFile.exists()) {
                        fis = new FileInputStream(new File(fullComponentFeedPath));
                        isr = new InputStreamReader(fis);

                        reader = new BufferedReader(isr);
                        writer = rsp.getWriter();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.write(line);
                        }
                    } else {
                        writer.write("No such feed");
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
