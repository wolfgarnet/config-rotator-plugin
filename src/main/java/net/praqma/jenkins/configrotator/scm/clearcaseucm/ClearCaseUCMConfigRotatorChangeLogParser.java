/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.util.Digester2;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogParser;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

/**
 *
 * @author Praqma
 */
public class ClearCaseUCMConfigRotatorChangeLogParser extends ConfigRotatorChangeLogParser {
   
    @Override
    public ChangeLogSet<? extends Entry> parse(AbstractBuild build, File changelogFile) throws IOException, SAXException {
        Digester digester = new Digester2();
        List<ClearCaseUCMConfigRotatorEntry> changesetList = new ArrayList<ClearCaseUCMConfigRotatorEntry>();
        digester.push(changesetList);
        digester.addObjectCreate("*/changelog/activity", ClearCaseUCMConfigRotatorEntry.class);
        digester.addSetProperties("*/changelog/activity");
        digester.addBeanPropertySetter("*/changelog/activity/author");
        digester.addBeanPropertySetter("*/changelog/activity/activityName"); 
        digester.addObjectCreate("*/changelog/activity/versions/", ClearCaseVersion.class);
        digester.addBeanPropertySetter("*/changelog/activity/versions/version/name");
        digester.addBeanPropertySetter("*/changelog/activity/versions/version/user");
        digester.addBeanPropertySetter("*/changelog/activity/versions/version/file");
        
        digester.addSetNext("*/changelog/activity/versions","addVersion");
        digester.addSetNext("*/changelog/activity", "add");
        try {
            FileReader reader = new FileReader(changelogFile);
            digester.parse(reader);
            reader.close();
        } catch (SAXException sex) {
            return new ClearCaseUCMConfigRotatorChangeLogSet(build);
        }
        ClearCaseUCMConfigRotatorChangeLogSet clogSet = new ClearCaseUCMConfigRotatorChangeLogSet(build, changesetList);
       
        return clogSet;
        
    }
}

/*
 * 
 *             writer = new PrintWriter(new FileWriter(f));
            
            
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<changelog>");
            
            for(ClearCaseActivity a : changes) {
                writer.println("<entry>");
                writer.println(String.format("<activityName>%s</activityName>", a.getActivityName()));
                writer.println("<versions>");
                for(ClearCaseVersion v : a.getVersions()) {
                    writer.println("<version>");
                    writer.println(String.format("<v>%</v>", v.version));
                    writer.println(String.format("<file>%</file>", v.sFile));
                    writer.println(String.format("<user>%</user>", v.user));
                    writer.println("</version>");
                }
                writer.println("</versions>");
                writer.print("</entry>");
                
                
            }
            
            writer.println("</changelog>");
 */