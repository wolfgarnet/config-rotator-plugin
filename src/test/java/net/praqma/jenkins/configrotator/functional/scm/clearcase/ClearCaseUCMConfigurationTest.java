package net.praqma.jenkins.configrotator.functional.scm.clearcase;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import junit.framework.TestCase;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.jenkins.configrotator.functional.scm.clearcaseucm.ClearCaseUCM;
import net.praqma.jenkins.configrotator.functional.scm.clearcaseucm.ClearCaseUCMConfiguration;
import net.praqma.jenkins.configrotator.functional.scm.clearcaseucm.ClearCaseUCMConfigurationComponent;
import net.praqma.jenkins.configrotator.functional.scm.clearcaseucm.ClearCaseUCMTarget;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

/**
 *
 * @author Praqma
 */
public class ClearCaseUCMConfigurationTest extends TestCase {
    
    static {
		Appender appender = new ConsoleAppender();
		appender.setMinimumLevel( Logger.LogLevel.DEBUG );
		Logger.addAppender( appender );
	}
	
	/* Typical jenkins objects */
	AbstractProject<?, ?> project;
	AbstractBuild<?, ?> build;
	Launcher launcher;
	TaskListener tasklistener;
	BuildListener buildlistener;
	FilePath workspace = new FilePath( new File( "" ) );
	
	@Before
	public void initialize() {
		project = Mockito.mock( FreeStyleProject.class );
		build = PowerMockito.mock( FreeStyleBuild.class );
		launcher = Mockito.mock( Launcher.class );
		tasklistener = Mockito.mock( TaskListener.class );
		buildlistener = Mockito.mock( BuildListener.class );
		
		/* Behaviour */
		Mockito.when( tasklistener.getLogger() ).thenReturn( System.out );
		Mockito.when( buildlistener.getLogger() ).thenReturn( System.out );
	}
    
    @Test 
    public void testClearCaseConfigurationGetChangedComponentMethods() throws ConfigurationRotatorException, IOException, UnableToInitializeEntityException {
        ClearCaseUCM ccucm = new ClearCaseUCM( "" );
        List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
		targets.add( new ClearCaseUCMTarget( "bl1@\\pvob", Project.PromotionLevel.INITIAL, false ) );
        targets.add( new ClearCaseUCMTarget("bl2@\\pvob", Project.PromotionLevel.INITIAL, false ) );
		ccucm.targets = targets;
		ClearCaseUCM spy = Mockito.spy( ccucm );
        
        Integer expectedIndex = -1; 
        
        List<ClearCaseUCMConfigurationComponent> comps = new ArrayList<ClearCaseUCMConfigurationComponent>();
        
        ClearCaseUCMConfigurationComponent comp = new ClearCaseUCMConfigurationComponent(Baseline.get("bl1@\\pvob"), Project.PromotionLevel.INITIAL, false);
        ClearCaseUCMConfigurationComponent comp2 = new ClearCaseUCMConfigurationComponent(Baseline.get("bl1@\\pvob"), Project.PromotionLevel.INITIAL, false);
        
        comps.add(comp);
        comps.add(comp2);

        net.praqma.jenkins.configrotator.functional.scm.clearcaseucm.ClearCaseUCMConfiguration ccc = new net.praqma.jenkins.configrotator.functional.scm.clearcaseucm.ClearCaseUCMConfiguration( comps );
               
        net.praqma.jenkins.configrotator.functional.scm.clearcaseucm.ClearCaseUCMConfiguration cccSpy = Mockito.spy(ccc);
        
        Mockito.doReturn(comps).when(cccSpy).getList();
        
        Mockito.doReturn(expectedIndex).when(cccSpy).getChangedComponentIndex();
        
        Integer indexZero = 0;
        ccc.getList().get(0).setChangedLast(true);
        Mockito.doReturn(indexZero).when(cccSpy).getChangedComponentIndex();
        
        ClearCaseUCMConfigurationComponent compIndexOne = cccSpy.getList().get(0);
        
        //Assert that the object returned matches the changed component
        Mockito.doReturn(compIndexOne).when(cccSpy).getChangedComponent();        
        
        ccc.getList().get(0).setChangedLast(false);
        Mockito.doReturn(null).when(cccSpy).getChangedComponent();
     
    }
    
    @Test
    public void testGetHtml () {
        ClearCaseUCMConfiguration configuration = new ClearCaseUCMConfiguration();
        Assert.assertNotNull(configuration.toHtml());
    }
    
    public void testDifferenceFunctionn() throws UnableToInitializeEntityException {
        ClearCaseUCMConfiguration configuration = new ClearCaseUCMConfiguration();
        configuration.getList().add(new ClearCaseUCMConfigurationComponent(Baseline.get("bl1@\\pvob"), Project.PromotionLevel.INITIAL, false));
        configuration.getList().get(0).setChangedLast(false);
        
        ClearCaseUCMConfiguration configuration2 = new ClearCaseUCMConfiguration();
        configuration2.getList().add(new ClearCaseUCMConfigurationComponent(Baseline.get("bl2@\\pvob"), Project.PromotionLevel.INITIAL, false));
        
        //No differences
        
        boolean caught = false;
        try {
            Assert.assertEquals(0, configuration.difference(configuration2.getList().get( 0 ), null).size());
            
            configuration.getList().get(0).setChangedLast(true);
            
            configuration.difference(configuration2.getList().get( 0 ), null);
            
        } catch (ConfigurationRotatorException ex) {
            caught = true;
        }
        assertTrue(caught); 
    }  
}
