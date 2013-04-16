package net.praqma.jenkins.configrotator.scm.mercurial;

import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSTarget;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author cwolfgang
 */
public class MercurialTarget extends BaseDVCSTarget {

    @DataBoundConstructor
    public MercurialTarget( String name, String repository, String branch, String commitId, boolean fixed ) {
        super( name, repository, branch, commitId, fixed );
    }
}
