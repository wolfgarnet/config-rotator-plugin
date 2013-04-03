package net.praqma.jenkins.configrotator.functional;

import hudson.model.AbstractBuild;
import hudson.model.Project;
import hudson.model.Result;
import net.praqma.jenkins.configrotator.AbstractTarget;
import net.praqma.jenkins.configrotator.ConfigRotatorRule2;
import net.praqma.jenkins.configrotator.ProjectBuilder;
import net.praqma.jenkins.configrotator.SystemValidator;
import net.praqma.jenkins.configrotator.fake.*;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author cwolfgang
 */
public class ModelTest {

    @ClassRule
    public static ConfigRotatorRule2 crRule = new ConfigRotatorRule2();

    @Test
    public void newProject() throws IOException {
        FakeSCM scm = new FakeSCM();
        ProjectBuilder builder = new ProjectBuilder( scm );

        Project project = builder.getProject().getJenkinsProject();

        AbstractBuild build = crRule.buildProject( project, false, null );

        new SystemValidator<AbstractTarget>( build ).checkAction( true ).checkCompatability( true ).checkExpectedResult( Result.SUCCESS ).checkWasReconfigured( true ).validate();
    }

    @Test
    public void newProjectIncompatible() throws IOException {
        FakeSCM scm = new FakeSCM();
        ProjectBuilder builder = new ProjectBuilder( scm ).setName( "Project02" );

        Project project = builder.getProject().getJenkinsProject();

        AbstractBuild build = crRule.buildProject( project, true, null );

        new SystemValidator<AbstractTarget>( build ).checkAction( true ).checkCompatability( false ).checkExpectedResult( Result.FAILURE ).checkWasReconfigured( true ).validate();
    }

    @Test
    public void newProjectTwoBuilds() throws IOException {
        FakeSCM scm = new FakeSCM();
        ProjectBuilder builder = new ProjectBuilder( scm ).setName( "Project03" );

        Project project = builder.getProject().getJenkinsProject();

        AbstractBuild build1 = crRule.buildProject( project, false, null );
        AbstractBuild build2 = crRule.buildProject( project, false, null );

        new SystemValidator<AbstractTarget>( build2 ).checkAction( true ).checkCompatability( true ).checkExpectedResult( Result.SUCCESS ).checkWasReconfigured( false ).validate();
    }
}
