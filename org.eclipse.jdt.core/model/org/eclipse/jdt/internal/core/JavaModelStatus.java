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

import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;

/**
 * @see IJavaModelStatus
 */

public class JavaModelStatus extends Status implements IJavaModelStatus, IJavaModelStatusConstants, IResourceStatus {

	/**
	 * The elements related to the failure, or <code>null</code>
	 * if no elements are involved.
	 */
	protected IJavaElement[] fElements = new IJavaElement[0];
	/**
	 * The path related to the failure, or <code>null</code>
	 * if no path is involved.
	 */
	protected IPath fPath;
	/**
	 * The <code>String</code> related to the failure, or <code>null</code>
	 * if no <code>String</code> is involved.
	 */
	protected String fString;
	/**
	 * Empty children
	 */
	protected final static IStatus[] fgEmptyChildren = new IStatus[] {};
	protected IStatus[] fChildren= fgEmptyChildren;

	/**
	 * Singleton OK object
	 */
	public static final IJavaModelStatus VERIFIED_OK = new JavaModelStatus(OK);

	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus() {
		// no code for an multi-status
		super(ERROR, JavaCore.PLUGIN_ID, 0, "JavaModelStatus", null); //$NON-NLS-1$
	}
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code) {
		super(ERROR, JavaCore.PLUGIN_ID, code, "JavaModelStatus", null); //$NON-NLS-1$
		fElements= JavaElementInfo.fgEmptyChildren;
	}
	/**
	 * Constructs an Java model status with the given corresponding
	 * elements.
	 */
	public JavaModelStatus(int code, IJavaElement[] elements) {
		super(ERROR, JavaCore.PLUGIN_ID, code, "JavaModelStatus", null); //$NON-NLS-1$
		fElements= elements;
		fPath= null;
	}
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code, String string) {
		super(ERROR, JavaCore.PLUGIN_ID, code, "JavaModelStatus", null); //$NON-NLS-1$
		fElements= JavaElementInfo.fgEmptyChildren;
		fPath= null;
		fString = string;
	}
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code, Throwable throwable) {
		super(ERROR, JavaCore.PLUGIN_ID, code, "JavaModelStatus", throwable); //$NON-NLS-1$
		fElements= JavaElementInfo.fgEmptyChildren;
	}
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code, IPath path) {
		super(ERROR, JavaCore.PLUGIN_ID, code, "JavaModelStatus", null); //$NON-NLS-1$
		fElements= JavaElementInfo.fgEmptyChildren;
		fPath= path;
	}
	/**
	 * Constructs an Java model status with the given corresponding
	 * element.
	 */
	public JavaModelStatus(int code, IJavaElement element) {
		this(code, new IJavaElement[]{element});
	}
	/**
	 * Constructs an Java model status with the given corresponding
	 * element and string
	 */
	public JavaModelStatus(int code, IJavaElement element, String string) {
		this(code, new IJavaElement[]{element});
		fString= string;
	}
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(CoreException coreException) {
		super(ERROR, JavaCore.PLUGIN_ID, CORE_EXCEPTION, "JavaModelStatus", coreException); //$NON-NLS-1$
		fElements= JavaElementInfo.fgEmptyChildren;
	}
	protected int getBits() {
		int severity = 1 << (getCode() % 100 / 33);
		int category = 1 << ((getCode() / 100) + 3);
		return severity | category;
	}
	/**
	 * @see IStatus
	 */
	public IStatus[] getChildren() {
		return fChildren;
	}
	/**
	 * @see IJavaModelStatus
	 */
	public IJavaElement[] getElements() {
		return fElements;
	}
	/**
	 * Returns the message that is relevant to the code of this status.
	 */
	public String getMessage() {
		if (getException() == null) {
			switch (getCode()) {
				case CORE_EXCEPTION :
					return Util.bind("status.coreException"); //$NON-NLS-1$
				case BUILDER_INITIALIZATION_ERROR:
					return Util.bind("build.initializationError"); //$NON-NLS-1$
				case BUILDER_SERIALIZATION_ERROR:
					return Util.bind("build.serializationError"); //$NON-NLS-1$
				case DEVICE_PATH:
					return Util.bind("status.cannotUseDeviceOnPath", getPath().toString()); //$NON-NLS-1$
				case DOM_EXCEPTION:
					return Util.bind("status.JDOMError"); //$NON-NLS-1$
				case ELEMENT_DOES_NOT_EXIST:
					return Util.bind("element.doesNotExist",fElements[0].getElementName()); //$NON-NLS-1$
				case EVALUATION_ERROR:
					return Util.bind("status.evaluationError", getString()); //$NON-NLS-1$
				case INDEX_OUT_OF_BOUNDS:
					return Util.bind("status.indexOutOfBounds"); //$NON-NLS-1$
				case INVALID_CONTENTS:
					return Util.bind("status.invalidContents"); //$NON-NLS-1$
				case INVALID_DESTINATION:
					return Util.bind("status.invalidDestination", fElements[0].getElementName()); //$NON-NLS-1$
				case INVALID_ELEMENT_TYPES:
					StringBuffer buff= new StringBuffer(Util.bind("operation.notSupported")); //$NON-NLS-1$
					for (int i= 0; i < fElements.length; i++) {
						if (i > 0) {
							buff.append(", "); //$NON-NLS-1$
						}
						buff.append(fElements[0].getElementName());
					}
					return buff.toString();
				case INVALID_NAME:
					return Util.bind("status.invalidName", getString()); //$NON-NLS-1$
				case INVALID_PACKAGE:
					return Util.bind("status.invalidPackage", getString()); //$NON-NLS-1$
				case INVALID_PATH:
					return Util.bind("status.invalidPath", getPath() == null ? "null" : getPath().toString()); //$NON-NLS-1$ //$NON-NLS-2$
				case INVALID_PROJECT:
					return Util.bind("status.invalidProject", getString()); //$NON-NLS-1$
				case INVALID_RESOURCE:
					return Util.bind("status.invalidResource", getString()); //$NON-NLS-1$
				case INVALID_RESOURCE_TYPE:
					return Util.bind("status.invalidResourceType", getString()); //$NON-NLS-1$
				case INVALID_SIBLING:
					return Util.bind("status.invalidSibling", fElements[0].getElementName()); //$NON-NLS-1$
				case IO_EXCEPTION:
					return Util.bind("status.IOException"); //$NON-NLS-1$
				case NAME_COLLISION:
					if (fElements != null && fElements.length > 0) {
						IJavaElement element = fElements[0];
						String name = element.getElementName();
						if (element instanceof IPackageFragment && name.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
							return Util.bind("operation.cannotRenameDefaultPackage"); //$NON-NLS-1$
						}
					}
					return Util.bind("status.nameCollision"); //$NON-NLS-1$
				case NO_ELEMENTS_TO_PROCESS:
					return Util.bind("operation.needElements"); //$NON-NLS-1$
				case NULL_NAME:
					return Util.bind("operation.needName"); //$NON-NLS-1$
				case NULL_PATH:
					return Util.bind("operation.needPath"); //$NON-NLS-1$
				case NULL_STRING:
					return Util.bind("operation.needString"); //$NON-NLS-1$
				case PATH_OUTSIDE_PROJECT:
					return Util.bind("operation.pathOutsideProject", getString(), fElements[0].getElementName()); //$NON-NLS-1$
				case READ_ONLY:
					IJavaElement element = fElements[0];
					String name = element.getElementName();
					if (element instanceof IPackageFragment && name.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
						return Util.bind("status.defaultPackageReadOnly"); //$NON-NLS-1$
					}
					return  Util.bind("status.readOnly", name); //$NON-NLS-1$
				case RELATIVE_PATH:
					return Util.bind("operation.needAbsolutePath", getPath().toString()); //$NON-NLS-1$
				case TARGET_EXCEPTION:
					return Util.bind("status.targetException"); //$NON-NLS-1$
				case UPDATE_CONFLICT:
					return Util.bind("status.updateConflict"); //$NON-NLS-1$
				case NO_LOCAL_CONTENTS :
					return Util.bind("status.noLocalContents", getPath().toString()); //$NON-NLS-1$
			}
			return getString();
		} else {
			return getException().getMessage();
		}
	}
	/**
	 * @see IJavaModelStatus#getPath()
	 */
	public IPath getPath() {
		return fPath;
	}
	/**
	 * @see IStatus#getSeverity()
	 */
	public int getSeverity() {
		if (fChildren == fgEmptyChildren) return super.getSeverity();
		int severity = -1;
		for (int i = 0, max = fChildren.length; i < max; i++) {
			int childrenSeverity = fChildren[i].getSeverity();
			if (childrenSeverity > severity) {
				severity = childrenSeverity;
			}
		}
		return severity;
	}
	/**
	 * @see IJavaModelStatus#getString()
	 */
	public String getString() {
		return fString;
	}
	/**
	 * @see IJavaModelStatus#isDoesNotExist()
	 */
	public boolean isDoesNotExist() {
		return getCode() == ELEMENT_DOES_NOT_EXIST;
	}
	/**
	 * @see IStatus#isMultiStatus()
	 */
	public boolean isMultiStatus() {
		return fChildren != fgEmptyChildren;
	}
	/**
	 * @see IStatus#isOK()
	 */
	public boolean isOK() {
		return getCode() == OK;
	}
	/**
	 * @see IStatus#matches(int)
	 */
	public boolean matches(int mask) {
		if (! isMultiStatus()) {
			return matches(this, mask);
		} else {
			for (int i = 0, max = fChildren.length; i < max; i++) {
				if (matches((JavaModelStatus) fChildren[i], mask))
					return true;
			}
			return false;
		}
	}
	/**
	 * Helper for matches(int).
	 */
	protected boolean matches(JavaModelStatus status, int mask) {
		int severityMask = mask & 0x7;
		int categoryMask = mask & ~0x7;
		int bits = status.getBits();
		return ((severityMask == 0) || (bits & severityMask) != 0) && ((categoryMask == 0) || (bits & categoryMask) != 0);
	}
	/**
	 * Creates and returns a new <code>IJavaModelStatus</code> that is a
	 * a multi-status status.
	 *
	 * @see IStatus#isMultiStatus()
	 */
	public static IJavaModelStatus newMultiStatus(IJavaModelStatus[] children) {
		JavaModelStatus jms = new JavaModelStatus();
		jms.fChildren = children;
		return jms;
	}
	/**
	 * Returns a printable representation of this exception for debugging
	 * purposes.
	 */
	public String toString() {
		if (this == VERIFIED_OK){
			return "JavaModelStatus[OK]"; //$NON-NLS-1$
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("Java Model Status ["); //$NON-NLS-1$
		buffer.append(getMessage());
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}
}
