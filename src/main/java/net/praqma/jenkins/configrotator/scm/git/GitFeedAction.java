package net.praqma.jenkins.configrotator.scm.git;

import net.praqma.jenkins.configrotator.FeedAction;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

public class GitFeedAction extends FeedAction {

    @Override
    public String getUrlName() {
        return Git.class.getSimpleName();
    }

    public void doIndex( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        rsp.getWriter().println( "BAM!" );
    }
}
