/*******************************************************************************
 * Copyright (c) 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Abstract base implementation of all classpath container initializer.
 * Classpath variable containers are used in conjunction with the
 * "org.eclipse.jdt.core.classpathContainerInitializer" extension point.
 * <p>
 * Clients should subclass this class to implement a specific classpath
 * container initializer. The subclass must have a public 0-argument
 * constructor and a concrete implementation of <code>resolve</code>.
 * <p>
 * Multiple classpath containers can be registered, each of them declares
 * the container ID they can handle, so as to narrow the set of containers they
 * can resolve, i.e. a container initializer is guaranteed to only be activated to 
 * resolve containers which match the ID they registered onto.
 * <p>
 * In case multiple container initializers collide on the same container ID, the first
 * registered one will be invoked.
 * 
 * @see IClasspathEntry
 * @see IClasspathContainer
 * @since 2.0
 */

public abstract class ClasspathContainerInitializer {
	
   /**
     * Creates a new classpath container initializer.
     */
    public ClasspathContainerInitializer() {
    }

    /**
     * Binds a classpath container to a <code>IClasspathContainer</code> for a given project,
     * or silently fails if unable to do so.
     * <p>
     * A container is identified by a container path, which must be formed of two segments.
     * The first segment is used as a unique identifier (which this initializer did register onto), and
     * the second segment can be used as an additional hint when performing the resolution.
     * <p>
     * The initializer is invoked if a container path needs to be resolved for a given project, and no
     * value for it was recorded so far. The implementation of the initializer can set the corresponding 
     * container using <code>JavaCore#setClasspathContainer</code>.
     * <p>
     * @param containerPath a two-segment path (ID/hint) identifying the container that needs 
     * 	to be resolved
     * @param project the Java project in which context the container is to be resolved.
     *    This allows generic containers to be bound with project specific values.
     * 
     * @see JavaCore#getClasspathContainer(IPath, IJavaProject)
     * @see JavaCore#setClasspathContainer(IPath, IJavaProject[], IClasspathContainer[], IProgressMonitor)
     * @see IClasspathContainer
     */
    public abstract void initialize(IPath containerPath, IJavaProject project) throws CoreException;
}

