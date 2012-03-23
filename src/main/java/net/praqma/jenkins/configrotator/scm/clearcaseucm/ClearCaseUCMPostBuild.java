package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import java.io.IOException;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.TaskListener;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.jenkins.configrotator.AbstractConfigurationRotatorSCM;
import net.praqma.jenkins.configrotator.AbstractPostConfigurationRotator;
import net.praqma.jenkins.configrotator.ConfigurationRotatorBuildAction;
import net.praqma.jenkins.utils.remoting.GetBaselines;

@Extension
public class ClearCaseUCMPostBuild extends AbstractPostConfigurationRotator {

	@Override
	public boolean perform( FilePath workspace, TaskListener listener, ConfigurationRotatorBuildAction action ) {
		listener.getLogger().println( "In post build" );
		/*
		SnapshotView view = ((ClearCaseUCMConfiguration)action.getConfiguration()).getView();
		if( view != null ) {
			listener.getLogger().println( "View is not null" );
			try {
				workspace.act( new EndView( view, listener ) );
			} catch( Exception e ) {
				ExceptionUtils.print( e, listener.getLogger(), false );
			}
		} else {
			listener.getLogger().println( "View is null" );
		}
		*/
		return true;
	}

	@Override
	public Class<? extends AbstractConfigurationRotatorSCM> tiedTo() {
		return ClearCaseUCM.class;
	}

}
