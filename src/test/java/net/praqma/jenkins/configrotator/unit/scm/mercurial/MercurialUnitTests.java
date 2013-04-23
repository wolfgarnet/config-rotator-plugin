package net.praqma.jenkins.configrotator.unit.scm.mercurial;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import net.praqma.jenkins.configrotator.AbstractConfigurationRotatorSCM;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCS;
import net.praqma.jenkins.configrotator.scm.mercurial.*;
import net.praqma.jenkins.configrotator.unit.scm.dvcs.BaseUnitTest;
import net.praqma.logging.PraqmaticLogFormatter;
import net.praqma.util.test.junit.LoggingRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class MercurialUnitTests extends BaseUnitTest<MercurialCommit, MercurialConfigurationComponent, MercurialTarget, MercurialConfiguration> {

    private static Logger logger = Logger.getLogger( MercurialUnitTests.class.getName() );

    @Test
    public void testNextDVCSConfigurationResolverOneNewCommit() throws ConfigurationRotatorException, IOException, InterruptedException {
        Mercurial hg = new Mercurial();

        BaseDVCS.NextDVCSConfigurationResolver next = (BaseDVCS.NextDVCSConfigurationResolver) hg.getNextConfigurationResolver();
        BaseDVCS.NextDVCSConfigurationResolver spy = Mockito.spy( next );

        MercurialConfigurationComponent c1 = new MercurialConfigurationComponent( "hg component 1", "url", "default", "asdf", false );

        MercurialConfiguration configuration = getConfiguration( "Configuration 1", Collections.singletonList( c1 ) );

        MercurialCommit commit1 = getMockedCommit( "hg commit 1", 1l );

        Mockito.doReturn( commit1 ).when( spy ).getCommit( Mockito.any( FilePath.class ), Mockito.any( String.class ), Mockito.any( String.class ), Mockito.any( String.class ) );

        MercurialConfiguration nc = (MercurialConfiguration) spy.resolve( tasklistener, configuration, workspace );

        assertNotNull( nc );
        assertThat( nc.getList().size(), is( 1 ) );
        assertThat( nc.getList().get( 0 ), not( c1 ) );
        assertThat( nc.getList().get( 0 ).getCommitId(), is( commit1.getName() ) );
    }

    @Test
    public void testNextDVCSConfigurationResolverExceptionGetCommit() throws ConfigurationRotatorException, IOException, InterruptedException {
        Mercurial hg = new Mercurial();

        BaseDVCS.NextDVCSConfigurationResolver next = (BaseDVCS.NextDVCSConfigurationResolver) hg.getNextConfigurationResolver();
        BaseDVCS.NextDVCSConfigurationResolver spy = Mockito.spy( next );

        MercurialConfigurationComponent c1 = new MercurialConfigurationComponent( "hg component 1", "url", "default", "sha1", false );

        MercurialConfiguration configuration = getConfiguration( "Configuration 1", Collections.singletonList( c1 ) );

        Mockito.doThrow( new IOException( "FAIL!" ) ).when( spy ).getCommit( Mockito.any( FilePath.class ), Mockito.any( String.class ), Mockito.any( String.class ), Mockito.any( String.class ) );

        MercurialConfiguration nc = (MercurialConfiguration) spy.resolve( tasklistener, configuration, workspace );

        assertNull( nc );
    }

    @Test
    public void testNextDVCSConfigurationResolverOneNewCommitTwoComponents() throws ConfigurationRotatorException, IOException, InterruptedException {
        Mercurial hg = new Mercurial();

        BaseDVCS.NextDVCSConfigurationResolver next = (BaseDVCS.NextDVCSConfigurationResolver) hg.getNextConfigurationResolver();
        BaseDVCS.NextDVCSConfigurationResolver spy = Mockito.spy( next );

        MercurialConfigurationComponent c1 = new MercurialConfigurationComponent( "hg component 1", "url1", "default", "sha1", false );
        MercurialConfigurationComponent c2 = new MercurialConfigurationComponent( "hg component 2", "url2", "default", "sha2", false );

        MercurialConfiguration configuration = getConfiguration( "Configuration 1", Arrays.asList( new MercurialConfigurationComponent[] { c1, c2 } ) );

        MercurialCommit commit1 = getMockedCommit( "hg commit 1", 1l );

        Mockito.doReturn( commit1 ).when( spy ).getCommit( Mockito.any( FilePath.class ), Mockito.eq( "hg component 1" ), Mockito.any( String.class ), Mockito.any( String.class ) );
        Mockito.doReturn( null ).when( spy ).getCommit( Mockito.any( FilePath.class ), Mockito.eq( "hg component 2" ), Mockito.any( String.class ), Mockito.any( String.class ) );

        MercurialConfiguration nc = (MercurialConfiguration) spy.resolve( tasklistener, configuration, workspace );

        assertNotNull( nc );
        assertThat( nc.getList().size(), is( 2 ) );
        assertThat( nc.getList().get( 0 ), not( c1 ) );
        assertThat( nc.getList().get( 1 ), is( c2 ) );
        assertThat( nc.getList().get( 0 ).getCommitId(), is( commit1.getName() ) );
    }

    @Test
    public void testNextDVCSConfigurationResolverTwoNewCommitsTwoComponents() throws ConfigurationRotatorException, IOException, InterruptedException {
        Mercurial hg = new Mercurial();

        BaseDVCS.NextDVCSConfigurationResolver next = (BaseDVCS.NextDVCSConfigurationResolver) hg.getNextConfigurationResolver();
        BaseDVCS.NextDVCSConfigurationResolver spy = Mockito.spy( next );

        MercurialConfigurationComponent c1 = new MercurialConfigurationComponent( "hg component 1", "url1", "default", "sha1", false );
        MercurialConfigurationComponent c2 = new MercurialConfigurationComponent( "hg component 2", "url2", "default", "sha2", false );

        MercurialConfiguration configuration = getConfiguration( "Configuration 1", Arrays.asList( new MercurialConfigurationComponent[] { c1, c2 } ) );

        MercurialCommit commit1 = getMockedCommit( "hg commit 1", 1l );
        MercurialCommit commit2 = getMockedCommit( "hg commit 2", 2l );

        Mockito.doReturn( commit1 ).when( spy ).getCommit( Mockito.any( FilePath.class ), Mockito.eq( "hg component 1" ), Mockito.any( String.class ), Mockito.any( String.class ) );
        Mockito.doReturn( commit2 ).when( spy ).getCommit( Mockito.any( FilePath.class ), Mockito.eq( "hg component 2" ), Mockito.any( String.class ), Mockito.any( String.class ) );

        MercurialConfiguration nc = (MercurialConfiguration) spy.resolve( tasklistener, configuration, workspace );

        assertNotNull( nc );
        assertThat( nc.getList().size(), is( 2 ) );
        assertThat( nc.getList().get( 0 ), not( c1 ) );
        assertThat( nc.getList().get( 1 ), is( c2 ) );
        assertThat( nc.getList().get( 0 ).getCommitId(), is( commit1.getName() ) );
        assertThat( nc.getList().get( 1 ).getCommitId(), is( "sha2" ) );
    }

    @Test
    public void testNextDVCSConfigurationResolverTwoNewCommitsTwoComponentsNewer() throws ConfigurationRotatorException, IOException, InterruptedException {
        Mercurial hg = new Mercurial();

        BaseDVCS.NextDVCSConfigurationResolver next = (BaseDVCS.NextDVCSConfigurationResolver) hg.getNextConfigurationResolver();
        BaseDVCS.NextDVCSConfigurationResolver spy = Mockito.spy( next );

        MercurialConfigurationComponent c1 = new MercurialConfigurationComponent( "hg component 1", "url1", "default", "sha1", false );
        MercurialConfigurationComponent c2 = new MercurialConfigurationComponent( "hg component 2", "url2", "default", "sha2", false );

        MercurialConfiguration configuration = getConfiguration( "Configuration 1", Arrays.asList( new MercurialConfigurationComponent[] { c1, c2 } ) );

        MercurialCommit commit1 = getMockedCommit( "hg commit 1", 2l );
        MercurialCommit commit2 = getMockedCommit( "hg commit 2", 1l );

        Mockito.doReturn( commit1 ).when( spy ).getCommit( Mockito.any( FilePath.class ), Mockito.eq( "hg component 1" ), Mockito.any( String.class ), Mockito.any( String.class ) );
        Mockito.doReturn( commit2 ).when( spy ).getCommit( Mockito.any( FilePath.class ), Mockito.eq( "hg component 2" ), Mockito.any( String.class ), Mockito.any( String.class ) );

        MercurialConfiguration nc = (MercurialConfiguration) spy.resolve( tasklistener, configuration, workspace );

        assertNotNull( nc );
        assertThat( nc.getList().size(), is( 2 ) );
        assertThat( nc.getList().get( 0 ), is( c1 ) );
        assertThat( nc.getList().get( 1 ), not( c2 ) );
        assertThat( nc.getList().get( 0 ).getCommitId(), is( "sha1" ) );
        assertThat( nc.getList().get( 1 ).getCommitId(), is( commit2.getName() ) );
    }

    @Test
    public void testNextDVCSConfigurationResolverTwoNewCommitsTwoComponentsOneFixed() throws ConfigurationRotatorException, IOException, InterruptedException {
        Mercurial hg = new Mercurial();

        BaseDVCS.NextDVCSConfigurationResolver next = (BaseDVCS.NextDVCSConfigurationResolver) hg.getNextConfigurationResolver();
        BaseDVCS.NextDVCSConfigurationResolver spy = Mockito.spy( next );

        MercurialConfigurationComponent c1 = new MercurialConfigurationComponent( "hg component 1", "url1", "default", "sha1", true );
        MercurialConfigurationComponent c2 = new MercurialConfigurationComponent( "hg component 2", "url2", "default", "sha2", false );

        MercurialConfiguration configuration = getConfiguration( "Configuration 1", Arrays.asList( new MercurialConfigurationComponent[] { c1, c2 } ) );

        MercurialCommit commit2 = getMockedCommit( "hg commit 2", 1l );

        Mockito.doReturn( commit2 ).when( spy ).getCommit( Mockito.any( FilePath.class ), Mockito.eq( "hg component 2" ), Mockito.any( String.class ), Mockito.any( String.class ) );

        MercurialConfiguration nc = (MercurialConfiguration) spy.resolve( tasklistener, configuration, workspace );

        assertNotNull( nc );
        assertThat( nc.getList().size(), is( 2 ) );
        assertThat( nc.getList().get( 0 ), is( c1 ) );
        assertThat( nc.getList().get( 1 ), not( c2 ) );
        assertThat( nc.getList().get( 0 ).getCommitId(), is( "sha1" ) );
        assertThat( nc.getList().get( 1 ).getCommitId(), is( commit2.getName() ) );
    }

    @Test
    public void testNextDVCSConfigurationResolverTwoComponentsNothingNewer() throws ConfigurationRotatorException, IOException, InterruptedException {
        Mercurial hg = new Mercurial();

        BaseDVCS.NextDVCSConfigurationResolver next = (BaseDVCS.NextDVCSConfigurationResolver) hg.getNextConfigurationResolver();
        BaseDVCS.NextDVCSConfigurationResolver spy = Mockito.spy( next );

        MercurialConfigurationComponent c1 = new MercurialConfigurationComponent( "hg component 1", "url1", "default", "sha1", false );
        MercurialConfigurationComponent c2 = new MercurialConfigurationComponent( "hg component 2", "url2", "default", "sha2", false );

        MercurialConfiguration configuration = getConfiguration( "Configuration 1", Arrays.asList( new MercurialConfigurationComponent[] { c1, c2 } ) );

        Mockito.doReturn( null ).when( spy ).getCommit( Mockito.any( FilePath.class ), Mockito.eq( "hg component 1" ), Mockito.any( String.class ), Mockito.any( String.class ) );
        Mockito.doReturn( null ).when( spy ).getCommit( Mockito.any( FilePath.class ), Mockito.eq( "hg component 2" ), Mockito.any( String.class ), Mockito.any( String.class ) );

        MercurialConfiguration nc = (MercurialConfiguration) spy.resolve( tasklistener, configuration, workspace );

        assertNull( nc );
    }

    @Override
    public MercurialConfiguration getDefaultConfiguration() {
        return new MercurialConfiguration();
    }

    @Override
    public Class<MercurialCommit> getCommitClass() {
        return MercurialCommit.class;
    }

    @Override
    public Class<MercurialConfigurationComponent> getComponentClass() {
        return MercurialConfigurationComponent.class;
    }

    @Override
    public Class<MercurialConfiguration> getConfigurationClass() {
        return MercurialConfiguration.class;
    }
}
