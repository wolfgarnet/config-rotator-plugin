package net.praqma.jenkins.configrotator.scm.mercurial;

import com.aragost.javahg.Changeset;
import hudson.FilePath;
import hudson.model.TaskListener;
import net.praqma.jenkins.configrotator.AbstractConfigurationComponent;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSConfigurationComponent;

import java.io.IOException;

/**
 * @author cwolfgang
 */
public class MercurialConfigurationComponent extends BaseDVCSConfigurationComponent {

    public MercurialConfigurationComponent( String name, String repository, String branch, String commitId, boolean fixed ) {
        super( name, repository, branch, commitId, fixed );
    }

    public MercurialConfigurationComponent( String name, String repository, String branch, Changeset changeset, boolean fixed ) {
        super( name, repository, branch, changeset.getRevision() + "", fixed );
    }

    @Override
    public void checkout( FilePath workspace, TaskListener listener ) throws IOException, InterruptedException {
        workspace.act( new Checkout( name,  branch, commitId ) );
    }

    @Override
    protected Object clone() {
        MercurialConfigurationComponent gcc = new MercurialConfigurationComponent( name, repository, branch, commitId, fixed );
        return  gcc;
    }

    @Override
    public String getType() {
        return "Mercurial component";
    }
}
