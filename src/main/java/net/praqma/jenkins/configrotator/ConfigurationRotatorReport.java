package net.praqma.jenkins.configrotator;

import java.io.IOException;

import hudson.DescriptorExtensionList;
import hudson.model.*;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMFeedAction;
import net.praqma.jenkins.configrotator.scm.git.GitFeedAction;
import net.praqma.jenkins.configrotator.scm.mercurial.MercurialFeedAction;
import net.praqma.util.xml.feed.AtomPublisher;
import net.praqma.util.xml.feed.Feed;
import net.praqma.util.xml.feed.FeedException;

import hudson.Extension;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

@Extension
public class ConfigurationRotatorReport extends Actionable implements UnprotectedRootAction {

    private static Logger logger = Logger.getLogger( ConfigurationRotatorReport.class.getName() );

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
        List<Action> actions = new ArrayList<Action>( 3 );
        actions.add( new GitFeedAction() );
        actions.add( new MercurialFeedAction() );
        actions.add( new ClearCaseUCMFeedAction() );
        return actions;
    }

    public String getUrl( ConfigurationRotatorSCMDescriptor<AbstractConfigurationRotatorSCM> scm ) {
        return getRootUrl() + getUrlName() + "/" + scm.getFeedComponentName();
    }


    public static Feed getFeedFromFile( File feedFile, String name, String feedId, Date feedUpdated ) throws FeedException, IOException {
        if( feedFile.exists() ) {
            return Feed.getFeed( new AtomPublisher(), feedFile );
        } else {
            return new Feed( name, feedId, feedUpdated );
        }
    }

    /**
     * Transform a URL to a safe String, where all the evil chars are removed
     * @param url
     * @return
     */
    public static String urlTtransform( String url ) {
        return url.replaceAll( "[^a-zA-Z0-9]", "_" );
    }

    public static String FeedFrontpageUrl() {
        return getRootUrl() + ConfigurationRotator.URL_NAME + "/";
    }

    /**
     * Factory method to create the job url for our feed.
     *
     */

    public static String GenerateJobUrl(AbstractBuild<?,?> build) {
        return getRootUrl() + build.getUrl();
    }

    public static String getRootUrl() {
        if( Jenkins.getInstance() == null || Jenkins.getInstance().getRootUrl() == null ) {
            return "http://localhost:8080/";
        } else {
            return Jenkins.getInstance().getRootUrl();
        }
    }

}
