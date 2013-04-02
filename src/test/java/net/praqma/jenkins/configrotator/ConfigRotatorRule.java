package net.praqma.jenkins.configrotator;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import hudson.tasks.Builder;
import net.praqma.clearcase.PVob;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCM;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMTarget;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

public class ConfigRotatorRule extends JenkinsRule {

	public FreeStyleProject createProject( String title ) throws IOException {
		return createFreeStyleProject( title );
	}

    private FreeStyleProject project;
    private ConfigurationRotator cr;
    private ClearCaseUCM ccucm;

    public ConfigRotatorRule() {

    }

    public ConfigRotatorRule initialize( String title, PVob pvob ) {
        System.out.println( "PVOB: " + pvob );
        System.out.println( "Title: " + title );
        System.out.println( "Jenkins: " + jenkins );
        try {
            project = createFreeStyleProject( title );
            System.out.println( "Project: " + project );

            ccucm = new ClearCaseUCM( pvob.toString() );
            System.out.println( "CCUCM: " + ccucm );

            ccucm.targets = new LinkedList<ClearCaseUCMTarget>();

            cr = new ConfigurationRotator( ccucm );
            project.setScm( cr );

            return this;
        } catch( Exception e ) {
            e.printStackTrace();
            throw new IllegalStateException( e );
        }
    }

    public ConfigRotatorRule addTarget( ClearCaseUCMTarget target ) {
        this.ccucm.targets.add( target );

        return this;
    }

    public FreeStyleBuild build( boolean fail ) throws ExecutionException, InterruptedException, IOException {
        if( fail ) {
            project.getBuildersList().add( new TestBuilder() {
                @Override
                public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener ) throws InterruptedException, IOException {
                    return false;
                }
            } );
        } else {
            Iterator<Builder> it = project.getBuildersList().iterator();
            while( it.hasNext() ) {
                Builder b = it.next();
                if( b instanceof TestBuilder ) {
                    System.out.println( "Removing test builder" );
                    it.remove();
                }
            }
        }

        return project.scheduleBuild2( 0 ).get();
    }

    public ConfigRotatorRule reconfigure() {
        cr.reconfigure = true;
        ccucm.setConfiguration( null );
        this.ccucm.targets = new LinkedList<ClearCaseUCMTarget>();

        return this;
    }

    public void printLog( AbstractBuild<?, ?> b, PrintStream out ) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(b.getLogFile()));
        String line = "";
        while ((line = br.readLine()) != null) {
            out.println("[JENKINS] " + line);
        }
        br.close();
    }
}
