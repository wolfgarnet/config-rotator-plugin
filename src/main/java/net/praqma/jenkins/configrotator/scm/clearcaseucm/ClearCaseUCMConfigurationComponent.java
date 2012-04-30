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
		this.baseline = Baseline.get( baseline ).load();
		this.plevel = Project.PromotionLevel.valueOf( plevel );
		if( fixed.equalsIgnoreCase( "manual" ) || fixed.matches( "^\\s*$" ) || fixed.matches( "^(?i)fixed*$" ) || fixed.matches( "^(?i)true*$" ) ) {
			this.fixed = true;
		} else {
			this.fixed = false;
		}
	}
    
	@Override
	public ClearCaseUCMConfigurationComponent clone() {
		ClearCaseUCMConfigurationComponent cc = new ClearCaseUCMConfigurationComponent( this.baseline, this.plevel, this.fixed );
		
		return cc;
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
	
    @Override
	public String toString() {
		return baseline.getNormalizedName() + "@" + plevel + "(" + fixed + ")";
	}
	
	@Override
	public boolean equals( Object other ) {
		if( other == this ) {
			return true;
		}
		
		if( other instanceof ClearCaseUCMConfigurationComponent ) {
			ClearCaseUCMConfigurationComponent o = (ClearCaseUCMConfigurationComponent)other;

			return ( o.baseline.equals( baseline ) && ( o.plevel.equals( plevel ) ) && ( o.isFixed() == fixed ) );
		} else {
			return false;
		}
	}
    
    /**
     * Test method...
     * 
     * @return 
     */
    public String toHtml() {
        StringBuilder builder = new StringBuilder();
        builder.append("<tr>");
        builder.append("<td>").append(baseline.getComponent().getNormalizedName()).append("</td>");
        builder.append("<td>").append(baseline.getStream().getNormalizedName()).append("</td>");
        builder.append("<td>").append(baseline.getNormalizedName()).append("</td>");
        builder.append("<td>").append(plevel.toString()).append("</td>");
        builder.append("<td>").append(fixed).append("</td>").append("</tr>");
        return builder.toString();
    }
    
    
}

/*
                 <table>
                 <tr>
                 	<td style="font-weight:bold">Component</td>
                 	<td style="font-weight:bold">Stream</td>
                 	<td style="font-weight:bold">Baseline</td>
                 	<td style="font-weight:bold">Level</td>
                 	<td style="font-weight:bold">Fixed</td>
                 </tr>
            	<j:forEach var="component" items="${it.getList()}">
            	
                       <!-- <b>${component.getBaseline().getComponent()}, ${component.getBaseline().getStream()}, ${component.getBaseline()}</b>, ${component.getPlevel()}, <j:if test="${component.isFixed()}">Fixed</j:if><j:if test="${!component.isFixed()}">Auto</j:if><br /> -->
                       
				<tr>
                 	<td style="padding-right:10px">${component.getBaseline().getComponent().getNormalizedName()}</td>
                 	<td style="padding-right:10px">${component.getBaseline().getStream().getNormalizedName()}</td>
                 	<td style="padding-right:10px">${component.getBaseline().getNormalizedName()}</td>
                 	<td style="padding-right:10px">${component.getPlevel()}</td>
                 	<td>${component.isFixed()}</td>
                 </tr>
                 * 
                 */