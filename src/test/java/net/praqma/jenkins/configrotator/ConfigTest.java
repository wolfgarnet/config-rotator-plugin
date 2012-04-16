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
  
  // Controls how many seconds a test as minimum takes by
  // waiting before asserting on the test.
  Integer watingSeconds = 10;
  // A time stamp added to ClearCase Vob names to make them unique for each
  // test. They also include the test name.
  // Division by 60000, giving milis to minute precission asuming all tests do
  // not complete within a minute!
  String uniqueTimeStamp = "" + System.currentTimeMillis()/60000;

  // Note a test must include the string "test" somehow, else 
  // surefire will not find the test-method.
  @Test
	public void testJustGetBuildAction() throws Exception {
    String testName = "testJustGetBuildAction";
    String debugLine = "'" + testName + "': ";
    System.out.println( debugLine + "Starting" );
    // ONLY alphanumeric chars
		String uniqueTestVobName = testName + uniqueTimeStamp;
    
    // set up cool to run tests with ClearCase environment
    // variables overwrite cool test case setup.xml setting
    // Unique names for each test is used to avoid all sort of clear case 
    // complications - but leaves as mess...
    coolTest.variables.put("vobname", uniqueTestVobName );
    coolTest.variables.put("pvobname", uniqueTestVobName );
		coolTest.bootStrap();
		System.out.println( debugLine + "Cool test case setup done." );
		
    // create Jenkins job - also use unique name
		FreeStyleProject project = createFreeStyleProject( uniqueTestVobName );
		// Setup ClearCase UCM as SCM and to use with config-rotator
		ClearCaseUCM ccucm = new ClearCaseUCM( coolTest.getPVob().toString() );
		List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
		targets.add( new ClearCaseUCMTarget( "model-1@" + coolTest.getPVob() + ", INITIAL, false" ) );
		ccucm.targets = targets;
    // create config-rotator, and set it as SCM
		ConfigurationRotator cr = new ConfigurationRotator( ccucm, true );
		project.setScm( cr );
	
		// schedule a build, just to see if the config-rotator setup works
		FreeStyleBuild b = project.scheduleBuild2( 0 ).get();
		
    // now investigate result and print debug out
		System.out.println( debugLine + "Workspace: " + b.getWorkspace() );
		// get build actions
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
    // NOTICE - this is very IMPORTANT to avoid Jenkins error on cleaning 
    // temporary dirs after jobs completes meaning test fails
    br.close();
		
    // waiting is important to ensure unique timestamps and let Jenkins clean
    // workspace after each test
    waiting(watingSeconds);
        
    // Build action should not be null
		assertNotNull( action );
	}
  
  // Note a test must include the string "test" somehow, else 
  // surefire will not find the test-method.
	@Test
	public void testManualIterateThroughAllBaselines() throws Exception {
    String testName = "ManualIterateThroughAllBaselines";
    String debugLine = "'" + testName + "': ";
    System.out.println( debugLine + "Starting" );
    // ONLY alphanumeric chars
		String uniqueTestVobName = testName + uniqueTimeStamp;
    
    // set up cool to run tests with ClearCase environment
    // variables overwrite cool test case setup.xml setting
    // Unique names for each test is used to avoid all sort of clear case 
    // complications - but leaves as mess...
    coolTest.variables.put("vobname", uniqueTestVobName );
    coolTest.variables.put("pvobname", uniqueTestVobName );
		coolTest.bootStrap();
		System.out.println( debugLine + "Cool test case setup done." );
		
    // create Jenkins job - also use unique name
		FreeStyleProject project = createFreeStyleProject( uniqueTestVobName );
		// Setup ClearCase UCM as SCM and to use with config-rotator
		ClearCaseUCM ccucm = new ClearCaseUCM( coolTest.getPVob().toString() );
		List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
		targets.add( new ClearCaseUCMTarget( "model-1@" + coolTest.getPVob() + ", INITIAL, false" ) ); // prøv forkert navn
    targets.add( new ClearCaseUCMTarget( "client-1@" + coolTest.getPVob() + ", INITIAL, false" ) );
		ccucm.targets = targets;
    // create config-rotator, and set it as SCM
		ConfigurationRotator cr = new ConfigurationRotator( ccucm, true );
		project.setScm( cr );
	
		// schedule a build, just to see if the config-rotator setup works
		FreeStyleBuild b = project.scheduleBuild2( 0 ).get();
		
    // now investigate result and print debug out
		System.out.println( debugLine + "Workspace: " + b.getWorkspace() );
		// get build actions
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
    // NOTICE - this is very IMPORTANT to avoid Jenkins error on cleaning 
    // temporary dirs after jobs completes meaning test fails
    br.close();
		
    // waiting is important to ensure unique timestamps and let Jenkins clean
    // workspace after each test
    waiting(watingSeconds);
        
    // Build action should not be null
		assertNotNull( action );
	}
    

  // busy wait....
  private static void waiting (int seconds){
        
        long t0, t1;

        t0 =  System.currentTimeMillis();

        do{
            t1 = System.currentTimeMillis();
        }
        while ((t1 - t0) < (seconds * 1000));
    }
}
