/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     PARC     initial implementation
 *     Alexandre Vasseur    support for @AJ aspects
 * ******************************************************************/

package org.aspectj.weaver.bcel;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.generic.ArrayType;
import org.aspectj.apache.bcel.generic.BranchHandle;
import org.aspectj.apache.bcel.generic.FieldInstruction;
import org.aspectj.apache.bcel.generic.INVOKEINTERFACE;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionBranch;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionLV;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.InstructionTargeter;
import org.aspectj.apache.bcel.generic.InvokeInstruction;
import org.aspectj.apache.bcel.generic.LineNumberTag;
import org.aspectj.apache.bcel.generic.LocalVariableTag;
import org.aspectj.apache.bcel.generic.MULTIANEWARRAY;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.TargetLostException;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberImpl;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.NewConstructorTypeMunger;
import org.aspectj.weaver.NewFieldTypeMunger;
import org.aspectj.weaver.NewMethodTypeMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Var;
import org.aspectj.weaver.patterns.AbstractPatternNodeVisitor;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.ThisOrTargetPointcut;


/*
 * Some fun implementation stuff:
 *
 *   * expressionKind advice is non-execution advice
 *     * may have a target.
 *     * if the body is extracted, it will be extracted into
 *       a static method.  The first argument to the static
 *       method is the target
 *     * advice may expose a this object, but that's the advice's
 *       consideration, not ours.  This object will NOT be cached in another
 *       local, but will always come from frame zero.
 *
 *   * non-expressionKind advice is execution advice
 *     * may have a this.
 *     * target is same as this, and is exposed that way to advice
 *       (i.e., target will not be cached, will always come from frame zero)
 *     * if the body is extracted, it will be extracted into a method
 *       with same static/dynamic modifier as enclosing method.  If non-static,
 *       target of callback call will be this.
 *
 *   * because of these two facts, the setup of the actual arguments (including
 *     possible target) callback method is the same for both kinds of advice:
 *     push the targetVar, if it exists (it will not exist for advice on static
 *     things), then push all the argVars.
 *
 * Protected things:
 *
 *   * the above is sufficient for non-expressionKind advice for protected things,
 *     since the target will always be this.
 *
 *   * For expressionKind things, we have to modify the signature of the callback
 *     method slightly.  For non-static expressionKind things, we modify
 *     the first argument of the callback method NOT to be the type specified
 *     by the method/field signature (the owner), but rather we type it to
 *     the currentlyEnclosing type. We are guaranteed this will be fine,
 *     since the verifier verifies that the target is a subtype of the currently
 *     enclosingType.
 *
 * Worries:
 *
 *    * ConstructorCalls will be weirder than all of these, since they
 *      supposedly don't have a target (according to AspectJ), but they clearly
 *      do have a target of sorts, just one that needs to be pushed on the stack,
 *      dupped, and not touched otherwise until the constructor runs.
 *
 * @author Jim Hugunin
 * @author Erik Hilsdale
 *
 */

public class BcelShadow extends Shadow {

	private static final String[] NoDeclaredExceptions = new String[0];

	private ShadowRange range;
	private final BcelWorld world;
	private final LazyMethodGen enclosingMethod;

	// TESTING this will tell us if the optimisation succeeded *on the last shadow processed*
	public static boolean appliedLazyTjpOptimization;

	// Some instructions have a target type that will vary
	// from the signature (pr109728) (1.4 declaring type issue)
	private String actualInstructionTargetType;

	/**
	 * This generates an unassociated shadow, rooted in a particular method but not rooted to any particular point in the code. It
	 * should be given to a rooted ShadowRange in the {@link ShadowRange#associateWithShadow(BcelShadow)} method.
	 */
	public BcelShadow(BcelWorld world, Kind kind, Member signature, LazyMethodGen enclosingMethod, BcelShadow enclosingShadow) {
		super(kind, signature, enclosingShadow);
		this.world = world;
		this.enclosingMethod = enclosingMethod;
	}

	// ---- copies all state, including Shadow's mungers...

	public BcelShadow copyInto(LazyMethodGen recipient, BcelShadow enclosing) {
		BcelShadow s = new BcelShadow(world, getKind(), getSignature(), recipient, enclosing);
		if (mungers.size() > 0) {
			List<ShadowMunger> src = mungers;
			if (s.mungers == Collections.EMPTY_LIST) {
				s.mungers = new ArrayList<>();
			}
			List<ShadowMunger> dest = s.mungers;
			for (ShadowMunger shadowMunger : src) {
				dest.add(shadowMunger);
			}
		}
		return s;
	}

	// ---- overridden behaviour

	@Override
	public World getIWorld() {
		return world;
	}

	// see comment in deleteNewAndDup
	// } else if (inst.opcode == Constants.DUP_X2) {
	// // This code seen in the wild (by Brad):
	// // 40: new #12; //class java/lang/StringBuffer
	// // STACK: STRINGBUFFER
	// // 43: dup
	// // STACK: STRINGBUFFER/STRINGBUFFER
	// // 44: aload_0
	// // STACK: STRINGBUFFER/STRINGBUFFER/THIS
	// // 45: dup_x2
	// // STACK: THIS/STRINGBUFFER/STRINGBUFFER/THIS
	// // 46: getfield #36; //Field value:Ljava/lang/String;
	// // STACK: THIS/STRINGBUFFER/STRINGBUFFER/STRING<value>
	// // 49: invokestatic #37; //Method java/lang/String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
	// // STACK: THIS/STRINGBUFFER/STRINGBUFFER/STRING
	// // 52: invokespecial #19; //Method java/lang/StringBuffer."<init>":(Ljava/lang/String;)V
	// // STACK: THIS/STRINGBUFFER
	// // 55: aload_1
	// // STACK: THIS/STRINGBUFFER/LOCAL1
	// // 56: invokevirtual #22; //Method java/lang/StringBuffer.append:(Ljava/lang/String;)Ljava/lang/StringBuffer;
	// // STACK: THIS/STRINGBUFFER
	// // 59: invokevirtual #34; //Method java/lang/StringBuffer.toString:()Ljava/lang/String;
	// // STACK: THIS/STRING
	// // 62: putfield #36; //Field value:Ljava/lang/String;
	// // STACK: <empty>
	// // 65: return
	//
	// // if we attempt to match on the ctor call to StringBuffer.<init> then we get into trouble.
	// // if we simply delete the new/dup pair without fixing up the dup_x2 then the dup_x2 will fail due to there
	// // not being 3 elements on the stack for it to work with. The fix *in this situation* is to change it to
	// // a simple 'dup'
	//
	// // this fix is *not* very clean - but a general purpose decent solution will take much longer and this
	// // bytecode sequence has only been seen once in the wild.
	// ih.setInstruction(InstructionConstants.DUP);

	/**
	 * The new/dup (or new/dup_x1/swap) are removed and will be readded later (after the advice call) by the caller of this method.
	 * The groovy compiler produces unusual code where the new/dup isn't visible (when making a this() call from an existing ctor),
	 * an aload_0 is used to load the uninitialized object (as an example see the ctors in grails.util.BuildSettings).
	 *
	 * @return true if managed to remove them
	 */
	private boolean deleteNewAndDup() {
		final ConstantPool cpool = getEnclosingClass().getConstantPool();
		int depth = 1;
		InstructionHandle ih = range.getStart();

		// Go back from where we are looking for 'NEW' that takes us to a stack depth of 0. INVOKESPECIAL <init>
		while (ih != null) {
			Instruction inst = ih.getInstruction();
			if (inst.opcode == Constants.INVOKESPECIAL && ((InvokeInstruction) inst).getName(cpool).equals("<init>")) {
				depth++;
			} else if (inst.opcode == Constants.NEW) {
				depth--;
				if (depth == 0) {
					break;
				}
				// need a testcase to show this can really happen in a modern compiler - removed due to 315398 - moved this out to
				// comment proceeding this method:

			}
			ih = ih.getPrev();
		}
		if (ih == null) {
			return false;
		}
		// now IH points to the NEW. We're followed by the DUP, and that is followed
		// by the actual instruction we care about.
		InstructionHandle newHandle = ih;
		InstructionHandle endHandle = newHandle.getNext();
		InstructionHandle nextHandle;
		if (endHandle.getInstruction().opcode == Constants.DUP) {
			nextHandle = endHandle.getNext();
			retargetFrom(newHandle, nextHandle);
			retargetFrom(endHandle, nextHandle);
		} else if (endHandle.getInstruction().opcode == Constants.DUP_X1) {
			InstructionHandle dupHandle = endHandle;
			endHandle = endHandle.getNext();
			nextHandle = endHandle.getNext();
			boolean skipEndRepositioning = false;
			if (endHandle.getInstruction().opcode == Constants.SWAP) {
			} else if (endHandle.getInstruction().opcode == Constants.IMPDEP1) {
				skipEndRepositioning = true; // pr186884
			} else {
				// XXX see next XXX comment
				throw new RuntimeException("Unhandled kind of new " + endHandle);
			}
			// Now make any jumps to the 'new', the 'dup' or the 'end' now target the nextHandle
			retargetFrom(newHandle, nextHandle);
			retargetFrom(dupHandle, nextHandle);
			if (!skipEndRepositioning) {
				retargetFrom(endHandle, nextHandle);
			}
		} else {
			endHandle = newHandle;
			nextHandle = endHandle.getNext();
			retargetFrom(newHandle, nextHandle);
			// add a POP here... we found a NEW w/o a dup or anything else, so
			// we must be in statement context.
			getRange().insert(InstructionConstants.POP, Range.OutsideAfter);
		}
		// assert (dupHandle.getInstruction() instanceof DUP);

		try {
			range.getBody().delete(newHandle, endHandle);
		} catch (TargetLostException e) {
			throw new BCException("shouldn't happen");
		}
		return true;
	}

	private void retargetFrom(InstructionHandle old, InstructionHandle fresh) {
		for (InstructionTargeter targeter : old.getTargetersCopy()) {
			if (targeter instanceof ExceptionRange) {
				ExceptionRange it = (ExceptionRange) targeter;
				it.updateTarget(old, fresh, it.getBody());
			} else {
				targeter.updateTarget(old, fresh);
			}
		}
	}

	// records advice that is stopping us doing the lazyTjp optimization
	private List<BcelAdvice> badAdvice = null;

	public void addAdvicePreventingLazyTjp(BcelAdvice advice) {
		if (badAdvice == null) {
			badAdvice = new ArrayList<>();
		}
		badAdvice.add(advice);
	}

	@Override
	protected void prepareForMungers() {
		// if we're a constructor call, we need to remove the new:dup or the new:dup_x1:swap,
		// and store all our arguments on the frame.

		// ??? This is a bit of a hack (for the Java langauge). We do this because
		// we sometime add code "outsideBefore" when dealing with weaving join points. We only
		// do this for exposing state that is on the stack. It turns out to just work for
		// everything except for constructor calls and exception handlers. If we were to clean
		// this up, every ShadowRange would have three instructionHandle points, the start of
		// the arg-setup code, the start of the running code, and the end of the running code.
		boolean deletedNewAndDup = true;
		if (getKind() == ConstructorCall) {
			if (!world.isJoinpointArrayConstructionEnabled() || !this.getSignature().getDeclaringType().isArray()) {
				deletedNewAndDup = deleteNewAndDup(); // no new/dup for new array construction
			}
			initializeArgVars();
		} else if (getKind() == PreInitialization) { // pr74952
			ShadowRange range = getRange();
			range.insert(InstructionConstants.NOP, Range.InsideAfter);
		} else if (getKind() == ExceptionHandler) {

			ShadowRange range = getRange();
			InstructionList body = range.getBody();
			InstructionHandle start = range.getStart();

			// Create a store instruction to put the value from the top of the
			// stack into a local variable slot. This is a trimmed version of
			// what is in initializeArgVars() (since there is only one argument
			// at a handler jp and only before advice is supported) (pr46298)
			argVars = new BcelVar[1];
			// int positionOffset = (hasTarget() ? 1 : 0) + ((hasThis() && !getKind().isTargetSameAsThis()) ? 1 : 0);
			UnresolvedType tx = getArgType(0);
			argVars[0] = genTempVar(tx, "ajc$arg0");
			InstructionHandle insertedInstruction = range.insert(argVars[0].createStore(getFactory()), Range.OutsideBefore);

			// Now the exception range starts just after our new instruction.
			// The next bit of code changes the exception range to point at
			// the store instruction
			for (InstructionTargeter t : start.getTargetersCopy()) {
				if (t instanceof ExceptionRange) {
					ExceptionRange er = (ExceptionRange) t;
					er.updateTarget(start, insertedInstruction, body);
				}
			}
		}

		// now we ask each munger to request our state
		isThisJoinPointLazy = true;// world.isXlazyTjp(); // lazy is default now

		badAdvice = null;
		for (ShadowMunger munger : mungers) {
			munger.specializeOn(this);
		}

		initializeThisJoinPoint();

		if (thisJoinPointVar != null && !isThisJoinPointLazy && badAdvice != null && badAdvice.size() > 1) {
			// something stopped us making it a lazy tjp
			// can't build tjp lazily, no suitable test...
			int valid = 0;
			for (BcelAdvice element : badAdvice) {
				ISourceLocation sLoc = element.getSourceLocation();
				if (sLoc != null && sLoc.getLine() > 0) {
					valid++;
				}
			}
			if (valid != 0) {
				ISourceLocation[] badLocs = new ISourceLocation[valid];
				int i = 0;
				for (BcelAdvice element : badAdvice) {
					ISourceLocation sLoc = element.getSourceLocation();
					if (sLoc != null) {
						badLocs[i++] = sLoc;
					}
				}
				world.getLint().multipleAdviceStoppingLazyTjp
						.signal(new String[] { this.toString() }, getSourceLocation(), badLocs);
			}
		}
		badAdvice = null;

		// If we are an expression kind, we require our target/arguments on the stack
		// before we do our actual thing. However, they may have been removed
		// from the stack as the shadowMungers have requested state.
		// if any of our shadowMungers requested either the arguments or target,
		// the munger will have added code
		// to pop the target/arguments into temporary variables, represented by
		// targetVar and argVars. In such a case, we must make sure to re-push the
		// values.

		// If we are nonExpressionKind, we don't expect arguments on the stack
		// so this is moot. If our argVars happen to be null, then we know that
		// no ShadowMunger has squirrelled away our arguments, so they're still
		// on the stack.
		InstructionFactory fact = getFactory();
		if (getKind().argsOnStack() && argVars != null) {

			// Special case first (pr46298). If we are an exception handler and the instruction
			// just after the shadow is a POP then we should remove the pop. The code
			// above which generated the store instruction has already cleared the stack.
			// We also don't generate any code for the arguments in this case as it would be
			// an incorrect aload.
			if (getKind() == ExceptionHandler && range.getEnd().getNext().getInstruction().equals(InstructionConstants.POP)) {
				// easier than deleting it ...
				range.getEnd().getNext().setInstruction(InstructionConstants.NOP);
			} else {
				range.insert(BcelRenderer.renderExprs(fact, world, argVars), Range.InsideBefore);
				if (targetVar != null) {
					range.insert(BcelRenderer.renderExpr(fact, world, targetVar), Range.InsideBefore);
				}
				if (getKind() == ConstructorCall) {
					if (!world.isJoinpointArrayConstructionEnabled() || !this.getSignature().getDeclaringType().isArray()) {
						if (deletedNewAndDup) { // if didnt delete them, dont insert any!
							range.insert(InstructionFactory.createDup(1), Range.InsideBefore);
							range.insert(fact.createNew((ObjectType) BcelWorld.makeBcelType(getSignature().getDeclaringType())),
									Range.InsideBefore);
						}
					}
				}
			}
		}
	}

	// ---- getters

	public ShadowRange getRange() {
		return range;
	}

	public void setRange(ShadowRange range) {
		this.range = range;
	}

	private int sourceline = -1;

	public int getSourceLine() {
		// if the kind of join point for which we are a shadow represents
		// a method or constructor execution, then the best source line is
		// the one from the enclosingMethod declarationLineNumber if available.
		if (sourceline != -1) {
			return sourceline;
		}
		Kind kind = getKind();
		if ((kind == MethodExecution) || (kind == ConstructorExecution) || (kind == AdviceExecution)
				|| (kind == StaticInitialization) || (kind == PreInitialization) || (kind == Initialization)) {
			if (getEnclosingMethod().hasDeclaredLineNumberInfo()) {
				sourceline = getEnclosingMethod().getDeclarationLineNumber();
				return sourceline;
			}
		}

		if (range == null) {
			if (getEnclosingMethod().hasBody()) {
				sourceline = Utility.getSourceLine(getEnclosingMethod().getBody().getStart());
				return sourceline;
			} else {
				sourceline = 0;
				return sourceline;
			}
		}
		sourceline = Utility.getSourceLine(range.getStart());
		if (sourceline < 0) {
			sourceline = 0;
		}
		return sourceline;
	}

	@Override
	public ResolvedType getEnclosingType() {
		return getEnclosingClass().getType();
	}

	public LazyClassGen getEnclosingClass() {
		return enclosingMethod.getEnclosingClass();
	}

	public BcelWorld getWorld() {
		return world;
	}

	// ---- factory methods

	public static BcelShadow makeConstructorExecution(BcelWorld world, LazyMethodGen enclosingMethod,
			InstructionHandle justBeforeStart) {
		final InstructionList body = enclosingMethod.getBody();
		BcelShadow s = new BcelShadow(world, ConstructorExecution, world.makeJoinPointSignatureFromMethod(enclosingMethod,
				Member.CONSTRUCTOR), enclosingMethod, null);
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(s);
		r.associateWithTargets(Range.genStart(body, justBeforeStart.getNext()), Range.genEnd(body));
		return s;
	}

