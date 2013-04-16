package net.praqma.jenkins.configrotator.fake;

import net.praqma.jenkins.configrotator.AbstractConfiguration;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogEntry;

import java.util.List;

public class FakeConfiguration extends AbstractConfiguration<FakeConfigurationComponent, FakeTarget> {
    @Override
    public List<ConfigRotatorChangeLogEntry> difference( FakeConfigurationComponent component, FakeConfigurationComponent other ) throws ConfigurationRotatorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FakeConfiguration clone() {
        return new FakeConfiguration();
    }

    @Override
    public String toHtml() {
        return "";
    }
}
