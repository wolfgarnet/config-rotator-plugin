package net.praqma.jenkins.configrotator.scm.git;

import net.praqma.jenkins.configrotator.ConfigurationRotator;
import net.praqma.jenkins.configrotator.ConfigurationRotatorFeedAction;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.util.logging.Logger;

public class GitFeedAction extends ConfigurationRotatorFeedAction {

    private static Logger logger = Logger.getLogger( GitFeedAction.class.getName() );

    @Override
    public String getUrlName() {
        return Git.class.getSimpleName();
    }

    @Override
    public String getComponentName() {
        return Git.class.getSimpleName();
    }

    @Override
    protected File getFeedFile( StaplerRequest req ) {
        String component = req.getParameter( "component" );
        String element = req.getParameter( "element" );

        if( component != null && element != null ) {
            File path = new File( new File( new File( ConfigurationRotator.FEED_PATH, getComponentName() ), component ), element + ".xml" );
            return path;
        } else {
            return null;
        }
    }
}
