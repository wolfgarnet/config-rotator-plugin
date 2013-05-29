package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Project;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author cwolfgang
 */
public class DetermineProject implements FilePath.FileCallable<Project> {

    private List<String> projects;
    private PVob pvob;

    public DetermineProject( List<String> projects, PVob pvob ) {
        this.projects = projects;
        this.pvob = pvob;
    }

    @Override
    public Project invoke( File f, VirtualChannel channel ) throws IOException, InterruptedException {
        for( String project : projects ) {
            try {
                Project ucmproject = Project.get( project, pvob ).load();
                return ucmproject;
            } catch( ClearCaseException e ) {
                /* Not a valid project */
            } catch( NullPointerException e ) {
                /* project was probably null, which is allowable */
            }
        }

        throw new IOException( "No such project" );
    }

}
