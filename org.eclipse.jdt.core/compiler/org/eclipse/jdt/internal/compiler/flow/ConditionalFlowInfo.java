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
package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;

/**
 * Record conditional initialization status during definite assignment analysis
 *
 */
public class ConditionalFlowInfo extends FlowInfo {
	public FlowInfo initsWhenTrue;
	public FlowInfo initsWhenFalse;
ConditionalFlowInfo(FlowInfo initsWhenTrue, FlowInfo initsWhenFalse){
	this.initsWhenTrue = initsWhenTrue;
	this.initsWhenFalse = initsWhenFalse; 
}
public UnconditionalFlowInfo addInitializationsFrom(UnconditionalFlowInfo otherInits) {
	return unconditionalInits().addInitializationsFrom(otherInits);
}
public UnconditionalFlowInfo addPotentialInitializationsFrom(UnconditionalFlowInfo otherInits) {
	return unconditionalInits().addPotentialInitializationsFrom(otherInits);
}
public FlowInfo asNegatedCondition() {
	FlowInfo extra = initsWhenTrue;
	initsWhenTrue = initsWhenFalse;
	initsWhenFalse = extra;
	return this;
}
public FlowInfo copy() {
	return new ConditionalFlowInfo(initsWhenTrue.copy(), initsWhenFalse.copy());
}
public FlowInfo initsWhenFalse() {
	return initsWhenFalse;
}
public FlowInfo initsWhenTrue() {
	return initsWhenTrue;
}
/**
 * Check status of definite assignment for a field.
 */
public boolean isDefinitelyAssigned(FieldBinding field) {
	return initsWhenTrue.isDefinitelyAssigned(field) 
			&& initsWhenFalse.isDefinitelyAssigned(field);
	
}
/**
 * Check status of definite assignment for a local variable.
 */
public boolean isDefinitelyAssigned(LocalVariableBinding local) {
	return initsWhenTrue.isDefinitelyAssigned(local) 
			&& initsWhenFalse.isDefinitelyAssigned(local);
	
}
public boolean isFakeReachable(){
	return unconditionalInits().isFakeReachable();	
	//should maybe directly be: false
}
/**
 * Check status of potential assignment for a field.
 */
public boolean isPotentiallyAssigned(FieldBinding field) {
	return initsWhenTrue.isPotentiallyAssigned(field) 
			|| initsWhenFalse.isPotentiallyAssigned(field);
	
}
/**
 * Check status of potential assignment for a local variable.
 */
public boolean isPotentiallyAssigned(LocalVariableBinding local) {
	return initsWhenTrue.isPotentiallyAssigned(local) 
			|| initsWhenFalse.isPotentiallyAssigned(local);
	
}
/**
 * Record a field got definitely assigned.
 */
public void markAsDefinitelyAssigned(FieldBinding field) {
	initsWhenTrue.markAsDefinitelyAssigned(field);
	initsWhenFalse.markAsDefinitelyAssigned(field);	
}
/**
 * Record a field got definitely assigned.
 */
public void markAsDefinitelyAssigned(LocalVariableBinding local) {
	initsWhenTrue.markAsDefinitelyAssigned(local);
	initsWhenFalse.markAsDefinitelyAssigned(local);	
}
/**
 * Clear the initialization info for a field
 */
public void markAsDefinitelyNotAssigned(FieldBinding field) {
	initsWhenTrue.markAsDefinitelyNotAssigned(field);
	initsWhenFalse.markAsDefinitelyNotAssigned(field);	
}
/**
 * Clear the initialization info for a local variable
 */
public void markAsDefinitelyNotAssigned(LocalVariableBinding local) {
	initsWhenTrue.markAsDefinitelyNotAssigned(local);
	initsWhenFalse.markAsDefinitelyNotAssigned(local);	
}
public FlowInfo markAsFakeReachable(boolean isFakeReachable) {
	initsWhenTrue.markAsFakeReachable(isFakeReachable);
	initsWhenFalse.markAsFakeReachable(isFakeReachable);
	return this;
}
public UnconditionalFlowInfo mergedWith(UnconditionalFlowInfo otherInits) {
	return unconditionalInits().mergedWith(otherInits);
}
public String toString() {
	return "FlowInfo<true: " + initsWhenTrue.toString() + ", false: " + initsWhenFalse.toString() + ">"; //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-2$
}
public UnconditionalFlowInfo unconditionalInits() {
	return initsWhenTrue.unconditionalInits().copy()
			.mergedWith(initsWhenFalse.unconditionalInits());
}
}
