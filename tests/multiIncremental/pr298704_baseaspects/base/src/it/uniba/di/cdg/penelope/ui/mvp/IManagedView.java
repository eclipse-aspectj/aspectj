/**
 * Copyright (c) 2009  Collaborative Development Group, C.S. Dept., University of Bari
 *
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 */
package it.uniba.di.cdg.penelope.ui.mvp;

/**
 *
 */
public interface IManagedView {
	void fire( Object event );

	void registerPresenter( IPresenter presenter );

	void unregisterPresenter( IPresenter presenter );
}
