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

import org.aspectj.bridge.IMessage;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.BetaException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;

/**
 * args(arguments)
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public class ArgsPointcut extends NameBindingPointcut {
	TypePatternList arguments;
	
	public ArgsPointcut(TypePatternList arguments) {
		this.arguments = arguments;
	}
	
    public FuzzyBoolean fastMatch(ResolvedTypeX type) {
		return FuzzyBoolean.MAYBE;
	}

	public FuzzyBoolean match(Shadow shadow) {
		FuzzyBoolean ret =
			arguments.matches(shadow.getIWorld().resolve(shadow.getArgTypes()), TypePattern.DYNAMIC);
		return ret;
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.ARGS);
		arguments.write(s);
		writeLocation(s);
	}
	
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		ArgsPointcut ret = new ArgsPointcut(TypePatternList.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	
	public boolean equals(Object other) {
		if (!(other instanceof ArgsPointcut)) return false;
		ArgsPointcut o = (ArgsPointcut)other;
		return o.arguments.equals(this.arguments);
	}

    public int hashCode() {
        return arguments.hashCode();
    }
  
	public void resolveBindings(IScope scope, Bindings bindings) {
		arguments.resolveBindings(scope, bindings, true, true);
		if (arguments.ellipsisCount > 1) {
			scope.message(IMessage.ERROR, this,
					"uses more than one .. in args (compiler limitation)");
		}
	}
	
	public void postRead(ResolvedTypeX enclosingType) {
		arguments.postRead(enclosingType);
	}


	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		TypePatternList args = arguments.resolveReferences(bindings);
		if (inAspect.crosscuttingMembers != null) {
			inAspect.crosscuttingMembers.exposeTypes(args.getExactTypes());
		}
		return new ArgsPointcut(args);
	}

	private Test findResidueNoEllipsis(Shadow shadow, ExposedState state, TypePattern[] patterns) {
		int len = shadow.getArgCount();
		//System.err.println("boudn to : " + len + ", " + patterns.length);
		if (patterns.length != len) {
			return Literal.FALSE;
		}
		
		Test ret = Literal.TRUE;
		
		for (int i=0; i < len; i++) {
			TypeX argType = shadow.getArgType(i);
			TypePattern type = patterns[i];
			if (!(type instanceof BindingTypePattern)) {
				if (type.matchesInstanceof(shadow.getIWorld().resolve(argType)).alwaysTrue()) {
					continue;
				}
			}
			ret = Test.makeAnd(ret,
				exposeStateForVar(shadow.getArgVar(i), type, state,shadow.getIWorld()));
		}
		
		return ret;		
	}

	public Test findResidue(Shadow shadow, ExposedState state) {
		if (arguments.matches(shadow.getIWorld().resolve(shadow.getArgTypes()), TypePattern.DYNAMIC).alwaysFalse()) {
			return Literal.FALSE;
		}
		int ellipsisCount = arguments.ellipsisCount;
		if (ellipsisCount == 0) {
			return findResidueNoEllipsis(shadow, state, arguments.getTypePatterns());		
		} else if (ellipsisCount == 1) {
			TypePattern[] patternsWithEllipsis = arguments.getTypePatterns();
			TypePattern[] patternsWithoutEllipsis = new TypePattern[shadow.getArgCount()];
			int lenWithEllipsis = patternsWithEllipsis.length;
			int lenWithoutEllipsis = patternsWithoutEllipsis.length;
			// l1+1 >= l0
			int indexWithEllipsis = 0;
			int indexWithoutEllipsis = 0;
			while (indexWithoutEllipsis < lenWithoutEllipsis) {
				TypePattern p = patternsWithEllipsis[indexWithEllipsis++];
				if (p == TypePattern.ELLIPSIS) {
					int newLenWithoutEllipsis =
						lenWithoutEllipsis - (lenWithEllipsis-indexWithEllipsis);
					while (indexWithoutEllipsis < newLenWithoutEllipsis) {
						patternsWithoutEllipsis[indexWithoutEllipsis++] = TypePattern.ANY;
					}
				} else {
					patternsWithoutEllipsis[indexWithoutEllipsis++] = p;
				}
			}
			return findResidueNoEllipsis(shadow, state, patternsWithoutEllipsis);
		} else {
			throw new BetaException("unimplemented");
		}
	}
	
	public String toString() {
		return "args" + arguments.toString() + "";
	}
}
