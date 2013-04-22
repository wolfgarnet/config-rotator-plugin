package net.praqma.jenkins.configrotator.functional.scm.mercurial;

import com.aragost.javahg.Changeset;
import net.praqma.jenkins.configrotator.MercurialRule;
import net.praqma.jenkins.configrotator.functional.scm.dvcs.BaseTest;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCS;
import net.praqma.jenkins.configrotator.scm.mercurial.Mercurial;
import net.praqma.jenkins.configrotator.scm.mercurial.MercurialTarget;


public class MercurialTest extends BaseTest<Changeset, MercurialTarget> {

    public MercurialTest() {
        super( new MercurialRule() );
    }

    @Override
    public BaseDVCS getSCM() {
        return new Mercurial();
    }

    @Override
    public MercurialTarget getTarget( String name, String repository, String branch, String commitId, boolean fixed ) {
        return new MercurialTarget( name, repository, branch, commitId, fixed );
    }

    @Override
    public String getRevision( Changeset commit ) {
        return String.valueOf( commit.getRevision() );
    }

    @Override
    public String getDefaultBranchName() {
        return "default";
    }
}
