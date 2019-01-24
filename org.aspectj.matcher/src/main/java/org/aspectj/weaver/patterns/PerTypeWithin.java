/* *******************************************************************
 * Copyright (c) 2005 IBM
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.Map;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.PerTypeWithinTargetTypeMunger;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;

// PTWIMPL Represents a parsed pertypewithin()
public class PerTypeWithin extends PerClause {

	private TypePattern typePattern;

	// Any shadow could be considered within a pertypewithin() type pattern
	private static final int kindSet = Shadow.ALL_SHADOW_KINDS_BITS;

	public TypePattern getTypePattern() {
		return typePattern;
	}

	public PerTypeWithin(TypePattern p) {
		typePattern = p;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public int couldMatchKinds() {
		return kindSet;
	}

	@Override
	public Pointcut parameterizeWith(Map<String,UnresolvedType> typeVariableMap, World w) {
		PerTypeWithin ret = new PerTypeWithin(typePattern.parameterizeWith(typeVariableMap, w));
		ret.copyLocationFrom(this);
		return ret;
	}

	// -----
	@Override
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
		if (typePattern.annotationPattern instanceof AnyAnnotationTypePattern) {
			return isWithinType(info.getType());
		}
		return FuzzyBoolean.MAYBE;
	}

	@Override
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		ResolvedType enclosingType = shadow.getIWorld().resolve(shadow.getEnclosingType(), true);
		if (enclosingType.isMissing()) {
			// PTWIMPL ?? Add a proper message
			IMessage msg = new Message("Cant find type pertypewithin matching...", shadow.getSourceLocation(), true,
					new ISourceLocation[] { getSourceLocation() });
			shadow.getIWorld().getMessageHandler().handleMessage(msg);
		}

		// See pr106554 - we can't put advice calls in an interface when the
		// advice is defined
		// in a pertypewithin aspect - the JPs only exist in the static
		// initializer and can't
		// call the localAspectOf() method.
		if (enclosingType.isInterface()) {
			return FuzzyBoolean.NO;
		}
		if (!(enclosingType.canBeSeenBy(inAspect) || inAspect.isPrivilegedAspect())) {
			return FuzzyBoolean.NO;
		}

		typePattern.resolve(shadow.getIWorld());
		return isWithinType(enclosingType);
	}

	@Override
	public void resolveBindings(IScope scope, Bindings bindings) {
		typePattern = typePattern.resolveBindings(scope, bindings, false, false);
	}

	@Override
	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		// Member ptwField =
		// AjcMemberMaker.perTypeWithinField(shadow.getEnclosingType
		// (),inAspect);

		Expr myInstance = Expr.makeCallExpr(AjcMemberMaker.perTypeWithinLocalAspectOf(shadow.getEnclosingType(), inAspect/*
																														 * shadow.
																														 * getEnclosingType
																														 * ( )
																														 */),
				Expr.NONE, inAspect);
		state.setAspectInstance(myInstance);

		// this worked at one point
		// Expr myInstance =
		// Expr.makeFieldGet(ptwField,shadow.getEnclosingType()
		// .resolve(shadow.getIWorld()));//inAspect);
		// state.setAspectInstance(myInstance);

		// return Test.makeFieldGetCall(ptwField,null,Expr.NONE);
		// cflowField, cflowCounterIsValidMethod, Expr.NONE

		// This is what is in the perObject variant of this ...
		// Expr myInstance =
		// Expr.makeCallExpr(AjcMemberMaker.perTypeWithinAspectOfMethod(inAspect)
		// ,
		// new Expr[] {getVar(shadow)}, inAspect);
		// state.setAspectInstance(myInstance);
		// return
		// Test.makeCall(AjcMemberMaker.perTypeWithinHasAspectMethod(inAspect),
		// new Expr[] { getVar(shadow) });
		//    	

		return match(shadow).alwaysTrue() ? Literal.TRUE : Literal.FALSE;
	}

	@Override
	public PerClause concretize(ResolvedType inAspect) {
		PerTypeWithin ret = new PerTypeWithin(typePattern);
		ret.copyLocationFrom(this);
		ret.inAspect = inAspect;
		if (inAspect.isAbstract()) {
			return ret;
		}

		World world = inAspect.getWorld();

		SignaturePattern sigpat = new SignaturePattern(Member.STATIC_INITIALIZATION, ModifiersPattern.ANY, TypePattern.ANY,
				TypePattern.ANY,// typePattern,
				NamePattern.ANY, TypePatternList.ANY, ThrowsPattern.ANY, AnnotationTypePattern.ANY);

		Pointcut staticInitStar = new KindedPointcut(Shadow.StaticInitialization, sigpat);
		Pointcut withinTp = new WithinPointcut(typePattern);
		Pointcut andPcut = new AndPointcut(staticInitStar, withinTp);
		// We want the pointcut to be:
		// 'staticinitialization(*) && within(<typepattern>)' -
		// we *cannot* shortcut this to staticinitialization(<typepattern>)
		// because it doesnt mean the same thing.

		// This munger will initialize the aspect instance field in the matched type

		inAspect.crosscuttingMembers.addConcreteShadowMunger(Advice.makePerTypeWithinEntry(world, andPcut, inAspect));

		ResolvedTypeMunger munger = new PerTypeWithinTargetTypeMunger(inAspect, ret);
		inAspect.crosscuttingMembers.addTypeMunger(world.getWeavingSupport().concreteTypeMunger(munger, inAspect));

		// ATAJ: add a munger to add the aspectOf(..) to the @AJ aspects
		if (inAspect.isAnnotationStyleAspect() && !inAspect.isAbstract()) {
			inAspect.crosscuttingMembers.addLateTypeMunger(world.getWeavingSupport().makePerClauseAspect(inAspect, getKind()));
		}

		// ATAJ inline around advice support - don't use a late munger to allow
		// around inling for itself
		if (inAspect.isAnnotationStyleAspect() && !world.isXnoInline()) {
			inAspect.crosscuttingMembers.addTypeMunger(world.getWeavingSupport().createAccessForInlineMunger(inAspect));
		}

		return ret;

	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		PERTYPEWITHIN.write(s);
		typePattern.write(s);
		writeLocation(s);
	}

	public static PerClause readPerClause(VersionedDataInputStream s, ISourceContext context) throws IOException {
		PerClause ret = new PerTypeWithin(TypePattern.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	@Override
	public PerClause.Kind getKind() {
		return PERTYPEWITHIN;
	}

	@Override
	public String toString() {
		return "pertypewithin(" + typePattern + ")";
	}

	@Override
	public String toDeclarationString() {
		return toString();
	}

	private FuzzyBoolean isWithinType(ResolvedType type) {
		while (type != null) {
			if (typePattern.matchesStatically(type)) {
				return FuzzyBoolean.YES;
			}
			type = type.getDeclaringType();
		}
		return FuzzyBoolean.NO;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof PerTypeWithin)) {
			return false;
		}
		PerTypeWithin pc = (PerTypeWithin) other;
		return ((pc.inAspect == null) ? (inAspect == null) : pc.inAspect.equals(inAspect))
				&& ((pc.typePattern == null) ? (typePattern == null) : pc.typePattern.equals(typePattern));
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + ((inAspect == null) ? 0 : inAspect.hashCode());
		result = 37 * result + ((typePattern == null) ? 0 : typePattern.hashCode());
		return result;
	}

}
