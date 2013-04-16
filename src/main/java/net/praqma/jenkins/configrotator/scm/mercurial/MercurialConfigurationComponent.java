package net.praqma.jenkins.configrotator.scm.mercurial;

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

    @Override
    public void checkout( FilePath workspace, TaskListener listener ) throws IOException, InterruptedException {
    }

    @Override
    protected Object clone() {
        MercurialConfigurationComponent gcc = new MercurialConfigurationComponent( name, repository, branch, commitId, fixed );
        return  gcc;
    }
}
