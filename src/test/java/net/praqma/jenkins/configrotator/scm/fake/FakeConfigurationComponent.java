package net.praqma.jenkins.configrotator.scm.fake;

import net.praqma.jenkins.configrotator.AbstractConfigurationComponent;

/**
 * User: cwolfgang
 * Date: 02-11-12
 * Time: 12:53
 */
public class FakeConfigurationComponent extends AbstractConfigurationComponent {

    public FakeConfigurationComponent( boolean fixed ) {
        super( fixed );
    }

    @Override
    public String getComponentName() {
        return "fake component";
    }

    @Override
    public String prettyPrint() {
        return "fake component";
    }

    @Override
    public String getFeedName() {
        return "fake";
    }

    @Override
    public String getFeedId() {
        return "fake/id/";
    }

    @Override
    public String toHtml() {
        return "<html><body>FAKE SCM</body></html>";
    }
}
