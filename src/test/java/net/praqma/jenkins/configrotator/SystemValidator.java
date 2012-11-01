package net.praqma.jenkins.configrotator;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.scm.SCM;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class SystemValidator<T extends AbstractTarget> {

    private ConfigurationRotator cr;
    private AbstractBuild<?, ?> build;
    private ConfigurationRotatorBuildAction action;
    private PrintStream out;

    /**/
    private Result expectedResult;
    private boolean checkExpectedResult = false;

    /**/
    private boolean compatible;
    private boolean checkCompatible = false;

    /**/
    private boolean wasReconfigured;
    private boolean checkWasReconfigured = false;

    /**/
    private List<AbstractTarget> targets = new LinkedList<AbstractTarget>();
    private boolean checkTargets = false;

    public SystemValidator( AbstractBuild<?, ?> build ) {
        this( build, System.out );
    }

    public SystemValidator( AbstractBuild<?, ?> build, PrintStream out ) {
        this.build = build;
        this.out = out;

        SCM scm = build.getProject().getScm();
        if( scm instanceof ConfigurationRotator ) {
            this.cr = (ConfigurationRotator) scm;
        } else {
            throw new IllegalStateException( build.getProject().getScm() + " is not ConfigRotator" );
        }

        action = build.getAction( ConfigurationRotatorBuildAction.class );
    }

    public void validate() {

        if( this.checkExpectedResult ) {
            out.println( "[Validating expected result] must be " + this.expectedResult );
            assertThat( "Validating expected result", build.getResult(), is( this.expectedResult ) );
        }

        if( this.checkExpectedResult ) {
            out.println( "[Validating compatability] must be " + ( this.compatible ? "compatible" : "incompatible" ) );
            assertThat( "Validating compatability", action.isCompatible(), is( this.compatible ) );
        }

        if( this.checkWasReconfigured ) {
            out.println( "[Validating reconfigured] must be " + this.wasReconfigured );
            assertThat( "Validating reconfiguration", cr.getAcrs().wasReconfigured( build.getProject() ), is( this.wasReconfigured) );
        }

        if( this.checkTargets ) {
            out.println( "[Validating targets] must be " + this.targets );
            for( int i = 0 ; i < this.targets.size() ; i++ ) {
                out.println( " * " + cr.getAcrs().getTargets().get( i ) + " == " + is( this.targets.get( i ) ) );
                assertThat( "Validating target", cr.getAcrs().getTargets().get( i ), is( this.targets.get( i ) ) );
            }
        }

        out.println( "Successfully validated system" );
    }

    public SystemValidator setExpectedResult( Result expectedResult ) {
        this.expectedResult = expectedResult;
        this.checkExpectedResult = true;

        return this;
    }

    public SystemValidator setCompatability( boolean compatible ) {
        this.compatible = compatible;
        this.checkCompatible = true;

        return this;
    }

    public SystemValidator setWasReconfigured( boolean wasReconfigured ) {
        this.wasReconfigured = wasReconfigured;
        this.checkWasReconfigured = true;

        return this;
    }

    public SystemValidator setTargets( T ... targets ) {
        for( T t : targets ) {
            this.targets.add( t );
        }
        this.checkTargets = true;

        return this;
    }
}
