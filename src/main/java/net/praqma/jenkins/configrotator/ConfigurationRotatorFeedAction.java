package net.praqma.jenkins.configrotator;

import hudson.model.Action;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public abstract class ConfigurationRotatorFeedAction implements Action {

    private static Logger logger = Logger.getLogger( ConfigurationRotatorFeedAction.class.getName() );

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    public abstract String getComponentName();

    /*
    public ArrayList<File> getComponents() {
        return getComponents( new String[]{} );
    }
    */

    public String getFeedUrl( String component, String element ) {
        return ConfigurationRotator.FEED_URL + getComponentName() + "/feed?component=" + component + "&element=" + element;
    }

    public String getElementName( String fileName ) {
        return fileName.substring( 0, fileName.lastIndexOf( "." ) );
    }

    public ArrayList<File> getComponents() {
        FileFilter listDirsFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };
        return getComponents( null, listDirsFilter );
    }

    public ArrayList<File> getElements( String component ) {
        FileFilter xmlFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith( ".xml" );
            }
        };
        return getComponents( Collections.singletonList( component ), xmlFilter );
    }


    public ArrayList<File> getComponents( List<String> elements, FileFilter filter ) {
        if( elements == null) {
            elements = Collections.emptyList();
        }

        logger.info("ELEMENTS: " + elements);



        ArrayList<File> list = new ArrayList<File>();

        File path = new File( ConfigurationRotator.FEED_PATH, getComponentName() );
        for( String e : elements ) {
            path = new File( path, e );
        }

        logger.info("PATH: " + path);

        for( File f : path.listFiles( filter ) ) {
            list.add( f );
        }

        logger.info("LIST: " + list);

        return list;
    }

    protected abstract File getFeedFile( StaplerRequest req );

    public void doFeed( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        File file = getFeedFile(req);
        if( file != null && file.exists() ) {
            rsp.serveFile( req, FileUtils.openInputStream( file ), file.lastModified(), file.getTotalSpace(), file.getName() );
        } else {
            rsp.sendError( HttpServletResponse.SC_NOT_FOUND );
        }
    }
}
