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

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.jdom.IDOMMethod;
import org.eclipse.jdt.core.jdom.IDOMNode;

/**
 * @see IMethod
 */

/* package */ class SourceMethod extends Member implements IMethod {

	/**
	 * The parameter type signatures of the method - stored locally
	 * to perform equality test. <code>null</code> indicates no
	 * parameters.
	 */
	protected String[] fParameterTypes;

	/**
	 * An empty list of Strings
	 */
	protected static final String[] fgEmptyList= new String[] {};
protected SourceMethod(IType parent, String name, String[] parameterTypes) {
	super(METHOD, parent, name);
	Assert.isTrue(name.indexOf('.') == -1);
	if (parameterTypes == null) {
		fParameterTypes= fgEmptyList;
	} else {
		fParameterTypes= parameterTypes;
	}
}
public boolean equals(Object o) {
	return super.equals(o) && Util.equalArraysOrNull(fParameterTypes, ((SourceMethod)o).fParameterTypes);
}
/**
 * @see JavaElement#equalsDOMNode
 */
protected boolean equalsDOMNode(IDOMNode node) throws JavaModelException {
	if (node.getNodeType() == IDOMNode.METHOD) {
		IDOMMethod m = (IDOMMethod)node;
		if (isConstructor()) {
			return 
				(m.isConstructor() || m.getName().equals(this.getElementName()) /* case of a constructor that is being renamed */) 
					&& signatureEquals(m);
		} else {
			return super.equalsDOMNode(node) && signatureEquals(m);
		}
	} else {
		return false;
	}

}
/**
 * @see IMethod
 */
public String[] getExceptionTypes() throws JavaModelException {
	SourceMethodElementInfo info = (SourceMethodElementInfo) getElementInfo();
	char[][] exs= info.getExceptionTypeNames();
	return CompilationUnitStructureRequestor.convertTypeNamesToSigs(exs);
}
/**
 * @see JavaElement#getHandleMemento()
 */
public String getHandleMemento() {
	StringBuffer buff = new StringBuffer(((JavaElement) getParent()).getHandleMemento());
	buff.append(getHandleMementoDelimiter());
	buff.append(getElementName());
	for (int i = 0; i < fParameterTypes.length; i++) {
		buff.append(getHandleMementoDelimiter());
		buff.append(fParameterTypes[i]);
	}
	return buff.toString();
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_METHOD;
}
/**
 * @see IMethod
 */
public int getNumberOfParameters() {
	return fParameterTypes == null ? 0 : fParameterTypes.length;
}
/**
 * @see IMethod
 */
public String[] getParameterNames() throws JavaModelException {
	SourceMethodElementInfo info = (SourceMethodElementInfo) getElementInfo();
	char[][] names= info.getArgumentNames();
	if (names == null || names.length == 0) {
		return fgEmptyList;
	}
	String[] strings= new String[names.length];
	for (int i= 0; i < names.length; i++) {
		strings[i]= new String(names[i]);
	}
	return strings;
}
/**
 * @see IMethod
 */
public String[] getParameterTypes() {
	return fParameterTypes;
}
/**
 * @see IMethod
 */
public String getReturnType() throws JavaModelException {
	SourceMethodElementInfo info = (SourceMethodElementInfo) getElementInfo();
	return Signature.createTypeSignature(info.getReturnTypeName(), false);
}
/**
 * @see IMethod
 */
public String getSignature() throws JavaModelException {
	SourceMethodElementInfo info = (SourceMethodElementInfo) getElementInfo();
	return info.getSignature();
}
/**
 * @see IMethod
 */
public boolean isConstructor() throws JavaModelException {
	SourceMethodElementInfo info = (SourceMethodElementInfo) getElementInfo();
	return info.isConstructor();
}
/**
 * @see IMethod#isMainMethod()
 */
public boolean isMainMethod() throws JavaModelException {
	return this.isMainMethod(this);
}

/**
 * @see IMethod#isSimilar(IMethod)
 */
public boolean isSimilar(IMethod method) {
	return 
		this.areSimilarMethods(
			this.getElementName(), this.getParameterTypes(),
			method.getElementName(), method.getParameterTypes(),
			null);
}

/**
 */
public String readableName() {

	StringBuffer buffer = new StringBuffer(super.readableName());
	buffer.append('(');
	String[] parameterTypes = this.getParameterTypes();
	int length;
	if (parameterTypes != null && (length = parameterTypes.length) > 0) {
		for (int i = 0; i < length; i++) {
			buffer.append(Signature.toString(parameterTypes[i]));
			if (i < length - 1) {
				buffer.append(", "); //$NON-NLS-1$
			}
		}
	}
	buffer.append(')');
	return buffer.toString();
}
/**
 * Returns <code>true</code> if the signature of this <code>SourceMethod</code> matches that of the given
 * <code>IDOMMethod</code>, otherwise <code>false</code>. 
 */
protected boolean signatureEquals(IDOMMethod method) throws JavaModelException {
	String[] otherTypes= method.getParameterTypes();
	String[] types= getParameterTypes();
	boolean ok= true;

	// ensure the number of parameters match
	if (otherTypes == null || otherTypes.length == 0) {
		ok= (types == null || types.length == 0);
	} else if (types != null) {
		ok= (otherTypes.length == types.length);
	} else {
		return false;
	}

	// ensure the parameter type signatures match
	if (ok) {
		if (types != null) {
			int i;
			for (i= 0; i < types.length; i++) {
				String otherType= Signature.createTypeSignature(otherTypes[i].toCharArray(), false);
				if (!types[i].equals(otherType)) {
					ok= false;
					break;
				}
			}
		}
	}

	return ok;
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(this.tabString(tab));
	if (info == null) {
		buffer.append(getElementName());
		buffer.append(" (not open)"); //$NON-NLS-1$
	} else if (info == NO_INFO) {
		buffer.append(getElementName());
	} else {
		try {
			if (Flags.isStatic(this.getFlags())) {
				buffer.append("static "); //$NON-NLS-1$
			}
			if (!this.isConstructor()) {
				buffer.append(Signature.toString(this.getReturnType()));
				buffer.append(' ');
			}
			buffer.append(this.getElementName());
			buffer.append('(');
			String[] parameterTypes = this.getParameterTypes();
			int length;
			if (parameterTypes != null && (length = parameterTypes.length) > 0) {
				for (int i = 0; i < length; i++) {
					buffer.append(Signature.toString(parameterTypes[i]));
					if (i < length - 1) {
						buffer.append(", "); //$NON-NLS-1$
					}
				}
			}
			buffer.append(')');
		} catch (JavaModelException e) {
			buffer.append("<JavaModelException in toString of " + getElementName()); //$NON-NLS-1$
		}
	}
}
}
