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
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;

public class PerSingleton extends PerClause {

	private ResolvedMember perSingletonAspectOfMethod;

	public PerSingleton() {
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public int couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS_BITS;
	}

	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return FuzzyBoolean.YES;
	}

	protected FuzzyBoolean matchInternal(Shadow shadow) {
		return FuzzyBoolean.YES;
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		// this method intentionally left blank
	}

	public Pointcut parameterizeWith(Map<String,UnresolvedType> typeVariableMap, World w) {
		return this;
	}

	public Test findResidueInternal(Shadow shadow, ExposedState state) {
		// TODO: the commented code is for slow Aspects.aspectOf() style - keep
		// or remove
		//
		// Expr myInstance =
		// Expr.makeCallExpr(AjcMemberMaker.perSingletonAspectOfMethod(inAspect),
		// Expr.NONE, inAspect);
		//
		// state.setAspectInstance(myInstance);
		//
		// // we have no test
		// // a NoAspectBoundException will be thrown if we need an instance of
		// this
		// // aspect before we are bound
		// return Literal.TRUE;
		// if (!Ajc5MemberMaker.isSlowAspect(inAspect)) {
		if (perSingletonAspectOfMethod == null) {
			// Build this just once
			perSingletonAspectOfMethod = AjcMemberMaker.perSingletonAspectOfMethod(inAspect);
		}
		Expr myInstance = Expr.makeCallExpr(perSingletonAspectOfMethod, Expr.NONE, inAspect);

		state.setAspectInstance(myInstance);

		// we have no test
		// a NoAspectBoundException will be thrown if we need an instance of
		// this
		// aspect before we are bound
		return Literal.TRUE;
		// } else {
		// CallExpr callAspectOf =Expr.makeCallExpr(
		// Ajc5MemberMaker.perSingletonAspectOfMethod(inAspect),
		// new Expr[]{
		// Expr.makeStringConstantExpr(inAspect.getName(), inAspect),
		// //FieldGet is using ResolvedType and I don't need that here
		// new FieldGetOn(Member.ajClassField, shadow.getEnclosingType())
		// },
		// inAspect
		// );
		// Expr castedCallAspectOf = new CastExpr(callAspectOf,
		// inAspect.getName());
		// state.setAspectInstance(castedCallAspectOf);
		// return Literal.TRUE;
		// }
	}

	public PerClause concretize(ResolvedType inAspect) {
		PerSingleton ret = new PerSingleton();

		ret.copyLocationFrom(this);

		World world = inAspect.getWorld();

		ret.inAspect = inAspect;

		// ATAJ: add a munger to add the aspectOf(..) to the @AJ aspects
		if (inAspect.isAnnotationStyleAspect() && !inAspect.isAbstract()) {
			// TODO will those change be ok if we add a serializable aspect ?
			// dig:
			// "can't be Serializable/Cloneable unless -XserializableAspects"
			if (getKind() == SINGLETON) { // pr149560
				inAspect.crosscuttingMembers.addTypeMunger(world.getWeavingSupport().makePerClauseAspect(inAspect, getKind()));
			} else {
				inAspect.crosscuttingMembers.addLateTypeMunger(world.getWeavingSupport().makePerClauseAspect(inAspect, getKind()));
			}
		}

		// ATAJ inline around advice support
		if (inAspect.isAnnotationStyleAspect() && !inAspect.getWorld().isXnoInline()) {
			inAspect.crosscuttingMembers.addTypeMunger(world.getWeavingSupport().createAccessForInlineMunger(inAspect));
		}

		return ret;
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		SINGLETON.write(s);
		writeLocation(s);
	}

	public static PerClause readPerClause(VersionedDataInputStream s, ISourceContext context) throws IOException {
		PerSingleton ret = new PerSingleton();
		ret.readLocation(context, s);
		return ret;
	}

	public PerClause.Kind getKind() {
		return SINGLETON;
	}

	public String toString() {
		return "persingleton(" + inAspect + ")";
	}

	public String toDeclarationString() {
		return "";
	}

	public boolean equals(Object other) {
		if (!(other instanceof PerSingleton)) {
			return false;
		}
		PerSingleton pc = (PerSingleton) other;
		return ((pc.inAspect == null) ? (inAspect == null) : pc.inAspect.equals(inAspect));
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + ((inAspect == null) ? 0 : inAspect.hashCode());
		return result;
	}

}
