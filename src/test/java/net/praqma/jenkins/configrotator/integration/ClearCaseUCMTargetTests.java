/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.integration;

import junit.framework.TestCase;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMTarget;
import org.junit.Test;

/**
 *
 * @author Praqma
 * 
 * This class overrides the setup() and teardown() methods from ClearCaseJenkinsTestCase.
 * 
 * This test does not need the boot-strap. 
 */
public class ClearCaseUCMTargetTests extends TestCase {
    
    @Test
    public void testClearCaseUCMTargetBeforeAndAfter() {
        System.out.println("Beginning testClearCaseUCMTargetBeforeAndAfter");
        ClearCaseUCMTarget oldConstructor = new ClearCaseUCMTarget("model-2@\\ManualIterateThroughAllBaselines22248765, INITIAL, false");
        System.out.println("Before: "+oldConstructor);
        ClearCaseUCMTarget newConstructor = new ClearCaseUCMTarget("model-2@\\ManualIterateThroughAllBaselines22248765", Project.PromotionLevel.INITIAL, false);
        System.out.println("After: "+newConstructor);
        assertTrue(oldConstructor.equals(newConstructor));
    }
    
}
