package net.praqma.jenkins.configrotator;

import java.io.IOException;

/**
 * @author cwolfgang
 */
public class ProjectBuilder {

    private String name = "Config-Rotator-project";
    private AbstractConfigurationRotatorSCM scm;

    public ProjectBuilder( AbstractConfigurationRotatorSCM scm ) {
        this.scm = scm;
    }

    public ProjectBuilder setName( String name ) {
        this.name = name;

        return this;
    }


    public ConfigRotatorProject getProject() throws IOException {

        return new ConfigRotatorProject( name, scm );
    }
}
