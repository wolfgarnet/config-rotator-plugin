package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import net.praqma.clearcase.ucm.entities.Project;
import org.kohsuke.stapler.DataBoundConstructor;

public class ClearCaseUCMTarget {

	private String component;
    private String baselineName;
    private Project.PromotionLevel level;
    private boolean fixed;

	public ClearCaseUCMTarget() {
		
	}
    
    /**
     * Warning: Only one databound constructor per component. Figured this out the hard way. 
     * @param component 
     */
    
	public ClearCaseUCMTarget( String component ) {
		this.component = component;
	}
    
    /**
     * New constructor. Builds a correct component string for backwards compatability.
     * @param baselineName
     * @param level
     * @param fixed 
     */
    @DataBoundConstructor
    public ClearCaseUCMTarget(String baselineName, Project.PromotionLevel level, boolean fixed) {
        this.component = baselineName+", "+level+", "+fixed;
        this.baselineName = baselineName;
        this.level = level;
        this.fixed = fixed;
    }
    

	public String getComponent() {
		return component;
	}
    
    public String getBaselineName () {
        return baselineName;
    }
    
    public void setBaselineName(String baselineName) {
        this.baselineName = baselineName;
    }
    
    public Project.PromotionLevel getLevel() {
        return level;
    }
    
    public void setLevel(Project.PromotionLevel level) {
        this.level = level;
    }
    
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }
    
    public boolean getFixed() {
        return fixed;
    }
             
	public void setComponent( String component ) {
		this.component = component;
	}
    
    @Override
	public String toString() {
		return String.format("%s",component);
	}
	
    @Override
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
