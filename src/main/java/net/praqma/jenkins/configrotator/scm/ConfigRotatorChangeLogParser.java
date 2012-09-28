/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;
import hudson.util.Digester2;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Praqma
 */
public class ConfigRotatorChangeLogParser extends ChangeLogParser {
    @Override
    public ChangeLogSet<? extends ChangeLogSet.Entry> parse( AbstractBuild build, File changelogFile ) throws IOException, SAXException {
        Digester digester = new Digester2();
        List<ConfigRotatorChangeLogEntry> changesetList = new ArrayList<ConfigRotatorChangeLogEntry>();
        digester.push( changesetList );
        digester.addObjectCreate( "*/changelog/commit", ConfigRotatorChangeLogEntry.class );
        digester.addSetProperties( "*/changelog/commit" );
        digester.addBeanPropertySetter( "*/changelog/commit/author" );
        digester.addBeanPropertySetter( "*/changelog/commit/commitMessage" );
        digester.addObjectCreate( "*/changelog/commit/versions/", ConfigRotatorVersion.class );
        digester.addBeanPropertySetter( "*/changelog/commit/versions/version/name" );
        digester.addBeanPropertySetter( "*/changelog/commit/versions/version/user" );
        digester.addBeanPropertySetter( "*/changelog/commit/versions/version/file" );

        digester.addSetNext( "*/changelog/activity/versions", "addFilename" );
        digester.addSetNext( "*/changelog/activity", "add" );
        try {
            FileReader reader = new FileReader( changelogFile );
            digester.parse( reader );
            reader.close();
        } catch( SAXException sex ) {
            return new ConfigRotatorChangeLogSet( build );
        }
        ConfigRotatorChangeLogSet clogSet = new ConfigRotatorChangeLogSet( build, changesetList );

        return clogSet;

    }
}