	public static BcelShadow makeStaticInitialization(BcelWorld world, LazyMethodGen enclosingMethod) {
		InstructionList body = enclosingMethod.getBody();
		// move the start past ajc$preClinit
		InstructionHandle clinitStart = body.getStart();
		if (clinitStart.getInstruction() instanceof InvokeInstruction) {
			InvokeInstruction ii = (InvokeInstruction) clinitStart.getInstruction();
			if (ii.getName(enclosingMethod.getEnclosingClass().getConstantPool()).equals(NameMangler.AJC_PRE_CLINIT_NAME)) {
				clinitStart = clinitStart.getNext();
			}
		}

		InstructionHandle clinitEnd = body.getEnd();

		// XXX should move the end before the postClinit, but the return is then tricky...
		// if (clinitEnd.getInstruction() instanceof InvokeInstruction) {
		// InvokeInstruction ii = (InvokeInstruction)clinitEnd.getInstruction();
		// if (ii.getName(enclosingMethod.getEnclosingClass().getConstantPool()).equals(NameMangler.AJC_POST_CLINIT_NAME)) {
		// clinitEnd = clinitEnd.getPrev();
		// }
		// }

		BcelShadow s = new BcelShadow(world, StaticInitialization, world.makeJoinPointSignatureFromMethod(enclosingMethod,
				Member.STATIC_INITIALIZATION), enclosingMethod, null);
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(s);
		r.associateWithTargets(Range.genStart(body, clinitStart), Range.genEnd(body, clinitEnd));
		return s;
	}

	/**
	 * Make the shadow for an exception handler. Currently makes an empty shadow that only allows before advice to be woven into it.
	 */

	public static BcelShadow makeExceptionHandler(BcelWorld world, ExceptionRange exceptionRange, LazyMethodGen enclosingMethod,
			InstructionHandle startOfHandler, BcelShadow enclosingShadow) {
		InstructionList body = enclosingMethod.getBody();
		UnresolvedType catchType = exceptionRange.getCatchType();
		UnresolvedType inType = enclosingMethod.getEnclosingClass().getType();

		ResolvedMemberImpl sig = MemberImpl.makeExceptionHandlerSignature(inType, catchType);
		sig.setParameterNames(new String[] { findHandlerParamName(startOfHandler) });

		BcelShadow s = new BcelShadow(world, ExceptionHandler, sig, enclosingMethod, enclosingShadow);
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(s);
		InstructionHandle start = Range.genStart(body, startOfHandler);
		InstructionHandle end = Range.genEnd(body, start);

		r.associateWithTargets(start, end);
		exceptionRange.updateTarget(startOfHandler, start, body);
		return s;
	}

	private static String findHandlerParamName(InstructionHandle startOfHandler) {
		if (startOfHandler.getInstruction().isStoreInstruction() && startOfHandler.getNext() != null) {
			int slot = startOfHandler.getInstruction().getIndex();
			// System.out.println("got store: " + startOfHandler.getInstruction() + ", " + index);
			for (InstructionTargeter targeter : startOfHandler.getNext().getTargeters()) {
				if (targeter instanceof LocalVariableTag) {
					LocalVariableTag t = (LocalVariableTag) targeter;
					if (t.getSlot() == slot) {
						return t.getName();
					}
				}
			}
		}

		return "<missing>";
	}

	/** create an init join point associated w/ an interface in the body of a constructor */

	public static BcelShadow makeIfaceInitialization(BcelWorld world, LazyMethodGen constructor,
			Member interfaceConstructorSignature) {
		// this call marks the instruction list as changed
		constructor.getBody();
		// UnresolvedType inType = constructor.getEnclosingClass().getType();
		BcelShadow s = new BcelShadow(world, Initialization, interfaceConstructorSignature, constructor, null);
		// s.fallsThrough = true;
		// ShadowRange r = new ShadowRange(body);
		// r.associateWithShadow(s);
		// InstructionHandle start = Range.genStart(body, handle);
		// InstructionHandle end = Range.genEnd(body, handle);
		//
		// r.associateWithTargets(start, end);
		return s;
	}

	public void initIfaceInitializer(InstructionHandle end) {
		final InstructionList body = enclosingMethod.getBody();
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(this);
		InstructionHandle nop = body.insert(end, InstructionConstants.NOP);

		r.associateWithTargets(Range.genStart(body, nop), Range.genEnd(body, nop));
	}

	// public static BcelShadow makeIfaceConstructorExecution(
	// BcelWorld world,
	// LazyMethodGen constructor,
	// InstructionHandle next,
	// Member interfaceConstructorSignature)
	// {
	// // final InstructionFactory fact = constructor.getEnclosingClass().getFactory();
	// InstructionList body = constructor.getBody();
	// // UnresolvedType inType = constructor.getEnclosingClass().getType();
	// BcelShadow s =
	// new BcelShadow(
	// world,
	// ConstructorExecution,
	// interfaceConstructorSignature,
	// constructor,
	// null);
	// s.fallsThrough = true;
	// ShadowRange r = new ShadowRange(body);
	// r.associateWithShadow(s);
	// // ??? this may or may not work
	// InstructionHandle start = Range.genStart(body, next);
	// //InstructionHandle end = Range.genEnd(body, body.append(start, fact.NOP));
	// InstructionHandle end = Range.genStart(body, next);
	// //body.append(start, fact.NOP);
	//
	// r.associateWithTargets(start, end);
	// return s;
	// }

	/**
	 * Create an initialization join point associated with a constructor, but not with any body of code yet. If this is actually
	 * matched, its range will be set when we inline self constructors.
	 *
	 * @param constructor The constructor starting this initialization.
	 */
	public static BcelShadow makeUnfinishedInitialization(BcelWorld world, LazyMethodGen constructor) {
		BcelShadow ret = new BcelShadow(world, Initialization, world.makeJoinPointSignatureFromMethod(constructor,
				Member.CONSTRUCTOR), constructor, null);
		if (constructor.getEffectiveSignature() != null) {
			ret.setMatchingSignature(constructor.getEffectiveSignature().getEffectiveSignature());
		}
		return ret;
	}

	public static BcelShadow makeUnfinishedPreinitialization(BcelWorld world, LazyMethodGen constructor) {
		BcelShadow ret = new BcelShadow(world, PreInitialization, world.makeJoinPointSignatureFromMethod(constructor,
				Member.CONSTRUCTOR), constructor, null);
		if (constructor.getEffectiveSignature() != null) {
			ret.setMatchingSignature(constructor.getEffectiveSignature().getEffectiveSignature());
		}
		return ret;
	}

	public static BcelShadow makeMethodExecution(BcelWorld world, LazyMethodGen enclosingMethod, boolean lazyInit) {
		if (!lazyInit) {
			return makeMethodExecution(world, enclosingMethod);
		}

		BcelShadow s = new BcelShadow(world, MethodExecution, enclosingMethod.getMemberView(), enclosingMethod, null);

		return s;
	}

	public void init() {
		if (range != null) {
			return;
		}

		final InstructionList body = enclosingMethod.getBody();
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(this);
		r.associateWithTargets(Range.genStart(body), Range.genEnd(body));
	}

	public static BcelShadow makeMethodExecution(BcelWorld world, LazyMethodGen enclosingMethod) {
		return makeShadowForMethod(world, enclosingMethod, MethodExecution, enclosingMethod.getMemberView());
	}

	public static BcelShadow makeShadowForMethod(BcelWorld world, LazyMethodGen enclosingMethod, Shadow.Kind kind, Member sig) {
		final InstructionList body = enclosingMethod.getBody();
		BcelShadow s = new BcelShadow(world, kind, sig, enclosingMethod, null);
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(s);
		r.associateWithTargets(// OPTIMIZE this occurs lots of times for all jp kinds...
				Range.genStart(body), Range.genEnd(body));
		return s;
	}

	public static BcelShadow makeAdviceExecution(BcelWorld world, LazyMethodGen enclosingMethod) {
		final InstructionList body = enclosingMethod.getBody();
		BcelShadow s = new BcelShadow(world, AdviceExecution,
				world.makeJoinPointSignatureFromMethod(enclosingMethod, Member.ADVICE), enclosingMethod, null);
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(s);
		r.associateWithTargets(Range.genStart(body), Range.genEnd(body));
		return s;
	}

	// constructor call shadows are <em>initially</em> just around the
	// call to the constructor. If ANY advice gets put on it, we move
	// the NEW instruction inside the join point, which involves putting
	// all the arguments in temps.
	public static BcelShadow makeConstructorCall(BcelWorld world, LazyMethodGen enclosingMethod, InstructionHandle callHandle,
			BcelShadow enclosingShadow) {
		final InstructionList body = enclosingMethod.getBody();

		Member sig = world.makeJoinPointSignatureForMethodInvocation(enclosingMethod.getEnclosingClass(),
				(InvokeInstruction) callHandle.getInstruction());

		BcelShadow s = new BcelShadow(world, ConstructorCall, sig, enclosingMethod, enclosingShadow);
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(s);
		r.associateWithTargets(Range.genStart(body, callHandle), Range.genEnd(body, callHandle));
		retargetAllBranches(callHandle, r.getStart());
		return s;
	}

	public static BcelShadow makeArrayConstructorCall(BcelWorld world, LazyMethodGen enclosingMethod,
			InstructionHandle arrayInstruction, BcelShadow enclosingShadow) {
		final InstructionList body = enclosingMethod.getBody();
		Member sig = world.makeJoinPointSignatureForArrayConstruction(enclosingMethod.getEnclosingClass(), arrayInstruction);
		BcelShadow s = new BcelShadow(world, ConstructorCall, sig, enclosingMethod, enclosingShadow);
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(s);
		r.associateWithTargets(Range.genStart(body, arrayInstruction), Range.genEnd(body, arrayInstruction));
		retargetAllBranches(arrayInstruction, r.getStart());
		return s;
	}

	public static BcelShadow makeMonitorEnter(BcelWorld world, LazyMethodGen enclosingMethod, InstructionHandle monitorInstruction,
			BcelShadow enclosingShadow) {
		final InstructionList body = enclosingMethod.getBody();
		Member sig = world.makeJoinPointSignatureForMonitorEnter(enclosingMethod.getEnclosingClass(), monitorInstruction);
		BcelShadow s = new BcelShadow(world, SynchronizationLock, sig, enclosingMethod, enclosingShadow);
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(s);
		r.associateWithTargets(Range.genStart(body, monitorInstruction), Range.genEnd(body, monitorInstruction));
		retargetAllBranches(monitorInstruction, r.getStart());
		return s;
	}

	public static BcelShadow makeMonitorExit(BcelWorld world, LazyMethodGen enclosingMethod, InstructionHandle monitorInstruction,
			BcelShadow enclosingShadow) {
		final InstructionList body = enclosingMethod.getBody();
		Member sig = world.makeJoinPointSignatureForMonitorExit(enclosingMethod.getEnclosingClass(), monitorInstruction);
		BcelShadow s = new BcelShadow(world, SynchronizationUnlock, sig, enclosingMethod, enclosingShadow);
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(s);
		r.associateWithTargets(Range.genStart(body, monitorInstruction), Range.genEnd(body, monitorInstruction));
		retargetAllBranches(monitorInstruction, r.getStart());
		return s;
	}

	// see pr77166
	// public static BcelShadow makeArrayLoadCall(
	// BcelWorld world,
	// LazyMethodGen enclosingMethod,
	// InstructionHandle arrayInstruction,
	// BcelShadow enclosingShadow)
	// {
	// final InstructionList body = enclosingMethod.getBody();
	// Member sig = world.makeJoinPointSignatureForArrayLoad(enclosingMethod.getEnclosingClass(),arrayInstruction);
	// BcelShadow s =
	// new BcelShadow(
	// world,
	// MethodCall,
	// sig,
	// enclosingMethod,
	// enclosingShadow);
	// ShadowRange r = new ShadowRange(body);
	// r.associateWithShadow(s);
	// r.associateWithTargets(
	// Range.genStart(body, arrayInstruction),
	// Range.genEnd(body, arrayInstruction));
	// retargetAllBranches(arrayInstruction, r.getStart());
	// return s;
	// }

	public static BcelShadow makeMethodCall(BcelWorld world, LazyMethodGen enclosingMethod, InstructionHandle callHandle,
			BcelShadow enclosingShadow) {
		final InstructionList body = enclosingMethod.getBody();
		BcelShadow s = new BcelShadow(world, MethodCall, world.makeJoinPointSignatureForMethodInvocation(
				enclosingMethod.getEnclosingClass(), (InvokeInstruction) callHandle.getInstruction()), enclosingMethod,
				enclosingShadow);
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(s);
		r.associateWithTargets(Range.genStart(body, callHandle), Range.genEnd(body, callHandle));
		retargetAllBranches(callHandle, r.getStart());
		return s;
	}

	public static BcelShadow makeShadowForMethodCall(BcelWorld world, LazyMethodGen enclosingMethod, InstructionHandle callHandle,
			BcelShadow enclosingShadow, Kind kind, ResolvedMember sig) {
		final InstructionList body = enclosingMethod.getBody();
		BcelShadow s = new BcelShadow(world, kind, sig, enclosingMethod, enclosingShadow);
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(s);
		r.associateWithTargets(Range.genStart(body, callHandle), Range.genEnd(body, callHandle));
		retargetAllBranches(callHandle, r.getStart());
		return s;
	}

	public static BcelShadow makeFieldGet(BcelWorld world, ResolvedMember field, LazyMethodGen enclosingMethod,
			InstructionHandle getHandle, BcelShadow enclosingShadow) {
		final InstructionList body = enclosingMethod.getBody();
		BcelShadow s = new BcelShadow(world, FieldGet, field,
		// BcelWorld.makeFieldSignature(
		// enclosingMethod.getEnclosingClass(),
		// (FieldInstruction) getHandle.getInstruction()),
				enclosingMethod, enclosingShadow);
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(s);
		r.associateWithTargets(Range.genStart(body, getHandle), Range.genEnd(body, getHandle));
		retargetAllBranches(getHandle, r.getStart());
		return s;
	}

	public static BcelShadow makeFieldSet(BcelWorld world, ResolvedMember field, LazyMethodGen enclosingMethod,
			InstructionHandle setHandle, BcelShadow enclosingShadow) {
		final InstructionList body = enclosingMethod.getBody();
		BcelShadow s = new BcelShadow(world, FieldSet, field,
		// BcelWorld.makeFieldJoinPointSignature(
		// enclosingMethod.getEnclosingClass(),
		// (FieldInstruction) setHandle.getInstruction()),
				enclosingMethod, enclosingShadow);
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(s);
		r.associateWithTargets(Range.genStart(body, setHandle), Range.genEnd(body, setHandle));
		retargetAllBranches(setHandle, r.getStart());
		return s;
	}

	public static void retargetAllBranches(InstructionHandle from, InstructionHandle to) {
		for (InstructionTargeter source : from.getTargetersCopy()) {
			if (source instanceof InstructionBranch) {
				source.updateTarget(from, to);
			}
		}
	}

	// // ---- type access methods
	// private ObjectType getTargetBcelType() {
	// return (ObjectType) BcelWorld.makeBcelType(getTargetType());
	// }
	// private Type getArgBcelType(int arg) {
	// return BcelWorld.makeBcelType(getArgType(arg));
	// }

	// ---- kinding

	/**
	 * If the end of my range has no real instructions following then my context needs a return at the end.
	 */
	public boolean terminatesWithReturn() {
		return getRange().getRealNext() == null;
	}

	/**
	 * Is arg0 occupied with the value of this
	 */
	public boolean arg0HoldsThis() {
		if (getKind().isEnclosingKind()) {
			return !Modifier.isStatic(getSignature().getModifiers());
		} else if (enclosingShadow == null) {
			// XXX this is mostly right
			// this doesn't do the right thing for calls in the pre part of introduced constructors.
			return !enclosingMethod.isStatic();
		} else {
			return ((BcelShadow) enclosingShadow).arg0HoldsThis();
		}
	}

	// ---- argument getting methods

	private BcelVar thisVar = null;
	private BcelVar targetVar = null;
	private BcelVar[] argVars = null;
	private Map<ResolvedType, AnnotationAccessVar> kindedAnnotationVars = null;
	private Map<ResolvedType, TypeAnnotationAccessVar> thisAnnotationVars = null;
	private Map<ResolvedType, TypeAnnotationAccessVar> targetAnnotationVars = null;
	// private Map/* <UnresolvedType,BcelVar> */[] argAnnotationVars = null;
	private Map<ResolvedType, AnnotationAccessVar> withinAnnotationVars = null;
	private Map<ResolvedType, AnnotationAccessVar> withincodeAnnotationVars = null;
	private boolean allArgVarsInitialized = false;

	// If in annotation style and the relevant advice is using PJP then this will
	// be set to true when the closure variable is initialized - if it gets set
	// (which means link() has been called) then we will need to call unlink()
	// after the code has been run.
	boolean closureVarInitialized = false;

	@Override
	public Var getThisVar() {
		if (!hasThis()) {
			throw new IllegalStateException("no this");
		}
		initializeThisVar();
		return thisVar;
	}

	@Override
	public Var getThisAnnotationVar(UnresolvedType forAnnotationType) {
		if (!hasThis()) {
			throw new IllegalStateException("no this");
		}
		initializeThisAnnotationVars(); // FIXME asc Why bother with this if we always return one?
		// Even if we can't find one, we have to return one as we might have this annotation at runtime
		Var v = thisAnnotationVars.get(forAnnotationType);
		if (v == null) {
			v = new TypeAnnotationAccessVar(forAnnotationType.resolve(world), (BcelVar) getThisVar());
		}
		return v;
	}

	@Override
	public Var getTargetVar() {
		if (!hasTarget()) {
			throw new IllegalStateException("no target");
		}
		initializeTargetVar();
		return targetVar;
	}

	@Override
	public Var getTargetAnnotationVar(UnresolvedType forAnnotationType) {
		if (!hasTarget()) {
			throw new IllegalStateException("no target");
		}
		initializeTargetAnnotationVars(); // FIXME asc why bother with this if we always return one?
		Var v = targetAnnotationVars.get(forAnnotationType);
		// Even if we can't find one, we have to return one as we might have this annotation at runtime
		if (v == null) {
			v = new TypeAnnotationAccessVar(forAnnotationType.resolve(world), (BcelVar) getTargetVar());
		}
		return v;
	}

	@Override
	public Var getArgVar(int i) {
		ensureInitializedArgVar(i);
		return argVars[i];
	}

