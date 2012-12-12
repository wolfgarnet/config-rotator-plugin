/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Praqma
 */
public class ClearCaseActivity implements Serializable {
    private String activityName; 
    private String activityHeadline = "None";
    private String author;
    private ArrayList<ClearCaseVersion> versions = new ArrayList<ClearCaseVersion>();
    
    public ClearCaseActivity() {
        
    }
    
    public ClearCaseActivity(String activityName) {
        this.activityName = activityName;
    }
    
    public ClearCaseActivity(String activityName, String author) {
        this.activityName = activityName;
        this.author = author;
    }
    
    public ClearCaseActivity(String activityName, String author, String activityHeadline) {
        this.activityName = activityName;
        this.author = author;
        this.activityHeadline = activityHeadline;
    }
    
    public void addVersion(ClearCaseVersion ccv) {
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
    public ArrayList<ClearCaseVersion> getVersions() {
        return versions;
    }

    /**
     * @param versions the versions to set
     */
    public void setVersions(ArrayList<ClearCaseVersion> versions) {
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
