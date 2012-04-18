package net.praqma.jenkins.configrotator;

import hudson.model.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


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
//  @Test
//	public void testJustGetBuildAction() throws Exception {
//		// This test is a first simple one, just making sure the plugin can be used
//		// and loaded, and ClearCaseUCM source control (Scm) can be used as
//		// config-rotator uses this.
//    String testName = "JustGetBuildAction";
//    String debugLine = "**************************************** '" + testName + "': ";
//    System.out.println( debugLine + "Starting" );
//    // ONLY alphanumeric chars
//		String uniqueTestVobName = testName + uniqueTimeStamp;
//    
//    // set up cool to run tests with ClearCase environment
//    // variables overwrite cool test case setup.xml setting
//    // Unique names for each test is used to avoid all sort of clear case 
//    // complications - but leaves as mess...
//    coolTest.variables.put("vobname", uniqueTestVobName );
//    coolTest.variables.put("pvobname", uniqueTestVobName );
//		coolTest.bootStrap();
//		System.out.println( debugLine + "Cool test case setup done." );
//		
//    // create Jenkins job - also use unique name
//		FreeStyleProject project = createFreeStyleProject( uniqueTestVobName );
//		// Setup ClearCase UCM as SCM and to use with config-rotator
//		ClearCaseUCM ccucm = new ClearCaseUCM( coolTest.getPVob().toString() );
//		List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
//		targets.add( new ClearCaseUCMTarget( "model-1@" + coolTest.getPVob() + ", INITIAL, false" ) );
//		ccucm.targets = targets;
//    // create config-rotator, and set it as SCM
//		ConfigurationRotator cr = new ConfigurationRotator( ccucm, true );
//		project.setScm( cr );
//	
//		// schedule a build, just to see if the config-rotator setup works
//		FreeStyleBuild b = project.scheduleBuild2( 0 ).get();
//		assertNotNull( b );
//		
//    // now investigate result and print debug out
//		System.out.println( debugLine + "Workspace: " + b.getWorkspace() );
//		// get build actions
//		ConfigurationRotatorBuildAction action = b.getAction( ConfigurationRotatorBuildAction.class );
//		
//		System.out.println( "Action: " + action );
//		System.out.println( "Logfile: " + b.getLogFile() );
//		
//		BufferedReader br = new BufferedReader( new FileReader( b.getLogFile() ) );
//		String line = "";
//		while( ( line = br.readLine() ) != null ) {
//			System.out.println( "[JENKINS] " + line );
//		}
//		
//		if( action != null ) {
//			System.out.println( "Action: " + action.getResult() );
//		} else {
//			System.out.println( "ACTION IS NULL" );
//		}
//    // NOTICE - this is very IMPORTANT to avoid Jenkins error on cleaning 
//    // temporary dirs after jobs completes meaning test fails
//    br.close();
//		
//    // waiting is important to ensure unique timestamps and let Jenkins clean
//    // workspace after each test
//    waiting(watingSeconds);
//        
//    // Build action should not be null
//		assertNotNull( action );
//	}
  
	
  // Note a test must include the string "test" somehow, else 
  // surefire will not find the test-method.
  @Test
	public void testAddRemoveTargets() throws Exception {
		// Testing config-rotator plugin setup methods, eg. add/remove targets
    String testName = "AddRemoveTargets";
    String debugLine = "**************************************** '" + testName + "': ";
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
		System.out.println( debugLine + "Create configurationRotator." );
		ConfigurationRotator cr = new ConfigurationRotator( ccucm, true );
		System.out.println( debugLine + "cr.supportsPolling: " + cr.supportsPolling() );
		System.out.println( debugLine + "cr.justConfigured: " + cr.justConfigured);
		assertTrue(cr.justConfigured);
		System.out.println( debugLine + "Set ConfigurationRotator as SCM" );
		project.setScm( cr );
		
		// Try to build
		System.out.println( debugLine + "Scheduling a build for ONLY model-1" );
		FreeStyleBuild b = project.scheduleBuild2( 0 ).get();
		// now investigate result and print debug out
		assertNotNull(b);
		System.out.println( debugLine + "... build is done" );
		System.out.println( debugLine + "Printing logfile: " + b.getLogFile() );
		BufferedReader br = new BufferedReader( new FileReader( b.getLogFile() ) );
		String line = "";
		while( ( line = br.readLine() ) != null ) {
			System.out.println( "[JENKINS] " + line );
		}
		br.close();
		System.out.println(debugLine + "... done printing logfile");
		// build should be good
		System.out.println( debugLine + "build.getResult():" + b.getResult().toString());
		assertEquals(b.getResult(), Result.SUCCESS);
		
				
		ConfigurationRotatorBuildAction action = b.getAction( ConfigurationRotatorBuildAction.class );
		System.out.println( debugLine + "action: " + action );
		// action expected not to be null
		assertNotNull(action);
		
		// check config rotator result
		System.out.println( debugLine + "action.getResult(): " + action.getResult() );
		assertEquals(action.getResult(), net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType.COMPATIBLE);
		System.out.println( debugLine + "action.isCompatible: " + action.isCompatible() );
		assertTrue(action.isCompatible());
			
		ClearCaseUCMConfiguration configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(0).getBaseline().getShortname() );
		assertEquals("model-1", configuration.getList().get(0).getBaseline().getShortname());
				
    
		// now after one successfull build, justconfigured should be false
		System.out.println( debugLine + "cr.justConfigured: " + cr.justConfigured);
		assertFalse(cr.justConfigured);
		
		// trying to change configuration, and then see status
		ccucm.targets.add( new ClearCaseUCMTarget( "client-1@" + coolTest.getPVob() + ", INITIAL, false" ) );
		System.out.println( debugLine + "Changed targets adding client-1." );
		cr.doReconfigure();
		System.out.println( debugLine + "cr.justConfigured: " + cr.justConfigured);
		//assertTrue(cr.justConfigured);
		
		// Try to build
		System.out.println( debugLine + "Scheduling a build for model-1 and client-1..." );
		b = project.scheduleBuild2( 0 ).get();
		// now investigate result and print debug out
		assertNotNull(b);
		System.out.println( debugLine + "... build is done" );
		System.out.println( debugLine + "Printing logfile: " + b.getLogFile() );
		br = new BufferedReader( new FileReader( b.getLogFile() ) );
		line = "";
		while( ( line = br.readLine() ) != null ) {
			System.out.println( "[JENKINS] " + line );
		}
		br.close();
		System.out.println(debugLine + "... done printing logfile");
		// build should be good
		System.out.println( debugLine + "build.getResult():" + b.getResult().toString());
		//assertEquals(b.getResult(), Result.SUCCESS);
		
				
		action = b.getAction( ConfigurationRotatorBuildAction.class );
		System.out.println( debugLine + "action: " + action );
		// action expected not to be null
		assertNotNull(action);
		
		// check config rotator result
		System.out.println( debugLine + "action.getResult(): " + action.getResult() );
		//assertEquals(action.getResult(), net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType.COMPATIBLE);
		System.out.println( debugLine + "action.isCompatible: " + action.isCompatible() );
		//assertTrue(action.isCompatible());
			
		configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(0).getBaseline().getShortname() );
		//assertEquals("model-1", configuration.getList().get(0).getBaseline().getShortname());
		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(1).getBaseline().getShortname() );
		//assertEquals("client-1", configuration.getList().get(1).getBaseline().getShortname());
		System.out.println( debugLine + "cr.justConfigured: " + cr.justConfigured);				

		
    // waiting is important to ensure unique timestamps and let Jenkins clean
    // workspace after each test
    waiting(watingSeconds);
        
	}
	
	// Note a test must include the string "test" somehow, else 
  // surefire will not find the test-method.
