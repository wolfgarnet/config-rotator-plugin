/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.model.AbstractBuild;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.praqma.jenkins.configrotator.ConfigurationRotator;
import net.praqma.jenkins.configrotator.ConfigurationRotatorBuildAction;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogSet;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorEntry;

/**
 *
 * @author Praqma
 */
public class ClearCaseUCMConfigRotatorChangeLogSet extends ConfigRotatorChangeLogSet<ClearCaseUCMConfigRotatorEntry> {
    private static final String NEW_CONFIG = "New configuration - no changes yet";
    public static final String CONF_ERROR = "Configuration error";

    public ClearCaseUCMConfigRotatorChangeLogSet(AbstractBuild<?,?> build) {
        super(build);
    }
    
    public ClearCaseUCMConfigRotatorChangeLogSet(AbstractBuild<?,?> build, List<ClearCaseUCMConfigRotatorEntry> entries) {       
       super(build);
       if(build == null) {
           setHeadline(CONF_ERROR);
           this.entries = entries;
       } else {
            if(build.getProject().getScm() instanceof ConfigurationRotator) {
                ClearCaseUCMConfiguration current = build.getAction(ConfigurationRotatorBuildAction.class).getConfiguration(ClearCaseUCMConfiguration.class);
                String curBaseline = current.getChangedComponent() != null ? current.getChangedComponent().getBaseline().getNormalizedName() : NEW_CONFIG;
                String prevBaseline = "";
                String header = "";
                int index = current.getChangedComponentIndex();               
                ConfigurationRotatorBuildAction lac = null;
                
                ConfigurationRotator rotator = (ConfigurationRotator)build.getProject().getScm();
                 
                ArrayList<ConfigurationRotatorBuildAction> results = rotator.getAcrs().getLastResults(build.getProject(), ClearCaseUCM.class, 2);
                lac = null;
                
                if(results.size() >= 2) {
                    lac = results.get(1);
                }
                
                
                if(index != -1 && lac != null) {
                    prevBaseline = lac.getConfiguration(ClearCaseUCMConfiguration.class).getList().get(index).getBaseline().getNormalizedName();
                } 

                if(curBaseline.equals(NEW_CONFIG)) {
                    header = NEW_CONFIG;
                } else {
                    header = String.format("Baseline changed from %s to %s", prevBaseline,curBaseline);
                }

                setHeadline(header);

                for(ConfigRotatorEntry e : entries) {
                    e.setParent(this);
                }
                this.entries = entries;
            }
       }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public Iterator<ClearCaseUCMConfigRotatorEntry> iterator() {
        return (Iterator<ClearCaseUCMConfigRotatorEntry>) entries.iterator();
    }
    
    @Override
    public String toString() {
        return entries.toString();
    }

}
