package net.praqma.jenkins.configrotator.scm.clearcase;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.jenkins.configrotator.ConfigRotatorRule;
import net.praqma.jenkins.configrotator.SystemValidator;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMTarget;
import net.praqma.util.test.junit.LoggingRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class NewModel {

    public static ClearCaseRule ccenv =  new ClearCaseRule( "cr1" );

    public static LoggingRule lrule = new LoggingRule( Level.ALL, "net.praqma" );

    @ClassRule
    public static TestRule chain = RuleChain.outerRule( lrule ).around( ccenv );

    @Rule
    public ConfigRotatorRule crrule = new ConfigRotatorRule();

    @Test
    @ClearCaseUniqueVobName( name = "config-testv2" )
    public void test1() throws IOException, ExecutionException, InterruptedException {

        crrule.initialize( "cr-test", ccenv.getPVob() ).
                addTarget( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addTarget( new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) );

        AbstractBuild<?, ?> build = crrule.build( false );
        crrule.printLog( build, System.out );

        SystemValidator<ClearCaseUCMTarget> val = new SystemValidator<ClearCaseUCMTarget>( build );
        val.checkExpectedResult( Result.SUCCESS ).
                checkCompatability( true ).
                checkTargets( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ), new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                validate();
    }

    @Test
    @ClearCaseUniqueVobName( name = "reconfig-testv2" )
    public void reconfigure() throws IOException, ExecutionException, InterruptedException {
        crrule.initialize( "cr-test", ccenv.getPVob() ).
                addTarget( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addTarget( new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) );

        /* Do the first build */
        AbstractBuild<?, ?> build1 = crrule.build( false );
        crrule.printLog( build1, System.out );

        /* Verify first build */
        SystemValidator<ClearCaseUCMTarget> val = new SystemValidator<ClearCaseUCMTarget>( build1 );
        val.checkExpectedResult( Result.SUCCESS ).checkCompatability( true ).checkWasReconfigured( false ).validate();

        crrule.reconfigure().
                addTarget( new ClearCaseUCMTarget( "model-3@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addTarget( new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) );

        SystemValidator<ClearCaseUCMTarget> reval = new SystemValidator<ClearCaseUCMTarget>( build1 );
        reval.checkWasReconfigured( true ).validate();

        /* Do the second build */
        AbstractBuild<?, ?> build2 = crrule.build( false );
        crrule.printLog( build2, System.out );

        /* Verify first build */
        SystemValidator<ClearCaseUCMTarget> val2 = new SystemValidator<ClearCaseUCMTarget>( build2 );
        val2.checkExpectedResult( Result.SUCCESS ).
                checkCompatability( true ).
                checkWasReconfigured( false ).
                checkTargets( new ClearCaseUCMTarget( "model-3@" + ccenv.getPVob() + ", INITIAL, false" ), new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                validate();
    }


    @Test
    @ClearCaseUniqueVobName( name = "wrongtargets-config-testv2" )
    public void wrongTargets() throws IOException, ExecutionException, InterruptedException {

        crrule.initialize( "cr-test", ccenv.getPVob() ).
                addTarget( new ClearCaseUCMTarget( "model-wrong@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addTarget( new ClearCaseUCMTarget( "client-wrong@" + ccenv.getPVob() + ", INITIAL, false" ) );

        AbstractBuild<?, ?> build = crrule.build( false );
        crrule.printLog( build, System.out );

        SystemValidator<ClearCaseUCMTarget> val = new SystemValidator<ClearCaseUCMTarget>( build );
        val.checkExpectedResult( Result.FAILURE ).
                checkAction( false ).
                validate();
    }
}
