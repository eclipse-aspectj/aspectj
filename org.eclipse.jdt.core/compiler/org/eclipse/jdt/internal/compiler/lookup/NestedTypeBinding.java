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
package org.eclipse.jdt.internal.compiler.lookup;

public class NestedTypeBinding extends SourceTypeBinding {
	public SourceTypeBinding enclosingType;

	public SyntheticArgumentBinding[] enclosingInstances;
	public SyntheticArgumentBinding[] outerLocalVariables;
	public int syntheticArgumentsOffset; // amount of slots used by synthetic constructor arguments
public NestedTypeBinding(char[][] typeName, ClassScope scope, SourceTypeBinding enclosingType) {
	super(typeName, enclosingType.fPackage, scope);
	this.tagBits |= IsNestedType;
	this.enclosingType = enclosingType;
}
/* Add a new synthetic argument for <actualOuterLocalVariable>.
* Answer the new argument or the existing argument if one already existed.
*/

public SyntheticArgumentBinding addSyntheticArgument(LocalVariableBinding actualOuterLocalVariable) {
	SyntheticArgumentBinding synthLocal = null;

	if (outerLocalVariables == null) {
		synthLocal = new SyntheticArgumentBinding(actualOuterLocalVariable);
		outerLocalVariables = new SyntheticArgumentBinding[] {synthLocal};
	} else {
		int size = outerLocalVariables.length;
		int newArgIndex = size;
		for (int i = size; --i >= 0;) {		// must search backwards
			if (outerLocalVariables[i].actualOuterLocalVariable == actualOuterLocalVariable)
				return outerLocalVariables[i];	// already exists
			if (outerLocalVariables[i].id > actualOuterLocalVariable.id)
				newArgIndex = i;
		}
		SyntheticArgumentBinding[] synthLocals = new SyntheticArgumentBinding[size + 1];
		System.arraycopy(outerLocalVariables, 0, synthLocals, 0, newArgIndex);
		synthLocals[newArgIndex] = synthLocal = new SyntheticArgumentBinding(actualOuterLocalVariable);
		System.arraycopy(outerLocalVariables, newArgIndex, synthLocals, newArgIndex + 1, size - newArgIndex);
		outerLocalVariables = synthLocals;
	}
	//System.out.println("Adding synth arg for local var: " + new String(actualOuterLocalVariable.name) + " to: " + new String(this.readableName()));
	if (scope.referenceCompilationUnit().isPropagatingInnerClassEmulation)
		this.updateInnerEmulationDependents();
	return synthLocal;
}
/* Add a new synthetic argument for <enclosingType>.
* Answer the new argument or the existing argument if one already existed.
*/

public SyntheticArgumentBinding addSyntheticArgument(ReferenceBinding enclosingType) {
	SyntheticArgumentBinding synthLocal = null;
	if (enclosingInstances == null) {
		synthLocal = new SyntheticArgumentBinding(enclosingType);
		enclosingInstances = new SyntheticArgumentBinding[] {synthLocal};
	} else {
		int size = enclosingInstances.length;
		int newArgIndex = size;
		for (int i = size; --i >= 0;) {
			if (enclosingInstances[i].type == enclosingType)
				return enclosingInstances[i]; // already exists
			if (this.enclosingType() == enclosingType)
				newArgIndex = 0;
		}
		SyntheticArgumentBinding[] newInstances = new SyntheticArgumentBinding[size + 1];
		System.arraycopy(enclosingInstances, 0, newInstances, newArgIndex == 0 ? 1 : 0, size);
		newInstances[newArgIndex] = synthLocal = new SyntheticArgumentBinding(enclosingType);
		enclosingInstances = newInstances;
	}
	//System.out.println("Adding synth arg for enclosing type: " + new String(enclosingType.readableName()) + " to: " + new String(this.readableName()));
	if (scope.referenceCompilationUnit().isPropagatingInnerClassEmulation)
		this.updateInnerEmulationDependents();
	return synthLocal;
}
/* Add a new synthetic argument and field for <actualOuterLocalVariable>.
* Answer the new argument or the existing argument if one already existed.
*/

public SyntheticArgumentBinding addSyntheticArgumentAndField(LocalVariableBinding actualOuterLocalVariable) {
	SyntheticArgumentBinding synthLocal = addSyntheticArgument(actualOuterLocalVariable);
	if (synthLocal == null) return null;

	if (synthLocal.matchingField == null)
		synthLocal.matchingField = addSyntheticField(actualOuterLocalVariable);
	return synthLocal;
}
/* Add a new synthetic argument and field for <enclosingType>.
* Answer the new argument or the existing argument if one already existed.
*/

public SyntheticArgumentBinding addSyntheticArgumentAndField(ReferenceBinding enclosingType) {
	SyntheticArgumentBinding synthLocal = addSyntheticArgument(enclosingType);
	if (synthLocal == null) return null;

	if (synthLocal.matchingField == null)
		synthLocal.matchingField = addSyntheticField(enclosingType);
	return synthLocal;
}
/**
 * Compute the resolved positions for all the synthetic arguments
 */
final public void computeSyntheticArgumentsOffset() {

	int position = 1; // inside constructor, reserve room for receiver
	
	// insert enclosing instances first, followed by the outerLocals
	SyntheticArgumentBinding[] enclosingInstances = this.syntheticEnclosingInstances();
	int enclosingInstancesCount = enclosingInstances == null ? 0 : enclosingInstances.length;
	for (int i = 0; i < enclosingInstancesCount; i++){
		SyntheticArgumentBinding syntheticArg = enclosingInstances[i];
		syntheticArg.resolvedPosition = position;
		if ((syntheticArg.type == LongBinding) || (syntheticArg.type == DoubleBinding)){
			position += 2;
		} else {
			position ++;
		}
	}
	SyntheticArgumentBinding[] outerLocals = this.syntheticOuterLocalVariables();
	int outerLocalsCount = outerLocals == null ? 0 : outerLocals.length;
		for (int i = 0; i < outerLocalsCount; i++){
		SyntheticArgumentBinding syntheticArg = outerLocals[i];
		syntheticArg.resolvedPosition = position;
		if ((syntheticArg.type == LongBinding) || (syntheticArg.type == DoubleBinding)){
			position += 2;
		} else {
			position ++;
		}
	}
	this.syntheticArgumentsOffset = position;
}
/* Answer the receiver's enclosing type... null if the receiver is a top level type.
*/

public ReferenceBinding enclosingType() {
	return enclosingType;
}
/* Answer the synthetic argument for <actualOuterLocalVariable> or null if one does not exist.
*/

public SyntheticArgumentBinding getSyntheticArgument(LocalVariableBinding actualOuterLocalVariable) {
	if (outerLocalVariables == null) return null;		// is null if no outer local variables are known

	for (int i = outerLocalVariables.length; --i >= 0;)
		if (outerLocalVariables[i].actualOuterLocalVariable == actualOuterLocalVariable)
			return outerLocalVariables[i];
	return null;
}
public SyntheticArgumentBinding[] syntheticEnclosingInstances() {
	return enclosingInstances;		// is null if no enclosing instances are required
}
public ReferenceBinding[] syntheticEnclosingInstanceTypes() {
	if (enclosingInstances == null)
		return null;

	int length = enclosingInstances.length;
	ReferenceBinding types[] = new ReferenceBinding[length];
	for (int i = 0; i < length; i++)
		types[i] = (ReferenceBinding) enclosingInstances[i].type;
	return types;
}
public SyntheticArgumentBinding[] syntheticOuterLocalVariables() {
	return outerLocalVariables;		// is null if no enclosing instances are required
}
/*
 * Trigger the dependency mechanism forcing the innerclass emulation
 * to be propagated to all dependent source types.
 */
public void updateInnerEmulationDependents() {
	// nothing to do in general, only local types are doing anything
}

/* Answer the synthetic argument for <targetEnclosingType> or null if one does not exist.
*/

public SyntheticArgumentBinding getSyntheticArgument(ReferenceBinding targetEnclosingType, BlockScope scope, boolean onlyExactMatch) {
	if (enclosingInstances == null) return null;		// is null if no enclosing instances are known

	// exact match
	for (int i = enclosingInstances.length; --i >= 0;)
		if (enclosingInstances[i].type == targetEnclosingType)
			if (enclosingInstances[i].actualOuterLocalVariable == null)
				return enclosingInstances[i];

	// type compatibility : to handle cases such as
	// class T { class M{}}
	// class S extends T { class N extends M {}} --> need to use S as a default enclosing instance for the super constructor call in N().
	if (!onlyExactMatch){
		for (int i = enclosingInstances.length; --i >= 0;)
			if (enclosingInstances[i].actualOuterLocalVariable == null)
				if (targetEnclosingType.isSuperclassOf((ReferenceBinding) enclosingInstances[i].type))
					return enclosingInstances[i];
	}
	return null;
}
}