//  @Test
//	public void testInputWrongTargetName() throws Exception {
//		// Do we handle user inputting wrong target names?
//    String testName = "InputWrongTargetName";
//    String debugLine = "**************************************** '" + testName + "': ";
//    System.out.println( debugLine + "Starting" );
//    // ONLY alphanumeric chars
//		String uniqueTestVobName = testName + uniqueTimeStamp;
//    
//    // set up cool to run tests with ClearCase environment
//    // variables overwrite cool test case setup.xml setting
//    // Unique names for each test is used to avoid all sort of clear case 
//    // complications - but leaves as mess...
//    coolTest.variables.put("vobname", uniqueTestVobName );
//    coolTest.variables.put("pvobname", uniqueTestVobName );
//		coolTest.bootStrap();
//		System.out.println( debugLine + "Cool test case setup done." );
//		
//    // create Jenkins job - also use unique name
//		FreeStyleProject project = createFreeStyleProject( uniqueTestVobName );
//		// Setup ClearCase UCM as SCM and to use with config-rotator
//		ClearCaseUCM ccucm = new ClearCaseUCM( coolTest.getPVob().toString() );
//		List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
//		System.out.println( debugLine + "Adding two targets with wrong name..." );
//		targets.add( new ClearCaseUCMTarget( "model-WrongName@" + coolTest.getPVob() + ", INITIAL, false" ) );
//		targets.add( new ClearCaseUCMTarget( "client-WrongName@" + coolTest.getPVob() + ", INITIAL, false" ) );
//		ccucm.targets = targets;
//    // create config-rotator, and set it as SCM
//		System.out.println( debugLine + "Create configurationRotator." );
//		ConfigurationRotator cr = new ConfigurationRotator( ccucm, true );
//		System.out.println( debugLine + "cr.supportsPolling: " + cr.supportsPolling() );
//		System.out.println( debugLine + "Set ConfigurationRotator as SCM" );
//		project.setScm( cr );
//		
//		// Try to build
//		System.out.println( debugLine + "Scheduling a build..." );
//		FreeStyleBuild b = project.scheduleBuild2( 0 ).get();
//		System.out.println( debugLine + "After scheduling build IS DONE!" );
//		// now investigate result and print debug out
//		System.out.println( debugLine + "build.getResult():" + b.getResult().toString());
//		// build should fail for wrong targets
//		assertEquals(b.getResult(), Result.FAILURE);
//				
//		ConfigurationRotatorBuildAction action = b.getAction( ConfigurationRotatorBuildAction.class );
//		System.out.println( debugLine + "action: " + action );
//		// action expected to be null
//		assertNull(action);
//		
//		System.out.println( debugLine + "Printing logfile: " + b.getLogFile() );
//		BufferedReader br = new BufferedReader( new FileReader( b.getLogFile() ) );
//		String line = "";
//		while( ( line = br.readLine() ) != null ) {
//			System.out.println( "[JENKINS] " + line );
//		}
//		br.close();
//		System.out.println(debugLine + "... done printing logfile");
//		
//    // waiting is important to ensure unique timestamps and let Jenkins clean
//    // workspace after each test
//    waiting(watingSeconds);
//		
//	}
	
	/* ************************************************************************ 
	 * Tests planned to also:
	 * - try reconfigure
	 * - try failing build, to check if configuration renders incompatible
	 * - setting up a real job, that runs - Mads is trying (goal, is possible, is
	 * to make a job, setup polling and let it runs by itself as if were a user how
	 * made the job. How to we set up polling? how do we follow the job?
	 * - test stuff with cr.*
	 */ 
	
  // Note a test must include the string "test" somehow, else 
  // surefire will not find the test-method.
