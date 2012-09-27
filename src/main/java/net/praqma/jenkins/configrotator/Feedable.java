package net.praqma.jenkins.configrotator;

import hudson.model.AbstractBuild;
import net.praqma.util.xml.feed.Entry;

import java.io.File;
import java.util.Date;

public interface Feedable {
    public String getFeedName();
    public String getFeedId();

    public File getFeedFile( File path );
    public Entry getFeedEntry( AbstractBuild<?, ?> build, Date updated );

    public String toHtml();
}
