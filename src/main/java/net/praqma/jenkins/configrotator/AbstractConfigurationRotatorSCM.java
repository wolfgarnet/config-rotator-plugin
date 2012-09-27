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
import java.util.logging.Level;
import java.util.logging.Logger;

import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogParser;

public abstract class AbstractConfigurationRotatorSCM implements Describable<AbstractConfigurationRotatorSCM>, ExtensionPoint {
	
	private static Logger logger = Logger.getLogger( AbstractConfigurationRotatorSCM.class.getName()  );

    protected AbstractConfiguration projectConfiguration;

    /**
     * Return the name of the type
     */
	public abstract String getName();

    public void setConfiguration( AbstractConfiguration configuration ) {
        this.projectConfiguration = configuration;
    }

    public abstract AbstractConfiguration nextConfiguration( TaskListener listener, AbstractConfiguration configuration, FilePath workspace ) throws ConfigurationRotatorException;

    public abstract Poller getPoller( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, boolean reconfigure );

    public abstract class Poller<C extends AbstractConfiguration, T extends AbstractTarget> {
        protected AbstractProject<?, ?> project;
        protected Launcher launcher;
        protected FilePath workspace;
        protected TaskListener listener;
        protected boolean reconfigure;

        public Poller( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, boolean reconfigure ) {
            this.project = project;
            this.launcher = launcher;
            this.workspace = workspace;
            this.listener = listener;
            this.reconfigure = reconfigure;
        }

        public abstract C getConfigurationFromTargets( List<T> targets ) throws ConfigurationRotatorException;
        public abstract List<T> getTargets() throws ConfigurationRotatorException;

        public PollingResult poll() throws AbortException {
            PrintStream out = listener.getLogger();
            logger.fine( ConfigurationRotator.LOGGERNAME + "Polling started" );

            C configuration = null;
            if( projectConfiguration == null ) {
                if( reconfigure ) {
                    try {
                        logger.fine( "Project was reconfigured" );
                        configuration = getConfigurationFromTargets( getTargets() );
                    } catch( ConfigurationRotatorException e ) {
                        logger.log( Level.WARNING, "Unable to get configurations from targets: Exception message", e );
                        throw new AbortException( ConfigurationRotator.LOGGERNAME + "Unable to get configurations from targets. " + e.getMessage() );
                    }
                } else {
                    logger.fine( "Project has no configuration, using configuration from last result" );
                    ConfigurationRotatorBuildAction action = getLastResult( project, null );

                    if( action == null ) {
                        logger.fine( "No last result, build now" );
                        return PollingResult.BUILD_NOW;
                    }

                    configuration = action.getConfiguration();
                }
            } else {
                logger.fine( "Project configuration found" );
                configuration = (C) projectConfiguration;
            }

            /* Only look ahead if the build was NOT reconfigured */
            if( configuration != null && !reconfigure ) {
                logger.fine( "Looking for changes" );
                try {
                    AbstractConfiguration other;
                    other = nextConfiguration( listener, configuration, workspace );
                    if( other != null ) {
                        logger.fine( "Found changes" );
                        printConfiguration( out, other );
                        return PollingResult.BUILD_NOW;
                    } else {
                        logger.fine( "No changes!" );
                        return PollingResult.NO_CHANGES;
                    }
                } catch( ConfigurationRotatorException e ) {
                    logger.log( Level.WARNING, "Unable to poll", e );
                    throw new AbortException( ConfigurationRotator.LOGGERNAME + "Unable to poll: " + e.getMessage() );
                } catch( Exception e ) {
                    logger.log( Level.WARNING, "Polling caught unhandled exception. Message was", e );
                    throw new AbortException( ConfigurationRotator.LOGGERNAME + "Polling caught unhandled exception! Message was: " + e.getMessage() );
                }
            } else {
                logger.fine( "Starting first build" );
                return PollingResult.BUILD_NOW;
            }
        }
    }

    /**
     * Perform the actual config rotation
     * @param build
     * @param launcher
     * @param workspace
     * @param listener
     * @return
     * @throws IOException
     */
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
            projectConfiguration = (AbstractConfiguration) configuration;
            final ConfigurationRotatorBuildAction action1 = new ConfigurationRotatorBuildAction( build, getSCMClass(), (AbstractConfiguration) configuration );
            build.addAction( action1 );
        }
    }
	
	public abstract void setConfigurationByAction( AbstractProject<?, ?> project, ConfigurationRotatorBuildAction action ) throws IOException;
	
	public abstract boolean wasReconfigured( AbstractProject<?, ?> project );
    
    public abstract ConfigRotatorChangeLogParser createChangeLogParser();

    public void printConfiguration( PrintStream out, AbstractConfiguration cfg ) {
        out.println( ConfigurationRotator.LOGGERNAME + "The configuration is:" );
        logger.fine( ConfigurationRotator.LOGGERNAME + "The configuration is:" );
        AbstractConfiguration config = cfg;
        for( Object c : config.getList() ) {
            out.println( " * " + c );
            logger.fine( " * " + c );
        }
        out.println( "" );
        logger.fine( "" );
    }
    
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
