/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.workbench.resources;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.*;

public abstract class AbstractContainer extends AbstractResource implements IContainer {

	public AbstractContainer() {
		super();
	}

	public abstract boolean exists(IPath path);

	public IFile[] findDeletedMembersWithHistory(
		int depth,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public IResource findMember(IPath path, boolean includePhantoms) {
		return findMember(path);
	}

	public abstract IResource findMember(IPath path);

	public IResource findMember(String name, boolean includePhantoms) {
		return findMember(new Path(name), includePhantoms);
	}

	public IResource findMember(String name) {
		return findMember(new Path(name));
	}

	public abstract IFile getFile(IPath path);

	public abstract IFolder getFolder(IPath path);

	public abstract IResource[] members() throws CoreException;

	public IResource[] members(boolean includePhantoms) throws CoreException {
		return members();
	}

	public IResource[] members(int memberFlags) throws CoreException {
		return members();
	}

	public final String getFileExtension() { return null; }
}
