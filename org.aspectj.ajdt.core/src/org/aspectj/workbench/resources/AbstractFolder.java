/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.workbench.resources;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.*;

public abstract class AbstractFolder extends AbstractContainer implements IFolder {

	public AbstractFolder() {
		super();
	}

	public abstract void create(boolean force, boolean local, IProgressMonitor monitor)
		throws CoreException;

	public void create(
		int updateFlags,
		boolean local,
		IProgressMonitor monitor)
		throws CoreException {
			create(isForce(updateFlags), local, monitor);
	}

	public void delete(
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException {
			delete(force, monitor);
	}

	public IFile getFile(String name) {
		return getFile(new Path(name));
	}

	public IFolder getFolder(String name) {
		return getFolder(new Path(name));
	}

	public void move(
		IPath destination,
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException {
			throw new RuntimeException("unimplemented");
	}

}
