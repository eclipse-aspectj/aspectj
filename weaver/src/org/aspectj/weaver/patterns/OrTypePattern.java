/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;

/**
 * left || right
 * 
 * <p>any binding to formals is explicitly forbidden for any composite by the language
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public class OrTypePattern extends TypePattern {
	private TypePattern left, right;
	
	public OrTypePattern(TypePattern left, TypePattern right) {
		super(false);  //??? we override all methods that care about includeSubtypes
		this.left = left;
		this.right = right;
		setLocation(left.getSourceContext(), left.getStart(), right.getEnd());
	}

	public FuzzyBoolean matchesInstanceof(ResolvedTypeX type) {
		return left.matchesInstanceof(type).or(right.matchesInstanceof(type));
	}

	protected boolean matchesExactly(ResolvedTypeX type) {
		//??? if these had side-effects, this sort-circuit could be a mistake
		return left.matchesExactly(type) || right.matchesExactly(type);
	}
	
	public boolean matchesStatically(ResolvedTypeX type) {
		return left.matchesStatically(type) || right.matchesStatically(type);
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(TypePattern.OR);
		left.write(s);
		right.write(s);
		writeLocation(s);
	}
	
	public static TypePattern read(DataInputStream s, ISourceContext context) throws IOException {
		TypePattern ret = new OrTypePattern(TypePattern.read(s, context), TypePattern.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	public TypePattern resolveBindings(
		IScope scope,
		Bindings bindings,
		boolean allowBinding, boolean requireExactType)
	{
		if (requireExactType) return notExactType(scope);
		left = left.resolveBindings(scope, bindings, false, false);
		right = right.resolveBindings(scope, bindings, false, false);
		return this;
	}
	
	public String toString() {
		return "(" + left.toString() + " || " + right.toString() + ")";
	}

}