//	@Test
//	public void testManualIterateThroughAllBaselines() throws Exception {
//		// This test is supposed to "manually" iterate over baselines by scheduling
//		// a build. Each build scheduled will poll scm and should find a new baseline
//		// to test, until there is no more.
//		// For each build, we check a lot of output, results etc.
//    String testName = "ManualIterateThroughAllBaselines";
//    String debugLine = "**************************************** '" + testName + "': ";
//    System.out.println( debugLine + "Starting" );
//    // ONLY alphanumeric chars
//		String uniqueTestVobName = testName + uniqueTimeStamp;
//    
//    // set up cool to run tests with ClearCase environment
//    // variables overwrite cool test case setup.xml setting
//    // Unique names for each test is used to avoid all sort of clear case 
//    // complications - but leaves as mess...
//    coolTest.variables.put("vobname", uniqueTestVobName );
//    coolTest.variables.put("pvobname", uniqueTestVobName );
//		coolTest.bootStrap();
//		System.out.println( debugLine + "Cool test case setup done." );
//		
//    // create Jenkins job - also use unique name
//		FreeStyleProject project = createFreeStyleProject( uniqueTestVobName );
//		// Setup ClearCase UCM as SCM and to use with config-rotator
//		ClearCaseUCM ccucm = new ClearCaseUCM( coolTest.getPVob().toString() );
//		List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
//		// A first configuration added as targets: model-1 and client-1 that we 
//		// would know to be compatible.
//		targets.add( new ClearCaseUCMTarget( "model-1@" + coolTest.getPVob() + ", INITIAL, false" ) );
//    targets.add( new ClearCaseUCMTarget( "client-1@" + coolTest.getPVob() + ", INITIAL, false" ) );
//		ccucm.targets = targets;
//    // create config-rotator, and set it as SCM
//		System.out.println( debugLine + "Create configurationRotator." );
//		ConfigurationRotator cr = new ConfigurationRotator( ccucm, true );
//		System.out.println( debugLine + "cr.supportsPolling: " + cr.supportsPolling() );
//		System.out.println( debugLine + "Set ConfigurationRotator as SCM" );
//		project.setScm( cr );
//		
//		// Try to build model-1 and client-1 to se if they are compatible
//		System.out.println( debugLine + "Scheduling a build for model-1 and client-1..." );
//		FreeStyleBuild b = project.scheduleBuild2( 0 ).get();
//		// now investigate result and print debug out
//		assertNotNull(b);
//		System.out.println( debugLine + "... build is done" );
//		System.out.println( debugLine + "Printing logfile: " + b.getLogFile() );
//		BufferedReader br = new BufferedReader( new FileReader( b.getLogFile() ) );
//		String line = "";
//		while( ( line = br.readLine() ) != null ) {
//			System.out.println( "[JENKINS] " + line );
//		}
//		br.close();
//		System.out.println(debugLine + "... done printing logfile");
//		// build should be good
//		System.out.println( debugLine + "build.getResult():" + b.getResult().toString());
//		assertEquals(b.getResult(), Result.SUCCESS);
//		
//				
//		ConfigurationRotatorBuildAction action = b.getAction( ConfigurationRotatorBuildAction.class );
//		System.out.println( debugLine + "action: " + action );
//		// action expected not to be null
//		assertNotNull(action);
//		
//		// check config rotator result
//		System.out.println( debugLine + "action.getResult(): " + action.getResult() );
//		assertEquals(action.getResult(), net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType.COMPATIBLE);
//		System.out.println( debugLine + "action.isCompatible: " + action.isCompatible() );
//		assertTrue(action.isCompatible());
//			
//		ClearCaseUCMConfiguration configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
//		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(0).getBaseline().getShortname() );
//		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(1).getBaseline().getShortname() );
//		assertEquals("model-1", configuration.getList().get(0).getBaseline().getShortname());
//		assertEquals("client-1", configuration.getList().get(1).getBaseline().getShortname());
//				
//
//    
//		
//		/* ******************************************************
//		 * Now doing a new build, and expect to find baseline
//		 * model-2, and that is compatible with client-1
//		 */
//		System.out.println( debugLine + "Scheduling a build for model-2 and client-1..." );
//		b = project.scheduleBuild2( 0 ).get();
//		// now investigate result and print debug out
//		assertNotNull(b);
//		System.out.println( debugLine + "... build is done" );
//		System.out.println( debugLine + "Printing logfile: " + b.getLogFile() );
//		br = new BufferedReader( new FileReader( b.getLogFile() ) );
//		line = "";
//		while( ( line = br.readLine() ) != null ) {
//			System.out.println( "[JENKINS] " + line );
//		}
//		br.close();
//		System.out.println(debugLine + "... done printing logfile");
//		// build should be good
//		System.out.println( debugLine + "build.getResult():" + b.getResult().toString());
//		assertEquals(b.getResult(), Result.SUCCESS);
//				
//		action = b.getAction( ConfigurationRotatorBuildAction.class );
//		System.out.println( debugLine + "action: " + action );
//		// action expected not to be null
//		assertNotNull(action);
//		
//		// check config rotator result
//		System.out.println( debugLine + "action.getResult(): " + action.getResult() );
//		assertEquals(action.getResult(), net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType.COMPATIBLE);
//		System.out.println( debugLine + "action.isCompatible: " + action.isCompatible() );
//		assertTrue(action.isCompatible());
//			
//		configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
//		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(0).getBaseline().getShortname() );
//		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(1).getBaseline().getShortname() );
//		assertEquals("model-2", configuration.getList().get(0).getBaseline().getShortname());
//		assertEquals("client-1", configuration.getList().get(1).getBaseline().getShortname());
//				
//
//		
//		
//		/* ******************************************************
//		 * Now doing a new build, and expect to find baseline
//		 * model-3, and that is compatible with client-1
//		 */
//		System.out.println( debugLine + "Scheduling a build for model-3 and client-1..." );
//		b = project.scheduleBuild2( 0 ).get();
//		// now investigate result and print debug out
//		assertNotNull(b);
//		System.out.println( debugLine + "... build is done" );
//		System.out.println( debugLine + "Printing logfile: " + b.getLogFile() );
//		br = new BufferedReader( new FileReader( b.getLogFile() ) );
//		line = "";
//		while( ( line = br.readLine() ) != null ) {
//			System.out.println( "[JENKINS] " + line );
//		}
//		br.close();
//		System.out.println(debugLine + "... done printing logfile");
//		// build should be good
//		System.out.println( debugLine + "build.getResult():" + b.getResult().toString());
//		assertEquals(b.getResult(), Result.SUCCESS);
//				
//		action = b.getAction( ConfigurationRotatorBuildAction.class );
//		System.out.println( debugLine + "action: " + action );
//		// action expected not to be null
//		assertNotNull(action);
//		
//		// check config rotator result
//		System.out.println( debugLine + "action.getResult(): " + action.getResult() );
//		assertEquals(action.getResult(), net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType.COMPATIBLE);
//		System.out.println( debugLine + "action.isCompatible: " + action.isCompatible() );
//		assertTrue(action.isCompatible());
//			
//		configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
//		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(0).getBaseline().getShortname() );
//		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(1).getBaseline().getShortname() );
//		assertEquals("model-3", configuration.getList().get(0).getBaseline().getShortname());
//		assertEquals("client-1", configuration.getList().get(1).getBaseline().getShortname());
//		
//		
//		
//		
//		/* ******************************************************
//		 * Now doing a new build, and expect to find baseline
//		 * model-3, and that is compatible with client-2
//		 */
//		System.out.println( debugLine + "Scheduling a build for model-3 and client-2..." );
//		b = project.scheduleBuild2( 0 ).get();
//		// now investigate result and print debug out
//		assertNotNull(b);
//		System.out.println( debugLine + "... build is done" );
//		System.out.println( debugLine + "Printing logfile: " + b.getLogFile() );
//		br = new BufferedReader( new FileReader( b.getLogFile() ) );
//		line = "";
//		while( ( line = br.readLine() ) != null ) {
//			System.out.println( "[JENKINS] " + line );
//		}
//		br.close();
//		System.out.println(debugLine + "... done printing logfile");
//		// build should be good
//		System.out.println( debugLine + "build.getResult():" + b.getResult().toString());
//		assertEquals(b.getResult(), Result.SUCCESS);
//				
//		action = b.getAction( ConfigurationRotatorBuildAction.class );
//		System.out.println( debugLine + "action: " + action );
//		// action expected not to be null
//		assertNotNull(action);
//		
//		// check config rotator result
//		System.out.println( debugLine + "action.getResult(): " + action.getResult() );
//		assertEquals(action.getResult(), net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType.COMPATIBLE);
//		System.out.println( debugLine + "action.isCompatible: " + action.isCompatible() );
//		assertTrue(action.isCompatible());
//			
//		configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
//		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(0).getBaseline().getShortname() );
//		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(1).getBaseline().getShortname() );
//		assertEquals("model-3", configuration.getList().get(0).getBaseline().getShortname());
//		assertEquals("client-2", configuration.getList().get(1).getBaseline().getShortname());
//		
//		
//		/* ******************************************************
//		 * Now doing a new build, and expect to find baseline
//		 * model-3, and that is compatible with client-3
//		 */
//		System.out.println( debugLine + "Scheduling a build for model-3 and client-3..." );
//		b = project.scheduleBuild2( 0 ).get();
//		// now investigate result and print debug out
//		assertNotNull(b);
//		System.out.println( debugLine + "... build is done" );
//		System.out.println( debugLine + "Printing logfile: " + b.getLogFile() );
//		br = new BufferedReader( new FileReader( b.getLogFile() ) );
//		line = "";
//		while( ( line = br.readLine() ) != null ) {
//			System.out.println( "[JENKINS] " + line );
//		}
//		br.close();
//		System.out.println(debugLine + "... done printing logfile");
//		// build should be good
//		System.out.println( debugLine + "build.getResult():" + b.getResult().toString());
//		assertEquals(b.getResult(), Result.SUCCESS);
//				
//		action = b.getAction( ConfigurationRotatorBuildAction.class );
//		System.out.println( debugLine + "action: " + action );
//		// action expected not to be null
//		assertNotNull(action);
//		
//		// check config rotator result
//		System.out.println( debugLine + "action.getResult(): " + action.getResult() );
//		assertEquals(action.getResult(), net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType.COMPATIBLE);
//		System.out.println( debugLine + "action.isCompatible: " + action.isCompatible() );
//		assertTrue(action.isCompatible());
//			
//		configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
//		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(0).getBaseline().getShortname() );
//		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(1).getBaseline().getShortname() );
//		assertEquals("model-3", configuration.getList().get(0).getBaseline().getShortname());
//		assertEquals("client-3", configuration.getList().get(1).getBaseline().getShortname());
//		
//		
//		
//		
//		/* ******************************************************
//		 * Now doing to do a new build but there will be NO new baselines
//		 */
//		System.out.println( debugLine + "Scheduling a build but expect no new baselines..." );
//		b = project.scheduleBuild2( 0 ).get();
//		// now investigate result and print debug out
//		assertNotNull(b);
//		System.out.println( debugLine + "... build is done" );
//		System.out.println( debugLine + "Printing logfile: " + b.getLogFile() );
//		br = new BufferedReader( new FileReader( b.getLogFile() ) );
//		line = "";
//		while( ( line = br.readLine() ) != null ) {
//			System.out.println( "[JENKINS] " + line );
//		}
//		br.close();
//		System.out.println(debugLine + "... done printing logfile");
//		// build should be good
//		System.out.println( debugLine + "build.getResult():" + b.getResult().toString());
//		
//		// a build finding no new baseline should still be a success, or at least not a fail
//		assertEquals(b.getResult(), Result.SUCCESS);
//				
//		action = b.getAction( ConfigurationRotatorBuildAction.class );
//		System.out.println( debugLine + "action: " + action );
//
//		// commented out due to FogBugz case 6069 - plz consider what tests should do!
//		
//		// action expected not to be null
////		assertNotNull(action);
////		
////		// check config rotator result
////		System.out.println( debugLine + "action.getResult(): " + action.getResult() );
////		assertEquals(action.getResult(), net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType.COMPATIBLE);
////		System.out.println( debugLine + "action.isCompatible: " + action.isCompatible() );
////		assertTrue(action.isCompatible());
////			
////		configuration = (ClearCaseUCMConfiguration) action.getConfiguration();
////		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(0).getBaseline().getShortname() );
////		System.out.println( debugLine + "getShortname(): " + configuration.getList().get(1).getBaseline().getShortname() );
////		assertEquals("model-3", configuration.getList().get(0).getBaseline().getShortname());
////		assertEquals("client-3", configuration.getList().get(1).getBaseline().getShortname());
//		
//		
//		
//		// waiting is important to ensure unique timestamps and let Jenkins clean
//    // workspace after each test
//    waiting(watingSeconds);
//		
//	}
    

  // busy wait....
  private static void waiting (int seconds){
        
        long t0, t1;

        t0 =  System.currentTimeMillis();

        do{
            t1 = System.currentTimeMillis();
        }
        while ((t1 - t0) < (seconds * 1000));
    }
	
