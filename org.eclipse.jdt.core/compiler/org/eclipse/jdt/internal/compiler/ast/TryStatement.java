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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class TryStatement extends Statement {
	
	public Block tryBlock;
	public Block[] catchBlocks;
	public Argument[] catchArguments;
	public Block finallyBlock;
	BlockScope scope;

	public boolean subRoutineCannotReturn = true;
	// should rename into subRoutineComplete to be set to false by default

	ReferenceBinding[] caughtExceptionTypes;
	boolean tryBlockExit;
	boolean[] catchExits;
	public int[] preserveExceptionHandler;

	Label subRoutineStartLabel;
	public LocalVariableBinding anyExceptionVariable,
		returnAddressVariable,
		secretReturnValue;

	public final static char[] SecretReturnName = " returnAddress".toCharArray(); //$NON-NLS-1$
	public final static char[] SecretAnyHandlerName = " anyExceptionHandler".toCharArray(); //$NON-NLS-1$
	public static final char[] SecretLocalDeclarationName = " returnValue".toCharArray(); //$NON-NLS-1$

	// for local variables table attributes
	int preTryInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// Consider the try block and catch block so as to compute the intersection of initializations and	
		// the minimum exit relative depth amongst all of them. Then consider the subroutine, and append its
		// initialization to the try/catch ones, if the subroutine completes normally. If the subroutine does not
		// complete, then only keep this result for the rest of the analysis

		// process the finally block (subroutine) - create a context for the subroutine

		preTryInitStateIndex =
			currentScope.methodScope().recordInitializationStates(flowInfo);

		if (anyExceptionVariable != null) {
			anyExceptionVariable.used = true;
		}
		if (returnAddressVariable != null) {
			returnAddressVariable.used = true;
		}
		InsideSubRoutineFlowContext insideSubContext;
		FinallyFlowContext finallyContext;
		UnconditionalFlowInfo subInfo;
		if (subRoutineStartLabel == null) {
			// no finally block
			insideSubContext = null;
			finallyContext = null;
			subInfo = null;
		} else {
			// analyse finally block first
			insideSubContext = new InsideSubRoutineFlowContext(flowContext, this);
			subInfo =
				finallyBlock
					.analyseCode(
						currentScope,
						finallyContext = new FinallyFlowContext(flowContext, finallyBlock),
						flowInfo.copy())
					.unconditionalInits();
			if (!((subInfo == FlowInfo.DeadEnd) || subInfo.isFakeReachable())) {
				subRoutineCannotReturn = false;
			}
		}
		// process the try block in a context handling the local exceptions.
		ExceptionHandlingFlowContext handlingContext =
			new ExceptionHandlingFlowContext(
				insideSubContext == null ? flowContext : insideSubContext,
				tryBlock,
				caughtExceptionTypes,
				scope,
				flowInfo.unconditionalInits());

		FlowInfo tryInfo;
		if (tryBlock.statements == null) {
			tryInfo = flowInfo;
			tryBlockExit = false;
		} else {
			tryInfo = tryBlock.analyseCode(currentScope, handlingContext, flowInfo.copy());
			tryBlockExit = (tryInfo == FlowInfo.DeadEnd) || tryInfo.isFakeReachable();
		}

		// check unreachable catch blocks
		handlingContext.complainIfUnusedExceptionHandlers(catchBlocks, scope, this);

		// process the catch blocks - computing the minimal exit depth amongst try/catch
		if (catchArguments != null) {
			int catchCount;
			catchExits = new boolean[catchCount = catchBlocks.length];
			for (int i = 0; i < catchCount; i++) {
				// keep track of the inits that could potentially have led to this exception handler (for final assignments diagnosis)
				///*
				FlowInfo catchInfo =
					flowInfo
						.copy()
						.unconditionalInits()
						.addPotentialInitializationsFrom(
							handlingContext.initsOnException(caughtExceptionTypes[i]).unconditionalInits())
						.addPotentialInitializationsFrom(tryInfo.unconditionalInits())
						.addPotentialInitializationsFrom(handlingContext.initsOnReturn);

				// catch var is always set
				catchInfo.markAsDefinitelyAssigned(catchArguments[i].binding);
				/*
				"If we are about to consider an unchecked exception handler, potential inits may have occured inside
				the try block that need to be detected , e.g. 
				try { x = 1; throwSomething();} catch(Exception e){ x = 2} "
				"(uncheckedExceptionTypes notNil and: [uncheckedExceptionTypes at: index])
				ifTrue: [catchInits addPotentialInitializationsFrom: tryInits]."
				*/
				if (tryBlock.statements == null) {
					catchInfo.markAsFakeReachable(true);
				}
				catchInfo =
					catchBlocks[i].analyseCode(
						currentScope,
						insideSubContext == null ? flowContext : insideSubContext,
						catchInfo);
				catchExits[i] =
					((catchInfo == FlowInfo.DeadEnd) || catchInfo.isFakeReachable());
				tryInfo = tryInfo.mergedWith(catchInfo.unconditionalInits());
			}
		}
		if (subRoutineStartLabel == null) {
			mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(tryInfo);
			return tryInfo;
		}

		// we also need to check potential multiple assignments of final variables inside the finally block
		// need to include potential inits from returns inside the try/catch parts - 1GK2AOF
		tryInfo.addPotentialInitializationsFrom(insideSubContext.initsOnReturn);
		finallyContext.complainOnRedundantFinalAssignments(tryInfo, currentScope);
		if (subInfo == FlowInfo.DeadEnd) {
			mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(subInfo);
			return subInfo;
		} else {
			FlowInfo mergedInfo = tryInfo.addInitializationsFrom(subInfo);
			mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(mergedInfo);
			return mergedInfo;
		}
	}

	public boolean cannotReturn() {

		return subRoutineCannotReturn;
	}

	/**
	 * Try statement code generation
	 *
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		if (tryBlock.isEmptyBlock()) {
			if (subRoutineStartLabel != null) {
				// since not passing the finallyScope, the block generation will exitUserScope(finallyScope)
				finallyBlock.generateCode(scope, codeStream);
			}
			// May loose some local variable initializations : affecting the local variable attributes
			if (mergedInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					mergedInitStateIndex);
			}
			// no local bytecode produced so no need for position remembering
			return;
		}
		int pc = codeStream.position;
		Label endLabel = new Label(codeStream);
		boolean requiresNaturalJsr = false;

		// preparing exception labels
		int maxCatches;
		ExceptionLabel[] exceptionLabels =
			new ExceptionLabel[maxCatches =
				catchArguments == null ? 0 : catchArguments.length];
		for (int i = 0; i < maxCatches; i++) {
			boolean preserveCurrentHandler =
				(preserveExceptionHandler[i
					/ ExceptionHandlingFlowContext.BitCacheSize]
						& (1 << (i % ExceptionHandlingFlowContext.BitCacheSize)))
					!= 0;
			if (preserveCurrentHandler) {
				exceptionLabels[i] =
					new ExceptionLabel(
						codeStream,
						(ReferenceBinding) catchArguments[i].binding.type);
			}
		}
		ExceptionLabel anyExceptionLabel = null;
		if (subRoutineStartLabel != null) {
			subRoutineStartLabel.codeStream = codeStream;
			anyExceptionLabel = new ExceptionLabel(codeStream, null);
		}
		// generate the try block
		tryBlock.generateCode(scope, codeStream);
		boolean tryBlockHasSomeCode = codeStream.position != pc;
		// flag telling if some bytecodes were issued inside the try block

		// natural exit: only if necessary
		boolean nonReturningSubRoutine =
			(subRoutineStartLabel != null) && subRoutineCannotReturn;
		if ((!tryBlockExit) && tryBlockHasSomeCode) {
			int position = codeStream.position;
			if (nonReturningSubRoutine) {
				codeStream.goto_(subRoutineStartLabel);
			} else {
				requiresNaturalJsr = true;
				codeStream.goto_(endLabel);
			}
			codeStream.updateLastRecordedEndPC(position);
			//goto is tagged as part of the try block
		}
		// place end positions of user-defined exception labels
		if (tryBlockHasSomeCode) {
			for (int i = 0; i < maxCatches; i++) {
				boolean preserveCurrentHandler =
					(preserveExceptionHandler[i
						/ ExceptionHandlingFlowContext.BitCacheSize]
							& (1 << (i % ExceptionHandlingFlowContext.BitCacheSize)))
						!= 0;
				if (preserveCurrentHandler) {
					exceptionLabels[i].placeEnd();
				}
			}
			/* generate sequence of handler, all starting by storing the TOS (exception
			thrown) into their own catch variables, the one specified in the source
			that must denote the handled exception.
			*/
			if (catchArguments == null) {
				if (anyExceptionLabel != null) {
					anyExceptionLabel.placeEnd();
				}
			} else {
				for (int i = 0; i < maxCatches; i++) {
					boolean preserveCurrentHandler =
						(preserveExceptionHandler[i
							/ ExceptionHandlingFlowContext.BitCacheSize]
								& (1 << (i % ExceptionHandlingFlowContext.BitCacheSize)))
							!= 0;
					if (preserveCurrentHandler) {
						// May loose some local variable initializations : affecting the local variable attributes
						if (preTryInitStateIndex != -1) {
							codeStream.removeNotDefinitelyAssignedVariables(
								currentScope,
								preTryInitStateIndex);
						}
						exceptionLabels[i].place();
						codeStream.incrStackSize(1);
						// optimizing the case where the exception variable is not actually used
						LocalVariableBinding catchVar;
						int varPC = codeStream.position;
						if ((catchVar = catchArguments[i].binding).resolvedPosition != -1) {
							codeStream.store(catchVar, false);
							catchVar.recordInitializationStartPC(codeStream.position);
							codeStream.addVisibleLocalVariable(catchVar);
						} else {
							codeStream.pop();
						}
						codeStream.recordPositionsFrom(varPC, catchArguments[i].sourceStart);
						// Keep track of the pcs at diverging point for computing the local attribute
						// since not passing the catchScope, the block generation will exitUserScope(catchScope)
						catchBlocks[i].generateCode(scope, codeStream);
					}
					if (i == maxCatches - 1) {
						if (anyExceptionLabel != null) {
							anyExceptionLabel.placeEnd();
						}
						if (subRoutineStartLabel != null) {
							if (!catchExits[i] && preserveCurrentHandler) {
								requiresNaturalJsr = true;
								codeStream.goto_(endLabel);
							}
						}
					} else {
						if (!catchExits[i] && preserveCurrentHandler) {
							if (nonReturningSubRoutine) {
								codeStream.goto_(subRoutineStartLabel);
							} else {
								requiresNaturalJsr = true;
								codeStream.goto_(endLabel);
							}
						}
					}
				}
			}
			// addition of a special handler so as to ensure that any uncaught exception (or exception thrown
			// inside catch blocks) will run the finally block
			int finallySequenceStartPC = codeStream.position;
			if (subRoutineStartLabel != null) {
				// the additional handler is doing: jsr finallyBlock and rethrow TOS-exception
				anyExceptionLabel.place();

				if (preTryInitStateIndex != -1) {
					// reset initialization state, as for a normal catch block
					codeStream.removeNotDefinitelyAssignedVariables(
						currentScope,
						preTryInitStateIndex);
				}

				codeStream.incrStackSize(1);
				if (nonReturningSubRoutine) {
					codeStream.pop();
					// "if subroutine cannot return, no need to jsr/jump to subroutine since it will be entered in sequence
				} else {
					codeStream.store(anyExceptionVariable, false);
					codeStream.jsr(subRoutineStartLabel);
					codeStream.load(anyExceptionVariable);
					codeStream.athrow();
				}
			}
			// end of catch sequence, place label that will correspond to the finally block beginning, or end of statement
			endLabel.place();
			if (subRoutineStartLabel != null) {
				if (nonReturningSubRoutine) {
					requiresNaturalJsr = false;
				}
				Label veryEndLabel = new Label(codeStream);
				if (requiresNaturalJsr) {
					codeStream.jsr(subRoutineStartLabel);
					codeStream.goto_(veryEndLabel);
				}
				subRoutineStartLabel.place();
				if (!nonReturningSubRoutine) {
					codeStream.incrStackSize(1);
					codeStream.store(returnAddressVariable, false);
				}
				codeStream.recordPositionsFrom(
					finallySequenceStartPC,
					finallyBlock.sourceStart);
				// entire sequence for finally is associated to finally block
				finallyBlock.generateCode(scope, codeStream);
				if (!nonReturningSubRoutine) {
					int position = codeStream.position;
					codeStream.ret(returnAddressVariable.resolvedPosition);
					codeStream.updateLastRecordedEndPC(position);
					// the ret bytecode is part of the subroutine
				}
				if (requiresNaturalJsr) {
					veryEndLabel.place();
				}
			}
		} else {
			// try block had no effect, only generate the body of the finally block if any
			if (subRoutineStartLabel != null) {
				finallyBlock.generateCode(scope, codeStream);
			}
		}
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				mergedInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public void resolve(BlockScope upperScope) {

		// special scope for secret locals optimization.	
		this.scope = new BlockScope(upperScope);

		BlockScope tryScope = new BlockScope(scope);
		BlockScope finallyScope = null;
		
		if (finallyBlock != null
			&& finallyBlock.statements != null) {

			finallyScope = new BlockScope(scope, false); // don't add it yet to parent scope

			// provision for returning and forcing the finally block to run
			MethodScope methodScope = scope.methodScope();

			// the type does not matter as long as its not a normal base type
			this.returnAddressVariable =
				new LocalVariableBinding(SecretReturnName, upperScope.getJavaLangObject(), AccDefault, false);
			finallyScope.addLocalVariable(returnAddressVariable);
			this.returnAddressVariable.constant = NotAConstant; // not inlinable
			this.subRoutineStartLabel = new Label();

			this.anyExceptionVariable =
				new LocalVariableBinding(SecretAnyHandlerName, scope.getJavaLangThrowable(), AccDefault, false);
			finallyScope.addLocalVariable(this.anyExceptionVariable);
			this.anyExceptionVariable.constant = NotAConstant; // not inlinable

			if (!methodScope.isInsideInitializer()) {
				MethodBinding methodBinding =
					((AbstractMethodDeclaration) methodScope.referenceContext).binding;
				if (methodBinding != null) {
					TypeBinding methodReturnType = methodBinding.returnType;
					if (methodReturnType.id != T_void) {
						this.secretReturnValue =
							new LocalVariableBinding(
								SecretLocalDeclarationName,
								methodReturnType,
								AccDefault,
								false);
						finallyScope.addLocalVariable(this.secretReturnValue);
						this.secretReturnValue.constant = NotAConstant; // not inlinable
					}
				}
			}
			finallyBlock.resolveUsing(finallyScope);
			// force the finally scope to have variable positions shifted after its try scope and catch ones
			finallyScope.shiftScopes = new BlockScope[catchArguments == null ? 1 : catchArguments.length+1];
			finallyScope.shiftScopes[0] = tryScope;
		}
		this.tryBlock.resolveUsing(tryScope);

		// arguments type are checked against JavaLangThrowable in resolveForCatch(..)
		if (this.catchBlocks != null) {
			int length = this.catchArguments.length;
			TypeBinding[] argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				BlockScope catchScope = new BlockScope(scope);
				if (finallyScope != null){
					finallyScope.shiftScopes[i+1] = catchScope;
				}
				// side effect on catchScope in resolveForCatch(..)
				if ((argumentTypes[i] = catchArguments[i].resolveForCatch(catchScope)) == null)
					return;
				catchBlocks[i].resolveUsing(catchScope);
			}

			// Verify that the catch clause are ordered in the right way:
			// more specialized first.
			this.caughtExceptionTypes = new ReferenceBinding[length];
			for (int i = 0; i < length; i++) {
				caughtExceptionTypes[i] = (ReferenceBinding) argumentTypes[i];
				for (int j = 0; j < i; j++) {
					if (scope.areTypesCompatible(caughtExceptionTypes[i], argumentTypes[j])) {
						scope.problemReporter().wrongSequenceOfExceptionTypesError(this, i, j);
						return;
					}
				}
			}
		} else {
			caughtExceptionTypes = new ReferenceBinding[0];
		}
		
		if (finallyScope != null){
			// add finallyScope as last subscope, so it can be shifted behind try/catch subscopes.
			// the shifting is necessary to achieve no overlay in between the finally scope and its
			// sibling in term of local variable positions.
			this.scope.addSubscope(finallyScope);
		}
	}

	public String toString(int tab) {
		String s = tabString(tab);
		//try
		s = s + "try "; //$NON-NLS-1$
		if (tryBlock == Block.None)
			s = s + "{}"; //$NON-NLS-1$
		else
			s = s + "\n" + tryBlock.toString(tab + 1); //$NON-NLS-1$

		//catches
		if (catchBlocks != null)
			for (int i = 0; i < catchBlocks.length; i++)
					s = s + "\n" + tabString(tab) + "catch (" //$NON-NLS-2$ //$NON-NLS-1$
						+catchArguments[i].toString(0) + ") " //$NON-NLS-1$
						+catchBlocks[i].toString(tab + 1);
		//finally
		if (finallyBlock != null) {
			if (finallyBlock == Block.None)
				s = s + "\n" + tabString(tab) + "finally {}"; //$NON-NLS-2$ //$NON-NLS-1$
			else
					s = s + "\n" + tabString(tab) + "finally\n" + //$NON-NLS-2$ //$NON-NLS-1$
			finallyBlock.toString(tab + 1);
		}

		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			tryBlock.traverse(visitor, scope);
			if (catchArguments != null) {
				for (int i = 0, max = catchBlocks.length; i < max; i++) {
					catchArguments[i].traverse(visitor, scope);
					catchBlocks[i].traverse(visitor, scope);
				}
			}
			if (finallyBlock != null)
				finallyBlock.traverse(visitor, scope);
		}
		visitor.endVisit(this, blockScope);
	}
}