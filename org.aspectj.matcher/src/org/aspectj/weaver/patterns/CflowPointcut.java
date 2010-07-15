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
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.IMessage;
import org.aspectj.util.FileUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.CrosscuttingMembers;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Test;

public class CflowPointcut extends Pointcut {
	private final Pointcut entry; // The pointcut inside the cflow() that
	// represents the 'entry' point
	boolean isBelow;// Is this cflowbelow?
	private int[] freeVars;

	/**
	 * Used to indicate that we're in the context of a cflow when concretizing if's
	 * 
	 * Will be removed or replaced with something better when we handle this as a non-error
	 */
	public static final ResolvedPointcutDefinition CFLOW_MARKER = new ResolvedPointcutDefinition(null, 0, null,
			UnresolvedType.NONE, Pointcut.makeMatchesNothing(Pointcut.RESOLVED));

	public CflowPointcut(Pointcut entry, boolean isBelow, int[] freeVars) {
		// System.err.println("Building cflow pointcut "+entry.toString());
		this.entry = entry;
		this.isBelow = isBelow;
		this.freeVars = freeVars;
		pointcutKind = CFLOW;
	}

	/**
	 * @return Returns true is this is a cflowbelow pointcut
	 */
	public boolean isCflowBelow() {
		return isBelow;
	}

