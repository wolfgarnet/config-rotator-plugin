package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import org.kohsuke.stapler.DataBoundConstructor;

public class ClearCaseUCMTarget {

	private String component;

	public ClearCaseUCMTarget() {
		
	}
	
	@DataBoundConstructor
	public ClearCaseUCMTarget( String component ) {
		this.component = component;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent( String component ) {
		this.component = component;
	}

	public String toString() {
		return component;
	}
	
	public boolean equals( Object other ) {
		if( other == this ) {
			return true;
		}
		
		if( other instanceof ClearCaseUCMTarget ) {
			ClearCaseUCMTarget o = (ClearCaseUCMTarget)other;
			
			return component.equals( o.getComponent() );
		} else {
			return false;
		}
	}
}
