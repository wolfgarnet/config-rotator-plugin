/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.unit;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import java.io.*;
import java.util.logging.Level;
import junit.framework.TestCase;
import net.praqma.jenkins.configrotator.ConfigurationRotatorBuildAction;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeSetDescriptor;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfigRotatorChangeLogParser;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfigRotatorChangeLogSet;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfigRotatorEntry;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfiguration;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.xml.sax.SAXException;

/**
 *
 * @author Praqma
 */
public class ConfigRotatorChangeSetTest extends TestCase {
    
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
    //ClearCaseUCMConfiguration conf = new ClearCaseUCMConfiguration();
    
	
	@Before
	public void initialize() {
		project = Mockito.mock( FreeStyleProject.class );
		build = PowerMockito.mock( FreeStyleBuild.class );
		launcher = Mockito.mock( Launcher.class );
		tasklistener = Mockito.mock( TaskListener.class );
		buildlistener = Mockito.mock( BuildListener.class );
        //conf = Mockito.mock(ClearCaseUCMConfiguration.class);
		
		/* Behaviour */
		Mockito.when( tasklistener.getLogger() ).thenReturn( System.out );
		Mockito.when( buildlistener.getLogger() ).thenReturn( System.out );
        //Mockito.when( build.getAction(ConfigurationRotatorBuildAction.class)).thenReturn( new ConfigurationRotatorBuildAction(build, null, conf) );
	}
    
    
    @Test
    public void testChangeSetAdditions() {
        
    }
    
    @Test
    public void testChangeLog() throws IOException, SAXException {
        ClearCaseUCMConfigRotatorChangeLogParser parser = new ClearCaseUCMConfigRotatorChangeLogParser();
        ClearCaseUCMConfigRotatorChangeLogParser spy = Mockito.spy(parser);
        
        //Load the provided changelog
        InputStream is = this.getClass().getResourceAsStream("changelog.xml");
        assertNotNull(is);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        
        File f = File.createTempFile("changeTest", ".xml");
        FileWriter fw = new FileWriter(f);
        
        String line;
        while((line = br.readLine()) != null ) {
            System.out.println("Read line: "+line);
            fw.write(line+System.getProperty("line.separator"));
        }
        
        fw.close();
        
        ChangeLogSet<? extends Entry> entry = parser.parse(build, f);
        
        assertTrue(f.delete());
        assertFalse(entry.isEmptySet());    
        
        //<owner>TestOwner</owner>
        //<componentChange>E:\jenkins-slave-mads\workspace\ChangeLogTest\view\chw_PVOB\CR-1\cr1.h</componentChange>
        //<date>Wed May 02 14:31:17 CEST 2012</date>
        
        //Create the above item. We want to assert that the two changeset objects are equal.
        ClearCaseUCMConfigRotatorEntry item = new ClearCaseUCMConfigRotatorEntry();
        item.setOwner("TestOwner");
        item.setComponentChange("E:\\jenkins-slave-mads\\workspace\\ChangeLogTest\\view\\chw_PVOB\\CR-1\\cr1.h");
        item.setDate("Wed May 02 14:31:17 CEST 2012");
        
        ClearCaseUCMConfigRotatorEntry parsedItem = ((ClearCaseUCMConfigRotatorEntry)entry.getItems()[0]);
        assertTrue(parsedItem.equals(item));        
        
        //The purpose of this test is to test that the changelog is parsed correctly, So we dont care about the other parts
        ConfigRotatorChangeSetDescriptor descriptor = (ConfigRotatorChangeSetDescriptor)entry;
        assertTrue(descriptor.getHeadline().equals(ClearCaseUCMConfigRotatorChangeLogSet.CONF_ERROR));
    }
}