	@Override
	public Var getArgAnnotationVar(int i, UnresolvedType forAnnotationType) {
		return new TypeAnnotationAccessVar(forAnnotationType.resolve(world), (BcelVar) getArgVar(i));
		// initializeArgAnnotationVars();
		//
		// Var v = (Var) argAnnotationVars[i].get(forAnnotationType);
		// if (v == null) {
		// v = new TypeAnnotationAccessVar(forAnnotationType.resolve(world), (BcelVar) getArgVar(i));
		// }
		// return v;
	}

	@Override
	public Var getKindedAnnotationVar(UnresolvedType forAnnotationType) {
		initializeKindedAnnotationVars();
		return kindedAnnotationVars.get(forAnnotationType);
	}

	@Override
	public Var getWithinAnnotationVar(UnresolvedType forAnnotationType) {
		initializeWithinAnnotationVars();
		return withinAnnotationVars.get(forAnnotationType);
	}

	@Override
	public Var getWithinCodeAnnotationVar(UnresolvedType forAnnotationType) {
		initializeWithinCodeAnnotationVars();
		return withincodeAnnotationVars.get(forAnnotationType);
	}

	// reflective thisJoinPoint support
	private BcelVar thisJoinPointVar = null;
	private boolean isThisJoinPointLazy;
	private int lazyTjpConsumers = 0;
	private BcelVar thisJoinPointStaticPartVar = null;

	// private BcelVar thisEnclosingJoinPointStaticPartVar = null;

	@Override
	public final Var getThisJoinPointStaticPartVar() {
		return getThisJoinPointStaticPartBcelVar();
	}

	@Override
	public final Var getThisEnclosingJoinPointStaticPartVar() {
		return getThisEnclosingJoinPointStaticPartBcelVar();
	}

	public void requireThisJoinPoint(boolean hasGuardTest, boolean isAround) {
		if (!isAround) {
			if (!hasGuardTest) {
				isThisJoinPointLazy = false;
			} else {
				lazyTjpConsumers++;
			}
		}
		// if (!hasGuardTest) {
		// isThisJoinPointLazy = false;
		// } else {
		// lazyTjpConsumers++;
		// }
		if (thisJoinPointVar == null) {
			thisJoinPointVar = genTempVar(UnresolvedType.forName("org.aspectj.lang.JoinPoint"));
		}
	}

	@Override
	public Var getThisJoinPointVar() {
		requireThisJoinPoint(false, false);
		return thisJoinPointVar;
	}

	void initializeThisJoinPoint() {
		if (thisJoinPointVar == null) {
			return;
		}

		if (isThisJoinPointLazy) {
			isThisJoinPointLazy = checkLazyTjp();
		}

		if (isThisJoinPointLazy) {
			appliedLazyTjpOptimization = true;
			createThisJoinPoint(); // make sure any state needed is initialized, but throw the instructions out

			if (lazyTjpConsumers == 1) {
				return; // special case only one lazyTjpUser
			}

			InstructionFactory fact = getFactory();
			InstructionList il = new InstructionList();
			il.append(InstructionConstants.ACONST_NULL);
			il.append(thisJoinPointVar.createStore(fact));
			range.insert(il, Range.OutsideBefore);
		} else {
			appliedLazyTjpOptimization = false;
			InstructionFactory fact = getFactory();
			InstructionList il = createThisJoinPoint();
			il.append(thisJoinPointVar.createStore(fact));
			range.insert(il, Range.OutsideBefore);
		}
	}

	private boolean checkLazyTjp() {
		// check for around advice
		for (ShadowMunger munger : mungers) {
			if (munger instanceof Advice) {
				if (((Advice) munger).getKind() == AdviceKind.Around) {
					if (munger.getSourceLocation() != null) { // do we know enough to bother reporting?
						if (world.getLint().canNotImplementLazyTjp.isEnabled()) {
							world.getLint().canNotImplementLazyTjp.signal(new String[]{toString()}, getSourceLocation(),
									new ISourceLocation[]{munger.getSourceLocation()});
						}
					}
					return false;
				}
			}
		}

		return true;
	}

	InstructionList loadThisJoinPoint() {
		InstructionFactory fact = getFactory();
		InstructionList il = new InstructionList();

		if (isThisJoinPointLazy) {
			// If we're lazy, build the join point right here.
			il.append(createThisJoinPoint());

			// Does someone else need it? If so, store it for later retrieval
			if (lazyTjpConsumers > 1) {
				il.append(thisJoinPointVar.createStore(fact));

				InstructionHandle end = il.append(thisJoinPointVar.createLoad(fact));

				il.insert(InstructionFactory.createBranchInstruction(Constants.IFNONNULL, end));
				il.insert(thisJoinPointVar.createLoad(fact));
			}
		} else {
			// If not lazy, its already been built and stored, just retrieve it
			thisJoinPointVar.appendLoad(il, fact);
		}

		return il;
	}

	InstructionList createThisJoinPoint() {
		InstructionFactory fact = getFactory();
		InstructionList il = new InstructionList();

		BcelVar staticPart = getThisJoinPointStaticPartBcelVar();
		staticPart.appendLoad(il, fact);
		if (hasThis()) {
			((BcelVar) getThisVar()).appendLoad(il, fact);
		} else {
			il.append(InstructionConstants.ACONST_NULL);
		}
		if (hasTarget()) {
			((BcelVar) getTargetVar()).appendLoad(il, fact);
		} else {
			il.append(InstructionConstants.ACONST_NULL);
		}

		switch (getArgCount()) {
		case 0:
			il.append(fact.createInvoke("org.aspectj.runtime.reflect.Factory", "makeJP", LazyClassGen.tjpType, new Type[] {
					LazyClassGen.staticTjpType, Type.OBJECT, Type.OBJECT }, Constants.INVOKESTATIC));
			break;
		case 1:
			((BcelVar) getArgVar(0)).appendLoadAndConvert(il, fact, world.getCoreType(ResolvedType.OBJECT));
			il.append(fact.createInvoke("org.aspectj.runtime.reflect.Factory", "makeJP", LazyClassGen.tjpType, new Type[] {
					LazyClassGen.staticTjpType, Type.OBJECT, Type.OBJECT, Type.OBJECT }, Constants.INVOKESTATIC));
			break;
		case 2:
			((BcelVar) getArgVar(0)).appendLoadAndConvert(il, fact, world.getCoreType(ResolvedType.OBJECT));
			((BcelVar) getArgVar(1)).appendLoadAndConvert(il, fact, world.getCoreType(ResolvedType.OBJECT));
			il.append(fact.createInvoke("org.aspectj.runtime.reflect.Factory", "makeJP", LazyClassGen.tjpType, new Type[] {
					LazyClassGen.staticTjpType, Type.OBJECT, Type.OBJECT, Type.OBJECT, Type.OBJECT }, Constants.INVOKESTATIC));
			break;
		default:
			il.append(makeArgsObjectArray());
			il.append(fact.createInvoke("org.aspectj.runtime.reflect.Factory", "makeJP", LazyClassGen.tjpType, new Type[] {
					LazyClassGen.staticTjpType, Type.OBJECT, Type.OBJECT, new ArrayType(Type.OBJECT, 1) }, Constants.INVOKESTATIC));
			break;
		}

		return il;
	}

	public BcelVar getThisJoinPointStaticPartBcelVar() {
		return getThisJoinPointStaticPartBcelVar(false);
	}

	@Override
	public BcelVar getThisAspectInstanceVar(ResolvedType aspectType) {
		return new AspectInstanceVar(aspectType);
	}

	/**
	 * Get the Var for the xxxxJpStaticPart, xxx = this or enclosing
	 *
	 * @param isEnclosingJp true to have the enclosingJpStaticPart
	 * @return
	 */
	public BcelVar getThisJoinPointStaticPartBcelVar(final boolean isEnclosingJp) {
		if (thisJoinPointStaticPartVar == null) {
			Field field = getEnclosingClass().getTjpField(this, isEnclosingJp);
			ResolvedType sjpType = null;
			if (world.isTargettingAspectJRuntime12()) { // TAG:SUPPORTING12: We didn't have different jpsp types in 1.2
				sjpType = world.getCoreType(UnresolvedType.JOINPOINT_STATICPART);
			} else {
				sjpType = isEnclosingJp ? world.getCoreType(UnresolvedType.JOINPOINT_ENCLOSINGSTATICPART) : world
						.getCoreType(UnresolvedType.JOINPOINT_STATICPART);
			}
			thisJoinPointStaticPartVar = new BcelFieldRef(sjpType, getEnclosingClass().getClassName(), field.getName());
			// getEnclosingClass().warnOnAddedStaticInitializer(this,munger.getSourceLocation());
		}
		return thisJoinPointStaticPartVar;
	}

	/**
	 * Get the Var for the enclosingJpStaticPart
	 *
	 * @return
	 */
	public BcelVar getThisEnclosingJoinPointStaticPartBcelVar() {
		if (enclosingShadow == null) {
			// the enclosing of an execution is itself
			return getThisJoinPointStaticPartBcelVar(true);
		} else {
			return ((BcelShadow) enclosingShadow).getThisJoinPointStaticPartBcelVar(true);
		}
	}

	// ??? need to better understand all the enclosing variants
	@Override
	public Member getEnclosingCodeSignature() {
		if (getKind().isEnclosingKind()) {
			return getSignature();
		} else if (getKind() == Shadow.PreInitialization) {
			// PreInit doesn't enclose code but its signature
			// is correctly the signature of the ctor.
			return getSignature();
		} else if (enclosingShadow == null) {
			return getEnclosingMethod().getMemberView();
		} else {
			return enclosingShadow.getSignature();
		}
	}

	public Member getRealEnclosingCodeSignature() {
		return enclosingMethod.getMemberView();
	}

	// public Member getEnclosingCodeSignatureForModel() {
	// if (getKind().isEnclosingKind()) {
	// return getSignature();
	// } else if (getKind() == Shadow.PreInitialization) {
	// // PreInit doesn't enclose code but its signature
	// // is correctly the signature of the ctor.
	// return getSignature();
	// } else if (enclosingShadow == null) {
	// return getEnclosingMethod().getMemberView();
	// } else {
	// if (enclosingShadow.getKind() == Shadow.MethodExecution && enclosingMethod.getEffectiveSignature() != null) {
	//
	// } else {
	// return enclosingShadow.getSignature();
	// }
	// }
	// }

	private InstructionList makeArgsObjectArray() {
		InstructionFactory fact = getFactory();
		BcelVar arrayVar = genTempVar(UnresolvedType.OBJECTARRAY);
		final InstructionList il = new InstructionList();
		int alen = getArgCount();
		il.append(Utility.createConstant(fact, alen));
		il.append(fact.createNewArray(Type.OBJECT, (short) 1));
		arrayVar.appendStore(il, fact);

		int stateIndex = 0;
		for (int i = 0, len = getArgCount(); i < len; i++) {
			arrayVar.appendConvertableArrayStore(il, fact, stateIndex, (BcelVar) getArgVar(i));
			stateIndex++;
		}
		arrayVar.appendLoad(il, fact);
		return il;
	}

	// ---- initializing var tables

	/*
	 * initializing this is doesn't do anything, because this is protected from side-effects, so we don't need to copy its location
	 */

	private void initializeThisVar() {
		if (thisVar != null) {
			return;
		}
		thisVar = new BcelVar(getThisType().resolve(world), 0);
		thisVar.setPositionInAroundState(0);
	}

	public void initializeTargetVar() {
		InstructionFactory fact = getFactory();
		if (targetVar != null) {
			return;
		}
		if (getKind().isTargetSameAsThis()) {
			if (hasThis()) {
				initializeThisVar();
			}
			targetVar = thisVar;
		} else {
			initializeArgVars(); // gotta pop off the args before we find the target
			UnresolvedType type = getTargetType();
			type = ensureTargetTypeIsCorrect(type);
			targetVar = genTempVar(type, "ajc$target");
			range.insert(targetVar.createStore(fact), Range.OutsideBefore);
			targetVar.setPositionInAroundState(hasThis() ? 1 : 0);
		}
	}

	/*
	 * PR 72528 This method double checks the target type under certain conditions. The Java 1.4 compilers seem to take calls to
	 * clone methods on array types and create bytecode that looks like clone is being called on Object. If we advise a clone call
	 * with around advice we extract the call into a helper method which we can then refer to. Because the type in the bytecode for
	 * the call to clone is Object we create a helper method with an Object parameter - this is not correct as we have lost the fact
	 * that the actual type is an array type. If we don't do the check below we will create code that fails java verification. This
	 * method checks for the peculiar set of conditions and if they are true, it has a sneak peek at the code before the call to see
	 * what is on the stack.
	 */
	public UnresolvedType ensureTargetTypeIsCorrect(UnresolvedType tx) {

		Member msig = getSignature();
		if (msig.getArity() == 0 && getKind() == MethodCall && msig.getName().charAt(0) == 'c' && tx.equals(ResolvedType.OBJECT)
				&& msig.getReturnType().equals(ResolvedType.OBJECT) && msig.getName().equals("clone")) {

			// Lets go back through the code from the start of the shadow
			InstructionHandle searchPtr = range.getStart().getPrev();
			while (Range.isRangeHandle(searchPtr) || searchPtr.getInstruction().isStoreInstruction()) { // ignore this instruction -
				// it doesnt give us the
				// info we want
				searchPtr = searchPtr.getPrev();
			}

			// A load instruction may tell us the real type of what the clone() call is on
			if (searchPtr.getInstruction().isLoadInstruction()) {
				LocalVariableTag lvt = LazyMethodGen.getLocalVariableTag(searchPtr, searchPtr.getInstruction().getIndex());
				if (lvt != null) {
					return UnresolvedType.forSignature(lvt.getType());
				}
			}
			// A field access instruction may tell us the real type of what the clone() call is on
			if (searchPtr.getInstruction() instanceof FieldInstruction) {
				FieldInstruction si = (FieldInstruction) searchPtr.getInstruction();
				Type t = si.getFieldType(getEnclosingClass().getConstantPool());
				return BcelWorld.fromBcel(t);
			}
			// A new array instruction obviously tells us it is an array type !
			if (searchPtr.getInstruction().opcode == Constants.ANEWARRAY) {
				// ANEWARRAY ana = (ANEWARRAY)searchPoint.getInstruction();
				// Type t = ana.getType(getEnclosingClass().getConstantPool());
				// Just use a standard java.lang.object array - that will work fine
				return BcelWorld.fromBcel(new ArrayType(Type.OBJECT, 1));
			}
			// A multi new array instruction obviously tells us it is an array type !
			if (searchPtr.getInstruction() instanceof MULTIANEWARRAY) {
				MULTIANEWARRAY ana = (MULTIANEWARRAY) searchPtr.getInstruction();
				// Type t = ana.getType(getEnclosingClass().getConstantPool());
				// t = new ArrayType(t,ana.getDimensions());
				// Just use a standard java.lang.object array - that will work fine
				return BcelWorld.fromBcel(new ArrayType(Type.OBJECT, ana.getDimensions()));
			}
			throw new BCException("Can't determine real target of clone() when processing instruction "
					+ searchPtr.getInstruction() + ".  Perhaps avoid selecting clone with your pointcut?");
		}
		return tx;
	}

	public void ensureInitializedArgVar(int argNumber) {
		if (allArgVarsInitialized || (argVars != null && argVars[argNumber] != null)) {
			return;
		}
		InstructionFactory fact = getFactory();
		int len = getArgCount();
		if (argVars == null) {
			argVars = new BcelVar[len];
		}

		// Need to initialize argument i
		int positionOffset = (hasTarget() ? 1 : 0) + ((hasThis() && !getKind().isTargetSameAsThis()) ? 1 : 0);

		if (getKind().argsOnStack()) {
			// Let's just do them all now since they are on the stack
			// we move backwards because we're popping off the stack
			for (int i = len - 1; i >= 0; i--) {
				UnresolvedType type = getArgType(i);
				BcelVar tmp = genTempVar(type, "ajc$arg" + i);
				range.insert(tmp.createStore(getFactory()), Range.OutsideBefore);
				int position = i;
				position += positionOffset;
				tmp.setPositionInAroundState(position);
				argVars[i] = tmp;
			}
			allArgVarsInitialized = true;
		} else {
			int index = 0;
			if (arg0HoldsThis()) {
				index++;
			}
			boolean allInited = true;
			for (int i = 0; i < len; i++) {
				UnresolvedType type = getArgType(i);
				if (i == argNumber) {
					argVars[argNumber] = genTempVar(type, "ajc$arg" + argNumber);
					range.insert(argVars[argNumber].createCopyFrom(fact, index), Range.OutsideBefore);
					argVars[argNumber].setPositionInAroundState(argNumber + positionOffset);
				}
				allInited = allInited && argVars[i] != null;
				index += type.getSize();
			}
			if (allInited && (argNumber + 1) == len) {
				allArgVarsInitialized = true;
			}
		}
	}

	/**
	 * Initialize all the available arguments at the shadow. This means creating a copy of them that we can then use for advice
	 * calls (the copy ensures we are not affected by other advice changing the values). This method initializes all arguments
	 * whereas the method ensureInitializedArgVar will only ensure a single argument is setup.
	 */
	public void initializeArgVars() {
		if (allArgVarsInitialized) {
			return;
		}
		InstructionFactory fact = getFactory();
		int len = getArgCount();
		if (argVars == null) {
			argVars = new BcelVar[len];
		}
		int positionOffset = (hasTarget() ? 1 : 0) + ((hasThis() && !getKind().isTargetSameAsThis()) ? 1 : 0);

		if (getKind().argsOnStack()) {
			// we move backwards because we're popping off the stack
			for (int i = len - 1; i >= 0; i--) {
				UnresolvedType type = getArgType(i);
				BcelVar tmp = genTempVar(type, "ajc$arg" + i);
				range.insert(tmp.createStore(getFactory()), Range.OutsideBefore);
				int position = i;
				position += positionOffset;
				tmp.setPositionInAroundState(position);
				argVars[i] = tmp;
			}
		} else {
			int index = 0;
			if (arg0HoldsThis()) {
				index++;
			}

			for (int i = 0; i < len; i++) {
				UnresolvedType type = getArgType(i);
				if (argVars[i] == null) {
					BcelVar tmp = genTempVar(type, "ajc$arg" + i);
					range.insert(tmp.createCopyFrom(fact, index), Range.OutsideBefore);
					argVars[i] = tmp;
					tmp.setPositionInAroundState(i + positionOffset);
				}
				index += type.resolve(world).getSize();
			}
		}
		allArgVarsInitialized = true;

	}

