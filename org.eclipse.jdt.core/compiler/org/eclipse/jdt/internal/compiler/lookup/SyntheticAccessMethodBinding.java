/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Palo Alto Research Center, Incorporated - AspectJ adaptation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * AspectJ - added simple constructor
 */
public class SyntheticAccessMethodBinding extends MethodBinding {

	public FieldBinding targetReadField;	// read access to a field
	public FieldBinding targetWriteField;		// write access to a field
	public MethodBinding targetMethod;		// method or constructor

	public int accessType;

	public final static int FieldReadAccess = 1;
	public final static int FieldWriteAccess = 2;
	public final static int MethodAccess = 3;
	public final static int ConstructorAccess = 4;

	final static char[] AccessMethodPrefix = { 'a', 'c', 'c', 'e', 's', 's', '$' };

	public int sourceStart = 0; // start position of the matching declaration
	public int index; // used for sorting access methods in the class file
public SyntheticAccessMethodBinding(FieldBinding targetField, boolean isReadAccess, ReferenceBinding declaringClass) {
	this.modifiers = AccDefault | AccStatic | AccSynthetic;
	SourceTypeBinding declaringSourceType = (SourceTypeBinding) declaringClass;
	SyntheticAccessMethodBinding[] knownAccessMethods = declaringSourceType.syntheticAccessMethods();
	int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
	this.index = methodId;
	this.selector = CharOperation.concat(AccessMethodPrefix, String.valueOf(methodId).toCharArray());
	if (isReadAccess) {
		this.returnType = targetField.type;
		if (targetField.isStatic()) {
			this.parameters = NoParameters;
		} else {
			this.parameters = new TypeBinding[1];
			this.parameters[0] = declaringSourceType;
		}
		this.targetReadField = targetField;
		this.accessType = FieldReadAccess;
	} else {
		this.returnType = VoidBinding;
		if (targetField.isStatic()) {
			this.parameters = new TypeBinding[1];
			this.parameters[0] = targetField.type;
		} else {
			this.parameters = new TypeBinding[2];
			this.parameters[0] = declaringSourceType;
			this.parameters[1] = targetField.type;
		}
		this.targetWriteField = targetField;
		this.accessType = FieldWriteAccess;
	}
	this.thrownExceptions = NoExceptions;
	this.declaringClass = declaringSourceType;

	// check for method collision
	boolean needRename;
	do {
		check : {
			needRename = false;
			// check for collision with known methods
			MethodBinding[] methods = declaringSourceType.methods;
			for (int i = 0, length = methods.length; i < length; i++) {
				if (this.selector == methods[i].selector && this.areParametersEqual(methods[i])) {
					needRename = true;
					break check;
				}
			}
			// check for collision with synthetic accessors
			if (knownAccessMethods != null) {
				for (int i = 0, length = knownAccessMethods.length; i < length; i++) {
					if (knownAccessMethods[i] == null) continue;
					if (this.selector == knownAccessMethods[i].selector && this.areParametersEqual(methods[i])) {
						needRename = true;
						break check;
					}
				}
			}
		}
		if (needRename) { // retry with a selector postfixed by a growing methodId
			this.selector(CharOperation.concat(AccessMethodPrefix, String.valueOf(++methodId).toCharArray()));
		}
	} while (needRename);

	// retrieve sourceStart position for the target field for line number attributes
	FieldDeclaration[] fieldDecls = declaringSourceType.scope.referenceContext.fields;
	if (fieldDecls != null) {
		for (int i = 0, max = fieldDecls.length; i < max; i++) {
			if (fieldDecls[i].binding == targetField) {
				this.sourceStart = fieldDecls[i].sourceStart;
				return;
			}
		}
	}

/* did not find the target field declaration - it is a synthetic one
	public class A {
		public class B {
			public class C {
				void foo() {
					System.out.println("A.this = " + A.this);
				}
			}
		}
		public static void main(String args[]) {
			new A().new B().new C().foo();
		}
	}	
*/
	// We now at this point - per construction - it is for sure an enclosing instance, we are going to
	// show the target field type declaration location.
	this.sourceStart = declaringSourceType.scope.referenceContext.sourceStart; // use the target declaring class name position instead
}
public SyntheticAccessMethodBinding(MethodBinding targetMethod, ReferenceBinding receiverType) {

	if (targetMethod.isConstructor()) {
		this.initializeConstructorAccessor(targetMethod);
	} else {
		this.initializeMethodAccessor(targetMethod, receiverType);
	}
}

public SyntheticAccessMethodBinding(MethodBinding myBinding) {
	super(myBinding, null);	
}
/**
 * An constructor accessor is a constructor with an extra argument (declaringClass), in case of
 * collision with an existing constructor, then add again an extra argument (declaringClass again).
 */
 public void initializeConstructorAccessor(MethodBinding targetConstructor) {

	this.targetMethod = targetConstructor;
	this.modifiers = AccDefault | AccSynthetic;
	SourceTypeBinding sourceType = 
		(SourceTypeBinding) targetConstructor.declaringClass; 
	SyntheticAccessMethodBinding[] knownAccessMethods = 
		sourceType.syntheticAccessMethods(); 
	this.index = knownAccessMethods == null ? 0 : knownAccessMethods.length;

	this.selector = targetConstructor.selector;
	this.returnType = targetConstructor.returnType;
	this.accessType = ConstructorAccess;
	this.parameters = new TypeBinding[targetConstructor.parameters.length + 1];
	System.arraycopy(
		targetConstructor.parameters, 
		0, 
		this.parameters, 
		0, 
		targetConstructor.parameters.length); 
	parameters[targetConstructor.parameters.length] = 
		targetConstructor.declaringClass; 
	this.thrownExceptions = targetConstructor.thrownExceptions;
	this.declaringClass = sourceType;

	// check for method collision
	boolean needRename;
	do {
		check : {
			needRename = false;
			// check for collision with known methods
			MethodBinding[] methods = sourceType.methods;
			for (int i = 0, length = methods.length; i < length; i++) {
				if (this.selector == methods[i].selector
					&& this.areParametersEqual(methods[i])) {
					needRename = true;
					break check;
				}
			}
			// check for collision with synthetic accessors
			if (knownAccessMethods != null) {
				for (int i = 0, length = knownAccessMethods.length; i < length; i++) {
					if (knownAccessMethods[i] == null)
						continue;
					if (this.selector == knownAccessMethods[i].selector
						&& this.areParametersEqual(methods[i])) {
						needRename = true;
						break check;
					}
				}
			}
		}
		if (needRename) { // retry with a new extra argument
			int length = this.parameters.length;
			System.arraycopy(
				this.parameters, 
				0, 
				this.parameters = new TypeBinding[length + 1], 
				0, 
				length); 
			this.parameters[length] = this.declaringClass;
		}
	} while (needRename);

	// retrieve sourceStart position for the target method for line number attributes
	AbstractMethodDeclaration[] methodDecls = 
		sourceType.scope.referenceContext.methods; 
	if (methodDecls != null) {
		for (int i = 0, length = methodDecls.length; i < length; i++) {
			if (methodDecls[i].binding == targetConstructor) {
				this.sourceStart = methodDecls[i].sourceStart;
				return;
			}
		}
	}
}
/**
 * An method accessor is a method with an access$N selector, where N is incremented in case of collisions.
 */

public void initializeMethodAccessor(MethodBinding targetMethod, ReferenceBinding declaringClass) {
	
	this.targetMethod = targetMethod;
	this.modifiers = AccDefault | AccStatic | AccSynthetic;
	SourceTypeBinding declaringSourceType = (SourceTypeBinding) declaringClass;
	SyntheticAccessMethodBinding[] knownAccessMethods = declaringSourceType.syntheticAccessMethods();
	int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
	this.index = methodId;

	this.selector = CharOperation.concat(AccessMethodPrefix, String.valueOf(methodId).toCharArray());
	this.returnType = targetMethod.returnType;
	this.accessType = MethodAccess;
	
	if (targetMethod.isStatic()) {
		this.parameters = targetMethod.parameters;
	} else {
		this.parameters = new TypeBinding[targetMethod.parameters.length + 1];
		this.parameters[0] = declaringSourceType;
		System.arraycopy(targetMethod.parameters, 0, this.parameters, 1, targetMethod.parameters.length);
	}
	this.thrownExceptions = targetMethod.thrownExceptions;
	this.declaringClass = declaringSourceType;

	// check for method collision
	boolean needRename;
	do {
		check : {
			needRename = false;
			// check for collision with known methods
			MethodBinding[] methods = declaringSourceType.methods;
			for (int i = 0, length = methods.length; i < length; i++) {
				if (this.selector == methods[i].selector && this.areParametersEqual(methods[i])) {
					needRename = true;
					break check;
				}
			}
			// check for collision with synthetic accessors
			if (knownAccessMethods != null) {
				for (int i = 0, length = knownAccessMethods.length; i < length; i++) {
					if (knownAccessMethods[i] == null) continue;
					if (this.selector == knownAccessMethods[i].selector && this.areParametersEqual(methods[i])) {
						needRename = true;
						break check;
					}
				}
			}
		}
		if (needRename) { // retry with a selector & a growing methodId
			this.selector(CharOperation.concat(AccessMethodPrefix, String.valueOf(++methodId).toCharArray()));
		}
	} while (needRename);

	// retrieve sourceStart position for the target method for line number attributes
	AbstractMethodDeclaration[] methodDecls = declaringSourceType.scope.referenceContext.methods;
	if (methodDecls != null) {
		for (int i = 0, length = methodDecls.length; i < length; i++) {
			if (methodDecls[i].binding == targetMethod) {
				this.sourceStart = methodDecls[i].sourceStart;
				return;
			}
		}
	}
}
protected boolean isConstructorRelated() {
	return accessType == ConstructorAccess;
}
}
