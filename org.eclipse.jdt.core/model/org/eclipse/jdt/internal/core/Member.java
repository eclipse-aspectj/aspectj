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

import java.util.ArrayList;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.jdom.IDOMNode;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 * @see IMember
 */

/* package */ abstract class Member extends SourceRefElement implements IMember {
protected Member(int type, IJavaElement parent, String name) {
	super(type, parent, name);
}
protected boolean areSimilarMethods(
	String name1, String[] params1, 
	String name2, String[] params2,
	String[] simpleNames1) {
		
	if (name1.equals(name2)) {
		int params1Length = params1.length;
		if (params1Length == params2.length) {
			for (int i = 0; i < params1Length; i++) {
				String simpleName1 = 
					simpleNames1 == null ? 
						Signature.getSimpleName(Signature.toString(params1[i])) :
						simpleNames1[i];
				String simpleName2 = Signature.getSimpleName(Signature.toString(params2[i]));
				if (!simpleName1.equals(simpleName2)) {
					return false;
				}
			}
			return true;
		}
	}
	return false;
}
/**
 * Converts a field constant from the compiler's representation
 * to the Java Model constant representation (Number or String).
 */
protected static Object convertConstant(Constant constant) {
	if (constant == null)
		return null;
	if (constant == Constant.NotAConstant) {
		return null;
	}
	switch (constant.typeID()) {
		case TypeIds.T_boolean :
			return constant.booleanValue() ? Boolean.TRUE : Boolean.FALSE;
		case TypeIds.T_byte :
			return new Byte(constant.byteValue());
		case TypeIds.T_char :
			return new Character(constant.charValue());
		case TypeIds.T_double :
			return new Double(constant.doubleValue());
		case TypeIds.T_float :
			return new Float(constant.floatValue());
		case TypeIds.T_int :
			return new Integer(constant.intValue());
		case TypeIds.T_long :
			return new Long(constant.longValue());
		case TypeIds.T_null :
			return null;
		case TypeIds.T_short :
			return new Short(constant.shortValue());
		case TypeIds.T_String :
			return constant.stringValue();
		default :
			return null;
	}
}
/**
 * @see JavaElement#equalsDOMNode
 */
protected boolean equalsDOMNode(IDOMNode node) throws JavaModelException {
	return getElementName().equals(node.getName());
}
/*
 * Helper method for SourceType.findMethods and BinaryType.findMethods
 */
protected IMethod[] findMethods(IMethod method, IMethod[] methods) {
	String elementName = method.getElementName();
	String[] parameters = method.getParameterTypes();
	int paramLength = parameters.length;
	String[] simpleNames = new String[paramLength];
	for (int i = 0; i < paramLength; i++) {
		simpleNames[i] = Signature.getSimpleName(Signature.toString(parameters[i]));
	}
	ArrayList list = new ArrayList();
	next: for (int i = 0, length = methods.length; i < length; i++) {
		IMethod existingMethod = methods[i];
		if (this.areSimilarMethods(
				elementName,
				parameters,
				existingMethod.getElementName(),
				existingMethod.getParameterTypes(),
				simpleNames)) {
			list.add(existingMethod);
		}
	}
	int size = list.size();
	if (size == 0) {
		return null;
	} else {
		IMethod[] result = new IMethod[size];
		list.toArray(result);
		return result;
	}
}
/**
 * @see IMember
 */
public IClassFile getClassFile() {
	return ((JavaElement)getParent()).getClassFile();
}
/**
 * @see IMember
 */
public IType getDeclaringType() {
	JavaElement parent = (JavaElement)getParent();
	if (parent.fLEType == TYPE) {
		return (IType) parent;
	}
	return null;
}
/**
 * @see IMember
 */
public int getFlags() throws JavaModelException {
	MemberElementInfo info = (MemberElementInfo) getElementInfo();
	return info.getModifiers();
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_TYPE;
}
/**
 * @see IMember
 */
public ISourceRange getNameRange() throws JavaModelException {
	MemberElementInfo info= (MemberElementInfo)getRawInfo();
	return new SourceRange(info.getNameSourceStart(), info.getNameSourceEnd() - info.getNameSourceStart() + 1);
}
/**
 * @see IMember
 */
public boolean isBinary() {
	return false;
}
protected boolean isMainMethod(IMethod method) throws JavaModelException {
	if ("main".equals(method.getElementName()) && Signature.SIG_VOID.equals(method.getReturnType())) { //$NON-NLS-1$
		int flags= method.getFlags();
		if (Flags.isStatic(flags) && Flags.isPublic(flags)) {
			String[] paramTypes= method.getParameterTypes();
			if (paramTypes.length == 1) {
				String name=  Signature.toString(paramTypes[0]);
				return "String[]".equals(Signature.getSimpleName(name)); //$NON-NLS-1$
			}
		}
	}
	return false;
}
/**
 * @see IJavaElement
 */
public boolean isReadOnly() {
	return getClassFile() != null;
}
/**
 * Changes the source indexes of this element.  Updates the name range as well.
 */
public void offsetSourceRange(int amount) {
	super.offsetSourceRange(amount);
	try {
		MemberElementInfo info = (MemberElementInfo) getRawInfo();
		info.setNameSourceStart(info.getNameSourceStart() + amount);
		info.setNameSourceEnd(info.getNameSourceEnd() + amount);
	} catch (JavaModelException npe) {
		return;
	}
}
/**
 */
public String readableName() {

	IJavaElement declaringType = getDeclaringType();
	if (declaringType != null) {
		String declaringName = ((JavaElement) getDeclaringType()).readableName();
		StringBuffer buffer = new StringBuffer(declaringName);
		buffer.append('.');
		buffer.append(this.getElementName());
		return buffer.toString();
	} else {
		return super.readableName();
	}
}
/**
 * Updates the source positions for this element.
 */
public void triggerSourceEndOffset(int amount, int nameStart, int nameEnd) {
	super.triggerSourceEndOffset(amount, nameStart, nameEnd);
	updateNameRange(nameStart, nameEnd);
}
/**
 * Updates the source positions for this element.
 */
public void triggerSourceRangeOffset(int amount, int nameStart, int nameEnd) {
	super.triggerSourceRangeOffset(amount, nameStart, nameEnd);
	updateNameRange(nameStart, nameEnd);
}
/**
 * Updates the name range for this element.
 */
protected void updateNameRange(int nameStart, int nameEnd) {
	try {
		MemberElementInfo info = (MemberElementInfo) getRawInfo();
		info.setNameSourceStart(nameStart);
		info.setNameSourceEnd(nameEnd);
	} catch (JavaModelException npe) {
		return;
	}
}
}
