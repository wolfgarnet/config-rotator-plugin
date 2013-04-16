package net.praqma.jenkins.configrotator.fake;

import net.praqma.jenkins.configrotator.AbstractConfigurationComponent;

/**
 * User: cwolfgang
 */
public class FakeConfigurationComponent extends AbstractConfigurationComponent {

    private String name = "Unnamed";

    public FakeConfigurationComponent( boolean fixed ) {
        super( fixed );
    }

    public FakeConfigurationComponent( boolean fixed, String name ) {
        super( fixed );

        this.name = name;
    }

    @Override
    public String getComponentName() {
        return "fake component[" + name + "]";
    }

    @Override
    public String prettyPrint() {
        return "fake component[" + name + "]";
    }

    @Override
    public String getFeedName() {
        return "fake[" + name + "]";
    }

    @Override
    public String getFeedId() {
        return "fake/id/";
    }

    @Override
    public String toHtml() {
        return "<html><body>FAKE SCM[" + name + "]</body></html>";
    }
}
