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

import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;

public abstract class FlowInfo {
	public static final UnconditionalFlowInfo DeadEnd = new UnconditionalFlowInfo(); // Represents a dead branch status of initialization
abstract public UnconditionalFlowInfo addInitializationsFrom(UnconditionalFlowInfo otherInits);
abstract public UnconditionalFlowInfo addPotentialInitializationsFrom(UnconditionalFlowInfo otherInits);
public FlowInfo asNegatedCondition() {
	return this;
}
public boolean complainIfUnreachable(Statement statement, BlockScope scope) {
	// Report an error if necessary

	return false;
}
public static FlowInfo conditional(FlowInfo initsWhenTrue, FlowInfo initsWhenFalse){
	// if (initsWhenTrue.equals(initsWhenFalse)) return initsWhenTrue; -- could optimize if #equals is defined
	return new ConditionalFlowInfo(initsWhenTrue, initsWhenFalse);
}
abstract public FlowInfo copy();
public static UnconditionalFlowInfo initial(int maxFieldCount) {
	UnconditionalFlowInfo info = new UnconditionalFlowInfo();
	info.maxFieldCount = maxFieldCount;
	return info;
}
abstract public FlowInfo initsWhenFalse();
abstract public FlowInfo initsWhenTrue();
final public boolean isDeadEnd() {
	return this == DeadEnd;
}
/**
 * Check status of definite assignment for a field.
 */
 abstract public boolean isDefinitelyAssigned(FieldBinding field);   
/**
 * Check status of definite assignment for a local.
 */
public abstract boolean isDefinitelyAssigned(LocalVariableBinding local);
abstract public boolean isFakeReachable();
/**
 * Check status of potential assignment for a field.
 */
 abstract public boolean isPotentiallyAssigned(FieldBinding field);   
/**
 * Check status of potential assignment for a local variable.
 */
 abstract public boolean isPotentiallyAssigned(LocalVariableBinding field);   
/**
 * Record a field got definitely assigned.
 */
abstract public void markAsDefinitelyAssigned(FieldBinding field);
/**
 * Record a local got definitely assigned.
 */
abstract public void markAsDefinitelyAssigned(LocalVariableBinding local);
/**
 * Clear the initialization info for a field
 */
abstract public void markAsDefinitelyNotAssigned(FieldBinding field);
/**
 * Clear the initialization info for a local variable
 */
abstract public void markAsDefinitelyNotAssigned(LocalVariableBinding local);
abstract public FlowInfo markAsFakeReachable(boolean isFakeReachable);
abstract public UnconditionalFlowInfo mergedWith(UnconditionalFlowInfo otherInits);
public String toString(){
	if (this == DeadEnd){
		return "FlowInfo.DeadEnd"; //$NON-NLS-1$
	}
	return super.toString();
}
abstract public UnconditionalFlowInfo unconditionalInits();
}
