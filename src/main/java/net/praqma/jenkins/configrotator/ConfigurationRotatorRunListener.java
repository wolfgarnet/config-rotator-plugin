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
            localListener.getLogger().println("onCompleted runlistener - we should write xml here");
            ConfigurationRotatorBuildAction action = build.getAction(ConfigurationRotatorBuildAction.class);
            // if no action, build failed someway to set ConfigurationRotatorBuildAction, thus we can not 
            // say anything about configuration.
            localListener.getLogger().println("onCompleted runlistener - action: " + action);
            if (action != null) {
                ClearCaseUCMConfiguration configuration = action.getConfiguration(ClearCaseUCMConfiguration.class);
                List<ClearCaseUCMConfigurationComponent> components = configuration.getList();
                
                String componentNameList = "";
                for (Iterator<ClearCaseUCMConfigurationComponent> comp = components.iterator(); comp.hasNext();) {
                    componentNameList += comp.next().getBaseline().getShortname();
                    if (comp.hasNext()) {
                        componentNameList += ", ";
                    }
                }
                localListener.getLogger().println("onCompleted runlistener - componentNameList: " + componentNameList);

                Date buildFinishTime = getDateTimeFromMilis(build.getTimeInMillis() + build.getDuration());

                try {
                    for (Iterator<ClearCaseUCMConfigurationComponent> comp = components.iterator(); comp.hasNext();) {
                        
                        ClearCaseUCMConfigurationComponent component = comp.next();
                        String componentName = component.getBaseline().getComponent().getShortname();
                        // default feed!
                     
                        File feedFile = new File(ConfigurationRotator.FEED_FULL_PATH + 
                                "defaultRunListenerError" + ConfigurationRotator.SEPARATOR 
                                + "defaultRunListenerError" + ".xml");
                        File feedFileDir = new File(ConfigurationRotator.FEED_FULL_PATH + 
                                "defaultRunListenerError" + ConfigurationRotator.SEPARATOR);
                        
                        String componentPVob = "defaultRunlistenerErrorPvob";
                        try
                        {
                            componentPVob = component.getBaseline().getComponent().getPVob().getName();
                            // FIXME pvob folder name!
                            feedFile = new File(ConfigurationRotator.FEED_FULL_PATH + componentPVob +
                                    ConfigurationRotator.SEPARATOR + componentName + ".xml");
                            feedFileDir = new File(ConfigurationRotator.FEED_FULL_PATH + componentPVob +
                                    ConfigurationRotator.SEPARATOR);
                        } catch (Exception ex) {
                            // if we can not load PVob name, the correct feed can not be written
                            // so we use a default one.
                            localListener.getLogger().println("onCompleted runlistener - caught Exception, trying to load PVob name. build: "
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
                        localListener.getLogger().println("onCompleted runlistener - entry created");
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
                        localListener.getLogger().println("onCompleted runlistener - done writing to file");
                    }
                }  catch (Exception fe) {
                    localListener.getLogger().println("onCompleted runlistener - caught FeedException, not feeding anything for build: "
                            + build.getDisplayName() + ", #" + build.getNumber()
                            + ". Exception was: " + fe.getMessage());
                }
            }
        } else {
            localListener.getLogger().println("onCompleted runlistener - was not a ConfigurationRotator");
        }
        localListener.getLogger().println("onCompleted runlistener - runListener ending");
    }

    /**
     * Helper function used to calculate build finish time from sum between
     * finish and duration time
     *
     * @param milisSinceEpoc
     * @return Date from milliseconds since EPOC.
     */
    private Date getDateTimeFromMilis(long milisSinceEpoc) {

        Date gmt = new Date(milisSinceEpoc);
        return gmt;
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

        localListener.getLogger().println(String.format("getFeedFromFile called"));
        //File feedFile = new File(feedFileNameURI);
        // initial feed
        Feed feed = new Feed(componentName, feedId, feedUpdated);
        // if component already have a feed, use that one
        if (feedFile.exists()) {
            localListener.getLogger().println(String.format("getFeedFromFile called - file exits"));
            try {
                feed = Feed.getFeed(new AtomPublisher(), feedFile);
                localListener.getLogger().println(String.format("getFeedFromFile got file"));
            } catch (IOException ex) {
                localListener.getLogger().println(String.format("Failed to get feed from file: %s. Exception is: ", feedFile.toString(), ex.getMessage()));                
            }
        }
        else
        {
            localListener.getLogger().println(String.format("Feed-file did not exist."));
        }

        localListener.getLogger().println("onCompleted runlistener getFeedFromFile returnsfeed - feed.getXML" + feed.getXML( new AtomPublisher() ) );
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
        localListener.getLogger().println("onCompleted runlistener, writeFeedToFile - feed.getXML" + feed.getXML( new AtomPublisher() ) );
        Writer writer = null;
        try {
            //localListener.getLogger().println("onCompleted runlistener, writeFeedToFile - " 
                    //+ componentFile.toString().substring(0, componentFile.toString().indexOf(".xml")));
            //boolean feedFileGood = new File(componentFile.toString().substring(0, componentFile.toString().indexOf(".xml"))).mkdirs();
            localListener.getLogger().println("onCompleted runlistener, writeFeedToFile - " 
                    + componentFile.toString());
            localListener.getLogger().println("onCompleted runlistener, writeFeedToFile - " 
                    + componentFile.toURI());
            File feedFile = componentFile;
            localListener.getLogger().println("onCompleted runlistener, writeFeedToFile - " 
                    + feedFile.toString());
            if (!feedFile.exists())
            {
                if (!componentFileDir.exists())
                {
                // create file including dirs
                boolean feedFileGood = new File(componentFileDir.toString()).mkdirs();
                    if (!feedFileGood)
                    {
                        localListener.getLogger().println("writeFeedToFile runListener: failed to make dirs");
                        throw new IOException("writeFeedToFile runListener: failed to make dirs");
                    }
                    else
                    {
                        localListener.getLogger().println("onCompleted runlistener, created FeedToFileDIR - " 
                        + componentFileDir.toString());                        
                    }
                }
                else
                {
                    localListener.getLogger().println("onCompleted runlistener, FeedToFileDIR existed " 
                        + componentFileDir.toString());                        
                    feedFile = new File(componentFile.toString());
                }
                
                localListener.getLogger().println("onCompleted runlistener, writeFeedToFile - " 
                    + feedFile.toString());
            }
            localListener.getLogger().println("onCompleted runlistener, writeFeedToFile - " 
                    + feedFile.toString());
            //File feedFile = new File(componentFile);
            writer = new FileWriter(feedFile);
            writer.write(feed.getXML(new AtomPublisher()));
            writer.close();
        } catch (IOException ex) {
            if(writer != null) {
                try {
                    localListener.getLogger().println("writeFeedToFile runlistener - write failed caught IOException meaning feed may not have been written "+" Exception was: " + ex.getMessage());
                    writer.close();
                } catch (IOException ex1) {
                    localListener.getLogger().println("writeFeedToFile runlistener - write.close failed too caught IOException meaning feed may not have been written "+" Exception was: " + ex1.getMessage());
                }
            } else
            {
                localListener.getLogger().println("writeFeedToFile runlistener - write.close  WAS NULLailed too caught IOException meaning feed may not have been written "+" Exception was: " + ex.getMessage());
            }
        } 
    }
}