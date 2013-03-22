package net.praqma.jenkins.configrotator;

import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCM;

/**
 * @author cwolfgang
 */
public class ClearCaseUCMBuilder {

    private ClearCaseUCM ccucm;
    private String pvob;

    public ClearCaseUCMBuilder( String pvob ) {
        ccucm = new ClearCaseUCM( pvob );
        this.pvob = pvob;
    }

    public ClearCaseUCMBuilder addBuilder() {
        //ccucm.

        return this;
    }

}
