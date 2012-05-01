/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.model.AbstractBuild;
import java.util.Iterator;
import java.util.List;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogSet;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorEntry;

/**
 *
 * @author Praqma
 */
public class ClearCaseUCMConfigRotatorChangeLogSet extends ConfigRotatorChangeLogSet<ClearCaseUCMConfigRotatorEntry> {

    public ClearCaseUCMConfigRotatorChangeLogSet(AbstractBuild<?,?> build) {
        super(build);
    }
    
    public ClearCaseUCMConfigRotatorChangeLogSet(AbstractBuild<?,?> build, List<ClearCaseUCMConfigRotatorEntry> entries) {       
       super(build);
       this.entries = entries;
       for(ConfigRotatorEntry e : entries) {
           e.setParent(this);
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
