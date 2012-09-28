/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import net.praqma.jenkins.configrotator.scm.ConfigRotatorVersion;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Praqma
 */
public class ClearCaseActivity implements Serializable {
    private String activityName;
    private String author;
    private ArrayList<ConfigRotatorVersion> versions = new ArrayList<ConfigRotatorVersion>();
    
    public ClearCaseActivity() {
        
    }
    
    public ClearCaseActivity(String activityName) {
        this.activityName = activityName;
    }
    
    public ClearCaseActivity(String activityName, String author) {
        this.activityName = activityName;
        this.author = author;
    }
    
    public void addVersion(ConfigRotatorVersion ccv) {
        versions.add(ccv);
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
    public ArrayList<ConfigRotatorVersion> getVersions() {
        return versions;
    }

    /**
     * @param versions the versions to set
     */
    public void setVersions(ArrayList<ConfigRotatorVersion> versions) {
        this.versions = versions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClearCaseActivity) { 
            ClearCaseActivity cca = (ClearCaseActivity)obj;
            return getActivityName().equals(cca.getActivityName());
        }
        return false;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }
}
