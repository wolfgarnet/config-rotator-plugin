package net.praqma.jenkins.configrotator.unit.scm.mercurial;

import net.praqma.jenkins.configrotator.scm.mercurial.Mercurial;
import net.praqma.jenkins.configrotator.scm.mercurial.MercurialConfiguration;
import net.praqma.jenkins.configrotator.scm.mercurial.MercurialConfigurationComponent;
import net.praqma.jenkins.configrotator.scm.mercurial.MercurialTarget;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class TargetsFromConfigurationTest extends MercurialBase {

    @Test
    public void empty() {
        Mercurial hg = new Mercurial();

        MercurialConfiguration c = getConfiguration( "config1", Collections.<MercurialConfigurationComponent>emptyList() );
        hg.setConfiguration( c );

        List<MercurialTarget> targets = hg.getTargets();

        assertThat( targets.size(), is( 0 ) );
    }

    @Test
    public void one() {
        Mercurial hg = new Mercurial();

        MercurialConfigurationComponent c1 = new MercurialConfigurationComponent( "hgc1", "url1", "default", "sha1", false );

        MercurialConfiguration c = getConfiguration( "config1", Collections.singletonList( c1 ) );
        hg.setConfiguration( c );

        List<MercurialTarget> targets = hg.getTargets();

        assertThat( targets.size(), is( 1 ) );
        checkTargetComponent( c1, targets.get( 0 ) );
    }

    @Test
    public void two() {
        Mercurial hg = new Mercurial();

        MercurialConfigurationComponent c1 = new MercurialConfigurationComponent( "hgc1", "url1", "default", "sha1", false );
        MercurialConfigurationComponent c2 = new MercurialConfigurationComponent( "hgc2", "url2", "default", "sha2", false );

        MercurialConfiguration c = getConfiguration( "config1", Arrays.asList( new MercurialConfigurationComponent[]{ c1, c2 } ) );
        hg.setConfiguration( c );

        List<MercurialTarget> targets = hg.getTargets();

        assertThat( targets.size(), is( 2 ) );
        checkTargetComponent( c1, targets.get( 0 ) );
        checkTargetComponent( c2, targets.get( 1 ) );
    }

    protected void checkTargetComponent( MercurialConfigurationComponent component, MercurialTarget target ) {
        assertThat( target.getBranch(), is( component.getBranch() ) );
        assertThat( target.getCommitId(), is( component.getCommitId() ) );
        assertThat( target.getFixed(), is( component.isFixed() ) );
        assertThat( target.getName(), is( component.getName() ) );
        assertThat( target.getRepository(), is( component.getRepository() ) );
    }
}
