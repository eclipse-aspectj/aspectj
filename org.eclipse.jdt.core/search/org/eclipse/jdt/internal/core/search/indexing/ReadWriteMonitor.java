/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

/**
 * Monitor ensuring no more than one writer working concurrently.
 * Multiple readers are allowed to perform simultaneously.
 */
public class ReadWriteMonitor {

	/**
	 * <0 : writing (cannot go beyond -1, i.e one concurrent writer)
	 * =0 : idle
	 * >0 : reading (number of concurrent readers)
	 */
	private int status = 0;
/**
 * Concurrent reading is allowed
 * Blocking only when already writing.
 */
public synchronized void enterRead() {

	while (status < 0){
		try {
			wait();
		} catch(InterruptedException e){
		}
	}
	status++;
}
/**
 * Only one writer at a time is allowed to perform
 * Blocking only when already writing or reading.
 */
public synchronized void enterWrite() {

	while (status != 0){
		try {
			wait();
		} catch(InterruptedException e){
		}
	}
	status--;
}
/**
 * Only notify waiting writer(s) if last reader
 */
public synchronized void exitRead() {

	if (--status == 0) notifyAll();
}
/**
 * When writing is over, all readers and possible
 * writers are granted permission to restart concurrently
 */
public synchronized void exitWrite() {

	if (++status == 0) notifyAll();
}
/**
 * Atomic exitWrite/enterRead: Allows to keep monitor in between
 * exit write and next enter read.
 * When writing is over, all readers are granted permissing to restart
 * concurrently.
 * This is the same as:
 * <pre>
 * synchronized(monitor) {
 *   monitor.exitWrite();
 *   monitor.enterRead();
 * }
 * </pre>
 */
public synchronized void exitWriteEnterRead() {
	this.exitWrite();
	this.enterRead();
} 
}
