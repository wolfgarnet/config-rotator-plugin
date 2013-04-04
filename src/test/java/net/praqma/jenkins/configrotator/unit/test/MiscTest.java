package net.praqma.jenkins.configrotator.unit.test;

import net.praqma.jenkins.configrotator.ConfigRotatorProject;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class MiscTest {

    @Test
    public void testGetSafeName() {
        String name1 = "name";
        String nameWithSpace = "name 1";
        String nameWithComma = "name,1";
        String nameWithCommas = "n,a,m,e,1";

        assertThat( ConfigRotatorProject.getSafeName( name1 ), is( "name" ) );
        assertThat( ConfigRotatorProject.getSafeName( nameWithSpace ), is( "name_1" ) );
        assertThat( ConfigRotatorProject.getSafeName( nameWithCommas ), is( "n_a_m_e_1" ) );
    }
}
