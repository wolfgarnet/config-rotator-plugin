package net.praqma.jenkins.configrotator.functional.scm.clearcase;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
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
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class JENKINS17830 {

    private static Logger logger = Logger.getLogger( JENKINS17830.class.getName() );

    public static ClearCaseRule ccenv = new ClearCaseRule( "JENKINS17830", "setup-rootless.xml" );

    public static LoggingRule lrule = new LoggingRule( "net.praqma" ).setFormat( PraqmaticLogFormatter.TINY_FORMAT );

    @ClassRule
    public static TestRule chain = RuleChain.outerRule( lrule ).around( ccenv );

    @ClassRule
    public static ConfigRotatorRule2 crrule = new ConfigRotatorRule2( JENKINS17830.class );

    @Test
    public void test() throws IOException, InterruptedException {
        ProjectBuilder builder = new ProjectBuilder( new ClearCaseUCM( ccenv.getPVob() ) ).setName( "config-spec" );
        ConfigRotatorProject project = builder.getProject();
        project.addTarget( new ClearCaseUCMTarget( "a-baseline-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addTarget( new ClearCaseUCMTarget( "b-baseline-1@" + ccenv.getPVob() + ", INITIAL, false" ) );

        AbstractBuild<?, ?> build = crrule.buildProject( project.getJenkinsProject(), false, null );

        FilePath path = new FilePath( project.getJenkinsProject().getLastBuiltOn().getWorkspaceFor( (FreeStyleProject)project.getJenkinsProject() ), "view/" + ccenv.getUniqueName() );
        listPath( path );

        SystemValidator<ClearCaseUCMTarget> val = new SystemValidator<ClearCaseUCMTarget>( build );
        val.checkExpectedResult( Result.SUCCESS ).
                checkAction( true ).
                checkTargets( new ClearCaseUCMTarget( "a-baseline-1@" + ccenv.getPVob() + ", INITIAL, false" ), new ClearCaseUCMTarget( "b-baseline-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addElementToPathCheck( path, new SystemValidator.Element( "a-server", true ) ).
                addElementToPathCheck( path, new SystemValidator.Element( "a-client", true ) ).
                addElementToPathCheck( path, new SystemValidator.Element( "b-server", true ) ).
                addElementToPathCheck( path, new SystemValidator.Element( "b-client", true ) ).
                checkCompatability( true ).
                validate();

        project.reconfigure().addTarget( new ClearCaseUCMTarget( "a-baseline-1@" + ccenv.getPVob() + ", INITIAL, false" ) );

        AbstractBuild<?, ?> build2 = crrule.buildProject( project.getJenkinsProject(), false, null );

        listPath( path );

        SystemValidator<ClearCaseUCMTarget> val2 = new SystemValidator<ClearCaseUCMTarget>( build2 );
        val2.checkExpectedResult( Result.SUCCESS ).
                checkAction( true ).
                checkTargets( new ClearCaseUCMTarget( "a-baseline-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addElementToPathCheck( path, new SystemValidator.Element( "a-server", true ) ).
                addElementToPathCheck( path, new SystemValidator.Element( "a-client", true ) ).
                addElementToPathCheck( path, new SystemValidator.Element( "b-server", true ) ).
                addElementToPathCheck( path, new SystemValidator.Element( "b-client", true ) ).
                checkCompatability( true ).
                validate();
    }

    protected void listPath( FilePath path ) throws IOException, InterruptedException {
        logger.info( "Listing " + path + "(" + path.exists() + ")" );
        for( FilePath f : path.list() ) {
            logger.info( " * " + f );
        }
    }
}
