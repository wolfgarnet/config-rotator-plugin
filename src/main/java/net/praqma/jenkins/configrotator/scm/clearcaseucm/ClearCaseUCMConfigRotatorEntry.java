/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.model.User;
import java.util.ArrayList;
import java.util.Collection;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorEntry;

/**
 *
 * @author Praqma
 */
public class ClearCaseUCMConfigRotatorEntry extends ConfigRotatorEntry {
    
    private String activityName;
    private String author;
    private ArrayList<ClearCaseVersion> versions;

    /**
     * Default constructor
     */
    public ClearCaseUCMConfigRotatorEntry() {
        versions = new ArrayList<ClearCaseVersion>();
    }
    
    @Override
    public String getMsg() {
        return "ClearCase UCM ConfigRotator Change";
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
    
    @Override
	public User getAuthor() {
		if( author == null ) {
			return User.getUnknown();
		}
		return User.get( author );
	}
    
    @Override
    public void setAuthor(String author) {
        this.author = author;
    }
}
