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
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * The skeleton of the class 'org.eclipse.jdt.internal.eval.target.CodeSnippet'
 * used at compile time. Note that the method run() is declared to
 * throw Throwable so that the user can write a code snipet that
 * throws checked exceptio without having to catch those.
 */
public class CodeSnippetSkeleton implements IBinaryType, EvaluationConstants {
	IBinaryMethod[] methods = new IBinaryMethod[] {
		new BinaryMethodSkeleton(
			"<init>".toCharArray(), //$NON-NLS-1$
			"()V".toCharArray(), //$NON-NLS-1$
			new char[][] {},
			true
		),
		new BinaryMethodSkeleton(
			"run".toCharArray(), //$NON-NLS-1$
			"()V".toCharArray(), //$NON-NLS-1$
			new char[][] {"java/lang/Throwable".toCharArray()}, //$NON-NLS-1$
			false
		),
		new BinaryMethodSkeleton(
			"setResult".toCharArray(), //$NON-NLS-1$
			"(Ljava/lang/Object;Ljava/lang/Class;)V".toCharArray(), //$NON-NLS-1$
			new char[][] {},
			false
		)
	};

	public class BinaryMethodSkeleton implements IBinaryMethod {
		char[][] exceptionTypeNames;
		char[] methodDescriptor;
		char[] selector;
		boolean isConstructor;
		
		public BinaryMethodSkeleton(char[] selector, char[] methodDescriptor, char[][] exceptionTypeNames, boolean isConstructor) {
			this.selector = selector;
			this.methodDescriptor = methodDescriptor;
			this.exceptionTypeNames = exceptionTypeNames;
			this.isConstructor = isConstructor;
		}
		
		public char[][] getExceptionTypeNames() {
			return this.exceptionTypeNames;
		}
		
		public char[] getMethodDescriptor() {
			return this.methodDescriptor;
		}
		
		public int getModifiers() {
			return IConstants.AccPublic;
		}
		
		public char[] getSelector() {
			return this.selector;
		}
		
		public boolean isClinit() {
			return false;
		}
		
		public boolean isConstructor() {
			return this.isConstructor;
		}
		/**
		 * @see IGenericMethod#getArgumentNames()
		 */
		public char[][] getArgumentNames() {
			return null;
		}

}
	
/**
 * CodeSnippetSkeleton constructor comment.
 */
public CodeSnippetSkeleton() {
	super();
}
public char[] getEnclosingTypeName() {
	return null;
}
public IBinaryField[] getFields() {
	return null;
}
public char[] getFileName() {
	return CharOperation.concat(CODE_SNIPPET_NAME, ".java".toCharArray()); //$NON-NLS-1$
}
public char[][] getInterfaceNames() {
	return null;
}
public IBinaryNestedType[] getMemberTypes() {
	return null;
}
public IBinaryMethod[] getMethods() {
	return this.methods;
}
public int getModifiers() {
	return IConstants.AccPublic;
}
public char[] getName() {
	return CODE_SNIPPET_NAME;
}
public char[] getSuperclassName() {
	return null;
}
public boolean isAnonymous() {
	return false;
}
public boolean isBinaryType() {
	return true;
}
public boolean isClass() {
	return true;
}
public boolean isInterface() {
	return false;
}
public boolean isLocal() {
	return false;
}
public boolean isMember() {
	return false;
}
public char[] sourceFileName() {
	return null;
}
}
