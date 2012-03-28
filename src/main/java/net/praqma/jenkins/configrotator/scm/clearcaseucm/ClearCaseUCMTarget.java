package net.praqma.jenkins.configrotator.scm.clearcaseucm;

public class ClearCaseUCMTarget {

	private String component;
	private boolean change;

	public String getComponent() {
		return component;
	}

	public void setComponent( String component ) {
		this.component = component;
	}

	public boolean isChange() {
		return change;
	}

	public void setChange( boolean change ) {
		this.change = change;
	}
	
}
