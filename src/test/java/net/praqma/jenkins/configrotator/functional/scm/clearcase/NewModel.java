package net.praqma.jenkins.configrotator.functional.scm.clearcase;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.jenkins.configrotator.*;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCM;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMTarget;
import net.praqma.logging.PraqmaticLogFormatter;
import net.praqma.util.test.junit.LoggingRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;

public class NewModel {

    private static Logger logger = Logger.getLogger( NewModel.class.getName() );

    public static ClearCaseRule ccenv = new ClearCaseRule( "cr1" );

    public static LoggingRule lrule = new LoggingRule( "net.praqma" ).setFormat( PraqmaticLogFormatter.TINY_FORMAT );

    @ClassRule
    public static TestRule chain = RuleChain.outerRule( lrule ).around( ccenv );

    @ClassRule
    public static ConfigRotatorRule2 crrule = new ConfigRotatorRule2();

    @Test
    @ClearCaseUniqueVobName( name = "configure-v2" )
    public void test1() throws IOException, ExecutionException, InterruptedException {

        ProjectBuilder builder = new ProjectBuilder( new ClearCaseUCM( ccenv.getPVob() ) ).setName( "project01" );
        ConfigRotatorProject project = builder.getProject();
        project.addTarget( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addTarget( new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) );

        AbstractBuild<?, ?> build = crrule.buildProject( project.getJenkinsProject(), false, null );

        SystemValidator<ClearCaseUCMTarget> val = new SystemValidator<ClearCaseUCMTarget>( build );
        val.checkExpectedResult( Result.SUCCESS ).
                checkAction( true ).
                checkCompatability( true ).
                checkTargets( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ), new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                validate();
    }

    @Test
    @ClearCaseUniqueVobName( name = "fail-v2" )
    public void fail() throws IOException, ExecutionException, InterruptedException {

        ProjectBuilder builder = new ProjectBuilder( new ClearCaseUCM( ccenv.getPVob() ) ).setName( "project-fail" );
        ConfigRotatorProject project = builder.getProject();
        project.addTarget( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addTarget( new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) );

        AbstractBuild<?, ?> build = crrule.buildProject( project.getJenkinsProject(), true, null );

        SystemValidator<ClearCaseUCMTarget> val = new SystemValidator<ClearCaseUCMTarget>( build );
        val.checkExpectedResult( Result.FAILURE ).
                checkAction( true ).
                checkCompatability( false ).
                checkTargets( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ), new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                validate();
    }

