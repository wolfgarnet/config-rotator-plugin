/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import java.util.Collection;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorEntry;

/**
 *
 * @author Praqma
 */
public class ClearCaseUCMConfigRotatorEntry extends ConfigRotatorEntry {
    
    private String owner;
    private String componentChange;
    private String date;

    /**
     * Default constructor
     */
    public ClearCaseUCMConfigRotatorEntry() {
        
    }
    
    @Override
    public String getMsg() {
        return "ClearCase UCM ConfigRotator Change";
    }

    @Override
    public Collection<String> getAffectedPaths() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return the componentChange
     */
    public String getComponentChange() {
        return componentChange;
    }

    /**
     * @param componentChange the componentChange to set
     */
    public void setComponentChange(String componentChange) {
        this.componentChange = componentChange;
    }

    @Override
    public String toString() {
        return String.format("Owner: %s CompoentChange: %s Date: %s", owner,componentChange,date);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClearCaseUCMConfigRotatorEntry)) {
            return false;
        }
        
        ClearCaseUCMConfigRotatorEntry comp = (ClearCaseUCMConfigRotatorEntry)obj;
        
        return (comp.getOwner().equals(getOwner()) && comp.getComponentChange().equals(getComponentChange()) && comp.getDate().equals(getDate()));
        
    }
    
}
