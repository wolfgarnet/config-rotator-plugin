package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.jenkins.configrotator.AbstractConfigurationComponent;

public class ClearCaseUCMConfigurationComponent extends AbstractConfigurationComponent {
	
	private Baseline baseline;
	private PromotionLevel plevel;
	private boolean fixed;
	
	public ClearCaseUCMConfigurationComponent( Baseline baseline, PromotionLevel plevel, boolean fixed ) {
		this.baseline = baseline;
		this.plevel = plevel;
		this.fixed = fixed;
	}
	
	public ClearCaseUCMConfigurationComponent( String baseline, String plevel, String fixed ) throws ClearCaseException {
		this.baseline = Baseline.get( baseline, false );
		this.plevel = Project.PromotionLevel.valueOf( plevel );
		if( fixed.equalsIgnoreCase( "manuel" ) || fixed.equalsIgnoreCase( "" ) ) {
			this.fixed = true;
		} else {
			this.fixed = false;
		}
	}
	
	public void setBaseline( Baseline baseline ) {
		this.baseline = baseline;
	}

	public Baseline getBaseline() {
		return baseline;
	}
	
	public PromotionLevel getPlevel() {
		return plevel;
	}
	
	public boolean isFixed() {
		return fixed;
	}
	
	public String toString() {
		return baseline.getNormalizedName() + "@" + plevel + "(" + fixed + ")";
	}
}