	public void initializeForAroundClosure() {
		initializeArgVars();
		if (hasTarget()) {
			initializeTargetVar();
		}
		if (hasThis()) {
			initializeThisVar();
			// System.out.println("initialized: " + this + " thisVar = " + thisVar);
		}
	}

	public void initializeThisAnnotationVars() {
		if (thisAnnotationVars != null) {
			return;
		}
		thisAnnotationVars = new HashMap<>();
		// populate..
	}

	public void initializeTargetAnnotationVars() {
		if (targetAnnotationVars != null) {
			return;
		}
		if (getKind().isTargetSameAsThis()) {
			if (hasThis()) {
				initializeThisAnnotationVars();
			}
			targetAnnotationVars = thisAnnotationVars;
		} else {
			targetAnnotationVars = new HashMap<>();
			ResolvedType[] rtx = this.getTargetType().resolve(world).getAnnotationTypes(); // what about annotations we havent
			// gotten yet but we will get in
			// subclasses?
			for (ResolvedType typeX : rtx) {
				targetAnnotationVars.put(typeX, new TypeAnnotationAccessVar(typeX, (BcelVar) getTargetVar()));
			}
			// populate.
		}
	}

	// public void initializeArgAnnotationVars() {
	// if (argAnnotationVars != null) {
	// return;
	// }
	// int numArgs = getArgCount();
	// argAnnotationVars = new Map[numArgs];
	// for (int i = 0; i < argAnnotationVars.length; i++) {
	// argAnnotationVars[i] = new HashMap();
	// // FIXME asc just delete this logic - we always build the Var on demand, as we don't know at weave time
	// // what the full set of annotations could be (due to static/dynamic type differences...)
	// }
	// }

	protected ResolvedMember getRelevantMember(ResolvedMember foundMember, Member relevantMember, ResolvedType relevantType) {
		if (foundMember != null) {
			return foundMember;
		}

		foundMember = getSignature().resolve(world);
		if (foundMember == null && relevantMember != null) {
			foundMember = relevantType.lookupMemberWithSupersAndITDs(relevantMember);
		}

		// check the ITD'd dooberries
		List<ConcreteTypeMunger> mungers = relevantType.resolve(world).getInterTypeMungers();
		for (ConcreteTypeMunger typeMunger : mungers) {
			if (typeMunger.getMunger() instanceof NewMethodTypeMunger || typeMunger.getMunger() instanceof NewConstructorTypeMunger) {
				ResolvedMember fakerm = typeMunger.getSignature();
				if (fakerm.getName().equals(getSignature().getName())
						&& fakerm.getParameterSignature().equals(getSignature().getParameterSignature())) {
					if (foundMember.getKind() == ResolvedMember.CONSTRUCTOR) {
						foundMember = AjcMemberMaker.interConstructor(relevantType, foundMember, typeMunger.getAspectType());
					} else {
						foundMember = AjcMemberMaker.interMethod(foundMember, typeMunger.getAspectType(), false);
						// ResolvedMember o = AjcMemberMaker.interMethodBody(fakerm, typeMunger.getAspectType());
						// // Object os = o.getAnnotations();
						// ResolvedMember foundMember2 = findMethod(typeMunger.getAspectType(), o);
						// Object os2 = foundMember2.getAnnotations();
						// int stop = 1;
						// foundMember = foundMember2;
						// foundMember = AjcMemberMaker.interMethod(foundMember, typeMunger.getAspectType());
					}
					// in the above.. what about if it's on an Interface? Can that happen?
					// then the last arg of the above should be true
					return foundMember;
				}
			}
		}
		return foundMember;
	}

	protected ResolvedType[] getAnnotations(ResolvedMember foundMember, Member relevantMember, ResolvedType relevantType) {
		if (foundMember == null) {
			// check the ITD'd dooberries
			List<ConcreteTypeMunger> mungers = relevantType.resolve(world).getInterTypeMungers();
			for (Object munger : mungers) {
				ConcreteTypeMunger typeMunger = (ConcreteTypeMunger) munger;
				if (typeMunger.getMunger() instanceof NewMethodTypeMunger
						|| typeMunger.getMunger() instanceof NewConstructorTypeMunger) {
					ResolvedMember fakerm = typeMunger.getSignature();
					// if (fakerm.hasAnnotations())

					ResolvedMember ajcMethod = (getSignature().getKind() == ResolvedMember.CONSTRUCTOR ? AjcMemberMaker
							.postIntroducedConstructor(typeMunger.getAspectType(), fakerm.getDeclaringType(),
									fakerm.getParameterTypes()) : AjcMemberMaker.interMethodDispatcher(fakerm,
							typeMunger.getAspectType()));
					// AjcMemberMaker.interMethodBody(fakerm,typeMunger.getAspectType()));
					ResolvedMember rmm = findMethod(typeMunger.getAspectType(), ajcMethod);
					if (fakerm.getName().equals(getSignature().getName())
							&& fakerm.getParameterSignature().equals(getSignature().getParameterSignature())) {
						relevantType = typeMunger.getAspectType();
						foundMember = rmm;
						return foundMember.getAnnotationTypes();
					}
				}
			}
			// didn't find in ITDs, look in supers
			foundMember = relevantType.lookupMemberWithSupersAndITDs(relevantMember);
			if (foundMember == null) {
				throw new IllegalStateException("Couldn't find member " + relevantMember + " for type " + relevantType);
			}
		}
		return foundMember.getAnnotationTypes();
	}

	/**
	 * By determining what "kind" of shadow we are, we can find out the annotations on the appropriate element (method, field,
	 * constructor, type). Then create one BcelVar entry in the map for each annotation, keyed by annotation type.
	 */
	public void initializeKindedAnnotationVars() {
		if (kindedAnnotationVars != null) {
			return;
		}
		kindedAnnotationVars = new HashMap<>();

		ResolvedType[] annotations = null;
		Member shadowSignature = getSignature();
		Member annotationHolder = getSignature();
		ResolvedType relevantType = shadowSignature.getDeclaringType().resolve(world);

		if (relevantType.isRawType() || relevantType.isParameterizedType()) {
			relevantType = relevantType.getGenericType();
		}

		// Determine the annotations that are of interest
		if (getKind() == Shadow.StaticInitialization) {
			annotations = relevantType.resolve(world).getAnnotationTypes();
		} else if (getKind() == Shadow.MethodCall || getKind() == Shadow.ConstructorCall) {
			ResolvedMember foundMember = findMethod2(relevantType.resolve(world).getDeclaredMethods(), getSignature());
			annotations = getAnnotations(foundMember, shadowSignature, relevantType);
			annotationHolder = getRelevantMember(foundMember, shadowSignature, relevantType);
			relevantType = annotationHolder.getDeclaringType().resolve(world);
		} else if (getKind() == Shadow.FieldSet || getKind() == Shadow.FieldGet) {
			annotationHolder = findField(relevantType.getDeclaredFields(), getSignature());

			if (annotationHolder == null) {
				// check the ITD'd dooberries
				List<ConcreteTypeMunger> mungers = relevantType.resolve(world).getInterTypeMungers();
				for (ConcreteTypeMunger typeMunger : mungers) {
					if (typeMunger.getMunger() instanceof NewFieldTypeMunger) {
						ResolvedMember fakerm = typeMunger.getSignature();
						// if (fakerm.hasAnnotations())
						ResolvedMember ajcMethod = AjcMemberMaker.interFieldInitializer(fakerm, typeMunger.getAspectType());
						ResolvedMember rmm = findMethod(typeMunger.getAspectType(), ajcMethod);
						if (fakerm.equals(getSignature())) {
							relevantType = typeMunger.getAspectType();
							annotationHolder = rmm;
						}
					}
				}
			}
			annotations = ((ResolvedMember) annotationHolder).getAnnotationTypes();

		} else if (getKind() == Shadow.MethodExecution || getKind() == Shadow.ConstructorExecution
				|| getKind() == Shadow.AdviceExecution) {

			ResolvedMember foundMember = findMethod2(relevantType.getDeclaredMethods(), getSignature());
			annotations = getAnnotations(foundMember, shadowSignature, relevantType);
			annotationHolder = getRelevantMember(foundMember, annotationHolder, relevantType);
			UnresolvedType ut = annotationHolder.getDeclaringType();
			relevantType = ut.resolve(world);

		} else if (getKind() == Shadow.ExceptionHandler) {
			relevantType = getSignature().getParameterTypes()[0].resolve(world);
			annotations = relevantType.getAnnotationTypes();

		} else if (getKind() == Shadow.PreInitialization || getKind() == Shadow.Initialization) {
			ResolvedMember found = findMethod2(relevantType.getDeclaredMethods(), getSignature());
			annotations = found.getAnnotationTypes();
		}

		if (annotations == null) {
			// We can't have recognized the shadow - should blow up now to be on the safe side
			throw new BCException("Could not discover annotations for shadow: " + getKind());
		}

		for (ResolvedType annotationType : annotations) {
			AnnotationAccessVar accessVar = new AnnotationAccessVar(this, getKind(), annotationType.resolve(world), relevantType,
					annotationHolder, false);
			kindedAnnotationVars.put(annotationType, accessVar);
		}
	}

	private ResolvedMember findMethod2(ResolvedMember members[], Member sig) {
		String signatureName = sig.getName();
		String parameterSignature = sig.getParameterSignature();
		for (ResolvedMember member : members) {
			if (member.getName().equals(signatureName) && member.getParameterSignature().equals(parameterSignature)) {
				return member;
			}
		}
		return null;
	}

	private ResolvedMember findMethod(ResolvedType aspectType, ResolvedMember ajcMethod) {
		ResolvedMember decMethods[] = aspectType.getDeclaredMethods();
		for (ResolvedMember member : decMethods) {
			if (member.equals(ajcMethod)) {
				return member;
			}
		}
		return null;
	}

	private ResolvedMember findField(ResolvedMember[] members, Member lookingFor) {
		for (ResolvedMember member : members) {
			if (member.getName().equals(getSignature().getName()) && member.getType().equals(getSignature().getType())) {
				return member;
			}
		}
		return null;
	}

	public void initializeWithinAnnotationVars() {
		if (withinAnnotationVars != null) {
			return;
		}
		withinAnnotationVars = new HashMap<>();

		ResolvedType[] annotations = getEnclosingType().resolve(world).getAnnotationTypes();
		for (ResolvedType ann : annotations) {
			Kind k = Shadow.StaticInitialization;
			withinAnnotationVars.put(ann, new AnnotationAccessVar(this, k, ann, getEnclosingType(), null, true));
		}
	}

	public void initializeWithinCodeAnnotationVars() {
		if (withincodeAnnotationVars != null) {
			return;
		}
		withincodeAnnotationVars = new HashMap<>();

		// For some shadow we are interested in annotations on the method containing that shadow.
		ResolvedType[] annotations = getEnclosingMethod().getMemberView().getAnnotationTypes();
		for (ResolvedType ann : annotations) {
			Kind k = (getEnclosingMethod().getMemberView().getKind() == Member.CONSTRUCTOR ? Shadow.ConstructorExecution
					: Shadow.MethodExecution);
			withincodeAnnotationVars.put(ann, new AnnotationAccessVar(this, k, ann, getEnclosingType(),
					getEnclosingCodeSignature(), true));
		}
	}

	// ---- weave methods

	void weaveBefore(BcelAdvice munger) {
		range.insert(munger.getAdviceInstructions(this, null, range.getRealStart()), Range.InsideBefore);
	}

	public void weaveAfter(BcelAdvice munger) {
		weaveAfterThrowing(munger, UnresolvedType.THROWABLE);
		weaveAfterReturning(munger);
	}

	/**
	 * The basic strategy here is to add a set of instructions at the end of the shadow range that dispatch the advice, and then
	 * return whatever the shadow was going to return anyway.
	 *
	 * To achieve this, we note all the return statements in the advice, and replace them with code that: 1) stores the return value
	 * on top of the stack in a temp var 2) jumps to the start of our advice block 3) restores the return value at the end of the
	 * advice block before ultimately returning
	 *
	 * We also need to bind the return value into a returning parameter, if the advice specified one.
	 */
	public void weaveAfterReturning(BcelAdvice munger) {
		List<InstructionHandle> returns = findReturnInstructions();
		boolean hasReturnInstructions = !returns.isEmpty();

		// list of instructions that handle the actual return from the join point
		InstructionList retList = new InstructionList();

		// variable that holds the return value
		BcelVar returnValueVar = null;

		if (hasReturnInstructions) {
			returnValueVar = generateReturnInstructions(returns, retList);
		} else {
			// we need at least one instruction, as the target for jumps
			retList.append(InstructionConstants.NOP);
		}

		// list of instructions for dispatching to the advice itself
		InstructionList advice = getAfterReturningAdviceDispatchInstructions(munger, retList.getStart());

		if (hasReturnInstructions) {
			InstructionHandle gotoTarget = advice.getStart();
			for (InstructionHandle ih : returns) {
				retargetReturnInstruction(munger.hasExtraParameter(), returnValueVar, gotoTarget, ih);
			}
		}

		range.append(advice);
		range.append(retList);
	}

	/**
	 * @return a list of all the return instructions in the range of this shadow
	 */
	private List<InstructionHandle> findReturnInstructions() {
		List<InstructionHandle> returns = new ArrayList<>();
		for (InstructionHandle ih = range.getStart(); ih != range.getEnd(); ih = ih.getNext()) {
			if (ih.getInstruction().isReturnInstruction()) {
				returns.add(ih);
			}
		}
		return returns;
	}

	/**
	 * Given a list containing all the return instruction handles for this shadow, finds the last return instruction and copies it,
	 * making this the ultimate return. If the shadow has a non-void return type, we also create a temporary variable to hold the
	 * return value, and load the value from this var before returning (see pr148007 for why we do this - it works around a JRockit
	 * bug, and is also closer to what javac generates)
	 *
	 * Sometimes the 'last return' isnt the right one - some rogue code can include the real return from the body of a subroutine
	 * that exists at the end of the method. In this case the last return is RETURN but that may not be correct for a method with a
	 * non-void return type... pr151673
	 *
	 * @param returns list of all the return instructions in the shadow
	 * @param returnInstructions instruction list into which the return instructions should be generated
	 * @return the variable holding the return value, if needed
	 */
	private BcelVar generateReturnInstructions(List<InstructionHandle> returns, InstructionList returnInstructions) {
		BcelVar returnValueVar = null;
		if (this.hasANonVoidReturnType()) {
			// Find the last *correct* return - this is a method with a non-void return type
			// so ignore RETURN
			Instruction newReturnInstruction = null;
			int i = returns.size() - 1;
			while (newReturnInstruction == null && i >= 0) {
				InstructionHandle ih = returns.get(i);
				if (ih.getInstruction().opcode != Constants.RETURN) {
					newReturnInstruction = Utility.copyInstruction(ih.getInstruction());
				}
				i--;
			}
			returnValueVar = genTempVar(this.getReturnType());
			returnValueVar.appendLoad(returnInstructions, getFactory());
			returnInstructions.append(newReturnInstruction);
		} else {
			InstructionHandle lastReturnHandle = returns.get(returns.size() - 1);
			Instruction newReturnInstruction = Utility.copyInstruction(lastReturnHandle.getInstruction());
			returnInstructions.append(newReturnInstruction);
		}
		return returnValueVar;
	}

	/**
	 * @return true, iff this shadow returns a value
	 */
	private boolean hasANonVoidReturnType() {
		return !this.getReturnType().equals(UnresolvedType.VOID);
	}

	/**
	 * Get the list of instructions used to dispatch to the after advice
	 *
	 * @param munger
	 * @param firstInstructionInReturnSequence
	 * @return
	 */
	private InstructionList getAfterReturningAdviceDispatchInstructions(BcelAdvice munger,
			InstructionHandle firstInstructionInReturnSequence) {
		InstructionList advice = new InstructionList();

		BcelVar tempVar = null;
		if (munger.hasExtraParameter()) {
			tempVar = insertAdviceInstructionsForBindingReturningParameter(advice);
		}
		advice.append(munger.getAdviceInstructions(this, tempVar, firstInstructionInReturnSequence));
		return advice;
	}

	/**
	 * If the after() returning(Foo f) form is used, bind the return value to the parameter. If the shadow returns void, bind null.
	 *
	 * @param advice
	 * @return
	 */
	private BcelVar insertAdviceInstructionsForBindingReturningParameter(InstructionList advice) {
		BcelVar tempVar;
		UnresolvedType tempVarType = getReturnType();
		if (tempVarType.equals(UnresolvedType.VOID)) {
			tempVar = genTempVar(UnresolvedType.OBJECT);
			advice.append(InstructionConstants.ACONST_NULL);
			tempVar.appendStore(advice, getFactory());
		} else {
			tempVar = genTempVar(tempVarType);
			advice.append(InstructionFactory.createDup(tempVarType.getSize()));
			tempVar.appendStore(advice, getFactory());
		}
		return tempVar;
	}

	/**
	 * Helper method for weaveAfterReturning
	 *
	 * Each return instruction in the method body is retargeted by calling this method. The return instruction is replaced by up to
	 * three instructions: 1) if the shadow returns a value, and that value is bound to an after returning parameter, then we DUP
	 * the return value on the top of the stack 2) if the shadow returns a value, we store it in the returnValueVar (it will be
	 * retrieved from here when we ultimately return after the advice dispatch) 3) if the return was the last instruction, we add a
	 * NOP (it will fall through to the advice dispatch), otherwise we add a GOTO that branches to the supplied gotoTarget (start of
	 * the advice dispatch)
	 */
	private void retargetReturnInstruction(boolean hasReturningParameter, BcelVar returnValueVar, InstructionHandle gotoTarget,
			InstructionHandle returnHandle) {
		// pr148007, work around JRockit bug
		// replace ret with store into returnValueVar, followed by goto if not
		// at the end of the instruction list...
		InstructionList newInstructions = new InstructionList();
		if (returnValueVar != null) {
			if (hasReturningParameter) {
				// we have to dup the return val before consuming it...
				newInstructions.append(InstructionFactory.createDup(this.getReturnType().getSize()));
			}
			// store the return value into this var
			returnValueVar.appendStore(newInstructions, getFactory());
		}
		if (!isLastInstructionInRange(returnHandle, range)) {
			newInstructions.append(InstructionFactory.createBranchInstruction(Constants.GOTO, gotoTarget));
		}
		if (newInstructions.isEmpty()) {
			newInstructions.append(InstructionConstants.NOP);
		}
		Utility.replaceInstruction(returnHandle, newInstructions, enclosingMethod);
	}

