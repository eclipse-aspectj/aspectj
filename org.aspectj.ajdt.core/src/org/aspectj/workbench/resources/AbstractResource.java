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
import org.eclipse.core.runtime.*;

public abstract class AbstractResource implements IResource {

	public AbstractResource() {
		super();
	}

	public void accept(IResourceVisitor visitor) throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void accept(
		IResourceVisitor visitor,
		int depth,
		boolean includePhantoms)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void accept(IResourceVisitor visitor, int depth, int memberFlags)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void clearHistory(IProgressMonitor monitor) throws CoreException {
		// nothing to do
	}

	public void copy(
		IProjectDescription description,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public abstract void copy(
		IPath destination,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException;

	public void copy(
		IProjectDescription description,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void copy(
		IPath destination,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
		copy(destination, isForce(updateFlags), monitor);
	}

	protected static boolean isForce(int updateFlags) {
		return (updateFlags & IResource.FORCE) != 0;
	}

	protected static boolean isHistory(int updateFlags) {
		return (updateFlags & IResource.KEEP_HISTORY) != 0;
	}

	public IMarker createMarker(String type) throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public abstract void delete(boolean force, IProgressMonitor monitor)
		throws CoreException;

	public void delete(int updateFlags, IProgressMonitor monitor)
		throws CoreException {
		delete(isForce(updateFlags), monitor);
	}

	public void deleteMarkers(String type, boolean includeSubtypes, int depth)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public abstract boolean exists();

	public IMarker findMarker(long id) throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public IMarker[] findMarkers(
		String type,
		boolean includeSubtypes,
		int depth)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public abstract String getFileExtension();

	public abstract IPath getFullPath();

	public abstract IPath getLocation();

	public IMarker getMarker(long id) {
		throw new RuntimeException("unimplemented");
	}

	public abstract long getModificationStamp();

	public abstract String getName();

	public abstract IContainer getParent();

	public String getPersistentProperty(QualifiedName key)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public IProject getProject() {
		throw new RuntimeException("unimplemented");
	}

	public IPath getProjectRelativePath() {
		throw new RuntimeException("unimplemented");
	}

	public Object getSessionProperty(QualifiedName key) throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public abstract int getType();

	public IWorkspace getWorkspace() {
		throw new RuntimeException("unimplemented");
	}

	public boolean isAccessible() {
		return exists();
	}

	public boolean isLocal(int depth) {
		return true;
	}

	public boolean isPhantom() {
		return false;
	}

	public abstract boolean isReadOnly();

	public boolean isSynchronized(int depth) {
		return true;
	}

	public void move(
		IProjectDescription description,
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void move(
		IPath destination,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented"); //??? we could make abstract
	}

	public void move(
		IProjectDescription description,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void move(
		IPath destination,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
		move(destination, isForce(updateFlags), monitor);
	}

	public void refreshLocal(int depth, IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void setLocal(boolean flag, int depth, IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void setPersistentProperty(QualifiedName key, String value)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public abstract void setReadOnly(boolean readOnly);

	public void setSessionProperty(QualifiedName key, Object value)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public abstract void touch(IProgressMonitor monitor) throws CoreException;
	
	public abstract boolean isDerived();

	public void setDerived(boolean isDerived) throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public boolean isTeamPrivateMember() {
		return false;
	}

	public void setTeamPrivateMember(boolean isTeamPrivate)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public Object getAdapter(Class adapter) {
		throw new RuntimeException("unimplemented");
	}

}
