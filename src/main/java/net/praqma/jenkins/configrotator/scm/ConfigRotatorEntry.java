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
    protected String user;
    
    @Override
	public User getAuthor() {
		if( user == null ) {
			return User.getUnknown();
		}
		return User.get( user );
	}
    
    public void setUser(String user) {
        this.user = user;
    }
     
    public <T extends ConfigRotatorChangeLogSet> void setParent(T t) {
        this.parent = t;
    }
    
    public <T extends ConfigRotatorChangeLogSet>  T getParent(Class<T> type) {
        return (T)parent;
    }
}