//	@Test
//	public void testPollSCMChange() throws Exception {
//    String testName = "PollSCMChange";
//    String debugLine = "**************************************** '" + testName + "': ";
//    System.out.println( debugLine + "Starting" );
//    // ONLY alphanumeric chars
//		String uniqueTestVobName = testName + uniqueTimeStamp;
//    
//    // set up cool to run tests with ClearCase environment
//    // variables overwrite cool test case setup.xml setting
//    // Unique names for each test is used to avoid all sort of clear case 
//    // complications - but leaves as mess...
//    coolTest.variables.put("vobname", uniqueTestVobName );
//    coolTest.variables.put("pvobname", uniqueTestVobName );
//		coolTest.bootStrap();
//		System.out.println( debugLine + "Setup done." );
//		
//    // create Jenkins job - also use unique name
//		FreeStyleProject project = createFreeStyleProject( uniqueTestVobName );
//		// Setup ClearCase UCM as SCM and to use with config-rotator
//		ClearCaseUCM ccucm = new ClearCaseUCM( coolTest.getPVob().toString() );
//		List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
//		targets.add( new ClearCaseUCMTarget( "model-1@" + coolTest.getPVob() + ", INITIAL, false" ) ); // prøv forkert navn
//    targets.add( new ClearCaseUCMTarget( "client-1@" + coolTest.getPVob() + ", INITIAL, false" ) );
//		ccucm.targets = targets;
//    // create config-rotator, and set it as SCM
//		ConfigurationRotator cr = new ConfigurationRotator( ccucm, true );
//		project.setScm( cr );
//	
//		
//		ConfigurationRotatorBuildAction action = null; 
//		Run build = null;		
//		
//		//Project setup done. Now attempt to poll for changes.
//		System.out.println(debugLine + "Polling project.");
//		System.out.println(debugLine + "Has changes?: " +project.poll(TaskListener.NULL).hasChanges());
//					
//		build = project.getLastBuild();
//		if(build != null) {
//			action = build.getAction(ConfigurationRotatorBuildAction.class);
//			System.out.println(debugLine+"Last build result was: "+action.getResult());
//		} else {
//			System.out.println(debugLine+"No previous build, this is the first build.");
//		}
//		
//		System.out.println( "Action: " + action );
//		
//		if(build!=null) {
//			System.out.println(debugLine+"Got build:");
//			
//		}
//		waiting(watingSeconds);
//		assertTrue(true);
//	}
}