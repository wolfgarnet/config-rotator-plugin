package net.praqma.jenkins.configrotator.functional.scm.clearcase;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.jenkins.configrotator.ConfigRotatorProject;
import net.praqma.jenkins.configrotator.ConfigRotatorRule2;
import net.praqma.jenkins.configrotator.ProjectBuilder;
import net.praqma.jenkins.configrotator.SystemValidator;
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

/**
 * @author cwolfgang
 */
public class FB8790 {

    private static Logger logger = Logger.getLogger( FB8790.class.getName() );

    public static ClearCaseRule ccenv = new ClearCaseRule( "FB8790" );

    public static LoggingRule lrule = new LoggingRule( "net.praqma" ).setFormat( PraqmaticLogFormatter.TINY_FORMAT );

    @ClassRule
    public static TestRule chain = RuleChain.outerRule( lrule ).around( ccenv );

    @ClassRule
    public static ConfigRotatorRule2 crrule = new ConfigRotatorRule2( FB8790.class );

    @Test
    public void removeTarget() throws IOException, ExecutionException, InterruptedException {
        ProjectBuilder builder = new ProjectBuilder( new ClearCaseUCM( ccenv.getPVob() ) ).setName( "remove-target" );
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


        project.reconfigure().addTarget( new ClearCaseUCMTarget( "model-2@" + ccenv.getPVob() + ", INITIAL, false" ) );

        /* Do the second build */
        AbstractBuild<?, ?> build2 = crrule.buildProject( project.getJenkinsProject(), false, null );

        /* Verify second build */
        SystemValidator<ClearCaseUCMTarget> val2 = new SystemValidator<ClearCaseUCMTarget>( build2 );
        val2.checkExpectedResult( Result.SUCCESS ).
                checkCompatability( true ).
                checkTargets( new ClearCaseUCMTarget( "model-2@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                validate();
    }
}
