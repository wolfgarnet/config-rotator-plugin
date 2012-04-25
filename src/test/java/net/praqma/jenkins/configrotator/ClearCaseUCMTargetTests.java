/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator;

import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMTarget;
import net.praqma.jenkins.utils.test.ClearCaseJenkinsTestCase;
import org.junit.Test;

/**
 *
 * @author Praqma
 * 
 * This class overrides the setup() and teardown() methods from ClearCaseJenkinsTestCase.
 * 
 * This test does not need the boot-strap. 
 */
public class ClearCaseUCMTargetTests extends ClearCaseJenkinsTestCase {

    @Override
    protected void setUp() throws Exception {
        
    }

    @Override
    public void tearDown() throws Exception {
        
    }
    
    @Test
    public void testClearCaseUCMTargetBeforeAndAfter() {
        ClearCaseUCMTarget oldConstructor = new ClearCaseUCMTarget("model-2@\\ManualIterateThroughAllBaselines22248765, INITIAL, false");
        ClearCaseUCMTarget newConstructor = new ClearCaseUCMTarget("model-2@\\ManualIterateThroughAllBaselines22248765", Project.PromotionLevel.INITIAL, false);
        
        assertTrue(oldConstructor.equals(newConstructor));
    }
    
}