    @Test
    @ClearCaseUniqueVobName( name = "reconfigure-v2" )
    public void reconfigure() throws IOException, ExecutionException, InterruptedException {
        ProjectBuilder builder = new ProjectBuilder( new ClearCaseUCM( ccenv.getPVob() ) ).setName( "project02" );
        ConfigRotatorProject project = builder.getProject();

        project.addTarget( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addTarget( new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) );

        AbstractBuild<?, ?> build1 = crrule.buildProject( project.getJenkinsProject(), false, null );

        /* Verify first build */
        SystemValidator<ClearCaseUCMTarget> val = new SystemValidator<ClearCaseUCMTarget>( build1 );
        val.checkExpectedResult( Result.SUCCESS ).checkCompatability( true ).checkWasReconfigured( false ).validate();

        project.reconfigure().
                addTarget( new ClearCaseUCMTarget( "model-3@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addTarget( new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) );

        SystemValidator<ClearCaseUCMTarget> reval = new SystemValidator<ClearCaseUCMTarget>( build1 );
        reval.checkWasReconfigured( true ).validate();

        /* Do the second build */
        AbstractBuild<?, ?> build2 = crrule.buildProject( project.getJenkinsProject(), false, null );

        /* Verify second build */
        SystemValidator<ClearCaseUCMTarget> val2 = new SystemValidator<ClearCaseUCMTarget>( build2 );
        val2.checkExpectedResult( Result.SUCCESS ).
                checkCompatability( true ).
                checkTargets( new ClearCaseUCMTarget( "model-3@" + ccenv.getPVob() + ", INITIAL, false" ), new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                validate();
    }


    @Test
    @ClearCaseUniqueVobName( name = "wrong-targets-configure-v2" )
    public void wrongTargets() throws IOException, ExecutionException, InterruptedException {

        ProjectBuilder builder = new ProjectBuilder( new ClearCaseUCM( ccenv.getPVob() ) ).setName( "project03" );
        ConfigRotatorProject project = builder.getProject();

        project.addTarget( new ClearCaseUCMTarget( "model-wrong@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addTarget( new ClearCaseUCMTarget( "client-wrong@" + ccenv.getPVob() + ", INITIAL, false" ) );

        AbstractBuild<?, ?> build = crrule.buildProject( project.getJenkinsProject(), false, null );

        SystemValidator<ClearCaseUCMTarget> val = new SystemValidator<ClearCaseUCMTarget>( build );
        val.checkExpectedResult( Result.FAILURE ).
                checkAction( false ).
                validate();
    }


    @Test
    @ClearCaseUniqueVobName( name = "revert-configuration-v2" )
    public void revert() throws IOException, ExecutionException, InterruptedException {
        ProjectBuilder builder = new ProjectBuilder( new ClearCaseUCM( ccenv.getPVob() ) ).setName( "project04" );
        ConfigRotatorProject project = builder.getProject();

        project.addTarget( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addTarget( new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) );

        AbstractBuild<?, ?> build1 = crrule.buildProject( project.getJenkinsProject(), false, null );

        /* Verify first build */
        SystemValidator<ClearCaseUCMTarget> val = new SystemValidator<ClearCaseUCMTarget>( build1 );
        val.checkExpectedResult( Result.SUCCESS ).checkCompatability( true ).checkWasReconfigured( false ).validate();

        /* Do the second build */
        AbstractBuild<?, ?> build2 = crrule.buildProject( project.getJenkinsProject(), false, null );

        /* Revert to first configuration */
        ConfigurationRotatorBuildAction action = build1.getAction( ConfigurationRotatorBuildAction.class );
        assertNotNull( action );
        project.getConfigurationRotator().setConfigurationByAction( project.getJenkinsProject(), action );

        /* Do the third build */
        AbstractBuild<?, ?> build3 = crrule.buildProject( project.getJenkinsProject(), false, null );

        /* Verify third build */
        SystemValidator<ClearCaseUCMTarget> val2 = new SystemValidator<ClearCaseUCMTarget>( build3 );
        val2.checkExpectedResult( Result.SUCCESS ).
                checkCompatability( true ).
                checkTargets( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ), new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                validate();
    }



    @Test
    @ClearCaseUniqueVobName( name = "full-v2" )
    public void full() throws IOException, ExecutionException, InterruptedException {
        ProjectBuilder builder = new ProjectBuilder( new ClearCaseUCM( ccenv.getPVob() ) ).setName( "project-full" );
        ConfigRotatorProject project = builder.getProject();

        project.addTarget( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addTarget( new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) );


        for( int i = 1 ; i < 6 ; i++ ) {
            logger.info( "Running build #" + i );
            AbstractBuild<?, ?> build = crrule.buildProject( project.getJenkinsProject(), false, null );

            SystemValidator<ClearCaseUCMTarget> val = new SystemValidator<ClearCaseUCMTarget>( build );
            val.checkExpectedResult( Result.SUCCESS ).checkCompatability( true ).validate();
        }

        /* Do the final build */
        AbstractBuild<?, ?> finalBuild = crrule.buildProject( project.getJenkinsProject(), false, null );

        /* Verify the final build */
        SystemValidator<ClearCaseUCMTarget> val2 = new SystemValidator<ClearCaseUCMTarget>( finalBuild );
        val2.checkExpectedResult( Result.NOT_BUILT ).
                checkAction( false ).
                checkTargets( new ClearCaseUCMTarget( "model-3@" + ccenv.getPVob() + ", INITIAL, false" ), new ClearCaseUCMTarget( "client-3@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                validate();
    }


    @Test
    @ClearCaseUniqueVobName( name = "fixed-targets-v2" )
    public void fixedTargets() throws IOException, ExecutionException, InterruptedException {
        ProjectBuilder builder = new ProjectBuilder( new ClearCaseUCM( ccenv.getPVob() ) ).setName( "project-fixed" );
        ConfigRotatorProject project = builder.getProject();

        project.addTarget( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, true" ) ).
                addTarget( new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, true" ) );

        AbstractBuild<?, ?> build1 = crrule.buildProject( project.getJenkinsProject(), false, null );

        /* Verify first build */
        SystemValidator<ClearCaseUCMTarget> val = new SystemValidator<ClearCaseUCMTarget>( build1 );
        val.checkExpectedResult( Result.SUCCESS ).checkCompatability( true ).validate();

        /* Do the second build */
        AbstractBuild<?, ?> build2 = crrule.buildProject( project.getJenkinsProject(), false, null );

        /* Verify second build */
        SystemValidator<ClearCaseUCMTarget> val2 = new SystemValidator<ClearCaseUCMTarget>( build2 );
        val2.checkExpectedResult( Result.SUCCESS ).
                checkCompatability( true ).
                checkTargets( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ), new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                validate();
    }
}
