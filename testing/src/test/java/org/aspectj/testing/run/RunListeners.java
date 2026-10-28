/* *******************************************************************
 * Copyright (c) 1999-2026 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/
package org.aspectj.testing.run;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Aggregate listeners into one and run synchronously in order added.
 * 
 * @author isberg
 */
public class RunListeners extends RunListener implements IRunListener {

	List<IRunListener> listeners;

	public RunListeners() {
		listeners = new ArrayList<>();
	}

	public void addListener(IRunListener listener) {
		if (null != listener) {
			listeners.add(listener);
		}
	}

	public void removeListener(IRunListener listener) {
		if (null != listener) {
			listeners.remove(listener);
		}
	}

	/**
	 * Run all listeners with the given status.
	 * 
	 * @see org.aspectj.testing.harness.run.IRunListener#runStarting(IRunStatus)
	 */
	public final void runStarting(IRunStatus status) {
		if (null == status) {
			throw new IllegalArgumentException("null RunStatusI");
		}
		Iterator<IRunListener> iter = listeners.iterator();
		while (!status.aborted() && iter.hasNext()) {
			IRunListener runListener = iter.next();
			runListener.runStarting(status);
		}
	}

	/**
	 * Signal all listeners with the given status.
	 * 
	 * @see org.aspectj.testing.harness.run.IRunListener#runCompleted(IRunStatus)
	 */
	public final void runCompleted(IRunStatus status) {
		if (null == status) {
			throw new IllegalArgumentException("null RunStatusI");
		}
		Iterator<IRunListener> runListenerIterator = listeners.iterator();
		while (!status.aborted() && runListenerIterator.hasNext()) {
			runListenerIterator.next().runCompleted(status);
		}
	}

	/**
	 * @see org.aspectj.testing.harness.run.IRunListener#addingChild(IRunStatus,
	 *      IRunStatus)
	 */
	public final void addingChild(IRunStatus parent, IRunStatus child) {
		if (null == child) {
			throw new IllegalArgumentException("null child");
		}
		if (null == parent) {
			throw new IllegalArgumentException("null parent");
		}
		Iterator<IRunListener> runListenerIterator = listeners.iterator();
		while (!parent.aborted() && !child.aborted() && runListenerIterator.hasNext()) {
			runListenerIterator.next().addingChild(parent, child);
		}
	}
}
