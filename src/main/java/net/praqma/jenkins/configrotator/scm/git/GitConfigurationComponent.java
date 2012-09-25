package net.praqma.jenkins.configrotator.scm.git;

import hudson.model.AbstractBuild;
import net.praqma.html.Html;
import net.praqma.jenkins.configrotator.AbstractConfiguration;
import net.praqma.jenkins.configrotator.AbstractConfigurationComponent;
import net.praqma.jenkins.configrotator.ConfigurationRotatorBuildAction;
import net.praqma.jenkins.configrotator.ConfigurationRotatorReport;
import net.praqma.util.xml.feed.Entry;
import net.praqma.util.xml.feed.Feed;
import net.praqma.util.xml.feed.FeedException;
import net.praqma.util.xml.feed.Person;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class GitConfigurationComponent extends AbstractConfigurationComponent {
    private RevCommit commit;
    private String name;
    private String branch;

    public GitConfigurationComponent( String name, String branch, RevCommit commit, boolean fixed ) {
        super( fixed );
        this.commit = commit;
        this.name = name;
        this.branch = branch;
    }

    public void setCommit( RevCommit commit ) {
        this.commit = commit;
    }

    public String getName() {
        return name;
    }

    public RevCommit getCommit() {
        return commit;
    }

    @Override
    public File getFeedFile( File path ) {
        return new File( new File( path, name ), branch );
    }

    @Override
    public Feed getFeed( File feedFile, String url, Date updated ) throws FeedException, IOException {
        String feedId = url + "feed/?component=" + name + "&branch=" + branch;
        String feedTitle = name;

        Feed feed = ConfigurationRotatorReport.getFeedFromFile( feedFile, feedTitle, feedId, updated );

        return feed;
    }

    @Override
    public Entry getFeedEntry( AbstractBuild<?, ?> build, Date updated ) {
        ConfigurationRotatorBuildAction action = build.getAction( ConfigurationRotatorBuildAction.class );
        AbstractConfiguration configuration = action.getConfigurationWithOutCast();
        List<AbstractConfigurationComponent> components = configuration.getList();


        String id = "'" + build.getParent().getDisplayName() + "'#" + build.getNumber() + ":" + branch + "@" + name;

        Entry entry = new Entry( name + " in new " + action.getResult().toString() + " configuration", id, updated );
        entry.summary = name + " found to be " + action.getResult().toString() + " with "
                + components.size() + " other components";

        entry.author = new Person( "Jenkins job using config-rotator. Job: "
                + build.getParent().getDisplayName() + ", build: #" + build.getNumber() );

        entry.content = action.getDescription();
        Html.Break br1 = new Html.Break();
        Html.Anchor linkFeeds = new Html.Anchor( ConfigurationRotatorReport.FeedFrontpageUrl(), "Click here for a list of available feeds" );
        Html.Break br2 = new Html.Break();
        Html.Anchor joblink = new Html.Anchor( ConfigurationRotatorReport.GenerateJobUrl( build ), "Click here to go to the build that created this feed" );

        entry.content += configuration.toHtml() + br1 + linkFeeds + br2 + joblink;

        return entry;
    }
}
