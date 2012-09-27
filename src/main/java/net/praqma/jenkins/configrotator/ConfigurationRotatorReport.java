package net.praqma.jenkins.configrotator;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import hudson.DescriptorExtensionList;
import hudson.model.*;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMFeedAction;
import net.praqma.jenkins.configrotator.scm.git.GitFeedAction;
import net.praqma.util.xml.feed.AtomPublisher;
import net.praqma.util.xml.feed.Feed;
import net.praqma.util.xml.feed.FeedException;
import org.kohsuke.stapler.*;

import hudson.Extension;

import java.io.*;
import java.util.*;

import jenkins.model.Jenkins;

@Extension
public class ConfigurationRotatorReport extends Actionable implements UnprotectedRootAction {

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

    public DescriptorExtensionList<AbstractConfigurationRotatorSCM, ConfigurationRotatorSCMDescriptor<AbstractConfigurationRotatorSCM>> getSCMs() {
        return AbstractConfigurationRotatorSCM.all();
    }

    @Override
    public synchronized List<Action> getActions() {
        /* TODO make this more generic */
        List<Action> actions = new ArrayList<Action>();
        actions.add( new GitFeedAction() );
        actions.add( new ClearCaseUCMFeedAction() );
        return actions;
    }

    public String getUrl( ConfigurationRotatorSCMDescriptor<AbstractConfigurationRotatorSCM> scm ) {
        return ConfigurationRotator.URL_NAME + "/" + scm.getFeedComponentName();
    }


    public static Feed getFeedFromFile( File feedFile, String name, String feedId, Date feedUpdated ) throws FeedException, IOException {
        if( feedFile.exists() ) {
            return Feed.getFeed( new AtomPublisher(), feedFile );
        } else {
            return new Feed( name, feedId, feedUpdated );
        }
    }
    
    /**
     * Factory to create url when subscribing to feeds
     * 
     */

    public static String urlTtransform( String url ) {
        return url.replaceAll( "[^a-zA-Z0-9]", "_" );
    }

    public static String FeedFrontpageUrl() {
        String url = (Jenkins.getInstance() == null || Jenkins.getInstance().getRootUrl() == null) ? ConfigurationRotator.DEFAULT_URL : Jenkins.getInstance().getRootUrl();
        url+= "/"+ConfigurationRotator.URL_NAME+"/";
        return url;
    }

    /**
     * Factory method to create the job url for our feed.
     * 
     */
    
    public static String GenerateJobUrl(AbstractBuild<?,?> build) {
        String url = (Jenkins.getInstance() == null || Jenkins.getInstance().getRootUrl() == null) ? ConfigurationRotator.DEFAULT_URL : Jenkins.getInstance().getRootUrl();
        String actionLink = url + "/" + build.getUrl();
        return actionLink;
    }

}
