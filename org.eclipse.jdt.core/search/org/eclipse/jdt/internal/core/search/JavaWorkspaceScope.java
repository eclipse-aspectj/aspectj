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
package org.eclipse.jdt.internal.core.search;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * A Java-specific scope for searching the entire workspace.
 * The scope can be configured to not search binaries. By default, binaries
 * are included.
 */
public class JavaWorkspaceScope extends JavaSearchScope {
	protected boolean needsInitialize;
	
public JavaWorkspaceScope() {
	JavaModelManager.getJavaModelManager().rememberScope(this);
}
public boolean encloses(IJavaElement element) {
	/*
	if (this.needsInitialize) {
		this.initialize();
	}
	return super.encloses(element);
	*/
	/*A workspace scope encloses all java elements (this assumes that the index selector
	 * and thus enclosingProjectAndJars() returns indexes on the classpath only and that these
	 * indexes are consistent.)
	 * NOTE: Returning true gains 20% of a hierarchy build on Object
	 */
	return true;
}
public boolean encloses(String resourcePathString) {
	/*
	if (this.needsInitialize) {
		this.initialize();
	}
	return super.encloses(resourcePathString);
	*/
	/*A workspace scope encloses all resources (this assumes that the index selector
	 * and thus enclosingProjectAndJars() returns indexes on the classpath only and that these
	 * indexes are consistent.)
	 * NOTE: Returning true gains 20% of a hierarchy build on Object
	 */
	return true;
}
public IPath[] enclosingProjectsAndJars() {
	if (this.needsInitialize) {
		this.initialize();
	}
	return super.enclosingProjectsAndJars();
}
public boolean equals(Object o) {
  return o instanceof JavaWorkspaceScope;
}
public int hashCode() {
	return JavaWorkspaceScope.class.hashCode();
}


public void initialize() {
	super.initialize();
	JavaCore javaCore = JavaCore.getJavaCore();
	IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
	for (int i = 0, length = projects.length; i < length; i++) {
		IProject project = projects[i];
		if (project.isAccessible()) {
			try {
				this.add(javaCore.create(project), false, new HashSet(2));
			} catch (JavaModelException e) {
			}
		}
	}
	this.needsInitialize = false;
}
public void processDelta(IJavaElementDelta delta) {
	if (this.needsInitialize) return;
	IJavaElement element = delta.getElement();
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			IJavaElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0, length = children.length; i < length; i++) {
				IJavaElementDelta child = children[i];
				this.processDelta(child);
			}
			break;
		case IJavaElement.JAVA_PROJECT:
			int kind = delta.getKind();
			switch (kind) {
				case IJavaElementDelta.ADDED:
				case IJavaElementDelta.REMOVED:
					this.needsInitialize = true;
					break;
				case IJavaElementDelta.CHANGED:
					children = delta.getAffectedChildren();
					for (int i = 0, length = children.length; i < length; i++) {
						IJavaElementDelta child = children[i];
						this.processDelta(child);
					}
					break;
			}
			break;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			kind = delta.getKind();
			switch (kind) {
				case IJavaElementDelta.ADDED:
				case IJavaElementDelta.REMOVED:
					this.needsInitialize = true;
					break;
				case IJavaElementDelta.CHANGED:
					int flags = delta.getFlags();
					if ((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) > 0
						|| (flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) > 0) {
						this.needsInitialize = true;
					}
					break;
			}
			break;
	}
}
public String toString() {
	return "JavaWorkspaceScope"; //$NON-NLS-1$
}
}
