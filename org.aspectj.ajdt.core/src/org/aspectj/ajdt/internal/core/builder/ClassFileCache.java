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


package org.aspectj.ajdt.internal.core.builder;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.workbench.resources.AbstractContainer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class ClassFileCache extends AbstractContainer {
	
	private IContainer container;
	private Map cache = new HashMap();
	private IMessageHandler handler;
	private Map/*IPath->ClassFileCacheFolder*/ folders = new HashMap();
	
	public ClassFileCache(IContainer container, IMessageHandler handler) {
//		super(container);
		this.container = container;	
		this.handler = handler;
		try {
		    getFolder(new Path("")).create(true, true, null);
		} catch (CoreException e) {
			throw new RuntimeException("can't happen");
		}
	}
	
	private Object makeKey(IPath path) {
		return path.toString();  //??? make sure this is stable
	}

	public boolean exists(IPath path) {
		if (folders.keySet().contains(makeKey(path))) {
			return getFolder(path).exists();
		} else {
			return getFile(path).exists();  //??? what if this is called for a folder
		}
	}

	public IFile getFile(IPath path) {
		EclipseUnwovenClassFile cachedFile = (EclipseUnwovenClassFile)cache.get(makeKey(path));
		if (cachedFile == null) {
			cachedFile = new EclipseUnwovenClassFile(container.getFile(path), handler);
			cache.put(makeKey(path), cachedFile);
		}
		
		return cachedFile.getFile();
	}

	public IFolder getFolder(IPath path) {
		//??? too naive
		ClassFileCacheFolder folder = (ClassFileCacheFolder)folders.get(makeKey(path));
		if (folder == null) {
			folder = new ClassFileCacheFolder(this, path, container.getFolder(path));
			folders.put(makeKey(path), folder);
		}
		return folder;
	}

	//XXX this doesn't include folders, is that okay?
	public IResource[] members() throws CoreException {
		List ret = new ArrayList();
		for (Iterator i = cache.values().iterator(); i.hasNext(); ) {
			EclipseUnwovenClassFile cachedFile = (EclipseUnwovenClassFile)i.next();
			ret.add(cachedFile.getFile());
		}
		return (IResource[])ret.toArray(new IResource[ret.size()]);
	}
	
	// extra methods for incremental use
	public void resetIncrementalInfo() {
		for (Iterator i = cache.values().iterator(); i.hasNext(); ) {
			EclipseUnwovenClassFile cachedFile = (EclipseUnwovenClassFile)i.next();
			DeferredWriteFile file = (DeferredWriteFile)cachedFile.getFile();
			file.setDirty(false);
		}
	}

	public List getDeleted() {
		List ret = new ArrayList();
		for (Iterator i = cache.values().iterator(); i.hasNext(); ) {
			EclipseUnwovenClassFile cachedFile = (EclipseUnwovenClassFile)i.next();
			DeferredWriteFile file = (DeferredWriteFile)cachedFile.getFile();
			if (file.isDirty() && !file.exists()) {
				ret.add(cachedFile);
			}
		}
		return ret;
	}

	public List getAddedOrChanged() {
		List ret = new ArrayList();
		for (Iterator i = cache.values().iterator(); i.hasNext(); ) {
			EclipseUnwovenClassFile cachedFile = (EclipseUnwovenClassFile)i.next();
			DeferredWriteFile file = (DeferredWriteFile)cachedFile.getFile();
			if (file.isDirty() && file.exists()) {
				ret.add(cachedFile);
			}
		}
		return ret;
	}


	public IResource findMember(IPath path) {
		throw new RuntimeException("unimplemented");
	}

	public void copy(
		IPath destination,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void delete(boolean force, IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public boolean exists() {
		return true;
	}


	public IPath getFullPath() {
		return container.getFullPath();
	}

	public IPath getLocation() {
		return container.getLocation();
	}

	public long getModificationStamp() {
		throw new RuntimeException("unimplemented");
	}

	public String getName() {
		return container.getName();
	}

	public IContainer getParent() {
		return null;
	}

	public int getType() {
		return IResource.FOLDER;
	}

	public boolean isDerived() {
		return true;
	}
	public void setDerived(boolean isDerived) {
		if (isDerived) return;
		throw new RuntimeException("unimplemented");
	}

	public boolean isReadOnly() {
		return false;
	}

	public void setReadOnly(boolean readOnly) {
		throw new RuntimeException("unimplemented");
	}

	public void touch(IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("unimplemented");
	}

}
