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
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfiguration;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfigurationComponent;
import net.praqma.util.xml.feed.*;
import org.apache.tools.ant.types.FileList;

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
            localListener.getLogger().println("onCompleted runlistener - we should write xml here");
            // FIXME explain
            ConfigurationRotatorBuildAction action = build.getAction(ConfigurationRotatorBuildAction.class);
            // if no action, build failed someway to set ConfigurationRotatorBuildAction, thus we can not 
            // say anything about configuration.
            localListener.getLogger().println("onCompleted runlistener - action: " + action);
            if (action != null) {
                ClearCaseUCMConfiguration configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
                List<ClearCaseUCMConfigurationComponent> components = configuration.getList();

                String id = build.getParent().getDisplayName() + "#" + build.getNumber();
                localListener.getLogger().println("onCompleted runlistener - feed id is: " + id);

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
                     
                        File componentFile = new File(ConfigurationRotator.FEED_FULL_PATH + 
                                "defaultRunListenerError" + ConfigurationRotator.SEPARATOR 
                                + "defaultRunListenerError" + ".xml");
                        localListener.getLogger().println("onCompleted runlistener - DEFAULT componentFileName: " + componentFile.toString());
                        try
                        {
                            String componentPVob = component.getBaseline().getComponent().getPVob().getName();
                            // FIXME pvob folder name!
                            componentFile = new File(ConfigurationRotator.FEED_FULL_PATH + componentPVob +
                                    ConfigurationRotator.SEPARATOR + componentName + ".xml");
                        } catch (Exception ex) // will handle all exception the same way
                        {
                            // if we can not load PVob name, the correct feed can not be written
                            // so we use a default one.
                            localListener.getLogger().println("onCompleted runlistener - caught Exception, trying to load PVob name. build: "
                            + build.getDisplayName() + ", #" + build.getNumber()
                            + ". Exception was: " + ex.getMessage());
                        }
                      
                        localListener.getLogger().println("onCompleted runlistener - REAL componentFileName: " + componentFile.toString());
                        
                        Date currentTime = new Date();

                        Feed feed = getFeedFromFile(componentFile,
                                componentName, id, currentTime);
                        localListener.getLogger().println("onCompleted runlistener - feed.getXML" + feed.getXML( new AtomPublisher() ) );
                        Entry e = new Entry(componentName + ": " + action.getResult().toString(),
                                id, currentTime);
                        localListener.getLogger().println("onCompleted runlistener - entry created");
                        e.summary = componentName + "found to be " + action.getResult().toString() + "with "
                                + components.size() + "other components";
                        localListener.getLogger().println("onCompleted runlistener - entry summary added");
                        localListener.getLogger().println("onCompleted runlistener - entry summary :" + e.summary);
                        e.author = new Person("Jenkins ConfigurationRotator-plugin, job: "
                                + build.getDisplayName() + ", build: #" + build.getNumber());
                        localListener.getLogger().println("onCompleted runlistener - entry author added");
                        localListener.getLogger().println("onCompleted runlistener - entry author:" + e.author);
                        e.content = "Job: " + build.getDisplayName() + ", build #" + build.getNumber()
                                + "finished at: " + buildFinishTime.toString()
                                + "found the following components to be " + action.getResult().toString()
                                + componentNameList;
                        localListener.getLogger().println("onCompleted runlistener - entry content added");
                        localListener.getLogger().println("onCompleted runlistener - entry content:" + e.content);
                        localListener.getLogger().println("onCompleted runlistener - entry done");
                        localListener.getLogger().println("onCompleted runlistener - entry added: " + e.toString());
                        feed.addEntry(e);
                        localListener.getLogger().println("onCompleted runlistener - entry added done");
                        localListener.getLogger().println("onCompleted runlistener - feed.getXML after entry add" + feed.getXML( new AtomPublisher() ) );
                        localListener.getLogger().println("onCompleted runlistener - done adding entry.");
                        writeFeedToFile(feed, componentFile);
                        localListener.getLogger().println("onCompleted runlistener - done writing to file");
                    }
                }  catch (Exception fe) {
                    localListener.getLogger().println("onCompleted runlistener - caught FeedException, not feeding anything for build: "
                            + build.getDisplayName() + ", #" + build.getNumber()
                            + ". Exception was: " + fe.getMessage());
                }
                localListener.getLogger().println("onCompleted runlistener - runListener ending");
            }
        } else {
            localListener.getLogger().println("onCompleted runlistener - was not a ConfigurationRotator");
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
    private void writeFeedToFile(Feed feed, File componentFile) throws FeedException {
        localListener.getLogger().println("onCompleted runlistener, writeFeedToFile - feed.getXML" + feed.getXML( new AtomPublisher() ) );
        Writer writer = null;
        try {
            localListener.getLogger().println("onCompleted runlistener, writeFeedToFile - " + componentFile.toString().substring(0, componentFile.toString().indexOf(".xml")));
            boolean feedFileGood = new File(componentFile.toString().substring(0, componentFile.toString().indexOf(".xml"))).mkdirs();
            if (!feedFileGood)
            {
                throw new IOException("writeFeedToFile runListener: failed to make dirs");
            }
            //File feedFile = new File(componentFile);
            writer = new FileWriter(componentFile);
            writer.write(feed.getXML(new AtomPublisher()) + "Testing output");
            writer.close();
        } catch (IOException ex) {
            if(writer != null) {
                try {
                    localListener.getLogger().println("writeFeedToFile runlistener - write failed caught IOException meaning feed may not have been written "+" Exception was: " + ex.getMessage());
                    writer.close();
                } catch (IOException ex1) {
                    localListener.getLogger().println("writeFeedToFile runlistener - write.close failed too caught IOException meaning feed may not have been written "+" Exception was: " + ex1.getMessage());
                }
            }
        } 
    }
}