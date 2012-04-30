package net.praqma.jenkins.configrotator.scm;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Praqma
 */
public abstract class ConfigRotatorChangeLogSet<T extends ConfigRotatorEntry> extends ChangeLogSet<T> {
    
    
    protected List<? extends ConfigRotatorEntry> entries;
    
    public ConfigRotatorChangeLogSet(AbstractBuild<?,?> build) {
        super(build);
        entries = new ArrayList<ConfigRotatorEntry>();
    }

    @Override
    public boolean isEmptySet() {
        return entries.isEmpty();
    }
    
    
}
