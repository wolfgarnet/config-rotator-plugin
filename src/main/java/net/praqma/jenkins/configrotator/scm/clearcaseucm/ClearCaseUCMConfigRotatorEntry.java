/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import java.util.ArrayList;
import java.util.Collection;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorEntry;

/**
 *
 * @author Praqma
 */
public class ClearCaseUCMConfigRotatorEntry extends ConfigRotatorEntry {
    
    private String activityName;  
    private String activityHeadline;
    private ArrayList<ClearCaseVersion> versions;

    /**
     * Default constructor
     */
    public ClearCaseUCMConfigRotatorEntry() {
        versions = new ArrayList<ClearCaseVersion>();
    }
    
    @Override
    public String getMsg() {
        return activityName;
    }

    @Override
    public Collection<String> getAffectedPaths() {
        ArrayList<String> strings = new ArrayList<String>();
        for(ClearCaseVersion ccv : getVersions()) {
            strings.add(ccv.getFile());
        }
        return strings;
    }

    /**
     * @return the activityName
     */
    public String getActivityName() {
        return activityName;
    }

    /**
     * @param activityName the activityName to set
     */
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    /**
     * @return the versions
     */
    public ArrayList<ClearCaseVersion> getVersions() {
        return versions;
    }

    /**
     * @param versions the versions to set
     */
    public void setVersions(ArrayList<ClearCaseVersion> files) {
        this.versions = files;
    }
    
    public void addVersion(ClearCaseVersion version) {
        versions.add(version);
    }
    /**
     * @return the activityHeadline
     */
    public String getActivityHeadline() {
        return activityHeadline;
    }

    /**
     * @param activityHeadline the activityHeadline to set
     */
    public void setActivityHeadline(String activityHeadline) {
        this.activityHeadline = activityHeadline;
    }
}