	private boolean isLastInstructionInRange(InstructionHandle ih, ShadowRange aRange) {
		return ih.getNext() == aRange.getEnd();
	}

	public void weaveAfterThrowing(BcelAdvice munger, UnresolvedType catchType) {
		// a good optimization would be not to generate anything here
		// if the shadow is GUARANTEED empty (i.e., there's NOTHING, not even
		// a shadow, inside me).
		if (getRange().getStart().getNext() == getRange().getEnd()) {
			return;
		}
		InstructionFactory fact = getFactory();
		InstructionList handler = new InstructionList();
		BcelVar exceptionVar = genTempVar(catchType);
		exceptionVar.appendStore(handler, fact);

		// pr62642
		// I will now jump through some firey BCEL hoops to generate a trivial bit of code:
		// if (exc instanceof ExceptionInInitializerError)
		// throw (ExceptionInInitializerError)exc;
		if (this.getEnclosingMethod().getName().equals("<clinit>")) {
			ResolvedType eiieType = world.resolve("java.lang.ExceptionInInitializerError");
			ObjectType eiieBcelType = (ObjectType) BcelWorld.makeBcelType(eiieType);
			InstructionList ih = new InstructionList(InstructionConstants.NOP);
			handler.append(exceptionVar.createLoad(fact));
			handler.append(fact.createInstanceOf(eiieBcelType));
			InstructionBranch bi = InstructionFactory.createBranchInstruction(Constants.IFEQ, ih.getStart());
			handler.append(bi);
			handler.append(exceptionVar.createLoad(fact));
			handler.append(fact.createCheckCast(eiieBcelType));
			handler.append(InstructionConstants.ATHROW);
			handler.append(ih);
		}

		InstructionList endHandler = new InstructionList(exceptionVar.createLoad(fact));
		handler.append(munger.getAdviceInstructions(this, exceptionVar, endHandler.getStart()));
		handler.append(endHandler);
		handler.append(InstructionConstants.ATHROW);
		InstructionHandle handlerStart = handler.getStart();

		if (isFallsThrough()) {
			InstructionHandle jumpTarget = handler.append(InstructionConstants.NOP);
			handler.insert(InstructionFactory.createBranchInstruction(Constants.GOTO, jumpTarget));
		}
		InstructionHandle protectedEnd = handler.getStart();
		range.insert(handler, Range.InsideAfter);

		enclosingMethod.addExceptionHandler(range.getStart().getNext(), protectedEnd.getPrev(), handlerStart,
				(ObjectType) BcelWorld.makeBcelType(catchType), // ???Type.THROWABLE,
				// high priority if our args are on the stack
				getKind().hasHighPriorityExceptions());
	}

	// ??? this shares a lot of code with the above weaveAfterThrowing
	// ??? would be nice to abstract that to say things only once
	public void weaveSoftener(BcelAdvice munger, UnresolvedType catchType) {
		// a good optimization would be not to generate anything here
		// if the shadow is GUARANTEED empty (i.e., there's NOTHING, not even
		// a shadow, inside me).
		if (getRange().getStart().getNext() == getRange().getEnd()) {
			return;
		}

		InstructionFactory fact = getFactory();
		InstructionList handler = new InstructionList();
		InstructionList rtExHandler = new InstructionList();
		BcelVar exceptionVar = genTempVar(catchType);

		handler.append(fact.createNew(NameMangler.SOFT_EXCEPTION_TYPE));
		handler.append(InstructionFactory.createDup(1));
		handler.append(exceptionVar.createLoad(fact));
		handler.append(fact.createInvoke(NameMangler.SOFT_EXCEPTION_TYPE, "<init>", Type.VOID, new Type[] { Type.THROWABLE },
				Constants.INVOKESPECIAL)); // ??? special
		handler.append(InstructionConstants.ATHROW);

		// ENH 42737
		exceptionVar.appendStore(rtExHandler, fact);
		// aload_1
		rtExHandler.append(exceptionVar.createLoad(fact));
		// instanceof class java/lang/RuntimeException
		rtExHandler.append(fact.createInstanceOf(new ObjectType("java.lang.RuntimeException")));
		// ifeq go to new SOFT_EXCEPTION_TYPE instruction
		rtExHandler.append(InstructionFactory.createBranchInstruction(Constants.IFEQ, handler.getStart()));
		// aload_1
		rtExHandler.append(exceptionVar.createLoad(fact));
		// athrow
		rtExHandler.append(InstructionFactory.ATHROW);

		InstructionHandle handlerStart = rtExHandler.getStart();

		if (isFallsThrough()) {
			InstructionHandle jumpTarget = range.getEnd();// handler.append(fact.NOP);
			rtExHandler.insert(InstructionFactory.createBranchInstruction(Constants.GOTO, jumpTarget));
		}

		rtExHandler.append(handler);

		InstructionHandle protectedEnd = rtExHandler.getStart();
		range.insert(rtExHandler, Range.InsideAfter);

		enclosingMethod.addExceptionHandler(range.getStart().getNext(), protectedEnd.getPrev(), handlerStart,
				(ObjectType) BcelWorld.makeBcelType(catchType),
				// high priority if our args are on the stack
				getKind().hasHighPriorityExceptions());
	}

	public void weavePerObjectEntry(final BcelAdvice munger, final BcelVar onVar) {
		final InstructionFactory fact = getFactory();

		InstructionList entryInstructions = new InstructionList();
		InstructionList entrySuccessInstructions = new InstructionList();
		onVar.appendLoad(entrySuccessInstructions, fact);

		entrySuccessInstructions
				.append(Utility.createInvoke(fact, world, AjcMemberMaker.perObjectBind(munger.getConcreteAspect())));

		InstructionList testInstructions = munger.getTestInstructions(this, entrySuccessInstructions.getStart(),
				range.getRealStart(), entrySuccessInstructions.getStart());

		entryInstructions.append(testInstructions);
		entryInstructions.append(entrySuccessInstructions);

		range.insert(entryInstructions, Range.InsideBefore);
	}

	// PTWIMPL Create static initializer to call the aspect factory
	/**
	 * Causes the aspect instance to be *set* for later retrievable through localAspectof()/aspectOf()
	 */
	public void weavePerTypeWithinAspectInitialization(final BcelAdvice munger, UnresolvedType t) {
		ResolvedType tResolved = t.resolve(world);
		if (tResolved.isInterface()) {
			return; // Don't initialize statics in interfaces
		}
		ResolvedType aspectRT = munger.getConcreteAspect();
		BcelWorld.getBcelObjectType(aspectRT);

		// Although matched, if the visibility rules prevent the aspect from seeing this type, don't
		// insert any code (easier to do it here than try to affect the matching logic, unfortunately)
		if (!(tResolved.canBeSeenBy(aspectRT) || aspectRT.isPrivilegedAspect())) {
			return;
		}

		final InstructionFactory fact = getFactory();

		InstructionList entryInstructions = new InstructionList();
		InstructionList entrySuccessInstructions = new InstructionList();

		String aspectname = munger.getConcreteAspect().getName();

		String ptwField = NameMangler.perTypeWithinFieldForTarget(munger.getConcreteAspect());
		entrySuccessInstructions.append(InstructionFactory.PUSH(fact.getConstantPool(), t.getName()));

		entrySuccessInstructions.append(fact.createInvoke(aspectname, "ajc$createAspectInstance", new ObjectType(aspectname),
				new Type[] { new ObjectType("java.lang.String") }, Constants.INVOKESTATIC));
		entrySuccessInstructions.append(fact.createPutStatic(t.getName(), ptwField, new ObjectType(aspectname)));

		entryInstructions.append(entrySuccessInstructions);

		range.insert(entryInstructions, Range.InsideBefore);
	}

	public void weaveCflowEntry(final BcelAdvice munger, final Member cflowField) {
		final boolean isPer = munger.getKind() == AdviceKind.PerCflowBelowEntry || munger.getKind() == AdviceKind.PerCflowEntry;
		if (!isPer && getKind() == PreInitialization) {
			return;
		}
		final Type objectArrayType = new ArrayType(Type.OBJECT, 1);
		final InstructionFactory fact = getFactory();

		final BcelVar testResult = genTempVar(UnresolvedType.BOOLEAN);

		InstructionList entryInstructions = new InstructionList();
		{
			InstructionList entrySuccessInstructions = new InstructionList();

			if (munger.hasDynamicTests()) {
				entryInstructions.append(Utility.createConstant(fact, 0));
				testResult.appendStore(entryInstructions, fact);

				entrySuccessInstructions.append(Utility.createConstant(fact, 1));
				testResult.appendStore(entrySuccessInstructions, fact);
			}

			if (isPer) {
				entrySuccessInstructions.append(fact.createInvoke(munger.getConcreteAspect().getName(),
						NameMangler.PERCFLOW_PUSH_METHOD, Type.VOID, new Type[] {}, Constants.INVOKESTATIC));
			} else {
				BcelVar[] cflowStateVars = munger.getExposedStateAsBcelVars(false);

				if (cflowStateVars.length == 0) {
					// This should be getting managed by a counter - lets make sure.
					if (!cflowField.getType().getName().endsWith("CFlowCounter")) {
						throw new RuntimeException("Incorrectly attempting counter operation on stacked cflow");
					}
					entrySuccessInstructions.append(Utility.createGet(fact, cflowField));
					// arrayVar.appendLoad(entrySuccessInstructions, fact);
					entrySuccessInstructions.append(fact.createInvoke(NameMangler.CFLOW_COUNTER_TYPE, "inc", Type.VOID,
							new Type[] {}, Constants.INVOKEVIRTUAL));
				} else {
					BcelVar arrayVar = genTempVar(UnresolvedType.OBJECTARRAY);

					int alen = cflowStateVars.length;
					entrySuccessInstructions.append(Utility.createConstant(fact, alen));
					entrySuccessInstructions.append(fact.createNewArray(Type.OBJECT, (short) 1));
					arrayVar.appendStore(entrySuccessInstructions, fact);

					for (int i = 0; i < alen; i++) {
						arrayVar.appendConvertableArrayStore(entrySuccessInstructions, fact, i, cflowStateVars[i]);
					}

					entrySuccessInstructions.append(Utility.createGet(fact, cflowField));
					arrayVar.appendLoad(entrySuccessInstructions, fact);

					entrySuccessInstructions.append(fact.createInvoke(NameMangler.CFLOW_STACK_TYPE, "push", Type.VOID,
							new Type[] { objectArrayType }, Constants.INVOKEVIRTUAL));
				}
			}

			InstructionList testInstructions = munger.getTestInstructions(this, entrySuccessInstructions.getStart(),
					range.getRealStart(), entrySuccessInstructions.getStart());
			entryInstructions.append(testInstructions);
			entryInstructions.append(entrySuccessInstructions);
		}

		BcelAdvice exitAdvice = new BcelAdvice(null, null, null, 0, 0, 0, null, munger.getConcreteAspect()) {
			@Override
			public InstructionList getAdviceInstructions(BcelShadow s, BcelVar extraArgVar, InstructionHandle ifNoAdvice) {
				InstructionList exitInstructions = new InstructionList();
				if (munger.hasDynamicTests()) {
					testResult.appendLoad(exitInstructions, fact);
					exitInstructions.append(InstructionFactory.createBranchInstruction(Constants.IFEQ, ifNoAdvice));
				}
				exitInstructions.append(Utility.createGet(fact, cflowField));
				if (munger.getKind() != AdviceKind.PerCflowEntry && munger.getKind() != AdviceKind.PerCflowBelowEntry
						&& munger.getExposedStateAsBcelVars(false).length == 0) {
					exitInstructions.append(fact.createInvoke(NameMangler.CFLOW_COUNTER_TYPE, "dec", Type.VOID, new Type[] {},
							Constants.INVOKEVIRTUAL));
				} else {
					exitInstructions.append(fact.createInvoke(NameMangler.CFLOW_STACK_TYPE, "pop", Type.VOID, new Type[] {},
							Constants.INVOKEVIRTUAL));
				}
				return exitInstructions;
			}
		};
//		if (getKind() == PreInitialization) {
//			weaveAfterReturning(exitAdvice);
//		}
//		else {
			weaveAfter(exitAdvice);
//		}

		range.insert(entryInstructions, Range.InsideBefore);
	}

	/*
	 * Implementation notes:
	 *
	 * AroundInline still extracts the instructions of the original shadow into an extracted method. This allows inlining of even
	 * that advice that doesn't call proceed or calls proceed more than once.
	 *
	 * It extracts the instructions of the original shadow into a method.
	 *
	 * Then it extracts the instructions of the advice into a new method defined on this enclosing class. This new method can then
	 * be specialized as below.
	 *
	 * Then it searches in the instructions of the advice for any call to the proceed method.
	 *
	 * At such a call, there is stuff on the stack representing the arguments to proceed. Pop these into the frame.
	 *
	 * Now build the stack for the call to the extracted method, taking values either from the join point state or from the new
	 * frame locs from proceed. Now call the extracted method. The right return value should be on the stack, so no cast is
	 * necessary.
	 *
	 * If only one call to proceed is made, we can re-inline the original shadow. We are not doing that presently.
	 *
	 * If the body of the advice can be determined to not alter the stack, or if this shadow doesn't care about the stack, i.e.
	 * method-execution, then the new method for the advice can also be re-lined. We are not doing that presently.
	 */
	public void weaveAroundInline(BcelAdvice munger, boolean hasDynamicTest) {
		// !!! THIS BLOCK OF CODE SHOULD BE IN A METHOD CALLED weaveAround(...);
		Member mungerSig = munger.getSignature();
		// Member originalSig = mungerSig; // If mungerSig is on a parameterized type, originalSig is the member on the generic type
		if (mungerSig instanceof ResolvedMember) {
			ResolvedMember rm = (ResolvedMember) mungerSig;
			if (rm.hasBackingGenericMember()) {
				mungerSig = rm.getBackingGenericMember();
			}
		}
		ResolvedType declaringAspectType = world.resolve(mungerSig.getDeclaringType(), true);
		if (declaringAspectType.isMissing()) {
			world.getLint().cantFindType.signal(
					new String[] { WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_DURING_AROUND_WEAVE,
							declaringAspectType.getClassName()) }, getSourceLocation(),
					new ISourceLocation[] { munger.getSourceLocation() });
		}

		// ??? might want some checks here to give better errors
		ResolvedType rt = (declaringAspectType.isParameterizedType() ? declaringAspectType.getGenericType() : declaringAspectType);
		BcelObjectType ot = BcelWorld.getBcelObjectType(rt);
		LazyMethodGen adviceMethod = ot.getLazyClassGen().getLazyMethodGen(mungerSig);
		if (!adviceMethod.getCanInline()) {
			weaveAroundClosure(munger, hasDynamicTest);
			return;
		}

		// specific test for @AJ proceedInInners
		if (isAnnotationStylePassingProceedingJoinPointOutOfAdvice(munger, hasDynamicTest, adviceMethod)) {
			return;
		}

		// We can't inline around methods if they have around advice on them, this
		// is because the weaving will extract the body and hence the proceed call.

		// TODO should consider optimizations to recognize simple cases that don't require body extraction

		enclosingMethod.setCanInline(false);

		LazyClassGen shadowClass = getEnclosingClass();

		// Extract the shadow into a new method. For example:
		// "private static final void method_aroundBody0(M, M, String, org.aspectj.lang.JoinPoint)"
		// Parameters are: this if there is one, target if there is one and its different to this, then original arguments
		// at the shadow, then tjp
		String extractedShadowMethodName = NameMangler.aroundShadowMethodName(getSignature(), shadowClass.getNewGeneratedNameTag());
		List<String> parameterNames = new ArrayList<>();
		boolean shadowClassIsInterface = shadowClass.isInterface();
		LazyMethodGen extractedShadowMethod = extractShadowInstructionsIntoNewMethod(extractedShadowMethodName,
				shadowClassIsInterface?Modifier.PUBLIC:Modifier.PRIVATE,
				munger.getSourceLocation(), parameterNames,shadowClassIsInterface);

		List<BcelVar> argsToCallLocalAdviceMethodWith = new ArrayList<>();
		List<BcelVar> proceedVarList = new ArrayList<>();
		int extraParamOffset = 0;

		// Create the extra parameters that are needed for passing to proceed
		// This code is very similar to that found in makeCallToCallback and should
		// be rationalized in the future

		if (thisVar != null) {
			argsToCallLocalAdviceMethodWith.add(thisVar);
			proceedVarList.add(new BcelVar(thisVar.getType(), extraParamOffset));
			extraParamOffset += thisVar.getType().getSize();
		}

		if (targetVar != null && targetVar != thisVar) {
			argsToCallLocalAdviceMethodWith.add(targetVar);
			proceedVarList.add(new BcelVar(targetVar.getType(), extraParamOffset));
			extraParamOffset += targetVar.getType().getSize();
		}
		for (int i = 0, len = getArgCount(); i < len; i++) {
			argsToCallLocalAdviceMethodWith.add(argVars[i]);
			proceedVarList.add(new BcelVar(argVars[i].getType(), extraParamOffset));
			extraParamOffset += argVars[i].getType().getSize();
		}
		if (thisJoinPointVar != null) {
			argsToCallLocalAdviceMethodWith.add(thisJoinPointVar);
			proceedVarList.add(new BcelVar(thisJoinPointVar.getType(), extraParamOffset));
			extraParamOffset += thisJoinPointVar.getType().getSize();
		}

		// We use the munger signature here because it allows for any parameterization of the mungers pointcut that
		// may have occurred ie. if the pointcut is p(T t) in the super aspect and that has become p(Foo t) in the sub aspect
		// then here the munger signature will have 'Foo' as an argument in it whilst the adviceMethod argument type will be
		// 'Object' - since it represents the advice method in the superaspect which uses the erasure of the type variable p(Object
		// t) - see pr174449.

