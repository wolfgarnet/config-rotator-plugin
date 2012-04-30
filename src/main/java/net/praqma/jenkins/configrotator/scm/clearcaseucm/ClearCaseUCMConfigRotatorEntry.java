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

    /**
     * Default constructor
     */
    public ClearCaseUCMConfigRotatorEntry() {
        
    }
    
    @Override
    public String getMsg() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<String> getAffectedPaths() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
