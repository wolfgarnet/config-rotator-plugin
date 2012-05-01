package net.praqma.jenkins.configrotator.unit;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.FreeStyleBuild;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.scm.PollingResult;
import junit.framework.TestCase;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.jenkins.configrotator.ConfigurationRotatorBuildAction;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfigurationComponent;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMTarget;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCM;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfiguration;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;

import org.junit.Test;

public class ConfigRotatorTest extends TestCase {
	
	static {
		Appender appender = new ConsoleAppender();
		appender.setMinimumLevel( LogLevel.DEBUG );
		Logger.addAppender( appender );
	}
	
	/* Typical jenkins objects */
	AbstractProject<?, ?> project = Mockito.mock( FreeStyleProject.class );
	AbstractBuild<?, ?> build = Mockito.mock( FreeStyleBuild.class );
	Launcher launcher = Mockito.mock( Launcher.class );
	TaskListener listener = Mockito.mock( TaskListener.class );
	FilePath workspace = new FilePath( new File( "" ) );

	@Test
	public void testPollNoProjectNoPrevious() throws IOException, InterruptedException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		
		/* Jenkins mock */		
		Mockito.when( listener.getLogger() ).thenReturn( System.out );
		
		/* ClearCase mock */
		Mockito.when( ccucm.getLastResult( project, ClearCaseUCM.class ) ).thenReturn( null );
		
		PollingResult result = ccucm.poll( project, launcher, workspace, listener, false );
		
		assertEquals( PollingResult.BUILD_NOW, result );
	}
	
	@Test
	public void testPollNoProjectPreviousNoChanges() throws IOException, InterruptedException, ConfigurationRotatorException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		ClearCaseUCM spy = Mockito.spy( ccucm );
		
		/* Jenkins mock */		
		Mockito.when( listener.getLogger() ).thenReturn( System.out );
		
		/* ClearCase mock */
		ClearCaseUCMConfiguration ccc = new ClearCaseUCMConfiguration();
		ConfigurationRotatorBuildAction action = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, ccc );
		
		/* Behaviour */
		Mockito.doReturn( action ).when( spy ).getLastResult( project, ClearCaseUCM.class );
		Mockito.doReturn( null ).when( spy ).nextConfiguration( listener, ccc, workspace );
		
		PollingResult result = spy.poll( project, launcher, workspace, listener, false );
		
		assertEquals( PollingResult.NO_CHANGES, result );
	}
	
	@Test
	public void testPollNoProjectPreviousChanges() throws IOException, InterruptedException, ConfigurationRotatorException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		ClearCaseUCM spy = Mockito.spy( ccucm );
		
		/* Jenkins mock */		
		Mockito.when( listener.getLogger() ).thenReturn( System.out );
		
		/* ClearCase mock */
		ClearCaseUCMConfiguration ccc = new ClearCaseUCMConfiguration();
		ConfigurationRotatorBuildAction action = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, ccc );
		
		/* Behaviour */
		Mockito.doReturn( action ).when( spy ).getLastResult( project, ClearCaseUCM.class );
		Mockito.doReturn( ccc ).when( spy ).nextConfiguration( listener, ccc, workspace );
		
		PollingResult result = spy.poll( project, launcher, workspace, listener, false );
		
		assertEquals( PollingResult.BUILD_NOW, result );
	}
	
	
	
	@Test
	public void testWasReconfigured() throws IOException, InterruptedException, ConfigurationRotatorException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		ClearCaseUCM spy = Mockito.spy( ccucm );
		
		/* ClearCase mock */
		ConfigurationRotatorBuildAction action = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, null );
		
		/* Behaviour */
		Mockito.doReturn( action ).when( spy ).getLastResult( project, ClearCaseUCM.class );
		
		boolean b = spy.wasReconfigured( project );
		
		assertEquals( true, b );
	}
	
	@Test
	public void testWasReconfiguredSize() throws IOException, InterruptedException, ConfigurationRotatorException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
		targets.add( new ClearCaseUCMTarget( "bl1", PromotionLevel.INITIAL, false ) );
		ccucm.targets = targets;
		ClearCaseUCM spy = Mockito.spy( ccucm );
		
		/* ClearCase mock */
		ClearCaseUCMConfiguration ccc = Mockito.mock( ClearCaseUCMConfiguration.class );
		ConfigurationRotatorBuildAction action = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, ccc );
		
		/* Behaviour */
		Mockito.doReturn( action ).when( spy ).getLastResult( project, ClearCaseUCM.class );
		Mockito.doReturn( new ArrayList<ClearCaseUCMConfigurationComponent>() ).when( ccc ).getList();
		
		boolean b = spy.wasReconfigured( project );
		
		assertEquals( true, b );
	}
	
	@Test
	public void testWasReconfiguredNotSame() throws IOException, InterruptedException, ConfigurationRotatorException, UnableToInitializeEntityException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
		targets.add( new ClearCaseUCMTarget( "bl1", PromotionLevel.INITIAL, false ) );
		ccucm.targets = targets;
		ClearCaseUCM spy = Mockito.spy( ccucm );
		
		/* ClearCase mock */
		List<ClearCaseUCMConfigurationComponent> comps = new ArrayList<ClearCaseUCMConfigurationComponent>();
		comps.add( new ClearCaseUCMConfigurationComponent( Baseline.get( "bl2@\\pvob" ), PromotionLevel.INITIAL, false ) );
		ClearCaseUCMConfiguration ccc = new ClearCaseUCMConfiguration( comps );
		ConfigurationRotatorBuildAction action = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, ccc );
		
		/* Behaviour */
		Mockito.doReturn( action ).when( spy ).getLastResult( project, ClearCaseUCM.class );
		
		boolean b = spy.wasReconfigured( project );
		
		assertEquals( true, b );
	}
	
	@Test
	public void testWasReconfiguredSame() throws IOException, InterruptedException, ConfigurationRotatorException, UnableToInitializeEntityException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		List<ClearCaseUCMTarget> targets = new ArrayList<ClearCaseUCMTarget>();
		targets.add( new ClearCaseUCMTarget( "bl1@\\pvob", PromotionLevel.INITIAL, false ) );
		ccucm.targets = targets;
		ClearCaseUCM spy = Mockito.spy( ccucm );
		
		/* ClearCase mock */
		List<ClearCaseUCMConfigurationComponent> comps = new ArrayList<ClearCaseUCMConfigurationComponent>();
		comps.add( new ClearCaseUCMConfigurationComponent( Baseline.get( "bl1@\\pvob" ), PromotionLevel.INITIAL, false ) );
		ClearCaseUCMConfiguration ccc = new ClearCaseUCMConfiguration( comps );
		ConfigurationRotatorBuildAction action = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, ccc );
		
		/* Behaviour */
		Mockito.doReturn( action ).when( spy ).getLastResult( project, ClearCaseUCM.class );
		
		boolean b = spy.wasReconfigured( project );
		
		assertEquals( false, b );
	}
}
