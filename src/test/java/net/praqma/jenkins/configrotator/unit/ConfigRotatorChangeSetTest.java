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
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfigRotatorChangeLogParser;
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
    ClearCaseUCMConfiguration conf = new ClearCaseUCMConfiguration();
    
	
	@Before
	public void initialize() {
		project = Mockito.mock( FreeStyleProject.class );
		build = PowerMockito.mock( FreeStyleBuild.class );
		launcher = Mockito.mock( Launcher.class );
		tasklistener = Mockito.mock( TaskListener.class );
		buildlistener = Mockito.mock( BuildListener.class );
        conf = Mockito.mock(ClearCaseUCMConfiguration.class);
		
		/* Behaviour */
		Mockito.when( tasklistener.getLogger() ).thenReturn( System.out );
		Mockito.when( buildlistener.getLogger() ).thenReturn( System.out );
        //Mockito.when( build.getAction(AbstractBuild.class))).thenR
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
        System.out.println("STARTING TO PARSE");
        
        //Mockito.doReturn(tasklistener).
        //ChangeLogSet<? extends Entry> entry = parser.parse(build, f);
        System.out.println("FINISHED PARSE");
        System.out.println("File is: "+f.getAbsolutePath());
        assertTrue(f.delete());
        //assertFalse(entry.isEmptySet());    
    }
}
