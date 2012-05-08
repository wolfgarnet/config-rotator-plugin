/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import java.io.Serializable;

/**
 *
 * @author Praqma
 */
public class ClearCaseVersion implements Serializable {
    private String file;
    private String name;
    private String user;
    
    public ClearCaseVersion() {
        
    }

    public ClearCaseVersion(String file, String name, String user) {
        this.file = file;
        this.name = name;
        this.user = user;
    }
    
    /**
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return String.format("Version ( %s, %s, %s )", getFile(), getUser(), getName());
    }
    
    
    
}
