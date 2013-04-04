package net.praqma.jenkins.configrotator;

import hudson.model.Action;

/**
 * @author cwolfgang
 */
public class DiedBecauseAction implements Action {

    private String cause;
    private Die die;

    public enum Die {
        die,
        survive
    }

    public DiedBecauseAction( String cause, Die die ) {
        this.cause = cause;
        this.die = die;
    }

    public boolean died() {
        return die.equals( Die.die );
    }

    public String getCause() {
        return cause;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Died because";
    }

    @Override
    public String getUrlName() {
        return null;
    }

    @Override
    public String toString() {
        return die + " because \"" + cause + "\"";
    }
}
