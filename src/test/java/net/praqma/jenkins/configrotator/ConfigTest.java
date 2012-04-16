package net.praqma.jenkins.configrotator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import org.junit.Test;

import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCM;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfiguration;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMTarget;
import net.praqma.jenkins.utils.test.ClearCaseJenkinsTestCase;

public class ConfigTest extends ClearCaseJenkinsTestCase {

  @Test
	public void test1() throws Exception {
		
		System.out.println( "I AMMMMM HEHREHHREHEHRHEHHERHHERHE" );
		
    String uniqueTestVobName = ""; //System.getenv("BUILD_TAG");
    if (uniqueTestVobName.isEmpty())
    {// NOT running under jenkins!
      uniqueTestVobName = "configrotatorplugintest1" + (System.currentTimeMillis()/1000);
    }
    // else using name telling it is a Jenkins job
    
    coolTest.variables.put("vobname", uniqueTestVobName );
    coolTest.variables.put("pvobname", uniqueTestVobName );
		coolTest.bootStrap();
    		
		System.out.println( "AFTER" );
		
		FreeStyleProject project = createFreeStyleProject( "configrotator" );
		
		/*  */
		ClearCaseUCM ccucm = new ClearCaseUCM( coolTest.getPVob().toString() );
		List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
		targets.add( new ClearCaseUCMTarget( "model-1@" + coolTest.getPVob() + ", INITIAL, false" ) );
		ccucm.targets = targets;
		
		ConfigurationRotator cr = new ConfigurationRotator( ccucm, true );
		
		project.setScm( cr );
		
		
		FreeStyleBuild b = project.scheduleBuild2( 0 ).get();
		
		System.out.println( "Workspace: " + b.getWorkspace() );
		
		ConfigurationRotatorBuildAction action = b.getAction( ConfigurationRotatorBuildAction.class );
		
		System.out.println( "Action: " + action );
		System.out.println( "Logfile: " + b.getLogFile() );
		
		BufferedReader br = new BufferedReader( new FileReader( b.getLogFile() ) );
		String line = "";
		while( ( line = br.readLine() ) != null ) {
			System.out.println( "[JENKINS] " + line );
		}
		
		if( action != null ) {
			System.out.println( "Action: " + action.getResult() );
		} else {
			System.out.println( "ACTION IS NULL" );
		}
		
		assertNotNull( action );
	}
  
  
  @Test
	public void manualIterateThroughAllBaselines() throws Exception {
		
		System.out.println( "Started test 'manualIterateThroughAllBaselines'" );
		
    String uniqueTestVobName = "configrotatorplugintest2" + (System.currentTimeMillis()/10000);
    
    coolTest.variables.put("vobname", uniqueTestVobName );
    coolTest.variables.put("pvobname", uniqueTestVobName );
		coolTest.bootStrap();
		System.out.println( "coolTest.bootStrap done." );
		
		FreeStyleProject project = createFreeStyleProject( "configrotator" );
		
		/*  */
		ClearCaseUCM ccucm = new ClearCaseUCM( coolTest.getPVob().toString() );
		List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
		targets.add( new ClearCaseUCMTarget( "model-1@" + coolTest.getPVob() + ", INITIAL, false" ) ); // prøv forkert navn
    targets.add( new ClearCaseUCMTarget( "client-1@" + coolTest.getPVob() + ", INITIAL, false" ) );
		ccucm.targets = targets;
		
		ConfigurationRotator cr = new ConfigurationRotator( ccucm, true );
		project.setScm( cr );
		
    // scheduling a build should get new baseline for model: model-2
		FreeStyleBuild b = project.scheduleBuild2( 0 ).get();
		
    System.out.println( "Workspace: " + b.getWorkspace() );
		ConfigurationRotatorBuildAction action = b.getAction( ConfigurationRotatorBuildAction.class );
		System.out.println( "Action: " + action );
		System.out.println( "Logfile: " + b.getLogFile() );
    
    BufferedReader br = new BufferedReader( new FileReader( b.getLogFile() ) );
		String line = "";
		while( ( line = br.readLine() ) != null ) {
			System.out.println( "[JENKINS] " + line );
		}
		
    // this test plan to iterate one baseline at a time
    // ... for now, just printing stuff out to se what I get
		if( action != null ) {
			System.out.println( "Action: " + action.getResult() );
      ClearCaseUCMConfiguration test = (ClearCaseUCMConfiguration) action.getConfiguration();
      System.out.println( "getShortname()" + test.getList().get(0).getBaseline().getShortname());
      System.out.println( "getComment()" + test.getList().get(0).getBaseline().getComment());
      System.out.println( "getPVob()" + test.getList().get(0).getBaseline().getPVob());
		} else {
			System.out.println( "ACTION IS NULL" );
		}
		assertNotNull( action );
    
	}
}