		Type[] adviceParameterTypes = BcelWorld.makeBcelTypes(munger.getSignature().getParameterTypes());

		// forces initialization ... dont like this but seems to be required for some tests to pass, I think that means there
		// is a LazyMethodGen method that is not correctly setup to call initialize() when it is invoked - but I dont have
		// time right now to discover which
		adviceMethod.getArgumentTypes();

		Type[] extractedMethodParameterTypes = extractedShadowMethod.getArgumentTypes();

		Type[] parameterTypes = new Type[extractedMethodParameterTypes.length + adviceParameterTypes.length + 1];
		int parameterIndex = 0;
		System.arraycopy(extractedMethodParameterTypes, 0, parameterTypes, parameterIndex, extractedMethodParameterTypes.length);
		parameterIndex += extractedMethodParameterTypes.length;
		parameterTypes[parameterIndex++] = BcelWorld.makeBcelType(adviceMethod.getEnclosingClass().getType());
		System.arraycopy(adviceParameterTypes, 0, parameterTypes, parameterIndex, adviceParameterTypes.length);

		// Extract the advice into a new method. This will go in the same type as the shadow
		// name will be something like foo_aroundBody1$advice
		String localAdviceMethodName = NameMangler.aroundAdviceMethodName(getSignature(), shadowClass.getNewGeneratedNameTag());
		int localAdviceMethodModifiers = Modifier.PRIVATE | (world.useFinal() & !shadowClassIsInterface ? Modifier.FINAL : 0) | Modifier.STATIC;
		LazyMethodGen localAdviceMethod = new LazyMethodGen(localAdviceMethodModifiers, BcelWorld.makeBcelType(mungerSig.getReturnType()), localAdviceMethodName, parameterTypes,
				NoDeclaredExceptions, shadowClass);

		// Doesnt work properly, so leave it out:
		// String aspectFilename = adviceMethod.getEnclosingClass().getInternalFileName();
		// String shadowFilename = shadowClass.getInternalFileName();
		// if (!aspectFilename.equals(shadowFilename)) {
		// localAdviceMethod.fromFilename = aspectFilename;
		// shadowClass.addInlinedSourceFileInfo(aspectFilename, adviceMethod.highestLineNumber);
		// }

		shadowClass.addMethodGen(localAdviceMethod);

		// create a map that will move all slots in advice method forward by extraParamOffset
		// in order to make room for the new proceed-required arguments that are added at
		// the beginning of the parameter list
		int nVars = adviceMethod.getMaxLocals() + extraParamOffset;
		IntMap varMap = IntMap.idMap(nVars);
		for (int i = extraParamOffset; i < nVars; i++) {
			varMap.put(i - extraParamOffset, i);
		}

		final InstructionFactory fact = getFactory();

		localAdviceMethod.getBody().insert(
				BcelClassWeaver.genInlineInstructions(adviceMethod, localAdviceMethod, varMap, fact, true));

		localAdviceMethod.setMaxLocals(nVars);

		// the shadow is now empty. First, create a correct call
		// to the around advice. This includes both the call (which may involve
		// value conversion of the advice arguments) and the return
		// (which may involve value conversion of the return value). Right now
		// we push a null for the unused closure. It's sad, but there it is.

		InstructionList advice = new InstructionList();
		// InstructionHandle adviceMethodInvocation;
		{
			for (BcelVar var : argsToCallLocalAdviceMethodWith) {
				var.appendLoad(advice, fact);
			}
			// ??? we don't actually need to push NULL for the closure if we take care
			boolean isAnnoStyleConcreteAspect = munger.getConcreteAspect().isAnnotationStyleAspect();
			boolean isAnnoStyleDeclaringAspect = munger.getDeclaringAspect() != null ? munger.getDeclaringAspect().resolve(world)
					.isAnnotationStyleAspect() : false;

			InstructionList iList = null;
			if (isAnnoStyleConcreteAspect && isAnnoStyleDeclaringAspect) {
				iList = this.loadThisJoinPoint();
				iList.append(Utility.createConversion(getFactory(), LazyClassGen.tjpType, LazyClassGen.proceedingTjpType));
			} else {
				iList = new InstructionList(InstructionConstants.ACONST_NULL);
			}
			advice.append(munger.getAdviceArgSetup(this, null, iList));
			// adviceMethodInvocation =
			advice.append(Utility.createInvoke(fact, localAdviceMethod)); // (fact, getWorld(), munger.getSignature()));
			advice.append(Utility.createConversion(getFactory(), BcelWorld.makeBcelType(mungerSig.getReturnType()),
					extractedShadowMethod.getReturnType(), world.isInJava5Mode()));
			if (!isFallsThrough()) {
				advice.append(InstructionFactory.createReturn(extractedShadowMethod.getReturnType()));
			}
		}

		// now, situate the call inside the possible dynamic tests,
		// and actually add the whole mess to the shadow
		if (!hasDynamicTest) {
			range.append(advice);
		} else {
			InstructionList afterThingie = new InstructionList(InstructionConstants.NOP);
			InstructionList callback = makeCallToCallback(extractedShadowMethod);
			if (terminatesWithReturn()) {
				callback.append(InstructionFactory.createReturn(extractedShadowMethod.getReturnType()));
			} else {
				// InstructionHandle endNop = range.insert(fact.NOP, Range.InsideAfter);
				advice.append(InstructionFactory.createBranchInstruction(Constants.GOTO, afterThingie.getStart()));
			}
			range.append(munger.getTestInstructions(this, advice.getStart(), callback.getStart(), advice.getStart()));
			range.append(advice);
			range.append(callback);
			range.append(afterThingie);
		}

		// now search through the advice, looking for a call to PROCEED.
		// Then we replace the call to proceed with some argument setup, and a
		// call to the extracted method.

		// inlining support for code style aspects
		if (!munger.getDeclaringType().isAnnotationStyleAspect()) {
			String proceedName = NameMangler.proceedMethodName(munger.getSignature().getName());

			InstructionHandle curr = localAdviceMethod.getBody().getStart();
			InstructionHandle end = localAdviceMethod.getBody().getEnd();
			ConstantPool cpg = localAdviceMethod.getEnclosingClass().getConstantPool();
			while (curr != end) {
				InstructionHandle next = curr.getNext();
				Instruction inst = curr.getInstruction();
				if ((inst.opcode == Constants.INVOKESTATIC) && proceedName.equals(((InvokeInstruction) inst).getMethodName(cpg))) {

					localAdviceMethod.getBody().append(curr,
							getRedoneProceedCall(fact, extractedShadowMethod, munger, localAdviceMethod, proceedVarList));
					Utility.deleteInstruction(curr, localAdviceMethod);
				}
				curr = next;
			}
			// and that's it.
		} else {
			// ATAJ inlining support for @AJ aspects
			// [TODO document @AJ code rule: don't manipulate 2 jps proceed at the same time.. in an advice body]
			InstructionHandle curr = localAdviceMethod.getBody().getStart();
			InstructionHandle end = localAdviceMethod.getBody().getEnd();
			ConstantPool cpg = localAdviceMethod.getEnclosingClass().getConstantPool();
			while (curr != end) {
				InstructionHandle next = curr.getNext();
				Instruction inst = curr.getInstruction();
				if ((inst instanceof INVOKEINTERFACE) && "proceed".equals(((INVOKEINTERFACE) inst).getMethodName(cpg))) {
					final boolean isProceedWithArgs;
					if (((INVOKEINTERFACE) inst).getArgumentTypes(cpg).length == 1) {
						// proceed with args as a boxed Object[]
						isProceedWithArgs = true;
					} else {
						isProceedWithArgs = false;
					}
					InstructionList insteadProceedIl = getRedoneProceedCallForAnnotationStyle(fact, extractedShadowMethod, munger,
							localAdviceMethod, proceedVarList, isProceedWithArgs);
					localAdviceMethod.getBody().append(curr, insteadProceedIl);
					Utility.deleteInstruction(curr, localAdviceMethod);
				}
				curr = next;
			}
		}

		// if (parameterNames.size() == 0) {
		// On return we have inserted the advice body into the local advice method. We have remapped all the local variables
		// that were referenced in the advice as we did the copy, and so the local variable table for localAdviceMethod is
		// now lacking any information about all the initial variables.
		InstructionHandle start = localAdviceMethod.getBody().getStart();
		InstructionHandle end = localAdviceMethod.getBody().getEnd();

