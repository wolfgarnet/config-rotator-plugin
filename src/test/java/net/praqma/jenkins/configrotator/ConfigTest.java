package net.praqma.jenkins.configrotator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hudson.model.Cause;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import org.junit.Test;

import net.praqma.jenkins.configrotator.ConfigurationRotator;
import net.praqma.jenkins.configrotator.ConfigurationRotatorBuildAction;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCM;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMTarget;
import net.praqma.jenkins.utils.test.ClearCaseJenkinsTestCase;

public class ConfigTest extends ClearCaseJenkinsTestCase {

	@Test
	public void test1() throws Exception {
		
		System.out.println( "I AMMMMM HEHREHHREHEHRHEHHERHHERHE" );
		
		coolTest.bootStrap();
		
		System.out.println( "AFTER" );
		
		FreeStyleProject project = createFreeStyleProject( "configrotator" );
		
		ClearCaseUCM ccucm = new ClearCaseUCM( coolTest.getPVob().toString() );
		List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
		targets.add( new ClearCaseUCMTarget( "_System_1.0@" + coolTest.getPVob() + ", INITIAL, false" ) );
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
}
