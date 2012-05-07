/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.praqma.html.Html;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogSet;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeSetDescriptor;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfigRotatorChangeLogSet;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfigRotatorEntry;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfiguration;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfigurationComponent;
import net.praqma.util.xml.feed.*;

@Extension
public class ConfigurationRotatorRunListener extends RunListener<Run> {
    
    private TaskListener localListener;
    
    public ConfigurationRotatorRunListener() {
        super(Run.class);

    }

    /**
     * Run listener for ConfigurationRotator jobs that does atom feed writing
     * for the results.
     *
     * @param run
     * @param listener
     */
    @Override
    public void onCompleted(Run run, TaskListener listener) {
        localListener = listener;
        
        AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) run;

        if (build.getProject().getScm() instanceof ConfigurationRotator) {
            ConfigurationRotatorBuildAction action = build.getAction(ConfigurationRotatorBuildAction.class);
            // if no action, build failed someway to set ConfigurationRotatorBuildAction, thus we can not 
            // say anything about configuration.
            if (action != null) {
                ClearCaseUCMConfiguration configuration = action.getConfiguration(ClearCaseUCMConfiguration.class);
                List<ClearCaseUCMConfigurationComponent> components = configuration.getList();
                
                try {
                                        
                    for (ClearCaseUCMConfigurationComponent component : components) {
                        String componentName = component.getBaseline().getComponent().getShortname();    
                        // default feed file to use if we can not get component Pvob name
                        // the file will show up on the feed page
                        File feedFile = new File(ConfigurationRotatorReport.createFeedXmlFile("ConfigRotatorDefaultFeedFile","ConfigRotatorDefaultFeedFile.xml"));
                        File feedFileDir = new File(ConfigurationRotatorReport.createFeedFolder("ConfigRotatorDefaultFeedFile"));
                        String componentPVob = "ConfigRotatorDefaultPvob";
                        try {
                            componentPVob = component.getBaseline().getComponent().getPVob().getName();
                            // set the correct component feed file overwriting the above default one
                            feedFile = new File(ConfigurationRotatorReport.createFeedXmlFile(componentPVob, componentName));
                            feedFileDir = new File(ConfigurationRotatorReport.createFeedFolder(componentPVob));
                        } catch (Exception ex) {
                            // if we can not load PVob name, the correct feed can not be written
                            // so we use a default one.
                            localListener.getLogger().println("ConfigRotator RunListener - caught Exception, trying to load PVob name. build: "
                            + build.getDisplayName() + ", #" + build.getNumber()
                            + ". Exception was: " + ex.getMessage());
                        }
                      
                        
                        // required feed element - need to create feed
                        String feedId = ConfigurationRotatorReport.CreateFeedUrl(componentPVob, componentName); // feed URL
                        String feedTitle = componentName;
                        Date updated = new Date();
                        Feed feed = getFeedFromFile(feedFile, feedTitle, feedId, updated);

                        
                        String id = "'" + build.getParent().getDisplayName() + "'#" + build.getNumber() + ":" + componentName + "@" + componentPVob;

                        Entry e = new Entry(componentName + " in new " + action.getResult().toString() + " configuration", id, updated);
                        e.summary = componentName + " found to be " + action.getResult().toString() + " with "
                                + components.size() + " other components";
 
                        e.author = new Person("Jenkins job using config-rotator. Job: "
                                + build.getParent().getDisplayName() + ", build: #" + build.getNumber());
                        
                        
                        //Now. Let's harvest the headline generated by this config change
                        if(build.getChangeSet() instanceof ConfigRotatorChangeSetDescriptor) {
                            ConfigRotatorChangeSetDescriptor cset = (ConfigRotatorChangeSetDescriptor)build.getChangeSet();
                            if(cset != null) {
                                if( cset.getHeadline() != null ) {
                                    e.content = new Html.Paragraph(cset.getHeadline()).toString();
                                }
                            }
                        } else if(build.getChangeSet().isEmptySet()) {
                            e.content = new Html.Paragraph(ConfigRotatorChangeLogSet.EMPTY_DESCRIPTOR).toString();
                        }
  
                        Html.Break br1 = new Html.Break();
                        Html.Anchor linkFeeds = new Html.Anchor(ConfigurationRotatorReport.FeedFrontpageUrl(), "Click here for a list of available feeds");
                        Html.Break br2 = new Html.Break();
                        Html.Anchor joblink = new Html.Anchor(ConfigurationRotatorReport.GenerateJobUrl(build),"Click here to go to the build that created this feed");
                        
                        e.content += configuration.toHtml()+br1+linkFeeds+br2+joblink;
                        
                        feed.addEntry(e);

                        writeFeedToFile(feed, feedFile, feedFileDir);
                    }
                }  catch (Exception fe) {
                    localListener.getLogger().println("ConfigRotator RunListener - caught FeedException, not feeding anything for build: "
                            + build.getDisplayName() + ", #" + build.getNumber()
                            + ". Exception was: " + fe.getMessage());
                }
            }
        } 
    }

    /**
     * Returns an atom feed that you can append entries on a from the file
     * feedFileNameURI. Feed may be initial and empty if file does not exist.
     *
     * @param feedFileNameURI
     * @param componentName
     * @param feedId
     * @param feedUpdated
     * @return
     * @throws FeedException
     * @throws IOException
     */
    private Feed getFeedFromFile(File feedFile, String componentName, String feedId, Date feedUpdated) throws FeedException {
        Feed feed = new Feed(componentName, feedId, feedUpdated);
        // if component already have a feed, use that one
        if (feedFile.exists()) {
            try {
                feed = Feed.getFeed(new AtomPublisher(), feedFile);
            } catch (IOException ex) {
                localListener.getLogger().println(String.format("ConfigRotator RunListener - Failed to get feed from file: %s. Exception is: ", feedFile.toString(), ex.getMessage()));                
            }
        } 
        return feed;
    }

    /**
     * Write the feed to the file componentFileName overwriting existing xml
     * file with atoms feed. If entries have unique id that are not changes,
     * subscribers will see only new ones.
     *
     * @param feed
     * @param componentFileName
     * @throws IOException
     * @throws FeedException
     */
    private void writeFeedToFile(Feed feed, File componentFile, File componentFileDir) throws FeedException {
        Writer writer = null;
        try {
            File feedFile = componentFile;
            if (!feedFile.exists()) {
                if (!componentFileDir.exists()) {
                    // create file including dirs
                    boolean feedFileGood = new File(componentFileDir.toString()).mkdirs();
                    if (!feedFileGood) {
                        throw new IOException("ConfigRotator RunListener - writeFeedToFile: failed to make dirs");
                    }
                } else {
                    feedFile = new File(componentFile.toString());
                }
            }
            
            writer = new FileWriter(feedFile);
            writer.write(feed.getXML(new AtomPublisher()));
            writer.close();
        } catch (IOException ex) {
            if(writer != null) {
                try {
                    localListener.getLogger().println("ConfigRotator RunListener - writeFeedToFile: write failed caught IOException meaning feed may not have been written "+" Exception was: " + ex.getMessage());
                    writer.close();
                } catch (IOException ex1) {
                    localListener.getLogger().println("ConfigRotator RunListener - writeFeedToFile: write.close failed too caught IOException meaning feed may not have been written "+" Exception was: " + ex1.getMessage());
                }
            } else
            {
                localListener.getLogger().println("ConfigRotator RunListener - writeFeedToFile: write.close  WAS NULLailed too caught IOException meaning feed may not have been written "+" Exception was: " + ex.getMessage());
            }
        } 
    }
}