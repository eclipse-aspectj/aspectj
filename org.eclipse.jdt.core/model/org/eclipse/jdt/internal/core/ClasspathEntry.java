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
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

/**
 * @see IClasspathEntry
 */
public class ClasspathEntry implements IClasspathEntry {

	/**
	 * Describes the kind of classpath entry - one of 
	 * CPE_PROJECT, CPE_LIBRARY, CPE_SOURCE, CPE_VARIABLE or CPE_CONTAINER
	 */
	protected int entryKind;

	/**
	 * Describes the kind of package fragment roots found on
	 * this classpath entry - either K_BINARY or K_SOURCE or
	 * K_OUTPUT.
	 */
	protected int contentKind;

	/**
	 * The meaning of the path of a classpath entry depends on its entry kind:<ul>
	 *	<li>Source code in the current project (<code>CPE_SOURCE</code>) -  
	 *      The path associated with this entry is the absolute path to the root folder. </li>
	 *	<li>A binary library in the current project (<code>CPE_LIBRARY</code>) - the path
	 *		associated with this entry is the absolute path to the JAR (or root folder), and 
	 *		in case it refers to an external JAR, then there is no associated resource in 
	 *		the workbench.
	 *	<li>A required project (<code>CPE_PROJECT</code>) - the path of the entry denotes the
	 *		path to the corresponding project resource.</li>
	 *  <li>A variable entry (<code>CPE_VARIABLE</code>) - the first segment of the path 
	 *      is the name of a classpath variable. If this classpath variable
	 *		is bound to the path <it>P</it>, the path of the corresponding classpath entry
	 *		is computed by appending to <it>P</it> the segments of the returned
	 *		path without the variable.</li>
	 *  <li> A container entry (<code>CPE_CONTAINER</code>) - the first segment of the path is denoting
	 *     the unique container identifier (for which a <code>ClasspathContainerInitializer</code> could be
	 * 	registered), and the remaining segments are used as additional hints for resolving the container entry to
	 * 	an actual <code>IClasspathContainer</code>.</li>
	 */
	protected IPath path;

	/**
	 * Describes the path to the source archive associated with this
	 * classpath entry, or <code>null</code> if this classpath entry has no
	 * source attachment.
	 * <p>
	 * Only library and variable classpath entries may have source attachments.
	 * For library classpath entries, the result path (if present) locates a source
	 * archive. For variable classpath entries, the result path (if present) has
	 * an analogous form and meaning as the variable path, namely the first segment 
	 * is the name of a classpath variable.
	 */
	protected IPath sourceAttachmentPath;

	/**
	 * Describes the path within the source archive where package fragments
	 * are located. An empty path indicates that packages are located at
	 * the root of the source archive. Returns a non-<code>null</code> value
	 * if and only if <code>getSourceAttachmentPath</code> returns 
	 * a non-<code>null</code> value.
	 */
	protected IPath sourceAttachmentRootPath;

	/**
	 * A constant indicating an output location.
	 */
	protected static final int K_OUTPUT = 10;

	/**
	 * The export flag
	 */
	protected boolean isExported;

	/**
	 * Creates a class path entry of the specified kind with the given path.
	 */
	public ClasspathEntry(
		int contentKind,
		int entryKind,
		IPath path,
		IPath sourceAttachmentPath,
		IPath sourceAttachmentRootPath,
		boolean isExported) {

		this.contentKind = contentKind;
		this.entryKind = entryKind;
		this.path = path;
		this.sourceAttachmentPath = sourceAttachmentPath;
		this.sourceAttachmentRootPath = sourceAttachmentRootPath;
		this.isExported = isExported;
	}
	
