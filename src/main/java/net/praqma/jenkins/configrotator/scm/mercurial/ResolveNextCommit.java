package net.praqma.jenkins.configrotator.scm.mercurial;

import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSCommit;
import net.praqma.jenkins.configrotator.scm.dvcs.NextCommitResolver;

/**
 * @author cwolfgang
 */
public class ResolveNextCommit extends NextCommitResolver<MercurialCommit> {

    public ResolveNextCommit( String name, String commitId ) {
        super( name, commitId );
    }

    @Override
    public MercurialCommit resolve() {
        return null;
    }

}
