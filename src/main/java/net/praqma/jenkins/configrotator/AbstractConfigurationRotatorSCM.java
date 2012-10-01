package net.praqma.jenkins.configrotator;

import java.io.*;
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

import java.util.logging.Level;
import java.util.logging.Logger;

import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogEntry;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogParser;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorVersion;

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
                logger.fine( "Using project configuration" );
                configuration = (C) projectConfiguration;
                //ConfigurationRotatorBuildAction action = getLastResult( project, null );
                //configuration = action.getConfiguration();
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
        if( cfg != null ) {
            out.println( ConfigurationRotator.LOGGERNAME + "The configuration is:" );
            logger.fine( "The configuration is:" );
            for( Object c : cfg.getList() ) {
                out.println( " * " + c );
                logger.fine( " * " + c );
            }
            out.println( "" );
            logger.fine( "" );
        } else {
            out.println( ConfigurationRotator.LOGGERNAME + "The configuration is null" );
            logger.fine( "The configuration is null" );
        }
    }
    
    /**
     * @param changeLogFile
     * @param listener
     * @param build
     * @throws IOException
     * @throws ConfigurationRotatorException
     * @throws InterruptedException 
     */
    //public abstract void writeChangeLog( File changeLogFile, BuildListener listener, AbstractBuild<?, ?> build ) throws IOException, ConfigurationRotatorException, InterruptedException;

    public abstract ChangeLogWriter getChangeLogWriter( File changeLogFile, BuildListener listener, AbstractBuild<?, ?> build );

    public abstract class ChangeLogWriter<C extends AbstractConfigurationComponent, T extends AbstractConfiguration<C>> {
        protected File changeLogFile;
        protected BuildListener listener;
        protected AbstractBuild<?, ?> build;

        protected ChangeLogWriter( File changeLogFile, BuildListener listener, AbstractBuild<?, ?> build ) {
            this.changeLogFile = changeLogFile;
            this.listener = listener;
            this.build = build;
        }

        /*
        public boolean isFirstBuild() {
            ConfigurationRotatorBuildAction crbac = getLastResult( build.getProject(), null );
            return crbac == null;
        }

        protected C getConfigurationComponent() {
            List<C> currentComponentList = null;
            ConfigurationRotatorBuildAction current = build.getAction( ConfigurationRotatorBuildAction.class );
            logger.fine("Current action: " + current);
            if( current != null ) {
                currentComponentList = (List<C>)current.getConfiguration().getList();
            }

            logger.fine("Current components: " + currentComponentList);


            if( currentComponentList != null ) {
                for( AbstractConfigurationComponent acc : currentComponentList ) {
                    if( acc.isChangedLast() ) {
                        return (C)acc;
                    }
                }
            }

            logger.fine("Null, huh?");
            return null;
        }

        public List<ConfigRotatorChangeLogEntry> getChangeLogEntries() throws ConfigurationRotatorException {
            return getChangeLogEntries( getConfigurationComponent() );
        }
        */

        public C getComponent( T configuration ) throws ConfigurationRotatorException {
            if( configuration != null ) {
                for( C acc : configuration.getList() ) {
                    if( acc.isChangedLast() ) {
                        return acc;
                    }
                }
            }

            throw new ConfigurationRotatorException( "No such component, " + configuration );
        }

        protected abstract List<ConfigRotatorChangeLogEntry> getChangeLogEntries( C configurationComponent ) throws ConfigurationRotatorException;

        public List<ConfigRotatorChangeLogEntry> getChangeLogEntries( T configuration ) throws ConfigurationRotatorException {
            return getChangeLogEntries( getComponent( configuration ) );
        }




        public void write( List<ConfigRotatorChangeLogEntry> entries ) {
            logger.fine("WRITING: " + entries);

            PrintWriter writer = null;
            try {
                writer = new PrintWriter( new FileWriter( changeLogFile ) );
                writer.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
                writer.println( "<changelog>" );

                for( ConfigRotatorChangeLogEntry entry : entries ) {
                    writer.println( "<commit>" );
                    writer.println( String.format( "<user>%s</user>", entry.getUser() ) );
                    writer.println( String.format( "<commitMessage>%s</commitMessage>", entry.getCommitMessage() ) );
                    writer.println( "<versions>" );
                    for( ConfigRotatorVersion v : entry.getVersions() ) {
                        writer.println( "<version>" );
                        writer.println( String.format( "<name>%s</name>", v.getName() ) );
                        writer.println( String.format( "<file>%s</file>", v.getFile() ) );
                        writer.println( String.format( "<user>%s</user>", v.getUser() ) );
                        writer.println( "</version>" );
                    }
                    writer.println( "</versions>" );
                    writer.print( "</commit>" );
                }

                writer.println( "</changelog>" );
            } catch( IOException e ) {
                listener.getLogger().println( "Unable to create change log!" + e );
            } finally {
                writer.close();
            }
        }
    }
		
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
        return "/" + ConfigurationRotator.URL_NAME + "/" + getClass().getSimpleName();
        //return ConfigurationRotator.FEED_URL + scm.getFeedComponentName();
        //return ConfigurationRotator.FEED_URL + "/" + getClass().getSimpleName() + "/";
    }
}
