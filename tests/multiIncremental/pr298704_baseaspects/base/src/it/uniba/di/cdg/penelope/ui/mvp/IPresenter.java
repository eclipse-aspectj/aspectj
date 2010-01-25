/**
 * Copyright (c) 2009  Collaborative Development Group, C.S. Dept., University of Bari
 *
 * All rights reserved. This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0  which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 */
package it.uniba.di.cdg.penelope.ui.mvp;

import java.beans.PropertyChangeListener;

/**
 * Basic interface for <strong>Presenter</strong> implementations. Within Penelope, a Presenter is a supervising controller 
 * which reacts to view events (see the View Events mechanism): the view just displays the session state stored within
 * the <strong>Presentation Model</strong>.
 */
public interface IPresenter extends PropertyChangeListener {
	/**
	 * View has been created.
	 */
	void onViewCreated(); 

	/**
	 * View has been destroyed.
	 */
	void onViewDisposed();

	/**
	 * @param event
	 */
	void dispatchEvent( Object event );
}
