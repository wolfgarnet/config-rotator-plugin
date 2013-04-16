package net.praqma.jenkins.configrotator.scm.mercurial;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import net.praqma.jenkins.configrotator.*;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogParser;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCS;
import net.praqma.jenkins.configrotator.scm.dvcs.BaseDVCSCommit;
import net.praqma.jenkins.configrotator.scm.dvcs.NextCommitResolver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class Mercurial extends BaseDVCS<MercurialConfigurationComponent, MercurialTarget, MercurialConfiguration> {

    private static Logger logger = Logger.getLogger( Mercurial.class.getName() );

    @Override
    public String getName() {
        return "Mercurial";
    }

    @Override
    public NextCommitResolver<MercurialCommit> getNextCommitResolver( String name, String commitId ) {
        return new ResolveNextCommit( name, commitId );
    }

    @Override
    public Poller getPoller( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener ) {
        return new Poller(project, launcher, workspace, listener );
    }

    @Override
    public Performer getPerform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) throws IOException {
        return new MercurialPerformer( build, launcher, workspace, listener );
    }

    public class MercurialPerformer extends Performer<MercurialConfiguration> {

        public MercurialPerformer( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) {
            super( build, launcher, workspace, listener );
        }

        @Override
        public MercurialConfiguration getInitialConfiguration() throws ConfigurationRotatorException, IOException {
            return new MercurialConfiguration( getTargets(), workspace, listener );
        }

        @Override
        public MercurialConfiguration getNextConfiguration( ConfigurationRotatorBuildAction action ) throws ConfigurationRotatorException {
            MercurialConfiguration oldconfiguration = action.getConfiguration();
            return new NextDVCSConfigurationResolver().resolve( listener, oldconfiguration, workspace );
        }

        @Override
        public void checkConfiguration( MercurialConfiguration configuration ) throws ConfigurationRotatorException {
            /* TODO: implement */
        }

        @Override
        public void createWorkspace( MercurialConfiguration configuration ) throws ConfigurationRotatorException, IOException, InterruptedException {
            configuration.checkout( workspace, listener );
        }

        @Override
        public void print( MercurialConfiguration configuration ) {
            /* TODO: implement */
        }
    }

    @Override
    public <TT extends AbstractTarget> void setTargets( List<TT> targets ) {
        this.targets = (List<MercurialTarget>) targets;
    }

    @Override
    public List<MercurialTarget> getTargets() {
        return null;
    }

    @Override
    public ChangeLogWriter getChangeLogWriter( File changeLogFile, BuildListener listener, AbstractBuild<?, ?> build ) {
        return null;
    }

    @Override
    public MercurialTarget createTarget( String name, String repository, String branch, String commitId, boolean fixed ) {
        return new MercurialTarget( name, repository, branch, commitId, fixed );
    }

    @Extension
    public static class MercurialDescriptor extends DVCSDescriptor<Mercurial> {

        @Override
        public String getDisplayName() {
            return "Mercurial Repositories";
        }
    }
}
