package net.praqma.jenkins.configrotator.unit.scm.dvcs;

import hudson.FilePath;
import hudson.model.TaskListener;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSCommit;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSConfiguration;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSConfigurationComponent;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSTarget;
import net.praqma.logging.PraqmaticLogFormatter;
import net.praqma.util.test.junit.LoggingRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.mockito.Mockito;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

/**
 * @author cwolfgang
 */
public abstract class BaseUnitTest<COMMIT extends BaseDVCSCommit, COMPONENT extends BaseDVCSConfigurationComponent, TARGET extends BaseDVCSTarget, CONFIG extends BaseDVCSConfiguration<COMPONENT, TARGET>> {

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

    public abstract Class<COMMIT> getCommitClass();
    public abstract Class<COMPONENT> getComponentClass();
    public abstract Class<CONFIG> getConfigurationClass();

    public abstract CONFIG getDefaultConfiguration();


    protected COMMIT getMockedCommit( String name, long time ) {
        COMMIT commit = Mockito.mock( getCommitClass() );
        Mockito.when( commit.getCommitTime() ).thenReturn( time );
        Mockito.when( commit.getName() ).thenReturn( name );
        Mockito.doReturn( name ).when( commit ).toString();

        return commit;
    }

    protected COMPONENT getMockedComponent( String name ) {
        COMPONENT component = Mockito.mock( getComponentClass() );
        Mockito.doNothing().when( component ).setCommitId( Mockito.any( String.class ) );
        Mockito.doNothing().when( component ).setChangedLast( Mockito.any( Boolean.class ) );
        Mockito.doReturn( name ).when( component ).toString();

        return component;
    }

    protected CONFIG getMockedConfiguration( String name, List<COMPONENT> components ) {
        CONFIG configuration = Mockito.mock( getConfigurationClass() );
        Mockito.doReturn( configuration ).when( configuration ).clone();
        Mockito.doReturn( components ).when( configuration ).getList();
        Mockito.doReturn( name ).when( configuration ).toString();

        return configuration;
    }

    protected CONFIG getSpiedConfiguration( String name, List<COMPONENT> components ) {
        CONFIG configuration = getDefaultConfiguration();
        CONFIG spy = Mockito.spy( configuration );
        spy.getList().addAll( components );

        return spy;
    }

    protected CONFIG getConfiguration( String name, List<COMPONENT> components ) {
        CONFIG configuration = getDefaultConfiguration();
        configuration.getList().addAll( components );

        return configuration;
    }
}
