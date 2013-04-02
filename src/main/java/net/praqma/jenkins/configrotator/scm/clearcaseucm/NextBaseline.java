package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.clearcase.ucm.utils.filters.AfterBaseline;
import net.praqma.clearcase.ucm.utils.filters.NoDeliver;
import net.praqma.clearcase.ucm.utils.filters.NoLabels;

import java.io.File;
import java.io.IOException;

/**
 * @author cwolfgang
 */
public class NextBaseline implements FilePath.FileCallable<Baseline> {

    private Stream stream;
    private Component component;
    private Project.PromotionLevel level;
    private Baseline offset;

    public NextBaseline( Stream stream, Component component, Project.PromotionLevel level, Baseline offset ) {
        this.stream = stream;
        this.component = component;
        this.level = level;
        this.offset = offset;
    }

    @Override
    public Baseline invoke( File f, VirtualChannel channel ) throws IOException, InterruptedException {
        BaselineList list = new BaselineList( stream, component, level ).
                addFilter( new AfterBaseline( offset ) ).
                addFilter( new NoDeliver() ).
                addFilter( new NoLabels() ).
                setSorting( new BaselineList.AscendingDateSort() ).
                load().
                setLimit( 1 );

        try {
            list.apply();
            return list.get( 0 );
        } catch( Exception e ) {
            throw new IOException( e );
        }
    }
}
