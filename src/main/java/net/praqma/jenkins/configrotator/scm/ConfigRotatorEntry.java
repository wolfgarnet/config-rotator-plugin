/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm;

import hudson.model.User;
import hudson.scm.ChangeLogSet.Entry;


/**
 *
 * @author Praqma
 */
public abstract class ConfigRotatorEntry extends Entry {
    protected ConfigRotatorChangeLogSet parent;
    protected String author;
    
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
