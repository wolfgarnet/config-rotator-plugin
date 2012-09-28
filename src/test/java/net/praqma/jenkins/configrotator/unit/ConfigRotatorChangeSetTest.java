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
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import junit.framework.TestCase;
import net.praqma.jenkins.configrotator.ConfigurationRotatorBuildAction;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogEntry;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogParser;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogSet;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorVersion;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.*;
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
    public void testDAOChangeLogItems() {
        ClearCaseActivity cca = new ClearCaseActivity();
        cca.setActivityName("TestActivity");
                
        ClearCaseActivity cca2 = new ClearCaseActivity("TestActivity");
        
        assertTrue(cca.equals(cca2));
        cca.setVersions(new ArrayList<ConfigRotatorVersion>());
        cca.addVersion(new ConfigRotatorVersion("Test","Test","Test"));
        
        
        assertEquals(1, cca.getVersions().size());
        
        assertEquals("Test", cca.getVersions().get(0).getFile());
        assertEquals("Test", cca.getVersions().get(0).getName());
        assertEquals("Test", cca.getVersions().get(0).getUser());
        
        ClearCaseActivity ccaTwo = new ClearCaseActivity("Test", "TestAuthorRemodify");
        ccaTwo.setAuthor("TestAuthor");
        assertTrue(ccaTwo.getAuthor().equals("TestAuthor"));
        
        assertFalse(ccaTwo.equals("FalseString"));
        
        
    }
    
    @Test
    public void testConfigRotatorEntry() {
        ClearCaseActivity cca = new ClearCaseActivity();
        cca.setActivityName("TestActivity");
                
        ClearCaseActivity cca2 = new ClearCaseActivity("TestActivity");
        assertEquals(cca2.getActivityName(),"TestActivity");
        
        cca2.setAuthor("TestAuthor");
        assertEquals(cca2.getAuthor(), "TestAuthor");
        
        ClearCaseActivity cca3 = new ClearCaseActivity("TestActivity");
        assertTrue(cca2.equals(cca3));
        
        
        
        cca.setVersions(new ArrayList<ConfigRotatorVersion>());
        cca.addVersion(new ConfigRotatorVersion("Test","Test","Test"));



        ConfigRotatorChangeLogEntry ccucroe = new ConfigRotatorChangeLogEntry();
        
        assertNull(ccucroe.getCommitMessage());
      
        ccucroe.setVersions(cca.getVersions());
        assertEquals("ClearCase UCM ConfigRotator Change",ccucroe.getMsg());
        
        assertEquals(1, ccucroe.getAffectedPaths().size());
        ccucroe.addVersion(new ConfigRotatorVersion("Test2", "Test2", "Test2"));
        assertEquals(2, ccucroe.getAffectedPaths().size());      
    }
    
    
    @Test
    public void testChangeLog() throws IOException, SAXException {
        ConfigRotatorChangeLogParser parser = new ConfigRotatorChangeLogParser();
        ConfigRotatorChangeLogParser spy = Mockito.spy(parser);
        
        //Load the provided changelog
        InputStream is = this.getClass().getResourceAsStream("changelog_1.xml");
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
        ConfigRotatorChangeLogSet<ConfigRotatorChangeLogEntry> set = (ConfigRotatorChangeLogSet<ConfigRotatorChangeLogEntry>)entry;
       
        System.out.println("PARENT: "+set.getEntries().get(0).getParent());
        assertEquals(set.getEntries().get(0).getParent(),null);
        
        //TODO: WHY ARE THESE NULL AFTERWARDS? Keeping test for later fix
        ConfigRotatorChangeLogEntry entri = set.getEntries().get(0);
        //entri.setParent(set);
        //assertEquals(entri.getParent(),set);
       
        assertEquals(2, set.getEntries().size());
        set.getEntries().add(new ConfigRotatorChangeLogEntry());
        assertEquals(3, set.getEntries().size());
        
        entri.setAuthor("EntriAuthor");
       
        assertTrue(f.delete());
        assertFalse(entry.isEmptySet());   
    }
}
