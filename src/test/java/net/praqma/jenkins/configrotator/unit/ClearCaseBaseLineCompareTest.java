/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.unit;

import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import java.io.IOException;
import java.util.ArrayList;
import junit.framework.TestCase;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseActivity;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseGetBaseLineCompare;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

/**
 *
 * @author Praqma
 */
public class ClearCaseBaseLineCompareTest extends TestCase {
    
    BuildListener buildlistener;
    ClearCaseGetBaseLineCompare compare;
    VirtualChannel channel;
    ClearCaseUCMConfiguration confone;
    ClearCaseUCMConfiguration conftwo = new ClearCaseUCMConfiguration();
    
    
    @Before
	public void initialize() throws IOException, InterruptedException, ConfigurationRotatorException {
        channel = PowerMockito.mock(VirtualChannel.class);
        confone = PowerMockito.mock(ClearCaseUCMConfiguration.class);
        buildlistener = PowerMockito.mock(BuildListener.class);
        compare = new ClearCaseGetBaseLineCompare(buildlistener, confone, conftwo);
        PowerMockito.when(confone.difference(Mockito.any(ClearCaseUCMConfiguration.class))).thenReturn(new ArrayList<ClearCaseActivity>());
    }
    
    
    /**
     * TODO:IMPLEMENT ME There need to be at least one test here, but
     * we did not finish the test in this version. will be done later.
     */
    @Test
    public void testClearCaseBaseLineCompare() {
        try {
            assertTrue(true);
        } catch (Exception ex) {
            System.out.println(ex);
            fail();
        }
    }
}