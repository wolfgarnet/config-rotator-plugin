package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.model.AbstractBuild;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.html.Html;
import net.praqma.jenkins.configrotator.AbstractConfiguration;
import net.praqma.jenkins.configrotator.AbstractConfigurationComponent;
import net.praqma.jenkins.configrotator.ConfigurationRotatorBuildAction;
import net.praqma.jenkins.configrotator.ConfigurationRotatorReport;
import net.praqma.util.xml.feed.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ClearCaseUCMConfigurationComponent extends AbstractConfigurationComponent {

    private Baseline baseline;
    private PromotionLevel plevel;

    public ClearCaseUCMConfigurationComponent( Baseline baseline, PromotionLevel plevel, boolean fixed ) {
        super( fixed );
        this.baseline = baseline;
        this.plevel = plevel;
    }

    public ClearCaseUCMConfigurationComponent( String baseline, String plevel, boolean fixed ) throws ClearCaseException {
        super( fixed );
        this.baseline = Baseline.get( baseline ).load();
        this.plevel = Project.PromotionLevel.valueOf( plevel );

    }

    @Override
    public ClearCaseUCMConfigurationComponent clone() {
        ClearCaseUCMConfigurationComponent cc = new ClearCaseUCMConfigurationComponent( this.baseline, this.plevel, this.fixed );

        return cc;
    }

    public void setBaseline( Baseline baseline ) {
        this.baseline = baseline;
    }

    public Baseline getBaseline() {
        return baseline;
    }

    public PromotionLevel getPlevel() {
        return plevel;
    }

    @Override
    public String toString() {
        return baseline.getNormalizedName() + "@" + plevel + "(" + fixed + "/" + changedLast + ")";
    }

    @Override
    public boolean equals( Object other ) {
        if( other == this ) {
            return true;
        }

        if( other instanceof ClearCaseUCMConfigurationComponent ) {
            ClearCaseUCMConfigurationComponent o = (ClearCaseUCMConfigurationComponent) other;

            return ( o.baseline.equals( baseline ) && ( o.plevel.equals( plevel ) ) && ( o.isFixed() == fixed ) );
        } else {
            return false;
        }
    }

    @Override
    public File getFeedFile( File path ) {
        return new File( new File( path, baseline.getPVob().getName() ), baseline.getComponent().getShortname() );
    }

    @Override
    public Feed getFeed( File feedFile, String url, Date updated ) throws FeedException, IOException {
        String feedId = url + "feed/?component=" + baseline.getComponent().getShortname() + "&pvob=" + baseline.getPVob().getName();
        String feedTitle = baseline.getComponent().getShortname();

        Feed feed = ConfigurationRotatorReport.getFeedFromFile( feedFile, feedTitle, feedId, updated );

        return feed;
    }

    @Override
    public Entry getFeedEntry( AbstractBuild<?, ?> build, Date updated ) {

        ConfigurationRotatorBuildAction action = build.getAction( ConfigurationRotatorBuildAction.class );
        AbstractConfiguration configuration = action.getConfigurationWithOutCast();
        List<AbstractConfigurationComponent> components = configuration.getList();

        String componentPVob = baseline.getPVob().getName();
        String componentName = baseline.getComponent().getShortname();


        String id = "'" + build.getParent().getDisplayName() + "'#" + build.getNumber() + ":" + componentName + "@" + componentPVob;

        Entry entry = new Entry( componentName + " in new " + action.getResult().toString() + " configuration", id, updated );
        entry.summary = componentName + " found to be " + action.getResult().toString() + " with "
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

    /**
     * Test method...
     *
     * @return a html'ified version of this clearcase components. In this case it is a table row.
     */
    public String toHtml() {
        StringBuilder builder = new StringBuilder();
        builder.append( "<tr>" );
        builder.append( "<td style=\"padding:5px 10px;\">" ).append( baseline.getComponent().getNormalizedName() ).append( "</td>" );
        builder.append( "<td style=\"padding:5px 10px;\">" ).append( baseline.getStream().getNormalizedName() ).append( "</td>" );
        if( isChangedLast() ) {
            builder.append( "<td style=\"font-weight:bold;color:#FF6633;padding:5px 10px;\">" ).append( baseline.getNormalizedName() ).append( "</td>" );
        } else {
            builder.append( "<td style=\"padding:5px 10px;\">" ).append( baseline.getNormalizedName() ).append( "</td>" );
        }
        builder.append( "<td style=\"padding:5px 10px;\">" ).append( plevel.toString() ).append( "</td>" );
        builder.append( "<td style=\"padding:5px 10px;\">" ).append( fixed ).append( "</td>" ).append( "</tr>" );
        return builder.toString();
    }


}