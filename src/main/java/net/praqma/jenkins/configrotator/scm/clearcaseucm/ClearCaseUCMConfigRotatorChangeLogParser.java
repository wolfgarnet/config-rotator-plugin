/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.util.Digester2;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogParser;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

/**
 *
 * @author Praqma
 */
public class ClearCaseUCMConfigRotatorChangeLogParser extends ConfigRotatorChangeLogParser{

    @Override
    public ChangeLogSet<? extends Entry> parse(AbstractBuild build, File changelogFile) throws IOException, SAXException {
        Digester digester = new Digester2();
        List<ClearCaseUCMConfigRotatorEntry> changesetList = new ArrayList<ClearCaseUCMConfigRotatorEntry>();
        digester.push(changesetList);
        
        /**
         * Do stuff 
         */ 
        
        
        FileReader reader = new FileReader(changelogFile);
        digester.parse(reader);
        reader.close();
        
        ClearCaseUCMConfigRotatorChangeLogSet clogSet = new ClearCaseUCMConfigRotatorChangeLogSet(build, changesetList);
        return clogSet;
        
    }
}