		// Find the real start and end
		while (start.getInstruction().opcode == Constants.IMPDEP1) {
			start = start.getNext();
		}
		while (end.getInstruction().opcode == Constants.IMPDEP1) {
			end = end.getPrev();
		}
		Type[] args = localAdviceMethod.getArgumentTypes();
		int argNumber = 0;
		for (int slot = 0; slot < extraParamOffset; argNumber++) { // slot will increase by the argument size each time
			String argumentName = null;
			if (argNumber >= args.length || parameterNames.size() == 0 || argNumber >= parameterNames.size()) {
				// this should be unnecessary as I think all known joinpoints and helper methods
				// propagate the parameter names around correctly - but just in case let us do this
				// rather than fail. If a bug is raised reporting unknown as a local variable name
				// then investigate the joinpoint giving rise to the ResolvedMember and why it has
				// no parameter names specified
				argumentName = new StringBuffer("unknown").append(argNumber).toString();
			} else {
				argumentName = parameterNames.get(argNumber);
			}
			String argumentSignature = args[argNumber].getSignature();
			LocalVariableTag lvt = new LocalVariableTag(argumentSignature, argumentName, slot, 0);
			start.addTargeter(lvt);
			end.addTargeter(lvt);
			slot += args[argNumber].getSize();
		}
	}

	/**
	 * Check if the advice method passes a pjp parameter out via an invoke instruction - if so we can't risk inlining.
	 */
	private boolean isAnnotationStylePassingProceedingJoinPointOutOfAdvice(BcelAdvice munger, boolean hasDynamicTest,
			LazyMethodGen adviceMethod) {
		if (munger.getConcreteAspect().isAnnotationStyleAspect()) {
			// if we can't find one proceed() we suspect that the call
			// is happening in an inner class so we don't inline it.
			// Note: for code style, this is done at Aspect compilation time.
			boolean canSeeProceedPassedToOther = false;
			InstructionHandle curr = adviceMethod.getBody().getStart();
			InstructionHandle end = adviceMethod.getBody().getEnd();
			ConstantPool cpg = adviceMethod.getEnclosingClass().getConstantPool();
			while (curr != end) {
				InstructionHandle next = curr.getNext();
				Instruction inst = curr.getInstruction();
				if ((inst instanceof InvokeInstruction)
						&& ((InvokeInstruction) inst).getSignature(cpg).indexOf("Lorg/aspectj/lang/ProceedingJoinPoint;") > 0) {
					// we may want to refine to exclude stuff returning jp ?
					// does code style skip inline if i write dump(thisJoinPoint) ?
					canSeeProceedPassedToOther = true;// we see one pjp passed around - dangerous
					break;
				}
				curr = next;
			}
			if (canSeeProceedPassedToOther) {
				// remember this decision to avoid re-analysis
				adviceMethod.setCanInline(false);
				weaveAroundClosure(munger, hasDynamicTest);
				return true;
			}
		}
		return false;
	}

	private InstructionList getRedoneProceedCall(InstructionFactory fact, LazyMethodGen callbackMethod, BcelAdvice munger,
			LazyMethodGen localAdviceMethod, List<BcelVar> argVarList) {
		InstructionList ret = new InstructionList();
		// we have on stack all the arguments for the ADVICE call.
		// we have in frame somewhere all the arguments for the non-advice call.

		BcelVar[] adviceVars = munger.getExposedStateAsBcelVars(true);
		IntMap proceedMap = makeProceedArgumentMap(adviceVars);

		// System.out.println(proceedMap + " for " + this);
		// System.out.println(argVarList);

		ResolvedType[] proceedParamTypes = world.resolve(munger.getSignature().getParameterTypes());
		// remove this*JoinPoint* as arguments to proceed
		if (munger.getBaseParameterCount() + 1 < proceedParamTypes.length) {
			int len = munger.getBaseParameterCount() + 1;
			ResolvedType[] newTypes = new ResolvedType[len];
			System.arraycopy(proceedParamTypes, 0, newTypes, 0, len);
			proceedParamTypes = newTypes;
		}

		// System.out.println("stateTypes: " + Arrays.asList(stateTypes));
		BcelVar[] proceedVars = Utility.pushAndReturnArrayOfVars(proceedParamTypes, ret, fact, localAdviceMethod);

		Type[] stateTypes = callbackMethod.getArgumentTypes();
		// System.out.println("stateTypes: " + Arrays.asList(stateTypes));

		for (int i = 0, len = stateTypes.length; i < len; i++) {
			Type stateType = stateTypes[i];
			ResolvedType stateTypeX = BcelWorld.fromBcel(stateType).resolve(world);
			if (proceedMap.hasKey(i)) {
				// throw new RuntimeException("unimplemented");
				proceedVars[proceedMap.get(i)].appendLoadAndConvert(ret, fact, stateTypeX);
			} else {
				argVarList.get(i).appendLoad(ret, fact);
			}
		}

		ret.append(Utility.createInvoke(fact, callbackMethod));
		ret.append(Utility.createConversion(fact, callbackMethod.getReturnType(),
				BcelWorld.makeBcelType(munger.getSignature().getReturnType()), world.isInJava5Mode()));
		return ret;
	}

	// private static boolean bindsThisOrTarget(Pointcut pointcut) {
	// ThisTargetFinder visitor = new ThisTargetFinder();
	// pointcut.accept(visitor, null);
	// return visitor.bindsThisOrTarget;
	// }

	// private static class ThisTargetFinder extends IdentityPointcutVisitor {
	// boolean bindsThisOrTarget = false;
	//
	// public Object visit(ThisOrTargetPointcut node, Object data) {
	// if (node.isBinding()) {
	// bindsThisOrTarget = true;
	// }
	// return node;
	// }
	//
	// public Object visit(AndPointcut node, Object data) {
	// if (!bindsThisOrTarget) node.getLeft().accept(this, data);
	// if (!bindsThisOrTarget) node.getRight().accept(this, data);
	// return node;
	// }
	//
	// public Object visit(NotPointcut node, Object data) {
	// if (!bindsThisOrTarget) node.getNegatedPointcut().accept(this, data);
	// return node;
	// }
	//
	// public Object visit(OrPointcut node, Object data) {
	// if (!bindsThisOrTarget) node.getLeft().accept(this, data);
	// if (!bindsThisOrTarget) node.getRight().accept(this, data);
	// return node;
	// }
	// }

	/**
	 * Annotation style handling for inlining.
	 *
	 * Note: The proceedingjoinpoint is already on the stack (since the user was calling pjp.proceed(...)
	 *
	 * The proceed map is ignored (in terms of argument repositioning) since we have a fixed expected format for annotation style.
	 * The aim here is to change the proceed() call into a call to the xxx_aroundBody0 method.
	 *
	 *
	 */
	private InstructionList getRedoneProceedCallForAnnotationStyle(InstructionFactory fact, LazyMethodGen callbackMethod,
			BcelAdvice munger, LazyMethodGen localAdviceMethod, List<BcelVar> argVarList, boolean isProceedWithArgs) {
		InstructionList ret = new InstructionList();

		// store the Object[] array on stack if proceed with args
		if (isProceedWithArgs) {

			// STORE the Object[] into a local variable
			Type objectArrayType = Type.OBJECT_ARRAY;
			int theObjectArrayLocalNumber = localAdviceMethod.allocateLocal(objectArrayType);
			ret.append(InstructionFactory.createStore(objectArrayType, theObjectArrayLocalNumber));

			// STORE the ProceedingJoinPoint instance into a local variable
			Type proceedingJpType = Type.getType("Lorg/aspectj/lang/ProceedingJoinPoint;");
			int pjpLocalNumber = localAdviceMethod.allocateLocal(proceedingJpType);
			ret.append(InstructionFactory.createStore(proceedingJpType, pjpLocalNumber));

			// Aim here initially is to determine whether the user will have provided a new
			// this/target in the object array and consume them if they have, leaving us the rest of
			// the arguments to process as regular arguments to the invocation at the original join point

			boolean pointcutBindsThis = bindsThis(munger);
			boolean pointcutBindsTarget = bindsTarget(munger);
			boolean targetIsSameAsThis = getKind().isTargetSameAsThis();

			int nextArgumentToProvideForCallback = 0;

			if (hasThis()) {
				if (!(pointcutBindsTarget && targetIsSameAsThis)) {
					if (pointcutBindsThis) {
						// they have supplied new this as first entry in object array, consume it
						ret.append(InstructionFactory.createLoad(objectArrayType, theObjectArrayLocalNumber));
						ret.append(Utility.createConstant(fact, 0));
						ret.append(InstructionFactory.createArrayLoad(Type.OBJECT));
						ret.append(Utility.createConversion(fact, Type.OBJECT, callbackMethod.getArgumentTypes()[0]));
					} else {
						// use local variable 0
						ret.append(InstructionFactory.createALOAD(0));
					}
					nextArgumentToProvideForCallback++;
				}
			}

			if (hasTarget()) {
				if (pointcutBindsTarget) {
					if (getKind().isTargetSameAsThis()) {
						ret.append(InstructionFactory.createLoad(objectArrayType, theObjectArrayLocalNumber));
						ret.append(Utility.createConstant(fact, pointcutBindsThis ? 1 : 0));
						ret.append(InstructionFactory.createArrayLoad(Type.OBJECT));
						ret.append(Utility.createConversion(fact, Type.OBJECT, callbackMethod.getArgumentTypes()[0]));
					} else {
						int position = (hasThis() && pointcutBindsThis)? 1 : 0;
						ret.append(InstructionFactory.createLoad(objectArrayType, theObjectArrayLocalNumber));
						ret.append(Utility.createConstant(fact, position));
						ret.append(InstructionFactory.createArrayLoad(Type.OBJECT));
						ret.append(Utility.createConversion(fact, Type.OBJECT, callbackMethod.getArgumentTypes()[nextArgumentToProvideForCallback]));
					}
					nextArgumentToProvideForCallback++;
				} else {
					if (getKind().isTargetSameAsThis()) {
						// ret.append(new ALOAD(0));
					} else {
						ret.append(InstructionFactory.createLoad(localAdviceMethod.getArgumentTypes()[0], hasThis() ? 1 : 0));
						nextArgumentToProvideForCallback++;
					}
				}
			}

			// Where to start in the object array in order to pick up arguments
			int indexIntoObjectArrayForArguments = (pointcutBindsThis ? 1 : 0) + (pointcutBindsTarget ? 1 : 0);

			int len = callbackMethod.getArgumentTypes().length;
			for (int i = nextArgumentToProvideForCallback; i < len; i++) {
				Type stateType = callbackMethod.getArgumentTypes()[i];
				BcelWorld.fromBcel(stateType).resolve(world);
				if ("Lorg/aspectj/lang/JoinPoint;".equals(stateType.getSignature())) {
					ret.append(new InstructionLV(Constants.ALOAD, pjpLocalNumber));
				} else {
					ret.append(InstructionFactory.createLoad(objectArrayType, theObjectArrayLocalNumber));
					ret.append(Utility
							.createConstant(fact, i - nextArgumentToProvideForCallback + indexIntoObjectArrayForArguments));
					ret.append(InstructionFactory.createArrayLoad(Type.OBJECT));
					ret.append(Utility.createConversion(fact, Type.OBJECT, stateType));
				}
			}

		} else {
			Type proceedingJpType = Type.getType("Lorg/aspectj/lang/ProceedingJoinPoint;");
			int localJp = localAdviceMethod.allocateLocal(proceedingJpType);
			ret.append(InstructionFactory.createStore(proceedingJpType, localJp));

			int idx = 0;
			for (int i = 0, len = callbackMethod.getArgumentTypes().length; i < len; i++) {
				Type stateType = callbackMethod.getArgumentTypes()[i];
				/* ResolvedType stateTypeX = */
				BcelWorld.fromBcel(stateType).resolve(world);
				if ("Lorg/aspectj/lang/JoinPoint;".equals(stateType.getSignature())) {
					ret.append(InstructionFactory.createALOAD(localJp));// from localAdvice signature
					// } else if ("Lorg/aspectj/lang/ProceedingJoinPoint;".equals(stateType.getSignature())) {
					// //FIXME ALEX?
					// ret.append(new ALOAD(localJp));// from localAdvice signature
					// // ret.append(fact.createCheckCast(
					// // (ReferenceType) BcelWorld.makeBcelType(stateTypeX)
					// // ));
					// // cast ?
					//
					idx++;
				} else {
					ret.append(InstructionFactory.createLoad(stateType, idx));
					idx += stateType.getSize();
				}
			}
		}

		// do the callback invoke
		ret.append(Utility.createInvoke(fact, callbackMethod));

		// box it again. Handles cases where around advice does return something else than Object
		if (!UnresolvedType.OBJECT.equals(munger.getSignature().getReturnType())) {
			ret.append(Utility.createConversion(fact, callbackMethod.getReturnType(), Type.OBJECT));
		}
		ret.append(Utility.createConversion(fact, callbackMethod.getReturnType(),
				BcelWorld.makeBcelType(munger.getSignature().getReturnType()), world.isInJava5Mode()));

		return ret;

		//
		//
		//
		// if (proceedMap.hasKey(i)) {
		// ret.append(new ALOAD(i));
		// //throw new RuntimeException("unimplemented");
		// //proceedVars[proceedMap.get(i)].appendLoadAndConvert(ret, fact, stateTypeX);
		// } else {
		// //((BcelVar) argVarList.get(i)).appendLoad(ret, fact);
		// //ret.append(new ALOAD(i));
		// if ("Lorg/aspectj/lang/JoinPoint;".equals(stateType.getSignature())) {
		// ret.append(new ALOAD(i));
		// } else {
		// ret.append(new ALOAD(i));
		// }
		// }
		// }
		//
		// ret.append(Utility.createInvoke(fact, callbackMethod));
		// ret.append(Utility.createConversion(fact, callbackMethod.getReturnType(),
		// BcelWorld.makeBcelType(munger.getSignature().getReturnType())));
		//
		// //ret.append(new ACONST_NULL());//will be POPed
		// if (true) return ret;
		//
		//
		//
		// // we have on stack all the arguments for the ADVICE call.
		// // we have in frame somewhere all the arguments for the non-advice call.
		//
		// BcelVar[] adviceVars = munger.getExposedStateAsBcelVars();
		// IntMap proceedMap = makeProceedArgumentMap(adviceVars);
		//
		// System.out.println(proceedMap + " for " + this);
		// System.out.println(argVarList);
		//
		// ResolvedType[] proceedParamTypes =
		// world.resolve(munger.getSignature().getParameterTypes());
		// // remove this*JoinPoint* as arguments to proceed
		// if (munger.getBaseParameterCount()+1 < proceedParamTypes.length) {
		// int len = munger.getBaseParameterCount()+1;
		// ResolvedType[] newTypes = new ResolvedType[len];
		// System.arraycopy(proceedParamTypes, 0, newTypes, 0, len);
		// proceedParamTypes = newTypes;
		// }
		//
		// //System.out.println("stateTypes: " + Arrays.asList(stateTypes));
		// BcelVar[] proceedVars =
		// Utility.pushAndReturnArrayOfVars(proceedParamTypes, ret, fact, localAdviceMethod);
		//
		// Type[] stateTypes = callbackMethod.getArgumentTypes();
		// // System.out.println("stateTypes: " + Arrays.asList(stateTypes));
		//
		// for (int i=0, len=stateTypes.length; i < len; i++) {
		// Type stateType = stateTypes[i];
		// ResolvedType stateTypeX = BcelWorld.fromBcel(stateType).resolve(world);
		// if (proceedMap.hasKey(i)) {
		// //throw new RuntimeException("unimplemented");
		// proceedVars[proceedMap.get(i)].appendLoadAndConvert(ret, fact, stateTypeX);
		// } else {
		// ((BcelVar) argVarList.get(i)).appendLoad(ret, fact);
		// }
		// }
		//
		// ret.append(Utility.createInvoke(fact, callbackMethod));
		// ret.append(Utility.createConversion(fact, callbackMethod.getReturnType(),
		// BcelWorld.makeBcelType(munger.getSignature().getReturnType())));
		// return ret;
	}

	private boolean bindsThis(BcelAdvice munger) {
		UsesThisVisitor utv = new UsesThisVisitor();
		munger.getPointcut().accept(utv, null);
		return utv.usesThis;
	}

	private boolean bindsTarget(BcelAdvice munger) {
		UsesTargetVisitor utv = new UsesTargetVisitor();
		munger.getPointcut().accept(utv, null);
		return utv.usesTarget;
	}

	private static class UsesThisVisitor extends AbstractPatternNodeVisitor {
		boolean usesThis = false;

		@Override
		public Object visit(ThisOrTargetPointcut node, Object data) {
			if (node.isThis() && node.isBinding()) {
				usesThis = true;
			}
			return node;
		}

		@Override
		public Object visit(AndPointcut node, Object data) {
			if (!usesThis) {
				node.getLeft().accept(this, data);
			}
			if (!usesThis) {
				node.getRight().accept(this, data);
			}
			return node;
		}

		@Override
		public Object visit(NotPointcut node, Object data) {
			if (!usesThis) {
				node.getNegatedPointcut().accept(this, data);
			}
			return node;
		}

		@Override
		public Object visit(OrPointcut node, Object data) {
			if (!usesThis) {
				node.getLeft().accept(this, data);
			}
			if (!usesThis) {
				node.getRight().accept(this, data);
			}
			return node;
		}
	}

	private static class UsesTargetVisitor extends AbstractPatternNodeVisitor {
		boolean usesTarget = false;

		@Override
		public Object visit(ThisOrTargetPointcut node, Object data) {
			if (!node.isThis() && node.isBinding()) {
				usesTarget = true;
			}
			return node;
		}

		@Override
		public Object visit(AndPointcut node, Object data) {
			if (!usesTarget) {
				node.getLeft().accept(this, data);
			}
			if (!usesTarget) {
				node.getRight().accept(this, data);
			}
			return node;
		}

		@Override
		public Object visit(NotPointcut node, Object data) {
			if (!usesTarget) {
				node.getNegatedPointcut().accept(this, data);
			}
			return node;
		}

		@Override
		public Object visit(OrPointcut node, Object data) {
			if (!usesTarget) {
				node.getLeft().accept(this, data);
			}
			if (!usesTarget) {
				node.getRight().accept(this, data);
			}
			return node;
		}
	}

	BcelVar aroundClosureInstance = null;

	public void weaveAroundClosure(BcelAdvice munger, boolean hasDynamicTest) {
		InstructionFactory fact = getFactory();

		enclosingMethod.setCanInline(false);

		int linenumber = getSourceLine();
		// MOVE OUT ALL THE INSTRUCTIONS IN MY SHADOW INTO ANOTHER METHOD!

		// callbackMethod will be something like: "static final void m_aroundBody0(I)"
		boolean shadowClassIsInterface = getEnclosingClass().isInterface();
		LazyMethodGen callbackMethod = extractShadowInstructionsIntoNewMethod(
				NameMangler.aroundShadowMethodName(getSignature(), getEnclosingClass().getNewGeneratedNameTag()), shadowClassIsInterface?Modifier.PUBLIC:0,
				munger.getSourceLocation(), new ArrayList<>(),shadowClassIsInterface);

		BcelVar[] adviceVars = munger.getExposedStateAsBcelVars(true);

		String closureClassName = NameMangler.makeClosureClassName(getEnclosingClass().getType(), getEnclosingClass()
				.getNewGeneratedNameTag());

		Member constructorSig = new MemberImpl(Member.CONSTRUCTOR, UnresolvedType.forName(closureClassName), 0, "<init>",
				"([Ljava/lang/Object;)V");

		BcelVar closureHolder = null;

		// This is not being used currently since getKind() == preinitializaiton
		// cannot happen in around advice
		if (getKind() == PreInitialization) {
			closureHolder = genTempVar(AjcMemberMaker.AROUND_CLOSURE_TYPE);
		}

		InstructionList closureInstantiation = makeClosureInstantiation(constructorSig, closureHolder);

		/* LazyMethodGen constructor = */
		makeClosureClassAndReturnConstructor(closureClassName, callbackMethod, makeProceedArgumentMap(adviceVars));

		InstructionList returnConversionCode;
		if (getKind() == PreInitialization) {
			returnConversionCode = new InstructionList();

			BcelVar stateTempVar = genTempVar(UnresolvedType.OBJECTARRAY);
			closureHolder.appendLoad(returnConversionCode, fact);

			returnConversionCode.append(Utility.createInvoke(fact, world, AjcMemberMaker.aroundClosurePreInitializationGetter()));
			stateTempVar.appendStore(returnConversionCode, fact);

			Type[] stateTypes = getSuperConstructorParameterTypes();

			returnConversionCode.append(InstructionConstants.ALOAD_0); // put "this" back on the stack
			for (int i = 0, len = stateTypes.length; i < len; i++) {
				UnresolvedType bcelTX = BcelWorld.fromBcel(stateTypes[i]);
				ResolvedType stateRTX = world.resolve(bcelTX, true);
				if (stateRTX.isMissing()) {
					world.getLint().cantFindType.signal(
							new String[] { WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_DURING_AROUND_WEAVE_PREINIT,
									bcelTX.getClassName()) }, getSourceLocation(),
							new ISourceLocation[] { munger.getSourceLocation() });
					// IMessage msg = new Message(
					// WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_DURING_AROUND_WEAVE_PREINIT,bcelTX.getClassName()),
					// "",IMessage.ERROR,getSourceLocation(),null,
					// new ISourceLocation[]{ munger.getSourceLocation()});
					// world.getMessageHandler().handleMessage(msg);
				}
				stateTempVar.appendConvertableArrayLoad(returnConversionCode, fact, i, stateRTX);
			}
		} else {
			// pr226201
			Member mungerSignature = munger.getSignature();
			if (munger.getSignature() instanceof ResolvedMember) {
				if (((ResolvedMember) mungerSignature).hasBackingGenericMember()) {
					mungerSignature = ((ResolvedMember) mungerSignature).getBackingGenericMember();
				}
			}
			UnresolvedType returnType = mungerSignature.getReturnType();
			returnConversionCode = Utility.createConversion(getFactory(), BcelWorld.makeBcelType(returnType),
					callbackMethod.getReturnType(), world.isInJava5Mode());
			if (!isFallsThrough()) {
				returnConversionCode.append(InstructionFactory.createReturn(callbackMethod.getReturnType()));
			}
		}

		// initialize the bit flags for this shadow
		int bitflags = 0x000000;
		if (getKind().isTargetSameAsThis()) {
			bitflags |= 0x010000;
		}
		if (hasThis()) {
			bitflags |= 0x001000;
		}
		if (bindsThis(munger)) {
			bitflags |= 0x000100;
		}
		if (hasTarget()) {
			bitflags |= 0x000010;
		}
		if (bindsTarget(munger)) {
			bitflags |= 0x000001;
		}

		closureVarInitialized = false;

		// ATAJ for @AJ aspect we need to link the closure with the joinpoint instance
		if (munger.getConcreteAspect() != null && munger.getConcreteAspect().isAnnotationStyleAspect()
				&& munger.getDeclaringAspect() != null && munger.getDeclaringAspect().resolve(world).isAnnotationStyleAspect()) {

			aroundClosureInstance = genTempVar(AjcMemberMaker.AROUND_CLOSURE_TYPE);
			closureInstantiation.append(fact.createDup(1));
			aroundClosureInstance.appendStore(closureInstantiation, fact);

			// stick the bitflags on the stack and call the variant of linkClosureAndJoinPoint that takes an int
			closureInstantiation.append(fact.createConstant(bitflags));
			if (needAroundClosureStacking) {
				closureInstantiation.append(Utility.createInvoke(getFactory(), getWorld(),
						new MemberImpl(Member.METHOD, UnresolvedType.forName("org.aspectj.runtime.internal.AroundClosure"),
								Modifier.PUBLIC, "linkStackClosureAndJoinPoint", String.format("%s%s", "(I)", "Lorg/aspectj/lang/ProceedingJoinPoint;"))));

			} else {
				closureInstantiation.append(Utility.createInvoke(getFactory(), getWorld(),
						new MemberImpl(Member.METHOD, UnresolvedType.forName("org.aspectj.runtime.internal.AroundClosure"),
								Modifier.PUBLIC, "linkClosureAndJoinPoint", String.format("%s%s", "(I)", "Lorg/aspectj/lang/ProceedingJoinPoint;"))));
			}

		}

		InstructionList advice = new InstructionList();
		advice.append(munger.getAdviceArgSetup(this, null, closureInstantiation));

		// invoke the advice
		InstructionHandle tryUnlinkPosition  = advice.append(munger.getNonTestAdviceInstructions(this));

		if (needAroundClosureStacking) {
			// Call AroundClosure.unlink() in a 'finally' block
			if (munger.getConcreteAspect() != null && munger.getConcreteAspect().isAnnotationStyleAspect()
					&& munger.getDeclaringAspect() != null
					&& munger.getDeclaringAspect().resolve(world).isAnnotationStyleAspect()
					&& closureVarInitialized) {

				// Call unlink when 'normal' flow occurring
				aroundClosureInstance.appendLoad(advice, fact);
				InstructionHandle unlinkInsn = advice.append(Utility.createInvoke(getFactory(), getWorld(), new MemberImpl(Member.METHOD, UnresolvedType
						.forName("org.aspectj.runtime.internal.AroundClosure"), Modifier.PUBLIC, "unlink",
						"()V")));

				BranchHandle jumpOverHandler = advice.append(new InstructionBranch(Constants.GOTO, null));
				// Call unlink in finally block

				// Do not POP the exception off, we need to rethrow it
				InstructionHandle handlerStart = advice.append(aroundClosureInstance.createLoad(fact));
				advice.append(Utility.createInvoke(getFactory(), getWorld(), new MemberImpl(Member.METHOD, UnresolvedType
						.forName("org.aspectj.runtime.internal.AroundClosure"), Modifier.PUBLIC, "unlink",
						"()V")));
				// After that exception is on the top of the stack again
				advice.append(InstructionConstants.ATHROW);
				InstructionHandle jumpTarget = advice.append(InstructionConstants.NOP);
				jumpOverHandler.setTarget(jumpTarget);
				enclosingMethod.addExceptionHandler(tryUnlinkPosition, unlinkInsn, handlerStart, null/* ==finally */, false);
			}
		}

		advice.append(returnConversionCode);
		if (getKind() == Shadow.MethodExecution && linenumber > 0) {
			advice.getStart().addTargeter(new LineNumberTag(linenumber));
		}

		if (!hasDynamicTest) {
			range.append(advice);
		} else {
			InstructionList callback = makeCallToCallback(callbackMethod);
			InstructionList postCallback = new InstructionList();
			if (terminatesWithReturn()) {
				callback.append(InstructionFactory.createReturn(callbackMethod.getReturnType()));
			} else {
				advice.append(InstructionFactory.createBranchInstruction(Constants.GOTO,
						postCallback.append(InstructionConstants.NOP)));
			}
			range.append(munger.getTestInstructions(this, advice.getStart(), callback.getStart(), advice.getStart()));
			range.append(advice);
			range.append(callback);
			range.append(postCallback);
		}
	}

	// exposed for testing
	InstructionList makeCallToCallback(LazyMethodGen callbackMethod) {
		InstructionFactory fact = getFactory();
		InstructionList callback = new InstructionList();
		if (thisVar != null) {
			callback.append(InstructionConstants.ALOAD_0);
		}
		if (targetVar != null && targetVar != thisVar) {
			callback.append(BcelRenderer.renderExpr(fact, world, targetVar));
		}
		callback.append(BcelRenderer.renderExprs(fact, world, argVars));
		// remember to render tjps
		if (thisJoinPointVar != null) {
			callback.append(BcelRenderer.renderExpr(fact, world, thisJoinPointVar));
		}
		callback.append(Utility.createInvoke(fact, callbackMethod));
		return callback;
	}

	/** side-effect-free */
	private InstructionList makeClosureInstantiation(Member constructor, BcelVar holder) {

		// LazyMethodGen constructor) {
		InstructionFactory fact = getFactory();
		BcelVar arrayVar = genTempVar(UnresolvedType.OBJECTARRAY);
		// final Type objectArrayType = new ArrayType(Type.OBJECT, 1);
		final InstructionList il = new InstructionList();
		int alen = getArgCount() + (thisVar == null ? 0 : 1) + ((targetVar != null && targetVar != thisVar) ? 1 : 0)
				+ (thisJoinPointVar == null ? 0 : 1);
		il.append(Utility.createConstant(fact, alen));
		il.append(fact.createNewArray(Type.OBJECT, (short) 1));
		arrayVar.appendStore(il, fact);

		int stateIndex = 0;
		if (thisVar != null) {
			arrayVar.appendConvertableArrayStore(il, fact, stateIndex, thisVar);
			thisVar.setPositionInAroundState(stateIndex);
			stateIndex++;
		}
		if (targetVar != null && targetVar != thisVar) {
			arrayVar.appendConvertableArrayStore(il, fact, stateIndex, targetVar);
			targetVar.setPositionInAroundState(stateIndex);
			stateIndex++;
		}
		for (int i = 0, len = getArgCount(); i < len; i++) {
			arrayVar.appendConvertableArrayStore(il, fact, stateIndex, argVars[i]);
			argVars[i].setPositionInAroundState(stateIndex);
			stateIndex++;
		}
		if (thisJoinPointVar != null) {
			arrayVar.appendConvertableArrayStore(il, fact, stateIndex, thisJoinPointVar);
			thisJoinPointVar.setPositionInAroundState(stateIndex);
			stateIndex++;
		}
		il.append(fact.createNew(new ObjectType(constructor.getDeclaringType().getName())));
		il.append(InstructionConstants.DUP);
		arrayVar.appendLoad(il, fact);
		il.append(Utility.createInvoke(fact, world, constructor));
		if (getKind() == PreInitialization) {
			il.append(InstructionConstants.DUP);
			holder.appendStore(il, fact);
		}
		return il;
	}

	private IntMap makeProceedArgumentMap(BcelVar[] adviceArgs) {
		// System.err.println("coming in with " + Arrays.asList(adviceArgs));

		IntMap ret = new IntMap();
		for (int i = 0, len = adviceArgs.length; i < len; i++) {
			BcelVar v = adviceArgs[i];
			if (v == null) {
				continue; // XXX we don't know why this is required
			}
			int pos = v.getPositionInAroundState();
			if (pos >= 0) { // need this test to avoid args bound via cflow
				ret.put(pos, i);
			}
		}
		// System.err.println("returning " + ret);

		return ret;
	}

	/**
	 *
	 * @param callbackMethod the method we will call back to when our run method gets called.
	 * @param proceedMap A map from state position to proceed argument position. May be non covering on state position.
	 */
	private LazyMethodGen makeClosureClassAndReturnConstructor(String closureClassName, LazyMethodGen callbackMethod,
			IntMap proceedMap) {
		String superClassName = "org.aspectj.runtime.internal.AroundClosure";
		Type objectArrayType = new ArrayType(Type.OBJECT, 1);

		LazyClassGen closureClass = new LazyClassGen(closureClassName, superClassName, getEnclosingClass().getFileName(),
				Modifier.PUBLIC, new String[] {}, getWorld());
		closureClass.setMajorMinor(getEnclosingClass().getMajor(), getEnclosingClass().getMinor());
		InstructionFactory fact = new InstructionFactory(closureClass.getConstantPool());

		// constructor
		LazyMethodGen constructor = new LazyMethodGen(Modifier.PUBLIC, Type.VOID, "<init>", new Type[] { objectArrayType },
				new String[] {}, closureClass);
		InstructionList cbody = constructor.getBody();
		cbody.append(InstructionFactory.createLoad(Type.OBJECT, 0));
		cbody.append(InstructionFactory.createLoad(objectArrayType, 1));
		cbody.append(fact
				.createInvoke(superClassName, "<init>", Type.VOID, new Type[] { objectArrayType }, Constants.INVOKESPECIAL));
		cbody.append(InstructionFactory.createReturn(Type.VOID));

		closureClass.addMethodGen(constructor);

		// Create the 'Object run(Object[])' method
		LazyMethodGen runMethod = new LazyMethodGen(Modifier.PUBLIC, Type.OBJECT, "run", new Type[] { objectArrayType },
				new String[] {}, closureClass);
		InstructionList mbody = runMethod.getBody();
		BcelVar proceedVar = new BcelVar(UnresolvedType.OBJECTARRAY.resolve(world), 1);
		// int proceedVarIndex = 1;
		BcelVar stateVar = new BcelVar(UnresolvedType.OBJECTARRAY.resolve(world), runMethod.allocateLocal(1));
		// int stateVarIndex = runMethod.allocateLocal(1);
		mbody.append(InstructionFactory.createThis());
		mbody.append(fact.createGetField(superClassName, "state", objectArrayType));
		mbody.append(stateVar.createStore(fact));
		// mbody.append(fact.createStore(objectArrayType, stateVarIndex));

		Type[] stateTypes = callbackMethod.getArgumentTypes();

		for (int i = 0, len = stateTypes.length; i < len; i++) {
			ResolvedType resolvedStateType = BcelWorld.fromBcel(stateTypes[i]).resolve(world);
			if (proceedMap.hasKey(i)) {
				mbody.append(proceedVar.createConvertableArrayLoad(fact, proceedMap.get(i), resolvedStateType));
			} else {
				mbody.append(stateVar.createConvertableArrayLoad(fact, i, resolvedStateType));
			}
		}

		mbody.append(Utility.createInvoke(fact, callbackMethod));

		if (getKind() == PreInitialization) {
			mbody.append(Utility.createSet(fact, AjcMemberMaker.aroundClosurePreInitializationField()));
			mbody.append(InstructionConstants.ACONST_NULL);
		} else {
			mbody.append(Utility.createConversion(fact, callbackMethod.getReturnType(), Type.OBJECT));
		}
		mbody.append(InstructionFactory.createReturn(Type.OBJECT));

		closureClass.addMethodGen(runMethod);

		// class
		getEnclosingClass().addGeneratedInner(closureClass);

		return constructor;
	}

	// ---- extraction methods

	/**
	 * Extract the instructions in the shadow to a new method.
	 *
	 * @param extractedMethodName name for the new method
	 * @param extractedMethodVisibilityModifier visibility modifiers for the new method
	 * @param adviceSourceLocation source location of the advice affecting the shadow
	 * @param beingPlacedInInterface is this new method going into an interface
	 */
	LazyMethodGen extractShadowInstructionsIntoNewMethod(String extractedMethodName, int extractedMethodVisibilityModifier,
			ISourceLocation adviceSourceLocation, List<String> parameterNames, boolean beingPlacedInInterface) {
		// LazyMethodGen.assertGoodBody(range.getBody(), extractedMethodName);
		if (!getKind().allowsExtraction()) {
			throw new BCException("Attempt to extract method from a shadow kind (" + getKind()
					+ ") that does not support this operation");
		}
		LazyMethodGen newMethod = createShadowMethodGen(extractedMethodName, extractedMethodVisibilityModifier, parameterNames, beingPlacedInInterface);
		IntMap remapper = makeRemap();
		range.extractInstructionsInto(newMethod, remapper, (getKind() != PreInitialization) && isFallsThrough());
		if (getKind() == PreInitialization) {
			addPreInitializationReturnCode(newMethod, getSuperConstructorParameterTypes());
		}
		getEnclosingClass().addMethodGen(newMethod, adviceSourceLocation);
		return newMethod;
	}

	private void addPreInitializationReturnCode(LazyMethodGen extractedMethod, Type[] superConstructorTypes) {
		InstructionList body = extractedMethod.getBody();
		final InstructionFactory fact = getFactory();

		BcelVar arrayVar = new BcelVar(world.getCoreType(UnresolvedType.OBJECTARRAY), extractedMethod.allocateLocal(1));

		int len = superConstructorTypes.length;

		body.append(Utility.createConstant(fact, len));

		body.append(fact.createNewArray(Type.OBJECT, (short) 1));
		arrayVar.appendStore(body, fact);

		for (int i = len - 1; i >= 0; i++) {
			// convert thing on top of stack to object
			body.append(Utility.createConversion(fact, superConstructorTypes[i], Type.OBJECT));
			// push object array
			arrayVar.appendLoad(body, fact);
			// swap
			body.append(InstructionConstants.SWAP);
			// do object array store.
			body.append(Utility.createConstant(fact, i));
			body.append(InstructionConstants.SWAP);
			body.append(InstructionFactory.createArrayStore(Type.OBJECT));
		}
		arrayVar.appendLoad(body, fact);
		body.append(InstructionConstants.ARETURN);
	}

	private Type[] getSuperConstructorParameterTypes() {
		// assert getKind() == PreInitialization
		InstructionHandle superCallHandle = getRange().getEnd().getNext();
		InvokeInstruction superCallInstruction = (InvokeInstruction) superCallHandle.getInstruction();
		return superCallInstruction.getArgumentTypes(getEnclosingClass().getConstantPool());
	}

	/**
	 * make a map from old frame location to new frame location. Any unkeyed frame location picks out a copied local
	 */
	private IntMap makeRemap() {
		IntMap ret = new IntMap(5);
		int reti = 0;
		if (thisVar != null) {
			ret.put(0, reti++); // thisVar guaranteed to be 0
		}
		if (targetVar != null && targetVar != thisVar) {
			ret.put(targetVar.getSlot(), reti++);
		}
		for (BcelVar argVar : argVars) {
			ret.put(argVar.getSlot(), reti);
			reti += argVar.getType().getSize();
		}
		if (thisJoinPointVar != null) {
			ret.put(thisJoinPointVar.getSlot(), reti++);
		}
		// we not only need to put the arguments, we also need to remap their
		// aliases, which we so helpfully put into temps at the beginning of this join
		// point.
		if (!getKind().argsOnStack()) {
			int oldi = 0;
			int newi = 0;
			// if we're passing in a this and we're not argsOnStack we're always
			// passing in a target too
			if (arg0HoldsThis()) {
				ret.put(0, 0);
				oldi++;
				newi += 1;
			}
			// assert targetVar == thisVar
			for (int i = 0; i < getArgCount(); i++) {
				UnresolvedType type = getArgType(i);
				ret.put(oldi, newi);
				oldi += type.getSize();
				newi += type.getSize();
			}
		}

		// System.err.println("making remap for : " + this);
		// if (targetVar != null) System.err.println("target slot : " + targetVar.getSlot());
		// if (thisVar != null) System.err.println("  this slot : " + thisVar.getSlot());
		// System.err.println(ret);

		return ret;
	}

	/**
	 * The new method always static. It may take some extra arguments: this, target. If it's argsOnStack, then it must take both
	 * this/target If it's argsOnFrame, it shares this and target. ??? rewrite this to do less array munging, please
	 */
	private LazyMethodGen createShadowMethodGen(String newMethodName, int visibilityModifier, List<String> parameterNames, boolean beingPlacedInInterface) {
		Type[] shadowParameterTypes = BcelWorld.makeBcelTypes(getArgTypes());
		int modifiers = (world.useFinal() && !beingPlacedInInterface ? Modifier.FINAL : 0) | Modifier.STATIC | visibilityModifier;
		if (targetVar != null && targetVar != thisVar) {
			UnresolvedType targetType = getTargetType();
			targetType = ensureTargetTypeIsCorrect(targetType);
			// see pr109728,pr229910 - this fixes the case when the declaring class is sometype 'X' but the (gs)etfield
			// in the bytecode refers to a subtype of 'X'. This makes sure we use the type originally
			// mentioned in the fieldget instruction as the method parameter and *not* the type upon which the
			// field is declared because when the instructions are extracted into the new around body,
			// they will still refer to the subtype.
			if ((getKind() == FieldGet || getKind() == FieldSet) && getActualTargetType() != null
					&& !getActualTargetType().equals(targetType.getName())) {
				targetType = UnresolvedType.forName(getActualTargetType()).resolve(world);
			}
			ResolvedMember resolvedMember = getSignature().resolve(world);

			// pr230075, pr197719
			if (resolvedMember != null && Modifier.isProtected(resolvedMember.getModifiers())
					&& !samePackage(resolvedMember.getDeclaringType().getPackageName(), getEnclosingType().getPackageName())
					&& !resolvedMember.getName().equals("clone")) {
				if (!hasThis()) { // pr197719 - static accessor has been created to handle the call
					if (Modifier.isStatic(enclosingMethod.getAccessFlags()) && enclosingMethod.getName().startsWith("access$")) {
						targetType = BcelWorld.fromBcel(enclosingMethod.getArgumentTypes()[0]);
					}
				} else {
					if (!targetType.resolve(world).isAssignableFrom(getThisType().resolve(world))) {
						throw new BCException("bad bytecode");
					}
					targetType = getThisType();
				}
			}
			parameterNames.add("target");
			// There is a 'target' and it is not the same as 'this', so add it to the parameter list
			shadowParameterTypes = addTypeToFront(BcelWorld.makeBcelType(targetType), shadowParameterTypes);
		}

		if (thisVar != null) {
			UnresolvedType thisType = getThisType();
			parameterNames.add(0, "ajc$this");
			shadowParameterTypes = addTypeToFront(BcelWorld.makeBcelType(thisType), shadowParameterTypes);
		}

		if (this.getKind() == Shadow.FieldSet || this.getKind() == Shadow.FieldGet) {
			parameterNames.add(getSignature().getName());
		} else {
			String[] pnames = getSignature().getParameterNames(world);
			if (pnames != null) {
				for (int i = 0; i < pnames.length; i++) {
					if (i == 0 && pnames[i].equals("this")) {
						parameterNames.add("ajc$this");
					} else {
						parameterNames.add(pnames[i]);
					}
				}
			}
		}

		// We always want to pass down thisJoinPoint in case we have already woven
		// some advice in here. If we only have a single piece of around advice on a
		// join point, it is unnecessary to accept (and pass) tjp.
		if (thisJoinPointVar != null) {
			parameterNames.add("thisJoinPoint");
			shadowParameterTypes = addTypeToEnd(LazyClassGen.tjpType, shadowParameterTypes);
		}

		UnresolvedType returnType;
		if (getKind() == PreInitialization) {
			returnType = UnresolvedType.OBJECTARRAY;
		} else {
			if (getKind() == ConstructorCall) {
				returnType = getSignature().getDeclaringType();
			} else if (getKind() == FieldSet) {
				returnType = UnresolvedType.VOID;
			} else {
				returnType = getSignature().getReturnType().resolve(world);
				// returnType = getReturnType(); // for this and above lines, see pr137496
			}
		}
		return new LazyMethodGen(modifiers, BcelWorld.makeBcelType(returnType), newMethodName, shadowParameterTypes,
				NoDeclaredExceptions, getEnclosingClass());
	}

	private boolean samePackage(String p1, String p2) {
		if (p1 == null) {
			return p2 == null;
		}
		if (p2 == null) {
			return false;
		}
		return p1.equals(p2);
	}

	private Type[] addTypeToFront(Type type, Type[] types) {
		int len = types.length;
		Type[] ret = new Type[len + 1];
		ret[0] = type;
		System.arraycopy(types, 0, ret, 1, len);
		return ret;
	}

	private Type[] addTypeToEnd(Type type, Type[] types) {
		int len = types.length;
		Type[] ret = new Type[len + 1];
		ret[len] = type;
		System.arraycopy(types, 0, ret, 0, len);
		return ret;
	}

	public BcelVar genTempVar(UnresolvedType utype) {
		ResolvedType rtype = utype.resolve(world);
		return new BcelVar(rtype, genTempVarIndex(rtype.getSize()));
	}

	// public static final boolean CREATE_TEMP_NAMES = true;

	public BcelVar genTempVar(UnresolvedType typeX, String localName) {
		BcelVar tv = genTempVar(typeX);

		// if (CREATE_TEMP_NAMES) {
		// for (InstructionHandle ih = range.getStart(); ih != range.getEnd(); ih = ih.getNext()) {
		// if (Range.isRangeHandle(ih)) continue;
		// ih.addTargeter(new LocalVariableTag(typeX, localName, tv.getSlot()));
		// }
		// }
		return tv;
	}

	// eh doesn't think we need to garbage collect these (64K is a big number...)
	private int genTempVarIndex(int size) {
		return enclosingMethod.allocateLocal(size);
	}

	public InstructionFactory getFactory() {
		return getEnclosingClass().getFactory();
	}

	@Override
	public ISourceLocation getSourceLocation() {
		int sourceLine = getSourceLine();
		if (sourceLine == 0 || sourceLine == -1) {
			// Thread.currentThread().dumpStack();
			// System.err.println(this + ": " + range);
			return getEnclosingClass().getType().getSourceLocation();
		} else {
			// For staticinitialization, if we have a nice offset, don't build a new source loc
			if (getKind() == Shadow.StaticInitialization && getEnclosingClass().getType().getSourceLocation().getOffset() != 0) {
				return getEnclosingClass().getType().getSourceLocation();
			} else {
				int offset = 0;
				Kind kind = getKind();
				if ((kind == MethodExecution) || (kind == ConstructorExecution) || (kind == AdviceExecution)
						|| (kind == StaticInitialization) || (kind == PreInitialization) || (kind == Initialization)) {
					if (getEnclosingMethod().hasDeclaredLineNumberInfo()) {
						offset = getEnclosingMethod().getDeclarationOffset();
					}
				}
				return getEnclosingClass().getType().getSourceContext().makeSourceLocation(sourceLine, offset);
			}
		}
	}

	public Shadow getEnclosingShadow() {
		return enclosingShadow;
	}

	public LazyMethodGen getEnclosingMethod() {
		return enclosingMethod;
	}

	public boolean isFallsThrough() {
		return !terminatesWithReturn();
	}

	public void setActualTargetType(String className) {
		this.actualInstructionTargetType = className;
	}

	public String getActualTargetType() {
		return actualInstructionTargetType;
	}
}
