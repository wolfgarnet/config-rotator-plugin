package net.praqma.jenkins.configrotator.unit;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.scm.PollingResult;
import junit.framework.TestCase;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.jenkins.configrotator.AbstractConfiguration;
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( PowerMockRunner.class )
@PrepareForTest( ClearCaseUCMConfiguration.class )
public class ConfigRotatorTest extends TestCase {
	
	static {
		Appender appender = new ConsoleAppender();
		appender.setMinimumLevel( LogLevel.DEBUG );
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
	public void testPollNoProjectNoPrevious() throws IOException, InterruptedException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		
		/* Jenkins mock */
		Mockito.when( tasklistener.getLogger() ).thenReturn( System.out );
		
		/* ClearCase mock */
		Mockito.when( ccucm.getLastResult( project, ClearCaseUCM.class ) ).thenReturn( null );
		
		PollingResult result = ccucm.poll( project, launcher, workspace, tasklistener, false );
		
		assertEquals( PollingResult.BUILD_NOW, result );
	}
	
	@Test
	public void testPollNoProjectPreviousNoChanges() throws IOException, InterruptedException, ConfigurationRotatorException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		ClearCaseUCM spy = Mockito.spy( ccucm );
		
		/* Jenkins mock */		
		Mockito.when( tasklistener.getLogger() ).thenReturn( System.out );
		
