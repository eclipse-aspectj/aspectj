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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

/**
 * Corresponds to target or this pcd.
 * 
 * <p>
 * type is initially a WildTypePattern. If it stays that way, it's a this(Foo) type deal. however, the resolveBindings method may
 * convert it to a BindingTypePattern, in which case, it's a this(foo) type deal.
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public class ThisOrTargetPointcut extends NameBindingPointcut {
	private boolean isThis;
	private TypePattern typePattern;
	private String declarationText;

	private static final int thisKindSet;
	private static final int targetKindSet;

	static {
		int thisFlags = Shadow.ALL_SHADOW_KINDS_BITS;
		int targFlags = Shadow.ALL_SHADOW_KINDS_BITS;
		for (int i = 0; i < Shadow.SHADOW_KINDS.length; i++) {
			Shadow.Kind kind = Shadow.SHADOW_KINDS[i];
			if (kind.neverHasThis()) {
				thisFlags -= kind.bit;
			}
			if (kind.neverHasTarget()) {
				targFlags -= kind.bit;
			}
		}
		thisKindSet = thisFlags;
		targetKindSet = targFlags;
	}

	public boolean isBinding() {
		return (typePattern instanceof BindingTypePattern);
	}

	public ThisOrTargetPointcut(boolean isThis, TypePattern type) {
		this.isThis = isThis;
		this.typePattern = type;
		this.pointcutKind = THIS_OR_TARGET;
		this.declarationText = (isThis ? "this(" : "target(") + type + ")";
	}

	public TypePattern getType() {
		return typePattern;
	}

	public boolean isThis() {
		return isThis;
	}

	@Override
	public Pointcut parameterizeWith(Map<String,UnresolvedType> typeVariableMap, World w) {
		ThisOrTargetPointcut ret = new ThisOrTargetPointcut(isThis, typePattern.parameterizeWith(typeVariableMap, w));
		ret.copyLocationFrom(this);
		return ret;
	}

	@Override
	public int couldMatchKinds() {
		return isThis ? thisKindSet : targetKindSet;
	}

	@Override
	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return FuzzyBoolean.MAYBE;
	}

	private boolean couldMatch(Shadow shadow) {
		return isThis ? shadow.hasThis() : shadow.hasTarget();
	}

	@Override
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		if (!couldMatch(shadow)) {
			return FuzzyBoolean.NO;
		}
		UnresolvedType typeToMatch = isThis ? shadow.getThisType() : shadow.getTargetType();
		// optimization for case of this(Object) or target(Object)
		// works for an ExactTypePattern (and we know there are no annotations to match here of course)
		if (typePattern.getExactType().equals(ResolvedType.OBJECT)) {
			return FuzzyBoolean.YES;
		}
		return typePattern.matches(typeToMatch.resolve(shadow.getIWorld()), TypePattern.DYNAMIC);
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Pointcut.THIS_OR_TARGET);
		s.writeBoolean(isThis);
		typePattern.write(s);
		writeLocation(s);
	}

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		boolean isThis = s.readBoolean();
		TypePattern type = TypePattern.read(s, context);
		ThisOrTargetPointcut ret = new ThisOrTargetPointcut(isThis, type);
		ret.readLocation(context, s);
		return ret;
	}

	@Override
	public void resolveBindings(IScope scope, Bindings bindings) {
		typePattern = typePattern.resolveBindings(scope, bindings, true, true);

		// look for parameterized type patterns which are not supported...
		HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor visitor = new HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor();
		typePattern.traverse(visitor, null);
		if (visitor.wellHasItThen/* ? */()) {
			scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.THIS_AND_TARGET_DONT_SUPPORT_PARAMETERS),
					getSourceLocation()));
		}
		// ??? handle non-formal
	}

	@Override
	public void postRead(ResolvedType enclosingType) {
		typePattern.postRead(enclosingType);
	}

	@Override
	public List<BindingPattern> getBindingAnnotationTypePatterns() {
		return Collections.emptyList();
	}

	@Override
	public List<BindingTypePattern> getBindingTypePatterns() {
		if (typePattern instanceof BindingTypePattern) {
			List<BindingTypePattern> l = new ArrayList<>();
			l.add((BindingTypePattern)typePattern);
			return l;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ThisOrTargetPointcut)) {
			return false;
		}
		ThisOrTargetPointcut o = (ThisOrTargetPointcut) other;
		return o.isThis == this.isThis && o.typePattern.equals(this.typePattern);
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + (isThis ? 0 : 1);
		result = 37 * result + typePattern.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return declarationText;
	}

	/**
	 * Residue is the remainder of the pointcut match that couldn't be performed with the purely static information at compile time
	 * and this method returns the residue of a pointcut at a particular shadow.
	 */
	@Override
	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		if (!couldMatch(shadow)) {
			return Literal.FALSE;
		}

		// if no preference is specified, just say TRUE which means no residue
		if (typePattern == TypePattern.ANY) {
			return Literal.TRUE;
		}

		Var var = isThis ? shadow.getThisVar() : shadow.getTargetVar();

		return exposeStateForVar(var, typePattern, state, shadow.getIWorld());
	}

	@Override
	public Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
		if (isDeclare(bindings.getEnclosingAdvice())) {
			// Enforce rule about which designators are supported in declare
			inAspect.getWorld().showMessage(IMessage.ERROR,
					WeaverMessages.format(WeaverMessages.THIS_OR_TARGET_IN_DECLARE, isThis ? "this" : "target"),
					bindings.getEnclosingAdvice().getSourceLocation(), null);
			return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}

		TypePattern newType = typePattern.remapAdviceFormals(bindings);
		if (inAspect.crosscuttingMembers != null) {
			inAspect.crosscuttingMembers.exposeType(newType.getExactType());
		}

		Pointcut ret = new ThisOrTargetPointcut(isThis, newType);
		ret.copyLocationFrom(this);
		return ret;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
