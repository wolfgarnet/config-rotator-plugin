/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm;
/**
 * Interface designed to ddescribe a change set. When you do a config rotate, we only flip one 'component'. Which includes a list of changed files.
 * 
 * This interface is used to provide a 'Summary' of what changed. 
 * 
 * @author Praqma
 */
public interface ConfigRotatorChangeSetDescriptor {
    public String getHeadline();
    public void setHeadline(String headline);
}
