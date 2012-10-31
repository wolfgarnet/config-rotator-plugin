package net.praqma.jenkins.configrotator.scm.clearcase;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.jenkins.configrotator.ConfigRotatorRule;
import net.praqma.jenkins.configrotator.SystemValidator;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMTarget;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class NewModel {

    @Rule
    private ClearCaseRule ccenv =  new ClearCaseRule( "cr1" );

    @Rule
    private ConfigRotatorRule crrule = new ConfigRotatorRule( "cr-test", ccenv.getPVob() ).
            addTarget( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
            addTarget( new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) );

    @Test
    public void test1() throws IOException, ExecutionException, InterruptedException {
        AbstractBuild<?, ?> build = crrule.build( false );

        SystemValidator<ClearCaseUCMTarget> val = new SystemValidator<ClearCaseUCMTarget>( build );
        val.setExpectedResult( Result.SUCCESS ).setCompatability( true );
    }
}
