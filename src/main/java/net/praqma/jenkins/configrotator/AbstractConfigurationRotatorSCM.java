package net.praqma.jenkins.configrotator;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import hudson.*;
import jenkins.model.Jenkins;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.TaskListener;
import hudson.model.Descriptor;
import hudson.scm.PollingResult;
import java.io.File;
import java.util.logging.Logger;

import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogParser;

public abstract class AbstractConfigurationRotatorSCM implements Describable<AbstractConfigurationRotatorSCM>, ExtensionPoint {
	
	private static Logger logger = Logger.getLogger( AbstractConfigurationRotatorSCM.class.getName()  );

    /**
     * Return the name of the type
     */
	public abstract String getName();

    /**
     * Determine if there's something new
     * @param project
     * @param launcher
     * @param workspace
     * @param listener
     * @param reconfigure
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
	public abstract PollingResult poll( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, boolean reconfigure ) throws IOException, InterruptedException;

    /**
     * Perform the actual config rotation
     * @param build
     * @param launcher
     * @param workspace
     * @param listener
     * @return
     * @throws IOException
     */
	//public abstract boolean perform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, boolean reconfigure ) throws IOException;

    public abstract Performer getPerform( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) throws IOException;

    public abstract class Performer<C> {
        protected AbstractBuild<?, ?> build;
        protected Launcher launcher;
        protected FilePath workspace;
        protected BuildListener listener;

        protected PrintStream out;

        public Performer( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener ) {
            this.build = build;
            this.launcher = launcher;
            this.workspace = workspace;
            this.listener = listener;

            this.out = listener.getLogger();
        }

        public abstract C getInitialConfiguration() throws ConfigurationRotatorException, IOException;
        public abstract C getNextConfiguration( ConfigurationRotatorBuildAction action ) throws ConfigurationRotatorException;
        public abstract void checkConfiguration( C configuration ) throws ConfigurationRotatorException;
        public abstract void createWorkspace( C configuration ) throws ConfigurationRotatorException;
        public Class getSCMClass() {
                return AbstractConfigurationRotatorSCM.this.getClass();
        }

        public abstract void print( C configuration );

        public void save( C configuration ) {
            final ConfigurationRotatorBuildAction action1 = new ConfigurationRotatorBuildAction( build, getSCMClass(), (AbstractConfiguration) configuration );
            build.addAction( action1 );
        }
    }
	
	public abstract void setConfigurationByAction( AbstractProject<?, ?> project, ConfigurationRotatorBuildAction action ) throws IOException;
	
	public abstract boolean wasReconfigured( AbstractProject<?, ?> project );
    
    public abstract ConfigRotatorChangeLogParser createChangeLogParser();
    
    /**
     * @param changeLogFile
     * @param listener
     * @param build
     * @throws IOException
     * @throws ConfigurationRotatorException
     * @throws InterruptedException 
     */
    public abstract void writeChangeLog( File changeLogFile, BuildListener listener, AbstractBuild<?, ?> build ) throws IOException, ConfigurationRotatorException, InterruptedException;
    
		
	@Override
	public Descriptor<AbstractConfigurationRotatorSCM> getDescriptor() {
		return (ConfigurationRotatorSCMDescriptor) Jenkins.getInstance().getDescriptorOrDie( getClass() );
	}
    
	/**
	 * All registered {@link AbstractConfigurationRotatorSCM}s.
	 */

	public static DescriptorExtensionList<AbstractConfigurationRotatorSCM, ConfigurationRotatorSCMDescriptor<AbstractConfigurationRotatorSCM>> all() {
		return Jenkins.getInstance().<AbstractConfigurationRotatorSCM, ConfigurationRotatorSCMDescriptor<AbstractConfigurationRotatorSCM>> getDescriptorList( AbstractConfigurationRotatorSCM.class );
	}

    /*
    public static DescriptorExtensionList<AbstractConfigurationRotatorSCM<?>, ConfigurationRotatorSCMDescriptor<AbstractConfigurationRotatorSCM<?>>> all() {
        Jenkins.getInstance().getDescriptorList( AbstractConfigurationRotatorSCM.class);
    }
    */

	public static List<ConfigurationRotatorSCMDescriptor<?>> getDescriptors() {
		List<ConfigurationRotatorSCMDescriptor<?>> list = new ArrayList<ConfigurationRotatorSCMDescriptor<?>>();
		for( ConfigurationRotatorSCMDescriptor<?> d : all() ) {
			list.add( d );
		}
		
		return list;
	}
	
	public ConfigurationRotatorBuildAction getLastResult( AbstractProject<?, ?> project, Class<? extends AbstractConfigurationRotatorSCM> clazz ) {
		logger.fine( "Getting last result: " + project );
		
		for( AbstractBuild<?, ?> b = getLastBuildToBeConsidered( project ); b != null; b = b.getPreviousBuild() ) {
			ConfigurationRotatorBuildAction r = b.getAction( ConfigurationRotatorBuildAction.class );

			if( r != null ) {
				if( r.isDetermined() && ( clazz == null || r.getClazz().equals( clazz ) ) ) {
					return r;
				}
			}
		}
		
		return null;
	}

    public ConfigurationRotatorBuildAction getPreviousResult( AbstractBuild<?, ?> build, Class<? extends AbstractConfigurationRotatorSCM> clazz ) {
        logger.fine( "Getting previous result: " + build );

        for( AbstractBuild<?, ?> b = build.getPreviousBuild(); b != null; b = b.getPreviousBuild() ) {
            ConfigurationRotatorBuildAction r = b.getAction( ConfigurationRotatorBuildAction.class );

            if( r != null ) {
                if( r.isDetermined() && ( clazz == null || r.getClazz().equals( clazz ) ) ) {
                    return r;
                }
            }
        }

        return null;
    }
    
    public ArrayList<ConfigurationRotatorBuildAction> getLastResults(AbstractProject<?, ?> project, Class<? extends AbstractConfigurationRotatorSCM> clazz, int limit) {
        ArrayList<ConfigurationRotatorBuildAction> actions = new ArrayList<ConfigurationRotatorBuildAction>();
        for( AbstractBuild<?, ?> b = getLastBuildToBeConsidered( project ); b != null; b = b.getPreviousBuild() ) {
			ConfigurationRotatorBuildAction r = b.getAction( ConfigurationRotatorBuildAction.class );
			if( r != null ) {
				if( r.isDetermined() && ( (clazz == null || r.getClazz().equals( clazz ))) ) {
					actions.add(r);
                    if(actions.size() >= limit) {
                        return actions;
                    }
				}
			}
		}
        return actions;
    }
	
	private AbstractBuild<?, ?> getLastBuildToBeConsidered( AbstractProject<?, ?> project ) {
		return project.getLastCompletedBuild();
	}

    public File getFeedPath() {
        return new File( ConfigurationRotator.FEED_PATH, getClass().getSimpleName() );
    }

    public String getFeedURL() {
        return ConfigurationRotator.FEED_URL + "/" + getClass().getSimpleName() + "/";
    }
}
