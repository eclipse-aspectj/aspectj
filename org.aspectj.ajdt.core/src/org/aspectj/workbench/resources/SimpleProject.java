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

import java.util.Map;

import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;

public class SimpleProject extends FilesystemFolder implements IProject {
	IProjectDescription description;

	public SimpleProject(IPath path, String name) {
		super(path.toString());
		description = new ProjectDescription();
		description.setName(name);
		description.setLocation(path);
	}

	public void build(
		int kind,
		String builderName,
		Map args,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void build(int kind, IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void close(IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void create(
		IProjectDescription description,
		IProgressMonitor monitor)
		throws CoreException {
		create(monitor);
	}

	public void create(IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void delete(
		boolean deleteContent,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public IProjectDescription getDescription() throws CoreException {
		return description;
	}

	public IFile getFile(String name) {
		throw new RuntimeException("unimplemented");
	}

	public IFolder getFolder(String name) {
		throw new RuntimeException("unimplemented");
	}

	public IProjectNature getNature(String natureId) throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public IPath getPluginWorkingLocation(IPluginDescriptor plugin) {
		throw new RuntimeException("unimplemented");
	}

	public IProject[] getReferencedProjects() throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public IProject[] getReferencingProjects() {
		throw new RuntimeException("unimplemented");
	}

	public boolean hasNature(String natureId) throws CoreException {
		return false;
	}

	public boolean isNatureEnabled(String natureId) throws CoreException {
		return false;
	}

	public boolean isOpen() {
		return false;
	}

	public void move(
		IProjectDescription description,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void open(IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void setDescription(
		IProjectDescription description,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void setDescription(
		IProjectDescription description,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

}
