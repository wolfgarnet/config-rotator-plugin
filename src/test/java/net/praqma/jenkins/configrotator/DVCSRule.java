package net.praqma.jenkins.configrotator;

import org.junit.rules.TestRule;

import java.io.File;

/**
 * @author cwolfgang
 */
public abstract class DVCSRule<T extends Object> {

    public abstract void initialize( File gitPath );

    public abstract T createCommit( String filename, String content ) throws Exception;

    public abstract String getRepo();
}
