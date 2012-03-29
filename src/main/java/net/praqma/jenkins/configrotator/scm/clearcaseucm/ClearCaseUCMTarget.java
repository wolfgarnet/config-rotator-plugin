package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import org.kohsuke.stapler.DataBoundConstructor;

public class ClearCaseUCMTarget {

	private String component;
	private boolean change;
	
	public ClearCaseUCMTarget() {
		
	}
	
	@DataBoundConstructor
	public ClearCaseUCMTarget( String component, boolean change ) {
		this.component = component;
		this.change = change;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent( String component ) {
		this.component = component;
	}

	public boolean doChange() {
		return change;
	}

	public void setChange( boolean change ) {
		this.change = change;
	}
	
	public String toString() {
		return component;
	}
}
