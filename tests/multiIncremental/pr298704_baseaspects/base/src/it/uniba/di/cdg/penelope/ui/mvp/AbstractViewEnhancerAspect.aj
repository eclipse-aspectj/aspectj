/**
 * Copyright (c) 2009  Collaborative Development Group, C.S. Dept., University of Bari
 *
 * All rights reserved. This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0  which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 */
package it.uniba.di.cdg.penelope.ui.mvp;

import it.uniba.di.cdg.penelope.ui.annotation.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Augments views with support for View events.
 */
public abstract aspect AbstractViewEnhancerAspect {
	pointcut scope(); 
	
	declare parents: (@View *) implements IManagedView;

	private final List<IPresenter> IManagedView.presenters = new ArrayList<IPresenter>(); 
	
	public void IManagedView.fire( Object event ) {
		for (IPresenter presenter : presenters) {
			presenter.dispatchEvent( event );
		}
	}
	
	public void IManagedView.registerPresenter( IPresenter presenter ) {
		presenters.add( presenter );
	}
	
	public void IManagedView.unregisterPresenter( IPresenter presenter ) {
		presenters.remove( presenter );
	}	
}
