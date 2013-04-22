package net.praqma.jenkins.configrotator.functional.scm.dvcs;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import net.praqma.jenkins.configrotator.*;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCS;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSTarget;
import net.praqma.logging.PraqmaticLogFormatter;
import net.praqma.util.test.junit.LoggingRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * @author cwolfgang
 */
public abstract class BaseTest<T extends Object, TARGET extends BaseDVCSTarget> {

    public abstract BaseDVCS getSCM();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @ClassRule
    public static ConfigRotatorRule2 crRule = new ConfigRotatorRule2( BaseTest.class );

    @ClassRule
    public static LoggingRule lrule = new LoggingRule( Level.ALL, "net.praqma" ).setFormat( PraqmaticLogFormatter.NORMAL_FORMAT );

    protected DVCSRule<T> drule;

    public BaseTest( DVCSRule drule ) {
        this.drule = drule;
    }

    public abstract TARGET getTarget( String name, String repository, String branch, String commitId, boolean fixed );
    public abstract String getRevision( T commit );
    public abstract String getDefaultBranchName();

    @Test
    public void basic() throws Exception {
        drule.initialize( folder.newFolder() );
        T commit1 = drule.createCommit( "text.txt", "1" );

        ProjectBuilder builder = new ProjectBuilder( getSCM() ).setName( this.getClass().getSimpleName() + "-01" );
        ConfigRotatorProject project = builder.getProject();
        project.addTarget( getTarget( "test", drule.getRepo(), getDefaultBranchName(), getRevision( commit1 ), false ) );

        AbstractBuild<?, ?> build = crRule.buildProject( project.getJenkinsProject(), false, null );

        FilePath path = new FilePath( project.getJenkinsProject().getLastBuiltOn().getWorkspaceFor( (FreeStyleProject)project.getJenkinsProject() ), "test" );
        File filePath = new File( path.toURI() );

        SystemValidator<TARGET> val = new SystemValidator<TARGET>( build );
        val.checkExpectedResult( Result.SUCCESS ).
                checkAction( true ).
                checkCompatability( true ).
                checkTargets( getTarget( "test", drule.getRepo(), getDefaultBranchName(), getRevision( commit1 ), false ) ).
                checkContent( new File( filePath, "text.txt" ), "1" ).
                validate();
    }


    @Test
    public void basicNothingToDo() throws Exception {
        drule.initialize( folder.newFolder() );
        T commit1 = drule.createCommit( "text.txt", "1" );

        ProjectBuilder builder = new ProjectBuilder( getSCM() ).setName( "git-test-02" );
        ConfigRotatorProject project = builder.getProject();
        project.addTarget( getTarget( "test", drule.getRepo(), getDefaultBranchName(), getRevision( commit1 ), false ) );

        AbstractBuild<?, ?> build = crRule.buildProject( project.getJenkinsProject(), false, null );

        FilePath path = new FilePath( project.getJenkinsProject().getLastBuiltOn().getWorkspaceFor( (FreeStyleProject)project.getJenkinsProject() ), "test" );
        File filePath = new File( path.toURI() );

        SystemValidator<TARGET> val = new SystemValidator<TARGET>( build );
        val.checkExpectedResult( Result.SUCCESS ).
                checkAction( true ).
                checkCompatability( true ).
                checkTargets( getTarget( "test", drule.getRepo(), getDefaultBranchName(), getRevision( commit1 ), false ) ).
                checkContent( new File( filePath, "text.txt" ), "1" ).
                validate();

        AbstractBuild<?, ?> build2 = crRule.buildProject( project.getJenkinsProject(), false, null );

        SystemValidator<TARGET> val2 = new SystemValidator<TARGET>( build2 );
        val2.checkExpectedResult( Result.NOT_BUILT ).checkAction( false ).validate();
    }


    @Test
    public void basicMultiple() throws Exception {
        drule.initialize( folder.newFolder() );
        T commit1 = drule.createCommit( "text.txt", "1" );
        T commit2 = drule.createCommit( "text.txt", "2" );

        ProjectBuilder builder = new ProjectBuilder( getSCM() ).setName( "git-test-03" );
        ConfigRotatorProject project = builder.getProject();
        project.addTarget( getTarget( "test", drule.getRepo(), getDefaultBranchName(), getRevision( commit1 ), false ) );

        AbstractBuild<?, ?> build1 = crRule.buildProject( project.getJenkinsProject(), false, null );
        AbstractBuild<?, ?> build2 = crRule.buildProject( project.getJenkinsProject(), false, null );

        FilePath path = new FilePath( project.getJenkinsProject().getLastBuiltOn().getWorkspaceFor( (FreeStyleProject)project.getJenkinsProject() ), "test" );
        File filePath = new File( path.toURI() );

        SystemValidator<TARGET> val2 = new SystemValidator<TARGET>( build2 );
        val2.checkExpectedResult( Result.SUCCESS ).
                checkAction( true ).
                checkCompatability( true ).
                checkTargets( getTarget( "test", drule.getRepo(), "master", getRevision( commit2 ) , false ) ).
                checkContent( new File( filePath, "text.txt" ), "2" ).
                validate();

        AbstractBuild<?, ?> build3 = crRule.buildProject( project.getJenkinsProject(), false, null );

        SystemValidator<TARGET> val3 = new SystemValidator<TARGET>( build3 );
        val3.checkExpectedResult( Result.NOT_BUILT ).validate();
    }
}
