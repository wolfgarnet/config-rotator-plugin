package net.praqma.jenkins.configrotator;

import java.io.Serializable;

/**
 * Abstract class defining one component of a configuration
 * 
 * @author wolfgang
 *
 */
public abstract class AbstractConfigurationComponent implements Serializable {
	protected boolean changedLast = false;
	
	public boolean isChangedLast() {
		return changedLast;
	}
	
	public void setChangedLast( boolean b ) {
		this.changedLast = b;
	}
}
