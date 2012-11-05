package net.praqma.jenkins.configrotator.scm.fake;

import com.google.common.collect.Lists;
import net.praqma.jenkins.configrotator.AbstractConfiguration;
import net.praqma.jenkins.configrotator.ConfigurationRotatorException;

import java.io.Serializable;
import java.util.List;

public class FakeConfiguration extends AbstractConfiguration<FakeConfigurationComponent> {
    @Override
    public List<? extends Serializable> difference( AbstractConfiguration<FakeConfigurationComponent> configuration ) throws ConfigurationRotatorException {
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