	/**
	 * Returns true if the given object is a classpath entry
	 * with equivalent attributes.
	 */
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object instanceof IClasspathEntry) {
			IClasspathEntry otherEntry = (IClasspathEntry) object;

			if (this.contentKind != otherEntry.getContentKind())
				return false;

			if (this.entryKind != otherEntry.getEntryKind())
				return false;

			if (this.isExported != otherEntry.isExported())
				return false;

			if (!this.path.equals(otherEntry.getPath()))
				return false;

			IPath otherPath = otherEntry.getSourceAttachmentPath();
			if (this.sourceAttachmentPath == null) {
				if (otherPath != null)
					return false;
			} else {
				if (!this.sourceAttachmentPath.equals(otherPath))
					return false;
			}

			otherPath = otherEntry.getSourceAttachmentRootPath();
			if (this.sourceAttachmentRootPath == null) {
				if (otherPath != null)
					return false;
			} else {
				if (!this.sourceAttachmentRootPath.equals(otherPath))
					return false;
			}

			return true;
		} else {
			return false;
		}
	}

	/**
	 * @see IClasspathEntry
	 */
	public int getContentKind() {
		return this.contentKind;
	}

	/**
	 * @see IClasspathEntry
	 */
	public int getEntryKind() {
		return this.entryKind;
	}

	/**
	 * @see IClasspathEntry
	 */
	public IPath getPath() {
		return this.path;
	}

	/**
	 * @see IClasspathEntry
	 */
	public IPath getSourceAttachmentPath() {
		return this.sourceAttachmentPath;
	}

	/**
	 * @see IClasspathEntry
	 */
	public IPath getSourceAttachmentRootPath() {
		return this.sourceAttachmentRootPath;
	}

	/**
	 * Returns the hash code for this classpath entry
	 */
	public int hashCode() {
		return this.path.hashCode();
	}

	/**
	 * @see IClasspathEntry#isExported()
	 */
	public boolean isExported() {
		return this.isExported;
	}

	/**
	 * Returns a printable representation of this classpath entry.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getPath().toString());
		buffer.append('[');
		switch (getEntryKind()) {
			case IClasspathEntry.CPE_LIBRARY :
				buffer.append("CPE_LIBRARY"); //$NON-NLS-1$
				break;
			case IClasspathEntry.CPE_PROJECT :
				buffer.append("CPE_PROJECT"); //$NON-NLS-1$
				break;
			case IClasspathEntry.CPE_SOURCE :
				buffer.append("CPE_SOURCE"); //$NON-NLS-1$
				break;
			case IClasspathEntry.CPE_VARIABLE :
				buffer.append("CPE_VARIABLE"); //$NON-NLS-1$
				break;
			case IClasspathEntry.CPE_CONTAINER :
				buffer.append("CPE_CONTAINER"); //$NON-NLS-1$
				break;
		}
		buffer.append("]["); //$NON-NLS-1$
		switch (getContentKind()) {
			case IPackageFragmentRoot.K_BINARY :
				buffer.append("K_BINARY"); //$NON-NLS-1$
				break;
			case IPackageFragmentRoot.K_SOURCE :
				buffer.append("K_SOURCE"); //$NON-NLS-1$
				break;
			case ClasspathEntry.K_OUTPUT :
				buffer.append("K_OUTPUT"); //$NON-NLS-1$
				break;
		}
		buffer.append(']');
		if (getSourceAttachmentPath() != null) {
			buffer.append("[sourcePath:"); //$NON-NLS-1$
			buffer.append(getSourceAttachmentPath());
			buffer.append(']');
		}
		if (getSourceAttachmentRootPath() != null) {
			buffer.append("[rootPath:"); //$NON-NLS-1$
			buffer.append(getSourceAttachmentRootPath());
			buffer.append(']');
		}
		buffer.append("[isExported:"); //$NON-NLS-1$
		buffer.append(this.isExported);
		buffer.append(']');
		return buffer.toString();
	}
	
	/**
	 * Answers an ID which is used to distinguish entries during package
	 * fragment root computations
	 */
	public String rootID(){
		StringBuffer buffer = new StringBuffer(10);
		buffer.append('[');
		switch(this.entryKind){
			case IClasspathEntry.CPE_LIBRARY :
				return "[LIB]"+this.path;  //$NON-NLS-1$
			case IClasspathEntry.CPE_PROJECT :
				return "[PRJ]"+this.path;  //$NON-NLS-1$
			case IClasspathEntry.CPE_SOURCE :
				return "[SRC]"+this.path;  //$NON-NLS-1$
			case IClasspathEntry.CPE_VARIABLE :
				return "[VAR]"+this.path;  //$NON-NLS-1$
			case IClasspathEntry.CPE_CONTAINER :
				return "[CON]"+this.path;  //$NON-NLS-1$
		}
		return "";  //$NON-NLS-1$
	}
	
	/**
	 * @see IClasspathEntry
	 * @deprecated
	 */
	public IClasspathEntry getResolvedEntry() {
	
		return JavaCore.getResolvedClasspathEntry(this);
	}
	
}