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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        /*
         * FIXME Test for MatrixBuild and add to context
         */
        localListener = listener;
        AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) run;

        if (build.getProject().getScm() instanceof ConfigurationRotator) {
            localListener.getLogger().print("onCompleted runlistener - we should write xml here");
            // FIXME explain
            ConfigurationRotatorBuildAction action = build.getAction(ConfigurationRotatorBuildAction.class);
            // if no action, build failed someway to set ConfigurationRotatorBuildAction, thus we can not 
            // say anything about configuration.
            if (action != null) {
                ClearCaseUCMConfiguration configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
                List<ClearCaseUCMConfigurationComponent> components = configuration.getList();

                String id = build.getDisplayName() + "#" + build.getNumber();

                String componentNameList = "";
                for (Iterator<ClearCaseUCMConfigurationComponent> comp = components.iterator(); comp.hasNext();) {
                    componentNameList += comp.next().getBaseline().getShortname();
                    if (comp.hasNext()) {
                        componentNameList += ", ";
                    }
                }

                Date buildFinishTime = getDateTimeFromMilis(build.getTimeInMillis() + build.getDuration());

                try {
                    for (Iterator<ClearCaseUCMConfigurationComponent> comp = components.iterator(); comp.hasNext();) {
                        String componentName = comp.next().getBaseline().getShortname();
                        String componentFileName = ConfigurationRotator.FEED_FULL_PATH + componentName + ".xml";
                        Date currentTime = new Date();

                        Feed feed = getFeedFromFile(componentFileName,
                                componentName, id, currentTime);
                        Entry e = new Entry(componentName + ": " + action.getResult().toString(),
                                id, currentTime);
                        e.summary = componentName + "found to be " + action.getResult().toString() + "with "
                                + components.size() + "other components";
                        e.author = new Person("Jenkins ConfigurationRotator-plugin, job: "
                                + build.getDisplayName() + ", build: #" + build.getNumber());
                        e.content = "Job: " + build.getDisplayName() + ", build #" + build.getNumber()
                                + "finished at: " + buildFinishTime.toString()
                                + "found the following components to be " + action.getResult().toString()
                                + componentNameList;
                        feed.addEntry(e);

                        writeFeedToFile(feed, componentFileName);
                    }
                }  catch (FeedException fe) {
                    localListener.getLogger().print("onCompleted runlistener - caught FeedException, not feeding anything for build: "
                            + build.getDisplayName() + ", #" + build.getNumber()
                            + ". Exception was: " + fe.getMessage());
                } 
            }
        } else {
            localListener.getLogger().print("onCompleted runlistener - was not a ConfigurationRotator");
        }
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
    private Feed getFeedFromFile(String feedFileNameURI, String componentName, String feedId, Date feedUpdated) throws FeedException {

        File feedFile = new File(feedFileNameURI);
        // initial feed
        Feed feed = new Feed(componentName, feedId, feedUpdated);
        // if component already have a feed, use that one
        if (feedFile.exists()) {
            try {
                feed = Feed.getFeed(new AtomPublisher(), feedFile);
            } catch (IOException ex) {
                localListener.getLogger().print(String.format("Failed to get feed from file: %s", feedFileNameURI));                
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
    private void writeFeedToFile(Feed feed, String componentFileName) throws FeedException {
        Writer writer = null;
        try {
            writer = new FileWriter(componentFileName);
            writer.write(feed.getXML(new AtomPublisher()));
            writer.close();
        } catch (IOException ex) {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException ex1) {
                    localListener.getLogger().print("writeFeedToFile runlistener - caught IOException meaning feed may not have been written "+" Exception was: " + ex1.getMessage());
                }
            }
        } 
    }
}