		/* ClearCase mock */
		ClearCaseUCMConfiguration ccc = new ClearCaseUCMConfiguration();
		ConfigurationRotatorBuildAction action = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, ccc );
		
		/* Behaviour */
		Mockito.doReturn( action ).when( spy ).getLastResult( project, ClearCaseUCM.class );
		Mockito.doReturn( null ).when( spy ).nextConfiguration( tasklistener, ccc, workspace );
		
		PollingResult result = spy.poll( project, launcher, workspace, tasklistener, false );
		
		assertEquals( PollingResult.NO_CHANGES, result );
	}
	
	@Test
	public void testPollNoProjectPreviousChanges() throws IOException, InterruptedException, ConfigurationRotatorException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		ClearCaseUCM spy = Mockito.spy( ccucm );
		
		/* Jenkins mock */		
		Mockito.when( tasklistener.getLogger() ).thenReturn( System.out );
		
		/* ClearCase mock */
		ClearCaseUCMConfiguration ccc = new ClearCaseUCMConfiguration();
		ConfigurationRotatorBuildAction action = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, ccc );
		
		/* Behaviour */
		Mockito.doReturn( action ).when( spy ).getLastResult( project, ClearCaseUCM.class );
		Mockito.doReturn( ccc ).when( spy ).nextConfiguration( tasklistener, ccc, workspace );
		
		PollingResult result = spy.poll( project, launcher, workspace, tasklistener, false );
		
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
	
	@Test
	public void testPerformReconfigured() throws IOException, InterruptedException, ConfigurationRotatorException, UnableToInitializeEntityException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		ClearCaseUCM spy = Mockito.spy( ccucm );
		
		/* ClearCase mock */
		List<ClearCaseUCMConfigurationComponent> comps = new ArrayList<ClearCaseUCMConfigurationComponent>();
		comps.add( new ClearCaseUCMConfigurationComponent( Baseline.get( "bl2@\\pvob" ), PromotionLevel.INITIAL, false ) );
		ClearCaseUCMConfiguration ccc = new ClearCaseUCMConfiguration( comps );
		ConfigurationRotatorBuildAction action = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, ccc );
		
		/* Behaviour */
		PowerMockito.mockStatic( ClearCaseUCMConfiguration.class );
		PowerMockito.when( ClearCaseUCMConfiguration.getConfigurationFromTargets( Mockito.anyListOf( ClearCaseUCMTarget.class ), Mockito.any( FilePath.class ), Mockito.any( TaskListener.class) ) ).thenReturn( ccc );
		
		PowerMockito.doReturn( null ).when( spy ).getLastResult( Mockito.any( AbstractProject.class ), Mockito.any( Class.class ) );
		
		Mockito.doNothing().when( spy ).printConfiguration( Mockito.any( PrintStream.class ), Mockito.any( AbstractConfiguration.class ) );
		
		PowerMockito.doReturn( ccc ).when( spy ).nextConfiguration( Mockito.any( TaskListener.class ), Mockito.any( ClearCaseUCMConfiguration.class ), Mockito.any( FilePath.class ) );
		
		PowerMockito.doReturn( null ).when( spy ).createView( Mockito.any( TaskListener.class ), Mockito.any( FreeStyleBuild.class ), Mockito.any( ClearCaseUCMConfiguration.class ), Mockito.any( FilePath.class ), Mockito.any( PVob.class ) );
		
		boolean b = spy.perform( build, launcher, workspace, buildlistener, true );
		
		assertEquals( true, b );
	}
	
	@Test( expected=AbortException.class )
	public void testPerformReconfiguredThrows() throws IOException, InterruptedException, ConfigurationRotatorException, UnableToInitializeEntityException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		ClearCaseUCM spy = Mockito.spy( ccucm );
		
		/* ClearCase mock */
		List<ClearCaseUCMConfigurationComponent> comps = new ArrayList<ClearCaseUCMConfigurationComponent>();
		comps.add( new ClearCaseUCMConfigurationComponent( Baseline.get( "bl2@\\pvob" ), PromotionLevel.INITIAL, false ) );
		ClearCaseUCMConfiguration ccc = new ClearCaseUCMConfiguration( comps );
		ConfigurationRotatorBuildAction action = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, ccc );
		
		/* Behaviour */
		PowerMockito.mockStatic( ClearCaseUCMConfiguration.class );
		PowerMockito.when( ClearCaseUCMConfiguration.getConfigurationFromTargets( Mockito.anyListOf( ClearCaseUCMTarget.class ), Mockito.any( FilePath.class ), Mockito.any( TaskListener.class) ) ).thenThrow( new AbortException( "Just fail, you" ) );
		PowerMockito.doReturn( null ).when( spy ).getLastResult( Mockito.any( AbstractProject.class ), Mockito.any( Class.class ) );

		spy.perform( build, launcher, workspace, buildlistener, true );
		
		fail();
	}
	
	@Test
	public void testPerform() throws IOException, InterruptedException, ConfigurationRotatorException, UnableToInitializeEntityException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		ClearCaseUCM spy = Mockito.spy( ccucm );
		
		/* ClearCase mock */
		List<ClearCaseUCMConfigurationComponent> comps = new ArrayList<ClearCaseUCMConfigurationComponent>();
		comps.add( new ClearCaseUCMConfigurationComponent( Baseline.get( "bl2@\\pvob" ), PromotionLevel.INITIAL, false ) );
		ClearCaseUCMConfiguration ccc = new ClearCaseUCMConfiguration( comps );
		ConfigurationRotatorBuildAction action = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, ccc );
		
		/* Behaviour */		
		PowerMockito.doReturn( action ).when( spy ).getLastResult( Mockito.any( AbstractProject.class ), Mockito.any( Class.class ) );
		
		Mockito.doNothing().when( spy ).printConfiguration( Mockito.any( PrintStream.class ), Mockito.any( AbstractConfiguration.class ) );
		
		PowerMockito.doReturn( ccc ).when( spy ).nextConfiguration( Mockito.any( TaskListener.class ), Mockito.any( ClearCaseUCMConfiguration.class ), Mockito.any( FilePath.class ) );
		
		PowerMockito.doReturn( null ).when( spy ).createView( Mockito.any( TaskListener.class ), Mockito.any( FreeStyleBuild.class ), Mockito.any( ClearCaseUCMConfiguration.class ), Mockito.any( FilePath.class ), Mockito.any( PVob.class ) );
		
		boolean b = spy.perform( build, launcher, workspace, buildlistener, false );
		
		assertEquals( true, b );
	}
	
	@Test
	public void testPerformConfigurationIsNull() throws IOException, InterruptedException, ConfigurationRotatorException, UnableToInitializeEntityException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		ClearCaseUCM spy = Mockito.spy( ccucm );
		
		/* ClearCase mock */
		List<ClearCaseUCMConfigurationComponent> comps = new ArrayList<ClearCaseUCMConfigurationComponent>();
		comps.add( new ClearCaseUCMConfigurationComponent( Baseline.get( "bl2@\\pvob" ), PromotionLevel.INITIAL, false ) );
		ClearCaseUCMConfiguration ccc = new ClearCaseUCMConfiguration( comps );
		ConfigurationRotatorBuildAction action = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, ccc );
		
		/* Behaviour */		
		PowerMockito.doReturn( action ).when( spy ).getLastResult( Mockito.any( AbstractProject.class ), Mockito.any( Class.class ) );
		
		Mockito.doNothing().when( spy ).printConfiguration( Mockito.any( PrintStream.class ), Mockito.any( AbstractConfiguration.class ) );
		
		PowerMockito.doReturn( null ).when( spy ).nextConfiguration( Mockito.any( TaskListener.class ), Mockito.any( ClearCaseUCMConfiguration.class ), Mockito.any( FilePath.class ) );
		
		PowerMockito.doReturn( null ).when( spy ).createView( Mockito.any( TaskListener.class ), Mockito.any( FreeStyleBuild.class ), Mockito.any( ClearCaseUCMConfiguration.class ), Mockito.any( FilePath.class ), Mockito.any( PVob.class ) );
		
		boolean b = spy.perform( build, launcher, workspace, buildlistener, false );
		
		assertEquals( false, b );
	}
	
	@Test( expected=AbortException.class )
	public void testPerformNextConfigurationThrows() throws IOException, InterruptedException, ConfigurationRotatorException, UnableToInitializeEntityException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		ClearCaseUCM spy = Mockito.spy( ccucm );
		
		/* ClearCase mock */
		List<ClearCaseUCMConfigurationComponent> comps = new ArrayList<ClearCaseUCMConfigurationComponent>();
		comps.add( new ClearCaseUCMConfigurationComponent( Baseline.get( "bl2@\\pvob" ), PromotionLevel.INITIAL, false ) );
		ClearCaseUCMConfiguration ccc = new ClearCaseUCMConfiguration( comps );
		ConfigurationRotatorBuildAction action = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, ccc );
		
		/* Behaviour */		
		PowerMockito.doReturn( action ).when( spy ).getLastResult( Mockito.any( AbstractProject.class ), Mockito.any( Class.class ) );
		
		Mockito.doNothing().when( spy ).printConfiguration( Mockito.any( PrintStream.class ), Mockito.any( AbstractConfiguration.class ) );
		
		PowerMockito.doThrow( new ConfigurationRotatorException( "Throwing from next" ) ).when( spy ).nextConfiguration( Mockito.any( TaskListener.class ), Mockito.any( ClearCaseUCMConfiguration.class ), Mockito.any( FilePath.class ) );
		
		PowerMockito.doReturn( null ).when( spy ).createView( Mockito.any( TaskListener.class ), Mockito.any( FreeStyleBuild.class ), Mockito.any( ClearCaseUCMConfiguration.class ), Mockito.any( FilePath.class ), Mockito.any( PVob.class ) );
		
		spy.perform( build, launcher, workspace, buildlistener, false );
		
		fail();
	}
	
	@Test( expected=AbortException.class )
	public void testPerformFailingView() throws IOException, InterruptedException, ConfigurationRotatorException, UnableToInitializeEntityException {
		ClearCaseUCM ccucm = new ClearCaseUCM( "" );
		ClearCaseUCM spy = Mockito.spy( ccucm );
		
		/* ClearCase mock */
		List<ClearCaseUCMConfigurationComponent> comps = new ArrayList<ClearCaseUCMConfigurationComponent>();
		comps.add( new ClearCaseUCMConfigurationComponent( Baseline.get( "bl2@\\pvob" ), PromotionLevel.INITIAL, false ) );
		ClearCaseUCMConfiguration ccc = new ClearCaseUCMConfiguration( comps );
		ConfigurationRotatorBuildAction action = new ConfigurationRotatorBuildAction( build, ClearCaseUCM.class, ccc );
		
		/* Behaviour */		
		PowerMockito.doReturn( action ).when( spy ).getLastResult( Mockito.any( AbstractProject.class ), Mockito.any( Class.class ) );
		
		Mockito.doNothing().when( spy ).printConfiguration( Mockito.any( PrintStream.class ), Mockito.any( AbstractConfiguration.class ) );
		
		PowerMockito.doReturn( ccc ).when( spy ).nextConfiguration( Mockito.any( TaskListener.class ), Mockito.any( ClearCaseUCMConfiguration.class ), Mockito.any( FilePath.class ) );
		
		PowerMockito.doThrow( new IOException( "Throwing from create view" ) ).when( spy ).createView( Mockito.any( TaskListener.class ), Mockito.any( FreeStyleBuild.class ), Mockito.any( ClearCaseUCMConfiguration.class ), Mockito.any( FilePath.class ), Mockito.any( PVob.class ) );
		
		spy.perform( build, launcher, workspace, buildlistener, false );
		
		fail();
	}
}
