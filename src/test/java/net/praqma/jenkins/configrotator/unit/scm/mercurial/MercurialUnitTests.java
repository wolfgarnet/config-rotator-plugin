package net.praqma.jenkins.configrotator.unit.scm.mercurial;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import net.praqma.jenkins.configrotator.AbstractConfigurationRotatorSCM;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCS;
import net.praqma.jenkins.configrotator.scm.mercurial.Mercurial;
import net.praqma.jenkins.configrotator.scm.mercurial.MercurialCommit;
import net.praqma.jenkins.configrotator.scm.mercurial.MercurialConfiguration;
import net.praqma.jenkins.configrotator.scm.mercurial.MercurialConfigurationComponent;
import net.praqma.logging.PraqmaticLogFormatter;
import net.praqma.util.test.junit.LoggingRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * @author cwolfgang
 */
public class MercurialUnitTests {

    @ClassRule
    public static LoggingRule lrule = new LoggingRule( Level.ALL, "net.praqma" ).setFormat( PraqmaticLogFormatter.TINY_FORMAT );

    protected TaskListener tasklistener;
    protected FilePath workspace;

    @Before
    public void initialize() {
        tasklistener = Mockito.mock( TaskListener.class );
        Mockito.when( tasklistener.getLogger() ).thenReturn( System.out );

        workspace = new FilePath( new File( "" ) );
    }

    @Test
    public void testNextDVCSConfigurationResolver() throws ConfigurationRotatorException, IOException, InterruptedException {
        Mercurial hg = new Mercurial();

        BaseDVCS.NextDVCSConfigurationResolver next = (BaseDVCS.NextDVCSConfigurationResolver) hg.getNextConfigurationResolver();
        BaseDVCS.NextDVCSConfigurationResolver spy = Mockito.spy( next );

        MercurialConfigurationComponent c1 = Mockito.mock( MercurialConfigurationComponent.class );
        Mockito.doNothing().when( c1 ).setCommitId( Mockito.any( String.class ) );
        Mockito.doNothing().when( c1 ).setChangedLast( Mockito.any( Boolean.class ) );

        MercurialConfiguration configuration = Mockito.mock( MercurialConfiguration.class );
        Mockito.doReturn( configuration ).when( configuration ).clone();
        Mockito.doReturn( Collections.singletonList( c1 ) ).when( configuration ).getList();

        MercurialCommit commit1 = Mockito.mock( MercurialCommit.class );
        Mockito.when( commit1.getCommitTime() ).thenReturn( 1l );
        Mockito.when( commit1.getName() ).thenReturn( "Commit 1" );

        Mockito.doReturn( commit1 ).when( spy ).getCommit( Mockito.any( FilePath.class ), Mockito.any( String.class ), Mockito.any( String.class ), Mockito.any( String.class ) );

        //Mockito.doReturn( commit1 ).when( workspace ).act( Mockito.any( FilePath.FileCallable.class ) );
        //Mockito.when( workspace.act( Mockito.any( FilePath.FileCallable.class) ) ).thenReturn( commit1 );
        //Mockito.doReturn( commit1 ).when( workspaceSpy ).act( Mockito.any( FilePath.FileCallable.class ) );

        /*
        Iterator<MercurialCommit> commitIterator = (Iterator<MercurialCommit>) Mockito.mock( Iterable.class );
        Mockito.when( commitIterator.hasNext() ).thenReturn( true, false );
        Mockito.when( commitIterator.next() ).thenReturn( commit1 );
        */

        spy.resolve( tasklistener, configuration, workspace );
    }
}
