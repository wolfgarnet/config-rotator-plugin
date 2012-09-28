/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm;

import hudson.model.User;
import hudson.scm.ChangeLogSet.Entry;

import java.util.ArrayList;
import java.util.Collection;


/**
 *
 * @author Praqma
 */
public class ConfigRotatorChangeLogEntry extends Entry {
    protected ConfigRotatorChangeLogSet parent;
    private String commitMessage;
    private String author;
    private ArrayList<ConfigRotatorVersion> versions;

    /**
     * Default constructor
     */
    public ConfigRotatorChangeLogEntry() {
        versions = new ArrayList<ConfigRotatorVersion>();
    }

    public ConfigRotatorChangeLogEntry( String commitMessage, String author, ArrayList<ConfigRotatorVersion> versions ) {
        this.commitMessage = commitMessage;
        this.author = author;
        this.versions = versions;
    }

    @Override
    public String getMsg() {
        return "Changes";
    }

    @Override
    public Collection<String> getAffectedPaths() {
        ArrayList<String> strings = new ArrayList<String>();
        for( ConfigRotatorVersion ccv : getVersions() ) {
            strings.add( ccv.getFile() );
        }
        return strings;
    }

    /**
     * @return the activityName
     */
    public String getCommitMessage() {
        return commitMessage;
    }


    public void setCommitMessage( String commitMessage ) {
        this.commitMessage = commitMessage;
    }

    public ArrayList<ConfigRotatorVersion> getVersions() {
        return versions;
    }


    public void setVersions( ArrayList<ConfigRotatorVersion> versions ) {
        this.versions = versions;
    }

    public void addVersion( ConfigRotatorVersion version ) {
        versions.add( version );
    }
    
    @Override
	public User getAuthor() {
		if( author == null ) {
			return User.getUnknown();
		}
		return User.get( author );
	}
    
    public void setAuthor(String author) {
        this.author = author;
    }
     
    public <T extends ConfigRotatorChangeLogSet> void setParent(T t) {
        this.parent = t;
    }
    
    public <T extends ConfigRotatorChangeLogSet>  T getParent(Class<T> type) {
        return (T)parent;
    }
}
