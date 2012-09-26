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
import java.util.logging.Logger;

public class GitConfigurationComponent extends AbstractConfigurationComponent {

    private static Logger logger = Logger.getLogger( GitConfigurationComponent.class.getName() );

    private transient RevCommit commit;
    private String commitId;
    private String name;
    private String branch;
    private String repository;

    private GitConfigurationComponent( String name, String repository, String branch, String commitId, boolean fixed ) {
        super( fixed );
        this.name = name;
        this.repository = repository;
        this.branch = branch;
        this.commitId = commitId;
    }

    public GitConfigurationComponent( String name, String repository, String branch, RevCommit commit, boolean fixed ) {
        super( fixed );
        this.commit = commit;
        if( commit != null ) {
            this.commitId = commit.getName();
        }
        this.name = name;
        this.branch = branch;
        this.repository = repository;
    }

    public String getBranch() {
        return branch;
    }

    public String getRepository() {
        return repository;
    }

    public String getName() {
        return name;
    }

    public RevCommit getCommit() {
        return commit;
    }

    public void setCommitId( String commitId ) {
        this.commitId = commitId;
    }

    public String getCommitId() {
        return commitId;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        GitConfigurationComponent gcc = new GitConfigurationComponent( name, repository, branch, commitId, fixed );
        return  gcc;
    }

    @Override
    public String toString() {
        return "GitComponent[" + name + ": " + repository + ", " + branch + ", " + commitId + "]";
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

        entry.content = configuration.getDescription( build );
        Html.Break br1 = new Html.Break();
        Html.Anchor linkFeeds = new Html.Anchor( ConfigurationRotatorReport.FeedFrontpageUrl(), "Click here for a list of available feeds" );
        Html.Break br2 = new Html.Break();
        Html.Anchor joblink = new Html.Anchor( ConfigurationRotatorReport.GenerateJobUrl( build ), "Click here to go to the build that created this feed" );

        entry.content += configuration.toHtml() + br1 + linkFeeds + br2 + joblink;

        return entry;
    }
}
