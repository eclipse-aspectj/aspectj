/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.UnresolvedType;


public class OrPointcut extends Pointcut {
	Pointcut left, right;
	private int couldMatchKinds;

	public OrPointcut(Pointcut left, Pointcut right) {
		super();
		this.left = left;
		this.right = right;
		setLocation(left.getSourceContext(), left.getStart(), right.getEnd());
		this.pointcutKind = OR;
		this.couldMatchKinds = left.couldMatchKinds() | right.couldMatchKinds();
	}

	public int couldMatchKinds() {
		return couldMatchKinds;
	}

	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		FuzzyBoolean leftMatch = left.fastMatch(type);
		if (leftMatch.alwaysTrue()) {
			return leftMatch;
		}
		return leftMatch.or(right.fastMatch(type));
	}

	protected FuzzyBoolean matchInternal(Shadow shadow) {
		FuzzyBoolean leftMatch = left.match(shadow);
		if (leftMatch.alwaysTrue()) {
			return leftMatch;
		}
		return leftMatch.or(right.match(shadow));
	}

	public String toString() {
		return "(" + left.toString() + " || " + right.toString() + ")";
	}

	public boolean equals(Object other) {
		if (!(other instanceof OrPointcut)) {
			return false;
		}
		OrPointcut o = (OrPointcut) other;
		return o.left.equals(left) && o.right.equals(right);
	}

	public int hashCode() {
		int result = 31;
		result = 37 * result + left.hashCode();
		result = 37 * result + right.hashCode();
		return result;
	}

	/**
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindings(IScope, Bindings)
	 */
	public void resolveBindings(IScope scope, Bindings bindings) {
		Bindings old = bindings == null ? null : bindings.copy();

		left.resolveBindings(scope, bindings);
		right.resolveBindings(scope, old);
		if (bindings != null) {
			bindings.checkEquals(old, scope);
		}

	}

	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Pointcut.OR);
		left.write(s);
		right.write(s);
		writeLocation(s);
	}

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		OrPointcut ret = new OrPointcut(Pointcut.read(s, context), Pointcut.read(s, context));
		ret.readLocation(context, s);
		return ret;

	}

	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		return Test.makeOr(left.findResidue(shadow, state), right.findResidue(shadow, state));
	}

	public Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
		Pointcut ret = new OrPointcut(left.concretize(inAspect, declaringType, bindings), right.concretize(inAspect, declaringType,
				bindings));
		ret.copyLocationFrom(this);
		return ret;
	}

	@Override
	public Pointcut parameterizeWith(Map<String,UnresolvedType> typeVariableMap, World w) {
		Pointcut ret = new OrPointcut(left.parameterizeWith(typeVariableMap, w), right.parameterizeWith(typeVariableMap, w));
		ret.copyLocationFrom(this);
		return ret;
	}

	public Pointcut getLeft() {
		return left;
	}

	public Pointcut getRight() {
		return right;
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public Object traverse(PatternNodeVisitor visitor, Object data) {
		Object ret = accept(visitor, data);
		if (this.left != null)
			this.left.traverse(visitor, ret);
		if (this.right != null)
			this.right.traverse(visitor, ret);
		return ret;
	}
}
