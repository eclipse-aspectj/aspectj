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

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.codegen.Label;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class FlowContext implements TypeConstants {
	public AstNode associatedNode;
	public FlowContext parent;

	public final static FlowContext NotContinuableContext =
		new FlowContext(null, null);
		
	public FlowContext(FlowContext parent, AstNode associatedNode) {
		this.parent = parent;
		this.associatedNode = associatedNode;
	}
	
	public Label breakLabel() {
		return null;
	}
	
	public void checkExceptionHandlers(
		TypeBinding[] raisedExceptions,
		AstNode location,
		FlowInfo flowInfo,
		BlockScope scope) {

		// check that all the argument exception types are handled
		// JDK Compatible implementation - when an exception type is thrown, 
		// all related catch blocks are marked as reachable... instead of those only
		// until the point where it is safely handled (Smarter - see comment at the end)
		int remainingCount; // counting the number of remaining unhandled exceptions
		int raisedCount; // total number of exceptions raised
		if ((raisedExceptions == null)
			|| ((raisedCount = raisedExceptions.length) == 0))
			return;
		remainingCount = raisedCount;

		// duplicate the array of raised exceptions since it will be updated
		// (null replaces any handled exception)
		System.arraycopy(
			raisedExceptions,
			0,
			(raisedExceptions = new TypeBinding[raisedCount]),
			0,
			raisedCount);
		FlowContext traversedContext = this;
		while (traversedContext != null) {
			AstNode sub;
			if (((sub = traversedContext.subRoutine()) != null) && sub.cannotReturn()) {
				// traversing a non-returning subroutine means that all unhandled 
				// exceptions will actually never get sent...
				return;
			}
			// filter exceptions that are locally caught from the most enclosing 
			// try statement to the outer ones.
			if (traversedContext instanceof ExceptionHandlingFlowContext) {
				ExceptionHandlingFlowContext exceptionContext =
					(ExceptionHandlingFlowContext) traversedContext;
				ReferenceBinding[] caughtExceptions;
				if ((caughtExceptions = exceptionContext.handledExceptions) != NoExceptions) {
					int caughtCount = caughtExceptions.length;
					boolean[] locallyCaught = new boolean[raisedCount]; // at most

					for (int caughtIndex = 0; caughtIndex < caughtCount; caughtIndex++) {
						ReferenceBinding caughtException = caughtExceptions[caughtIndex];
						for (int raisedIndex = 0; raisedIndex < raisedCount; raisedIndex++) {
							TypeBinding raisedException;
							if ((raisedException = raisedExceptions[raisedIndex]) != null) {
								switch (Scope.compareTypes(raisedException, caughtException)) {
									case EqualOrMoreSpecific :
										exceptionContext.recordHandlingException(
											caughtException,
											flowInfo.unconditionalInits(),
											raisedException,
											location,
											locallyCaught[raisedIndex]);
										// was already definitely caught ?
										if (!locallyCaught[raisedIndex]) {
											locallyCaught[raisedIndex] = true;
											// remember that this exception has been definitely caught
											remainingCount--;
										}
										break;
									case MoreGeneric :
										exceptionContext.recordHandlingException(
											caughtException,
											flowInfo.unconditionalInits(),
											raisedException,
											location,
											false);
										// was not caught already per construction
								}
							}
						}
					}
					// remove locally caught exceptions from the remaining ones
					for (int i = 0; i < raisedCount; i++) {
						if (locallyCaught[i]) {
							raisedExceptions[i] = null; // removed from the remaining ones.
						}
					}
				}
				// method treatment for unchecked exceptions
				if (exceptionContext.isMethodContext) {
					for (int i = 0; i < raisedCount; i++) {
						TypeBinding raisedException;
						if ((raisedException = raisedExceptions[i]) != null) {
							if (scope
								.areTypesCompatible(raisedException, scope.getJavaLangRuntimeException())
								|| scope.areTypesCompatible(raisedException, scope.getJavaLangError())) {
								remainingCount--;
								raisedExceptions[i] = null;
							}
						}
					}
					// anonymous constructors are allowed to throw any exceptions (their thrown exceptions
					// clause will be fixed up later as per JLS 8.6).
					if (exceptionContext.associatedNode instanceof AbstractMethodDeclaration){
						AbstractMethodDeclaration method = (AbstractMethodDeclaration)exceptionContext.associatedNode;
						if (method.isConstructor() && method.binding.declaringClass.isAnonymousType()){
								
							for (int i = 0; i < raisedCount; i++) {
								TypeBinding raisedException;
								if ((raisedException = raisedExceptions[i]) != null) {
									exceptionContext.mergeUnhandledException(raisedException);
								}
							}
							return; // no need to complain, will fix up constructor exceptions						
						}
					}
					break; // not handled anywhere, thus jump to error handling
				}
			}
			if (remainingCount == 0)
				return;
			traversedContext = traversedContext.parent;
		}
		// if reaches this point, then there are some remaining unhandled exception types.	
		for (int i = 0; i < raisedCount; i++) {
			TypeBinding exception;
			if ((exception = raisedExceptions[i]) != null) {
				scope.problemReporter().unhandledException(exception, location);
			}
		}
	}

	public void checkExceptionHandlers(
		TypeBinding raisedException,
		AstNode location,
		FlowInfo flowInfo,
		BlockScope scope) {

		// LIGHT-VERSION OF THE EQUIVALENT WITH AN ARRAY OF EXCEPTIONS
		// check that all the argument exception types are handled
		// JDK Compatible implementation - when an exception type is thrown, 
		// all related catch blocks are marked as reachable... instead of those only
		// until the point where it is safely handled (Smarter - see comment at the end)
		FlowContext traversedContext = this;
		while (traversedContext != null) {
			AstNode sub;
			if (((sub = traversedContext.subRoutine()) != null) && sub.cannotReturn()) {
				// traversing a non-returning subroutine means that all unhandled 
				// exceptions will actually never get sent...
				return;
			}
			// filter exceptions that are locally caught from the most enclosing 
			// try statement to the outer ones.
			if (traversedContext instanceof ExceptionHandlingFlowContext) {
				ExceptionHandlingFlowContext exceptionContext =
					(ExceptionHandlingFlowContext) traversedContext;
				ReferenceBinding[] caughtExceptions;
				if ((caughtExceptions = exceptionContext.handledExceptions) != NoExceptions) {
					boolean definitelyCaught = false;
					for (int caughtIndex = 0, caughtCount = caughtExceptions.length;
						caughtIndex < caughtCount;
						caughtIndex++) {
						ReferenceBinding caughtException = caughtExceptions[caughtIndex];
						switch (Scope.compareTypes(raisedException, caughtException)) {
							case EqualOrMoreSpecific :
								exceptionContext.recordHandlingException(
									caughtException,
									flowInfo.unconditionalInits(),
									raisedException,
									location,
									definitelyCaught);
								// was it already definitely caught ?
								definitelyCaught = true;
								break;
							case MoreGeneric :
								exceptionContext.recordHandlingException(
									caughtException,
									flowInfo.unconditionalInits(),
									raisedException,
									location,
									false);
								// was not caught already per construction
						}
					}
					if (definitelyCaught)
						return;
				}
				// method treatment for unchecked exceptions
				if (exceptionContext.isMethodContext) {
					if (scope
						.areTypesCompatible(raisedException, scope.getJavaLangRuntimeException())
						|| scope.areTypesCompatible(raisedException, scope.getJavaLangError()))
						return;
						
					// anonymous constructors are allowed to throw any exceptions (their thrown exceptions
					// clause will be fixed up later as per JLS 8.6).
					if (exceptionContext.associatedNode instanceof AbstractMethodDeclaration){
						AbstractMethodDeclaration method = (AbstractMethodDeclaration)exceptionContext.associatedNode;
						if (method.isConstructor() && method.binding.declaringClass.isAnonymousType()){
									
							exceptionContext.mergeUnhandledException(raisedException);
							return; // no need to complain, will fix up constructor exceptions						
						}
					}
					break; // not handled anywhere, thus jump to error handling
				}
			}
			traversedContext = traversedContext.parent;
		}
		// if reaches this point, then there are some remaining unhandled exception types.
		scope.problemReporter().unhandledException(raisedException, location);
	}

	public Label continueLabel() {
		return null;
	}

	/*
	 * lookup through break labels
	 */
	public FlowContext getTargetContextForBreakLabel(char[] labelName) {
		FlowContext current = this, lastNonReturningSubRoutine = null;
		while (current != null) {
			if (current.isNonReturningContext()) {
				lastNonReturningSubRoutine = current;
			}
			char[] currentLabelName;
			if (((currentLabelName = current.labelName()) != null)
				&& CharOperation.equals(currentLabelName, labelName)) {
				if (lastNonReturningSubRoutine == null) {
					return current;
				} else {
					return lastNonReturningSubRoutine;
				}
			}
			current = current.parent;
		}
		// not found
		return null;
	}

	/*
	 * lookup through continue labels
	 */
	public FlowContext getTargetContextForContinueLabel(char[] labelName) {
		FlowContext current = this,
			lastContinuable = null,
			lastNonReturningSubRoutine = null;
		while (current != null) {
			if (current.isNonReturningContext()) {
				lastNonReturningSubRoutine = current;
			} else {
				if (current.isContinuable()) {
					lastContinuable = current;
				}
			}
			char[] currentLabelName;
			if (((currentLabelName = current.labelName()) != null)
				&& CharOperation.equals(currentLabelName, labelName)) {
				if ((lastContinuable != null)
					&& (current.associatedNode.concreteStatement()
						== lastContinuable.associatedNode)) {
					if (lastNonReturningSubRoutine == null) {
						return lastContinuable;
					} else {
						return lastNonReturningSubRoutine;
					}
				} else {
					// label is found, but not a continuable location
					return NotContinuableContext;
				}
			}
			current = current.parent;
		}
		// not found
		return null;
	}

	/*
	 * lookup a default break through breakable locations
	 */
	public FlowContext getTargetContextForDefaultBreak() {
		FlowContext current = this, lastNonReturningSubRoutine = null;
		while (current != null) {
			if (current.isNonReturningContext()) {
				lastNonReturningSubRoutine = current;
			}
			if (current.isBreakable()) {
				if (lastNonReturningSubRoutine == null) {
					return current;
				} else {
					return lastNonReturningSubRoutine;
				}
			}
			current = current.parent;
		}
		// not found
		return null;
	}

	/*
	 * lookup a default continue amongst continuable locations
	 */
	public FlowContext getTargetContextForDefaultContinue() {
		FlowContext current = this, lastNonReturningSubRoutine = null;
		while (current != null) {
			if (current.isNonReturningContext()) {
				lastNonReturningSubRoutine = current;
			}
			if (current.isContinuable()) {
				if (lastNonReturningSubRoutine == null) {
					return current;
				} else {
					return lastNonReturningSubRoutine;
				}
			}
			current = current.parent;
		}
		// not found
		return null;
	}

	public String individualToString() {
		return "Flow context"; //$NON-NLS-1$
	}

	public FlowInfo initsOnBreak() {
		return FlowInfo.DeadEnd;
	}

	public boolean isBreakable() {
		return false;
	}

	public boolean isContinuable() {
		return false;
	}

	public boolean isNonReturningContext() {
		return false;
	}

	public boolean isSubRoutine() {
		return false;
	}

	public char[] labelName() {
		return null;
	}

	public void recordBreakFrom(FlowInfo flowInfo) {
	}

	public void recordContinueFrom(FlowInfo flowInfo) {
	}

	boolean recordFinalAssignment(
		VariableBinding variable,
		Reference finalReference) {
		return true; // keep going
	}

	public void recordReturnFrom(UnconditionalFlowInfo flowInfo) {
	}

	public void recordSettingFinal(
		VariableBinding variable,
		Reference finalReference) {
		// for initialization inside looping statement that effectively loops
		FlowContext context = this;
		while (context != null) {
			if (!context.recordFinalAssignment(variable, finalReference)) {
				break; // no need to keep going
			}
			context = context.parent;
		}
	}

	void removeFinalAssignmentIfAny(Reference reference) {
	}

	public AstNode subRoutine() {
		return null;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		FlowContext current = this;
		int parentsCount = 0;
		while ((current = current.parent) != null) {
			parentsCount++;
		}
		FlowContext[] parents = new FlowContext[parentsCount + 1];
		current = this;
		int index = parentsCount;
		while (index >= 0) {
			parents[index--] = current;
			current = current.parent;
		}
		for (int i = 0; i < parentsCount; i++) {
			for (int j = 0; j < i; j++)
				buffer.append('\t');
			buffer.append(parents[i].individualToString()).append('\n');
		}
		buffer.append('*');
		for (int j = 0; j < parentsCount + 1; j++)
			buffer.append('\t');
		buffer.append(individualToString()).append('\n');
		return buffer.toString();
	}
}