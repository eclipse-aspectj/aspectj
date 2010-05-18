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
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Test;

public class NotPointcut extends Pointcut {
	private Pointcut body;

	public NotPointcut(Pointcut negated) {
		super();
		this.body = negated;
		this.pointcutKind = NOT;
		setLocation(negated.getSourceContext(), negated.getStart(), negated.getEnd()); // should that be at least start-1?
	}

	public NotPointcut(Pointcut pointcut, int startPos) {
		this(pointcut);
		setLocation(pointcut.getSourceContext(), startPos, pointcut.getEnd());
	}

	public int couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS_BITS;
	}

	public Pointcut getNegatedPointcut() {
		return body;
	}

	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return body.fastMatch(type).not();
	}

	protected FuzzyBoolean matchInternal(Shadow shadow) {
		return body.match(shadow).not();
	}

	public String toString() {
		return "!" + body.toString();

	}

	public boolean equals(Object other) {
		if (!(other instanceof NotPointcut)) {
			return false;
		}
		NotPointcut o = (NotPointcut) other;
		return o.body.equals(body);
	}

	public int hashCode() {
		return 37 * 23 + body.hashCode();
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		// Bindings old = bindings.copy();

		// Bindings newBindings = new Bindings(bindings.size());

		body.resolveBindings(scope, null);

		// newBindings.checkEmpty(scope, "negation does not allow binding");
		// bindings.checkEquals(old, scope);

	}

	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Pointcut.NOT);
		body.write(s);
		writeLocation(s);
	}

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		NotPointcut ret = new NotPointcut(Pointcut.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		return Test.makeNot(body.findResidue(shadow, state));
	}

	public Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
		Pointcut ret = new NotPointcut(body.concretize(inAspect, declaringType, bindings));
		ret.copyLocationFrom(this);
		return ret;
	}

	public Pointcut parameterizeWith(Map typeVariableMap, World w) {
		Pointcut ret = new NotPointcut(body.parameterizeWith(typeVariableMap, w));
		ret.copyLocationFrom(this);
		return ret;
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public Object traverse(PatternNodeVisitor visitor, Object data) {
		Object ret = accept(visitor, data);
		this.body.traverse(visitor, ret);
		return ret;
	}

}
