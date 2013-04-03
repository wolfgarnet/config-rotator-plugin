package net.praqma.jenkins.configrotator;

import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.model.TopLevelItem;
import hudson.scm.SCM;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMTarget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author cwolfgang
 */
public class ConfigRotatorProject {

    private Project project;
    private AbstractConfigurationRotatorSCM crSCM;

    private List<AbstractTarget> targets = new ArrayList<AbstractTarget>();

    Class<? extends TopLevelItem> projectClass = FreeStyleProject.class;

    private Project<?, ?> jenkinsProject;

    public ConfigRotatorProject( String name, AbstractConfigurationRotatorSCM crSCM ) throws IOException {
        this.crSCM = crSCM;

        crSCM.setTargets( targets );

        SCM scm = new ConfigurationRotator( crSCM );
        jenkinsProject = (Project) Hudson.getInstance().createProject( projectClass, name );
        jenkinsProject.setScm( scm );
    }

    public ConfigurationRotator getConfigurationRotator() {
        return (ConfigurationRotator) jenkinsProject.getScm();
    }

    public ConfigRotatorProject reconfigure() {

        targets = new ArrayList<AbstractTarget>();

        crSCM.setConfiguration( null );
        crSCM.setTargets( targets );

        return this;
    }

    public ConfigRotatorProject addTarget( AbstractTarget target ) {
        targets.add( target );

        return this;
    }

    public Project<?, ?> getJenkinsProject() {
        return jenkinsProject;
    }
}
