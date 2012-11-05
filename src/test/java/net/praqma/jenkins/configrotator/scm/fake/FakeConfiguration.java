package net.praqma.jenkins.configrotator.scm.fake;

import net.praqma.jenkins.configrotator.AbstractConfiguration;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogEntry;

public class FakeConfiguration extends AbstractConfiguration<FakeConfigurationComponent> {
    @Override
    public ConfigRotatorChangeLogEntry difference( AbstractConfiguration<FakeConfigurationComponent> configuration ) throws ConfigurationRotatorException {
        return null;
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
