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
	Integer watingSeconds = 30;
  // A time stamp added to ClearCase Vob names to make them unique for each
  // test. They also include the test name.
  // Division by 60000, giving milis to minute precission asuming all tests do
  // not complete within a minute!
	String uniqueTimeStamp = "" + System.currentTimeMillis()/60000;

//  // Note a test must include the string "test" somehow, else 
//  // surefire will not find the test-method.
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
//  
//	  @Test
//	public void testStuffOnConfigRotatorObject() throws Exception {
//		// Testing getting stuff and info from config rotator
//    String testName = "StuffOnConfigRotatorObject";
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
//		// Setup ClearCase UCM as SCM and to use with config-rotator
//		ClearCaseUCM ccucm = new ClearCaseUCM( coolTest.getPVob().toString() );
//		List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
//		targets.add( new ClearCaseUCMTarget( "model-1@" + coolTest.getPVob() + ", INITIAL, false" ) );
//    ccucm.targets = targets;
//    // create config-rotator, and set it as SCM
//		System.out.println( debugLine + "Creating configurationRotator." );
//		ConfigurationRotator cr = new ConfigurationRotator( ccucm, true );
//		
//		/* *** now getting and testing a lot of stuff */
//		System.out.println( debugLine + "cr.supportsPolling: " + cr.supportsPolling() );
//		assertTrue(cr.supportsPolling());
//		
//		System.out.println( debugLine + "cr.justConfigured: " + cr.justConfigured);
//		assertTrue(cr.justConfigured); //justconfigured should initially be true
//		
//		System.out.println( debugLine + "cr.reconfigure: " + cr.reconfigure);
//		assertFalse(cr.reconfigure); //should initially be false
//		
//		System.out.println( debugLine + "cr.createChangeLogParser(): " + cr.createChangeLogParser());
//		//assertNotNull(cr.createChangeLogParser());
//		
//		System.out.println( debugLine + "cr.getAcrs(): " + cr.getAcrs());
//		assertNotNull(cr.getAcrs());
//		
//		System.out.println( debugLine + "Doing cr.setReconfigure(true), now testing for it"); 
//		System.out.println( debugLine + "cr.setReconfigure(true), now testing for it"); 
//		System.out.println( debugLine + "cr.reconfigure: " + cr.reconfigure);
//		//assertTrue(cr.reconfigure); //should initially be false, but now true
//		
//		
//    // waiting is important to ensure unique timestamps and let Jenkins clean
//    // workspace after each test
//    waiting(watingSeconds);
//	}
//		
//	
//  // Note a test must include the string "test" somehow, else 
//  // surefire will not find the test-method.
//  @Test
//	public void testTryReconfigure() throws Exception {
//		// Testing reconfigure
//    String testName = "TryReconfigure";
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
//		    // create Jenkins job - also use unique name
//		FreeStyleProject project = createFreeStyleProject( uniqueTestVobName );
//		// Setup ClearCase UCM as SCM and to use with config-rotator
//		ClearCaseUCM ccucm = new ClearCaseUCM( coolTest.getPVob().toString() );
//		List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
//		targets.add( new ClearCaseUCMTarget( "model-1@" + coolTest.getPVob() + ", INITIAL, false" ) );
//    ccucm.targets = targets;
//    // create config-rotator, and set it as SCM
//		System.out.println( debugLine + "Create configurationRotator." );
//		ConfigurationRotator cr = new ConfigurationRotator( ccucm, true );
//		System.out.println( debugLine + "cr.supportsPolling: " + cr.supportsPolling() );
//		System.out.println( debugLine + "cr.justConfigured: " + cr.justConfigured);
//		assertTrue(cr.justConfigured);
//		System.out.println( debugLine + "Set ConfigurationRotator as SCM" );
//		project.setScm( cr );
//		
//		// Try to build
//		System.out.println( debugLine + "Scheduling a build for ONLY model-1" );
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
//		assertEquals("model-1", configuration.getList().get(0).getBaseline().getShortname());
//				
//    
//		// now after one successfull build, justconfigured should be false
//		System.out.println( debugLine + "cr.justConfigured: " + cr.justConfigured);
//		assertFalse(cr.justConfigured);
//		
//		// trying to change configuration to what happens....
//		targets.add( new ClearCaseUCMTarget( "client-1@" + coolTest.getPVob() + ", INITIAL, false" ) );
//		ccucm.targets = targets;
//		//cr.setReconfigure(true);
//		cr.doReconfigure();
//		System.out.println( debugLine + "Changed targets adding client-1." );
//		System.out.println( debugLine + "cr.justConfigured: " + cr.justConfigured);
//		System.out.println( debugLine + "cr.reconfigure: " + cr.reconfigure);
//		//assertTrue(cr.justConfigured);
//		//assertTrue(cr.reconfigure);
//		
//		// Try to build
//		//assertTrue(false);
//		// ...
//
//		
//    // waiting is important to ensure unique timestamps and let Jenkins clean
//    // workspace after each test
//    waiting(watingSeconds);
//        
//	}
//	
//		// Note a test must include the string "test" somehow, else 
//	// surefire will not find the test-method.
//	@Test
//	public void testGettingStuffOnAction() throws Exception {
//		// Just boosting coverage getting stuff on action object
//    String testName = "GettingStuffOnAction";
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
//		System.out.println( debugLine + "Creating configurationRotator." );
//		ConfigurationRotator cr = new ConfigurationRotator( ccucm, true );
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
//		// Now trying to test methods on action
//		ConfigurationRotatorBuildAction action = b.getAction( ConfigurationRotatorBuildAction.class );
//		System.out.println( debugLine + "action: " + action );
//		// action expected not to be null
//		assertNotNull(action);
//		
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
//		System.out.println( debugLine + "action.isFresh(): " + action.isFresh() );
//		//assertTrue(action.isFresh()); // would expect isFresh to be true if just builded?
//		System.out.println( debugLine + "action.isDetermined(): " + action.isDetermined() );
//		assertTrue(action.isDetermined());
//		System.out.println( debugLine + "action.isCompatible(): " + action.isCompatible() );
//		assertTrue(action.isCompatible());
//		
//		System.out.println( debugLine + "action.getIconFileName(): " + action.getIconFileName() );
//		assertEquals("/plugin/config-rotator/images/rotate.png", action.getIconFileName());
//		
//		System.out.println( debugLine + "action.getDisplayName(): " + action.getDisplayName());
//		assertEquals("Config Rotator", action.getDisplayName());
//		
//		System.out.println( debugLine + "action.getUrlName(): " + action.getUrlName() );
//		assertEquals("config-rotator", action.getUrlName());
//		
//		System.out.println( debugLine + "Setting result failed... action.setResult(ConfigurationRotator.ResultType.FAILED)");
//		action.setResult(ConfigurationRotator.ResultType.FAILED);
//		System.out.println( debugLine + "action.getResult(): " +  action.getResult() );
//		assertEquals(action.getResult(), ConfigurationRotator.ResultType.FAILED);
//		
//	
//		// waiting is important to ensure unique timestamps and let Jenkins clean
//    // workspace after each test
//    waiting(watingSeconds);
//	}
//	
//	// Note a test must include the string "test" somehow, else 
//	// surefire will not find the test-method.
//	@Test
//	public void testRevertToConfiguration() throws Exception {
//		// Tests if we can revert to another configuration from an old build
//    String testName = "RevertToConfiguration";
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
//		System.out.println( debugLine + "Creating configurationRotator." );
//		ConfigurationRotator cr = new ConfigurationRotator( ccucm, true );
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
//		// Building again, to make it possible to reset later to model-1 and client-1
//		System.out.println( debugLine + "Scheduling a build 2, for model-2 and client-1..." );
//		FreeStyleBuild b2 = project.scheduleBuild2( 0 ).get();
//		// now investigate result and print debug out
//		assertNotNull(b2);
//		System.out.println( debugLine + "... build is done" );
//		System.out.println( debugLine + "Printing logfile: " + b2.getLogFile() );
//		br = new BufferedReader( new FileReader( b2.getLogFile() ) );
//		line = "";
//		while( ( line = br.readLine() ) != null ) {
//			System.out.println( "[JENKINS] " + line );
//		}
//		br.close();
//		System.out.println(debugLine + "... done printing logfile");
//		// build should be good
//		System.out.println( debugLine + "build.getResult():" + b2.getResult().toString());
//		assertEquals(b2.getResult(), Result.SUCCESS);
//		
//				
//		ConfigurationRotatorBuildAction action2 = b2.getAction( ConfigurationRotatorBuildAction.class );
//		System.out.println( debugLine + "action: " + action2 );
//		// action expected not to be null
//		assertNotNull(action2);
//		
//		System.out.println(debugLine + "Setting configuration from last action.");
//		cr.setConfigurationByAction(project, action);
//		System.out.println( debugLine + "cr.justConfigured: " + cr.justConfigured);
//		assertFalse(cr.justConfigured); // dit not just configure...
//		System.out.println( debugLine + "cr.reconfigure: " + cr.reconfigure);
//		assertTrue(cr.reconfigure); //but RECONFIGURED ....
//		
//		// seems configuration worked, not trying to build to see if it is the old one
//		System.out.println( debugLine + "Scheduling a build 3, that should be for old config model-1 and client-1..." );
//		FreeStyleBuild b3 = project.scheduleBuild2( 0 ).get();
//		// now investigate result and print debug out
//		assertNotNull(b3);
//		System.out.println( debugLine + "... build is done" );
//		System.out.println( debugLine + "Printing logfile: " + b3.getLogFile() );
//		br = new BufferedReader( new FileReader( b3.getLogFile() ) );
//		line = "";
//		while( ( line = br.readLine() ) != null ) {
//			System.out.println( "[JENKINS] " + line );
//		}
//		br.close();
//		System.out.println(debugLine + "... done printing logfile");
//		// build should be good
//		System.out.println( debugLine + "build.getResult():" + b3.getResult().toString());
//		assertEquals(b3.getResult(), Result.SUCCESS);
//		
//				
//		ConfigurationRotatorBuildAction action3 = b3.getAction( ConfigurationRotatorBuildAction.class );
//		System.out.println( debugLine + "action: " + action3 );
//		// action expected not to be null
//		assertNotNull(action3);
//		
//		// check config rotator result
//		System.out.println( debugLine + "action.getResult(): " + action3.getResult() );
//		assertEquals(action3.getResult(), net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType.COMPATIBLE);
//		System.out.println( debugLine + "action.isCompatible: " + action3.isCompatible() );
//		assertTrue(action3.isCompatible());
//			
//		ClearCaseUCMConfiguration configuration3 = (ClearCaseUCMConfiguration) action3.getConfiguration();
//		System.out.println( debugLine + "getShortname(): " + configuration3.getList().get(0).getBaseline().getShortname() );
//		System.out.println( debugLine + "getShortname(): " + configuration3.getList().get(1).getBaseline().getShortname() );
//		assertEquals("model-1", configuration3.getList().get(0).getBaseline().getShortname());
//		assertEquals("client-1", configuration3.getList().get(1).getBaseline().getShortname());
//		
//		// waiting is important to ensure unique timestamps and let Jenkins clean
//    // workspace after each test
//    waiting(watingSeconds);
//	}
//	
//	// Note a test must include the string "test" somehow, else 
//  // surefire will not find the test-method.
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
	
	// Note a test must include the string "test" somehow, else 
  // surefire will not find the test-method.
	@Test
	public void testClearCaseUCMTarget() throws Exception {
		// Test is supposed to test last parts of UCMTarget component
		// not hit in the other tests
    String testName = "ClearCaseUCMTarget";
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
			
		// Make targets.... and test on those
		// try constructor 
		System.out.println( debugLine + "Trying CCUCMtarget constructor - plain" );
		ClearCaseUCMTarget target1 = new ClearCaseUCMTarget();
		// set component on it
		System.out.println( debugLine + "Setting component" );
		target1.setComponent("model-1@" + coolTest.getPVob() + ", INITIAL, false");
		// test getComponent - should be the same
		System.out.println( debugLine + "Getting component and checking equality" );
		assertEquals("model-1@" + coolTest.getPVob() + ", INITIAL, false", target1.getComponent());
		// test equals
		System.out.println( debugLine + "Comparing target1 with target1" );
		assertTrue(target1.equals(target1));
		System.out.println( debugLine + "Creating a target2" );
		ClearCaseUCMTarget target2 = new ClearCaseUCMTarget( "client-1@" + coolTest.getPVob() + ", INITIAL, false" );
		System.out.println( debugLine + "Comparing target1 with target2" );
		assertFalse(target1.equals(target2));
		
		
		// waiting is important to ensure unique timestamps and let Jenkins clean
    // workspace after each test
    waiting(watingSeconds);
		
		assertTrue(false); // fail test, to avoid disturbing covere yet...
				
	}
	
	@Test
	public void testClearCaseUCM() throws Exception {
		// Boosting coverage
    String testName = "ClearCaseUCM";
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
		// A first configuration added as targets: model-1 and client-1 that we 
		// would know to be compatible.
		targets.add( new ClearCaseUCMTarget( "model-1@" + coolTest.getPVob() + ", INITIAL, false" ) );
    targets.add( new ClearCaseUCMTarget( "client-1@" + coolTest.getPVob() + ", INITIAL, false" ) );
		ccucm.targets = targets;
		System.out.println( debugLine + "Usual ccucm target set on ccucm" );
		
		System.out.println( debugLine + "ccucm.getPvobName():" + ccucm.getPvobName());
		assertEquals("\\" + uniqueTestVobName, ccucm.getPvobName());
		
		System.out.println( debugLine + "ccucm.getName():" + ccucm.getName());
		assertEquals("ClearCase UCM", ccucm.getName());
		
		System.out.println( debugLine + "ccucm.wasReconfigured(project):" + ccucm.wasReconfigured(project));
		ccucm.wasReconfigured(project);
				
    // create config-rotator, and set it as SCM
		System.out.println( debugLine + "Create configurationRotator." );
		ConfigurationRotator cr = new ConfigurationRotator( ccucm, true );
		System.out.println( debugLine + "cr.supportsPolling: " + cr.supportsPolling() );
		System.out.println( debugLine + "Set ConfigurationRotator as SCM" );
		project.setScm( cr );
		
		System.out.println( debugLine + "ccucm.wasReconfigured(project):" + ccucm.wasReconfigured(project));
		ccucm.wasReconfigured(project);
		
		// Try to build
		System.out.println( debugLine + "Scheduling a build ..." );
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
		
		// change targets
		System.out.println( debugLine + "clearing targets on ccucm" );
		ccucm.targets.clear();
		System.out.println( debugLine + "ccucm.wasReconfigured(project):" + ccucm.wasReconfigured(project));
		ccucm.wasReconfigured(project);
		
		System.out.println( debugLine + "adding one target to ccucm" );
		ccucm.targets.add(new ClearCaseUCMTarget( "model-1@" + coolTest.getPVob() + ", INITIAL, false" ));
		System.out.println( debugLine + "ccucm.wasReconfigured(project):" + ccucm.wasReconfigured(project));
		ccucm.wasReconfigured(project);
				
		System.out.println( debugLine + "ccucm.wasReconfigured(project):" + ccucm.wasReconfigured(project));
		ccucm.wasReconfigured(project);
		
		
		assertTrue(false); // fail test, to avoid disturbing covere yet...
}
	
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