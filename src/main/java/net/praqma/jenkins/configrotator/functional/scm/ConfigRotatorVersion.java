package net.praqma.jenkins.configrotator.functional.scm;


import java.io.Serializable;

public class ConfigRotatorVersion implements Serializable {
    private String file;
    private String name;
    private String user;

    public ConfigRotatorVersion() {
    }

    public ConfigRotatorVersion( String file, String name, String user ) {
        this.file = file;
        this.name = name;
        this.user = user;
    }

    public String getFile() {
        return file;
    }

    public void setFile( String file ) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser( String user ) {
        this.user = user;
    }

    @Override
    public String toString() {
        return file + ", " + name + ", " + user;
    }
}
