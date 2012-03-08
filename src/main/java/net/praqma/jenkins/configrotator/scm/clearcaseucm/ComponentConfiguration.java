package net.praqma.jenkins.configrotator.scm.clearcaseucm;

import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project.Plevel;
import net.praqma.clearcase.ucm.entities.Stream;

public class ComponentConfiguration {
	private Component component;
	private Stream stream;
	private Baseline baseline;
	private Plevel plevel;
	
	public ComponentConfiguration( Component component, Stream stream, Baseline baseline, Plevel plevel ) {
		this.component = component;
		this.stream = stream;
		this.baseline = baseline;
		this.plevel = plevel;
	}

	public Component getComponent() {
		return component;
	}

	public Stream getStream() {
		return stream;
	}

	public Baseline getBaseline() {
		return baseline;
	}
	
	public Plevel getPlevel() {
		return plevel;
	}
}
