package net.praqma.jenkins.configrotator;

import hudson.model.AbstractBuild;
import net.praqma.util.xml.feed.Entry;
import net.praqma.util.xml.feed.Feed;
import net.praqma.util.xml.feed.FeedException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

/**
 * Abstract class defining one component of a configuration
 * 
 * @author wolfgang
 *
 */
public abstract class AbstractConfigurationComponent implements Serializable {
	protected boolean changedLast = false;
    protected boolean fixed = false;

    public AbstractConfigurationComponent( boolean fixed ) {
        this.fixed = fixed;
    }

	public boolean isChangedLast() {
		return changedLast;
	}
	
	public void setChangedLast( boolean b ) {
		this.changedLast = b;
	}

    public boolean isFixed() {
        return fixed;
    }

    public abstract File getFeedFile( File path );

    public abstract Feed getFeed( File feedFile, String url, Date updated ) throws FeedException, IOException;

    public abstract Entry getFeedEntry( AbstractBuild<?, ?> build, Date updated );
}
