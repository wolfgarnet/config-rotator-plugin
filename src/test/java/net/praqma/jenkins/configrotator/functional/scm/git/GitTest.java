package net.praqma.jenkins.configrotator.functional.scm.git;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import net.praqma.jenkins.configrotator.*;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMTarget;
import net.praqma.jenkins.configrotator.scm.git.Git;
import net.praqma.jenkins.configrotator.scm.git.GitTarget;
import net.praqma.logging.PraqmaticLogFormatter;
import net.praqma.util.test.junit.LoggingRule;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class GitTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public GitRule git = new GitRule();

    @ClassRule
    public static LoggingRule lrule = new LoggingRule( Level.ALL, "net.praqma" ).setFormat( PraqmaticLogFormatter.NORMAL_FORMAT );

    @ClassRule
    public static ConfigRotatorRule2 crRule = new ConfigRotatorRule2( GitTest.class );

    @Test
    public void basic() throws IOException, GitAPIException, InterruptedException {
        git.initialize( folder.newFolder() );
        RevCommit commit1 = git.createCommit( "text.txt", "1" );

        ProjectBuilder builder = new ProjectBuilder( new Git() ).setName( "git-test-01" );
        ConfigRotatorProject project = builder.getProject();
        project.addTarget( new GitTarget( "test", git.getRepo(), "master", commit1.getName(), false ) );

        AbstractBuild<?, ?> build = crRule.buildProject( project.getJenkinsProject(), false, null );

        FilePath path = new FilePath( project.getJenkinsProject().getLastBuiltOn().getWorkspaceFor( (FreeStyleProject)project.getJenkinsProject() ), "test" );
        File filePath = new File( path.toURI() );

        SystemValidator<GitTarget> val = new SystemValidator<GitTarget>( build );
        val.checkExpectedResult( Result.SUCCESS ).
                checkAction( true ).
                checkCompatability( true ).
                checkTargets( new GitTarget( "test", git.getRepo(), "master", commit1.getName(), false ) ).
                checkContent( new File( filePath, "text.txt" ), "1" ).
                validate();
    }

    @Test
    public void basicNothingToDo() throws IOException, GitAPIException, InterruptedException {
        git.initialize( folder.newFolder() );
        RevCommit commit1 = git.createCommit( "text.txt", "1" );

        ProjectBuilder builder = new ProjectBuilder( new Git() ).setName( "git-test-02" );
        ConfigRotatorProject project = builder.getProject();
        project.addTarget( new GitTarget( "test", git.getRepo(), "master", commit1.getName(), false ) );

        AbstractBuild<?, ?> build = crRule.buildProject( project.getJenkinsProject(), false, null );

        FilePath path = new FilePath( project.getJenkinsProject().getLastBuiltOn().getWorkspaceFor( (FreeStyleProject)project.getJenkinsProject() ), "test" );
        File filePath = new File( path.toURI() );

        SystemValidator<GitTarget> val = new SystemValidator<GitTarget>( build );
        val.checkExpectedResult( Result.SUCCESS ).
                checkAction( true ).
                checkCompatability( true ).
                checkTargets( new GitTarget( "test", git.getRepo(), "master", commit1.getName(), false ) ).
                checkContent( new File( filePath, "text.txt" ), "1" ).
                validate();

        AbstractBuild<?, ?> build2 = crRule.buildProject( project.getJenkinsProject(), false, null );

        SystemValidator<ClearCaseUCMTarget> val2 = new SystemValidator<ClearCaseUCMTarget>( build2 );
        val2.checkExpectedResult( Result.NOT_BUILT ).checkAction( false ).validate();
    }


    @Test
    public void basicMultiple() throws IOException, GitAPIException, InterruptedException {
        git.initialize( folder.newFolder() );
        RevCommit commit1 = git.createCommit( "text.txt", "1" );
        RevCommit commit2 = git.createCommit( "text.txt", "2" );

        ProjectBuilder builder = new ProjectBuilder( new Git() ).setName( "git-test-03" );
        ConfigRotatorProject project = builder.getProject();
        project.addTarget( new GitTarget( "test", git.getRepo(), "master", commit1.getName(), false ) );

        AbstractBuild<?, ?> build1 = crRule.buildProject( project.getJenkinsProject(), false, null );
        AbstractBuild<?, ?> build2 = crRule.buildProject( project.getJenkinsProject(), false, null );

        FilePath path = new FilePath( project.getJenkinsProject().getLastBuiltOn().getWorkspaceFor( (FreeStyleProject)project.getJenkinsProject() ), "test" );
        File filePath = new File( path.toURI() );

        SystemValidator<GitTarget> val2 = new SystemValidator<GitTarget>( build2 );
        val2.checkExpectedResult( Result.SUCCESS ).
                checkAction( true ).
                checkCompatability( true ).
                checkTargets( new GitTarget( "test", git.getRepo(), "master", commit2.getName() , false ) ).
                checkContent( new File( filePath, "text.txt" ), "2" ).
                validate();

        AbstractBuild<?, ?> build3 = crRule.buildProject( project.getJenkinsProject(), false, null );

        SystemValidator<ClearCaseUCMTarget> val3 = new SystemValidator<ClearCaseUCMTarget>( build3 );
        val3.checkExpectedResult( Result.NOT_BUILT ).validate();
    }

    //@Test
    public void testing() throws IOException, GitAPIException {

        String commitId = "HEAD";
        //String commitId = "8ecfebfbda136914da727334c6fc94ab56a05ac5";
        //String commitId = "6035e2b582d5547c250dd398d4edc5fedfc57dd8";
        //String commitId = "1d5e0bcaa5b94359fac20e642e3b757989270859";

        String repository = "http://github.com/praqma-test/module1.git";

        File local = new File( "C:\\temp\\configrotater\\test" );

        try {
            org.eclipse.jgit.api.Git.cloneRepository().setURI( repository ).setDirectory( local ).setBare(true).call();
        } catch( JGitInternalException e ) {
            System.out.println( e.getMessage() );
        }

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder.setGitDir(local).readEnvironment().findGitDir().build();
        repo.updateRef("master");
        System.out.println( "REPO: " + repo.getObjectDatabase() );

        ObjectId ohead = repo.resolve( commitId );
        ObjectId o1 = repo.resolve( "8ecfebfbda136914da727334c6fc94ab56a05ac5" );
        ObjectId o2 = repo.resolve( "6035e2b582d5547c250dd398d4edc5fedfc57dd8" );
        ObjectId o3 = repo.resolve( "1d5e0bcaa5b94359fac20e642e3b757989270859" );
        System.out.println( "OBEJCTID: " + ohead.getName() );

        /*
        Git git = new Git( repo );
        Iterable<RevCommit> logs = git.log().add( o ).call();
        System.out.println( "COMMIT: " + logs );
        */

        RevWalk w = new RevWalk( repo );

        RevCommit commithead = w.parseCommit( ohead );
        RevCommit commit1 = w.parseCommit( o1 );
        RevCommit commit2 = w.parseCommit( o2 );
        RevCommit commit3 = w.parseCommit( o3 );

        System.out.println( "1" );
        //w.sort( RevSort.COMMIT_TIME_DESC );
        w.sort( RevSort.REVERSE );
        System.out.println( "2" );
        w.markStart( commithead );
        w.markUninteresting( commit1 );
        System.out.println( "3" );

        System.out.println( "START: " + commit1 );

        /*
        for( RevCommit c : w ) {
            System.out.println( "::: " + c );
        }
        */

        System.out.println( "next: " + w.next() );
    }
}
