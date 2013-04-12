package net.praqma.jenkins.configrotator.functional.scm.clearcase;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import net.praqma.clearcase.ConfigSpec;
import net.praqma.clearcase.Rebase;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.GetView;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UpdateView;
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

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

        FilePath path = new FilePath( project.getJenkinsProject().getLastBuiltOn().getWorkspaceFor( (FreeStyleProject)project.getJenkinsProject() ), "view/" + ccenv.getUniqueName() );
        listPath( path );

        SystemValidator<ClearCaseUCMTarget> val = new SystemValidator<ClearCaseUCMTarget>( build );
        val.checkExpectedResult( Result.SUCCESS ).
                checkAction( true ).
                checkCompatability( true ).
                checkTargets( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ), new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addElementToPathCheck( path, new SystemValidator.Element( "Model", true ) ).
                addElementToPathCheck( path, new SystemValidator.Element( "Clientapp", true ) ).
                validate();


        project.reconfigure().addTarget( new ClearCaseUCMTarget( "model-2@" + ccenv.getPVob() + ", INITIAL, false" ) );

        /* Do the second build */
        AbstractBuild<?, ?> build2 = crrule.buildProject( project.getJenkinsProject(), false, null );

        listPath( path );

        /* Verify second build */
        SystemValidator<ClearCaseUCMTarget> val2 = new SystemValidator<ClearCaseUCMTarget>( build2 );
        val2.checkExpectedResult( Result.SUCCESS ).
                checkCompatability( true ).
                checkTargets( new ClearCaseUCMTarget( "model-2@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addElementToPathCheck( path, new SystemValidator.Element( "Model", true ) ).
                addElementToPathCheck( path, new SystemValidator.Element( "Clientapp", false ) ).
                validate();
    }


    @Test
    public void wipedWorkspace() throws IOException, ExecutionException, InterruptedException, ServletException {
        ProjectBuilder builder = new ProjectBuilder( new ClearCaseUCM( ccenv.getPVob() ) ).setName( "wiped-workspace" );
        ConfigRotatorProject project = builder.getProject();
        project.addTarget( new ClearCaseUCMTarget( "model-1@" + ccenv.getPVob() + ", INITIAL, false" ) ).
                addTarget( new ClearCaseUCMTarget( "client-1@" + ccenv.getPVob() + ", INITIAL, false" ) );

        AbstractBuild<?, ?> build = crrule.buildProject( project.getJenkinsProject(), false, null );

        project.getJenkinsProject().doDoWipeOutWorkspace();

        /* Do the second build */
        AbstractBuild<?, ?> build2 = crrule.buildProject( project.getJenkinsProject(), false, null );

        FilePath path = new FilePath( project.getJenkinsProject().getLastBuiltOn().getWorkspaceFor( (FreeStyleProject)project.getJenkinsProject() ), "view/" + ccenv.getUniqueName() );

        /* Verify second build */
        SystemValidator<ClearCaseUCMTarget> val2 = new SystemValidator<ClearCaseUCMTarget>( build2 );
        val2.checkExpectedResult( Result.SUCCESS ).
                checkCompatability( true ).
                addElementToPathCheck( path, new SystemValidator.Element( "Model", true ) ).
                addElementToPathCheck( path, new SystemValidator.Element( "Clientapp", true ) ).
                validate();
    }


    @Test
    public void testView() throws IOException, ClearCaseException, InterruptedException {
        File path = createTempPath();
        String viewTag = ccenv.getUniqueName() + "_TAG";

        Stream oneInt = ccenv.context.streams.get( "one_int" );
        Baseline model1 = ccenv.context.baselines.get( "model-1" );
        Baseline client1 = ccenv.context.baselines.get( "client-1" );

        List<Baseline> bls = new ArrayList<Baseline>( 2 );
        bls.add( model1 );
        bls.add( client1 );
        Stream container = Stream.create( oneInt, "container", true, bls );

        GetView gv = new GetView( path, viewTag ).createIfAbsent().setStream( container );
        SnapshotView view = gv.get();

        SnapshotView.LoadRules lr = new SnapshotView.LoadRules( view, SnapshotView.Components.ALL );
        new UpdateView( view ).setLoadRules( lr ).update();

        /* Verify first */
        FilePath viewroot = new FilePath( view.getViewRoot() );
        FilePath filepath = new FilePath( viewroot, ccenv.getUniqueName() );
        listPath( filepath );

        new SystemValidator().addElementToPathCheck( filepath, new SystemValidator.Element( "Model", true ) ).
                addElementToPathCheck( filepath, new SystemValidator.Element( "Clientapp", true ) ).
                validatePath();


        new Rebase( container ).addBaseline( model1 ).dropFromStream().rebase( true );
        new ConfigSpec( new File( viewroot.toURI() ) ).addLoadRulesFromBaseline( model1 ).generate().appy();

        new UpdateView( view ).update();

        new SystemValidator().addElementToPathCheck( filepath, new SystemValidator.Element( "Model", true ) ).
                addElementToPathCheck( filepath, new SystemValidator.Element( "Clientapp", false ) ).
                validatePath();
    }

    public File createTempPath() throws IOException {
        File path = path = File.createTempFile( "snapshot", "view" );

        if( !path.delete() ) {
            throw new IOException( "Unable to delete dir " + path );
        }

        if( !path.mkdirs() ) {
            throw new IOException( "Unable to make dir " + path );
        }
        System.out.println( "Path: " + path );

        return path;
    }

    protected void listPath( FilePath path ) throws IOException, InterruptedException {
        logger.info( "Listing " + path + "(" + path.exists() + ")" );
        for( FilePath f : path.list() ) {
            logger.info( " * " + f );
        }
    }
}