	public int couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS_BITS;
	}

	// enh 76055
	public Pointcut getEntry() {
		return entry;
	}

	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return FuzzyBoolean.MAYBE;
	}

	protected FuzzyBoolean matchInternal(Shadow shadow) {
		// ??? this is not maximally efficient
		return FuzzyBoolean.MAYBE;
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Pointcut.CFLOW);
		entry.write(s);
		s.writeBoolean(isBelow);
		FileUtil.writeIntArray(freeVars, s);
		writeLocation(s);
	}

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {

		CflowPointcut ret = new CflowPointcut(Pointcut.read(s, context), s.readBoolean(), FileUtil.readIntArray(s));
		ret.readLocation(context, s);
		return ret;
	}

	public Pointcut parameterizeWith(Map typeVariableMap, World w) {
		CflowPointcut ret = new CflowPointcut(entry.parameterizeWith(typeVariableMap, w), isBelow, freeVars);
		ret.copyLocationFrom(this);
		return ret;
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		if (bindings == null) {
			entry.resolveBindings(scope, null);
			entry.state = RESOLVED;
			freeVars = new int[0];
		} else {
			// ??? for if's sake we might need to be more careful here
			Bindings entryBindings = new Bindings(bindings.size());

			entry.resolveBindings(scope, entryBindings);
			entry.state = RESOLVED;

			freeVars = entryBindings.getUsedFormals();

			bindings.mergeIn(entryBindings, scope);
		}
	}

	public boolean equals(Object other) {
		if (!(other instanceof CflowPointcut)) {
			return false;
		}
		CflowPointcut o = (CflowPointcut) other;
		return o.entry.equals(entry) && o.isBelow == isBelow;
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + entry.hashCode();
		result = 37 * result + (isBelow ? 0 : 1);
		return result;
	}

	public String toString() {
		return "cflow" + (isBelow ? "below" : "") + "(" + entry + ")";
	}

	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		throw new RuntimeException("unimplemented - did concretization fail?");
	}

	public Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {

		// the pointcut is marked as CONCRETE after returning from this
		// call - so we can't skip concretization
		// if (this.entry.state == Pointcut.SYMBOLIC) {
		// // too early to concretize, return unchanged
		// return this;
		// }

		// Enforce rule about which designators are supported in declare
		if (isDeclare(bindings.getEnclosingAdvice())) {
			inAspect.getWorld().showMessage(IMessage.ERROR,
					WeaverMessages.format(WeaverMessages.CFLOW_IN_DECLARE, isBelow ? "below" : ""),
					bindings.getEnclosingAdvice().getSourceLocation(), null);
			return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}

		// make this remap from formal positions to arrayIndices
		IntMap entryBindings = new IntMap();
		if (freeVars != null) {
			for (int i = 0, len = freeVars.length; i < len; i++) {
				int freeVar = freeVars[i];
				// int formalIndex = bindings.get(freeVar);
				entryBindings.put(freeVar, i);
			}
		}
		entryBindings.copyContext(bindings);
		// System.out.println(this + " bindings: " + entryBindings);

		World world = inAspect.getWorld();

		Pointcut concreteEntry;

		ResolvedType concreteAspect = bindings.getConcreteAspect();

		CrosscuttingMembers xcut = concreteAspect.crosscuttingMembers;
		Collection<ShadowMunger> previousCflowEntries = xcut.getCflowEntries();

		entryBindings.pushEnclosingDefinition(CFLOW_MARKER);
		// This block concretizes the pointcut within the cflow pointcut
		try {
			concreteEntry = entry.concretize(inAspect, declaringType, entryBindings);
		} finally {
			entryBindings.popEnclosingDefinitition();
		}

		List<ShadowMunger> innerCflowEntries = new ArrayList<ShadowMunger>(xcut.getCflowEntries());
		innerCflowEntries.removeAll(previousCflowEntries);

		// Four routes of interest through this code (did I hear someone say
		// refactor??)
		// 1) no state in the cflow - we can use a counter *and* we have seen
		// this pointcut
		// before - so use the same counter as before.
		// 2) no state in the cflow - we can use a counter, but this is the
		// first time
		// we have seen this pointcut, so build the infrastructure.
		// 3) state in the cflow - we need to use a stack *and* we have seen
		// this pointcut
		// before - so share the stack.
		// 4) state in the cflow - we need to use a stack, but this is the first
		// time
		// we have seen this pointcut, so build the infrastructure.

		if (freeVars == null || freeVars.length == 0) { // No state, so don't
			// use a stack, use a
			// counter.
			ResolvedMember localCflowField = null;

			Object field = getCflowfield(xcut, concreteEntry, concreteAspect, "counter");

			// Check if we have already got a counter for this cflow pointcut
			if (field != null) {
				localCflowField = (ResolvedMember) field; // Use the one we
				// already have

			} else {

				// Create a counter field in the aspect
				localCflowField = new ResolvedMemberImpl(Member.FIELD, concreteAspect, Modifier.STATIC | Modifier.PUBLIC
						| Modifier.FINAL, NameMangler.cflowCounter(xcut), UnresolvedType.forName(NameMangler.CFLOW_COUNTER_TYPE)
						.getSignature());

				// Create type munger to add field to the aspect
				concreteAspect.crosscuttingMembers.addTypeMunger(world.getWeavingSupport().makeCflowCounterFieldAdder(
						localCflowField));

				// Create shadow munger to push stuff onto the stack
				concreteAspect.crosscuttingMembers.addConcreteShadowMunger(Advice.makeCflowEntry(world, concreteEntry, isBelow,
						localCflowField, freeVars == null ? 0 : freeVars.length, innerCflowEntries, inAspect));

				putCflowfield(xcut, concreteEntry, concreteAspect, localCflowField, "counter"); // Remember
				// it
			}

			Pointcut ret = new ConcreteCflowPointcut(concreteAspect, localCflowField, null, true);
			ret.copyLocationFrom(this);
			return ret;
		} else {

			List slots = new ArrayList();

			for (int i = 0, len = freeVars.length; i < len; i++) {
				int freeVar = freeVars[i];

				// we don't need to keep state that isn't actually exposed to
				// advice
				// ??? this means that we will store some state that we won't
				// actually use, optimize this later
				if (!bindings.hasKey(freeVar)) {
					continue;
				}

				int formalIndex = bindings.get(freeVar);

				// We need to look in the right place for the type of the
				// formal. Suppose the advice looks like this:
				// before(String s): somePointcut(*,s)
				// where the first argument in somePointcut is of type Number
				// for free variable 0 we want to ask the pointcut for the type
				// of its first argument, if we only
				// ask the advice for the type of its first argument then we'll
				// get the wrong type (pr86903)

				ResolvedPointcutDefinition enclosingDef = bindings.peekEnclosingDefinition();
				ResolvedType formalType = null;

				// Is there a useful enclosing pointcut?
				if (enclosingDef != null && enclosingDef.getParameterTypes().length > 0) {
					formalType = enclosingDef.getParameterTypes()[freeVar].resolve(world);
				} else {
					formalType = bindings.getAdviceSignature().getParameterTypes()[formalIndex].resolve(world);
				}

				ConcreteCflowPointcut.Slot slot = new ConcreteCflowPointcut.Slot(formalIndex, formalType, i);
				slots.add(slot);
			}
			ResolvedMember localCflowField = null;
			Object field = getCflowfield(xcut, concreteEntry, concreteAspect, "stack");
			if (field != null) {
				localCflowField = (ResolvedMember) field;
			} else {

				localCflowField = new ResolvedMemberImpl(Member.FIELD, concreteAspect, Modifier.STATIC | Modifier.PUBLIC
						| Modifier.FINAL, NameMangler.cflowStack(xcut), UnresolvedType.forName(NameMangler.CFLOW_STACK_TYPE)
						.getSignature());
				// System.out.println("adding field to: " + inAspect + " field "
				// + cflowField);

				// add field and initializer to inAspect
				// XXX and then that info above needs to be mapped down here to
				// help with
				// XXX getting the exposed state right
				concreteAspect.crosscuttingMembers.addConcreteShadowMunger(Advice.makeCflowEntry(world, concreteEntry, isBelow,
						localCflowField, freeVars.length, innerCflowEntries, inAspect));

				concreteAspect.crosscuttingMembers.addTypeMunger(world.getWeavingSupport()
						.makeCflowStackFieldAdder(localCflowField));
				putCflowfield(xcut, concreteEntry, concreteAspect, localCflowField, "stack");
			}
			Pointcut ret = new ConcreteCflowPointcut(concreteAspect, localCflowField, slots, false);
			ret.copyLocationFrom(this);
			return ret;
		}

	}

	private String getKey(Pointcut p, ResolvedType a, String stackOrCounter) {
		StringBuffer sb = new StringBuffer();
		sb.append(a.getName());
		sb.append("::");
		sb.append(p.toString());
		sb.append("::");
		sb.append(stackOrCounter);
		return sb.toString();
	}

	private Object getCflowfield(CrosscuttingMembers xcut, Pointcut pcutkey, ResolvedType concreteAspect, String stackOrCounter) {
		String key = getKey(pcutkey, concreteAspect, stackOrCounter);
		Object o = null;
		if (isBelow) {
			o = xcut.getCflowBelowFields().get(key);
		} else {
			o = xcut.getCflowFields().get(key);
		}
		// System.err.println("Retrieving for key "+key+" returning "+o);
		return o;
	}

	private void putCflowfield(CrosscuttingMembers xcut, Pointcut pcutkey, ResolvedType concreteAspect, Object o,
			String stackOrCounter) {
		String key = getKey(pcutkey, concreteAspect, stackOrCounter);
		// System.err.println("Storing cflow field for key"+key);
		if (isBelow) {
			xcut.getCflowBelowFields().put(key, o);
		} else {
			xcut.getCflowFields().put(key, o);
		}
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

}
