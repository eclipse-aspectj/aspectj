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
import org.eclipse.jdt.internal.compiler.SourceElementRequestorAdapter;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;

/**
 * @see IMethod
 */

/* package */ class BinaryMethod extends BinaryMember implements IMethod {
	
	class DecodeParametersNames extends SourceElementRequestorAdapter {
			String[] parametersNames;
		
			public void enterMethod(
				int declarationStart,
				int modifiers,
				char[] returnType,
				char[] name,
				int nameSourceStart,
				int nameSourceEnd,
				char[][] parameterTypes,
				char[][] parameterNames,
				char[][] exceptionTypes) {
					if (parameterNames != null) {
						int length = parameterNames.length;
						this.parametersNames = new String[length];
						for (int i = 0; i < length; i++) {
							this.parametersNames[i] = new String(parameterNames[i]);
						}
					}
				}
				
			public void enterConstructor(
				int declarationStart,
				int modifiers,
				char[] name,
				int nameSourceStart,
				int nameSourceEnd,
				char[][] parameterTypes,
				char[][] parameterNames,
				char[][] exceptionTypes) {
					if (parameterNames != null) {
						int length = parameterNames.length;
						this.parametersNames = new String[length];
						for (int i = 0; i < length; i++) {
							this.parametersNames[i] = new String(parameterNames[i]);
						}
					}
				}
				
				public String[] getParametersNames() {
					return this.parametersNames;
				}
	}

	/**
	 * The parameter type signatures of the method - stored locally
	 * to perform equality test. <code>null</code> indicates no
	 * parameters.
	 */
	protected String[] fParameterTypes;
	/**
	 * The parameter names for the method.
	 */
	protected String[] fParameterNames;

	/**
	 * An empty list of Strings
	 */
	protected static final String[] fgEmptyList= new String[] {};
	protected String[] fExceptionTypes;
	protected String fReturnType;
protected BinaryMethod(IType parent, String name, String[] parameterTypes) {
	super(METHOD, parent, name);
	Assert.isTrue(name.indexOf('.') == -1);
	if (parameterTypes == null) {
		fParameterTypes= fgEmptyList;
	} else {
		fParameterTypes= parameterTypes;
	}
}
public boolean equals(Object o) {
	return super.equals(o) && Util.equalArraysOrNull(fParameterTypes, ((BinaryMethod)o).fParameterTypes);
}
/**
 * @see IMethod
 */
public String[] getExceptionTypes() throws JavaModelException {
	if (fExceptionTypes == null) {
		IBinaryMethod info = (IBinaryMethod) getRawInfo();
		char[][] eTypeNames = info.getExceptionTypeNames();
		if (eTypeNames == null || eTypeNames.length == 0) {
			fExceptionTypes = fgEmptyList;
		} else {
			eTypeNames = ClassFile.translatedNames(eTypeNames);
			fExceptionTypes = new String[eTypeNames.length];
			for (int j = 0, length = eTypeNames.length; j < length; j++) {
				// 1G01HRY: ITPJCORE:WINNT - method.getExceptionType not in correct format
				int nameLength = eTypeNames[j].length;
				char[] convertedName = new char[nameLength + 2];
				System.arraycopy(eTypeNames[j], 0, convertedName, 1, nameLength);
				convertedName[0] = 'L';
				convertedName[nameLength + 1] = ';';
				fExceptionTypes[j] = new String(convertedName);
			}
		}
	}
	return fExceptionTypes;
}
/**
 * @see IMember
 */
public int getFlags() throws JavaModelException {
	IBinaryMethod info = (IBinaryMethod) getRawInfo();
	return info.getModifiers();
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
 * Look for source attachment information to retrieve the actual parameter names as stated in source.
 */
public String[] getParameterNames() throws JavaModelException {
	if (fParameterNames == null) {

		// force source mapping if not already done
		IType type = (IType) getParent();
		SourceMapper mapper = getSourceMapper();
		if (mapper != null) {
			char[][] parameterNames = mapper.getMethodParameterNames(this);
			
			// map source and try to find parameter names
			if(parameterNames == null) {
				char[] source = mapper.findSource(type);
				if (source != null){
					mapper.mapSource(type, source);
				}
				parameterNames = mapper.getMethodParameterNames(this);
			}
			
			// if parameter names exist, convert parameter names to String array
			if(parameterNames != null) {
				fParameterNames = new String[parameterNames.length];
				for (int i = 0; i < parameterNames.length; i++) {
					fParameterNames[i] = new String(parameterNames[i]);
				}
			}
		}
		// if still no parameter names, produce fake ones
		if (fParameterNames == null) {
			IBinaryMethod info = (IBinaryMethod) getRawInfo();
			int paramCount = Signature.getParameterCount(new String(info.getMethodDescriptor()));
			fParameterNames = new String[paramCount];
			for (int i = 0; i < paramCount; i++) {
				fParameterNames[i] = "arg" + i; //$NON-NLS-1$
			}
		}
	}
	return fParameterNames;
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
	IBinaryMethod info = (IBinaryMethod) getRawInfo();
	if (fReturnType == null) {
		String returnType= Signature.getReturnType(new String(info.getMethodDescriptor()));
		fReturnType= new String(ClassFile.translatedName(returnType.toCharArray()));
	}
	return fReturnType;
}
/**
 * @see IMethod
 */
public String getSignature() throws JavaModelException {
	IBinaryMethod info = (IBinaryMethod) getRawInfo();
	return new String(info.getMethodDescriptor());
}
/**
 * @see IMethod
 */
public boolean isConstructor() throws JavaModelException {
	IBinaryMethod info = (IBinaryMethod) getRawInfo();
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
	buffer.append("("); //$NON-NLS-1$
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
	buffer.append(")"); //$NON-NLS-1$
	return buffer.toString();
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
