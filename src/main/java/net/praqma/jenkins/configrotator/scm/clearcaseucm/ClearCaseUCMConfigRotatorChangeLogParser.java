package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import java.io.*;
import java.util.logging.Logger;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogParser;
import org.xml.sax.SAXException;

/**
 * Keps for backwards compatibility.
 * @author Praqma
 */
public class ClearCaseUCMConfigRotatorChangeLogParser extends ConfigRotatorChangeLogParser {

    private static final Logger logger = Logger.getLogger(ClearCaseUCMConfigRotatorChangeLogParser.class.toString());

    @Override
    public ChangeLogSet<? extends Entry> parse(AbstractBuild build, File changelogFile) throws IOException, SAXException {
        return null;
    }
}