package net.praqma.jenkins.configrotator.scm;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;
import java.util.ArrayList;
import java.util.List;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfigRotatorEntry;

/**
 *
 * @author Praqma
 */
public abstract class ConfigRotatorChangeLogSet<T extends ConfigRotatorEntry> extends ChangeLogSet<T> {
    
    
    protected List<T> entries;
    
    public ConfigRotatorChangeLogSet(AbstractBuild<?,?> build) {
        super(build);
        entries = new ArrayList<T>();
    }

    @Override
    public boolean isEmptySet() {
        return entries.isEmpty();
    }
    
    /**
     * Adds the entry to the changelogset
     * @param entry 
     */
    public void add(T entry) {
        entries.add(entry);
    }

    public List<T> getEntries() {
        return entries;
    }
   
}
