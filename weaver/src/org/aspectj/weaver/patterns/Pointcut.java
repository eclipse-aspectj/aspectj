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

import org.apache.bcel.classfile.JavaClass;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.util.TypeSafeEnum;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;

/**
 * The lifecycle of Pointcuts is modeled by Pointcut.State.   It has three things:
 * 
 * <p>Creation -- SYMBOLIC -- then resolve(IScope) -- RESOLVED -- concretize(...) -- CONCRETE
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public abstract class Pointcut extends PatternNode {
	public static final class State extends TypeSafeEnum {
		public State(String name, int key) {
			super(name, key);
		}
	}
	
	public static final State SYMBOLIC = new State("symbolic", 0);
	public static final State RESOLVED = new State("resolved", 1);
	public static final State CONCRETE = new State("concrete", 2);


	public State state;

	/**
	 * Constructor for Pattern.
	 */
	public Pointcut() {
		super();
		this.state = SYMBOLIC;
	}
	

	/**
	 * Could I match any shadows in the code defined within this type?
	 */
	public abstract FuzzyBoolean fastMatch(ResolvedTypeX type);
	
	/**
	 * Do I really match this shadow?
	 * XXX implementors need to handle state
	 */
	public abstract FuzzyBoolean match(Shadow shadow);


	public static final byte KINDED = 1;
	public static final byte WITHIN = 2;
	public static final byte THIS_OR_TARGET = 3;
	public static final byte ARGS = 4;
	public static final byte AND = 5;
	public static final byte OR = 6;
	public static final byte NOT = 7;
	public static final byte REFERENCE = 8;
	public static final byte IF = 9;
	public static final byte CFLOW = 10;
	public static final byte WITHINCODE = 12;
	public static final byte HANDLER = 13;
	
	public static final byte NONE = 20;


	// internal, only called from resolve
	protected abstract void resolveBindings(IScope scope, Bindings bindings);
	
    /**
     * Returns this pointcut mutated
     */
    public Pointcut resolve(IScope scope) {
    	assertState(SYMBOLIC);
    	Bindings bindingTable = new Bindings(scope.getFormalCount());
        this.resolveBindings(scope, bindingTable);
        bindingTable.checkAllBound(scope);
        this.state = RESOLVED;
        return this;  	
    }
	
	/**
	 * Returns a new pointcut
	 */
    public Pointcut concretize(ResolvedTypeX inAspect, int arity) {
        return concretize(inAspect, IntMap.idMap(arity));
    }
	
	
	//XXX this is the signature we're moving to
	public Pointcut concretize(ResolvedTypeX inAspect, int arity, ShadowMunger advice) {
		//if (state == CONCRETE) return this; //???
		IntMap map = IntMap.idMap(arity);
		map.setEnclosingAdvice(advice);
		map.setConcreteAspect(inAspect);
		return concretize(inAspect, map);
	}
	
	
	
	public Pointcut concretize(ResolvedTypeX inAspect, IntMap bindings) {
		assertState(RESOLVED);
		Pointcut ret = this.concretize1(inAspect, bindings);
		ret.state = CONCRETE;
		return ret;
	}
	
	
	/**
	 * Resolves and removes ReferencePointcuts, replacing with basic ones
	 * 
	 * @param inAspect the aspect to resolve relative to
	 * @param bindings a Map from formal index in the current lexical context
	 *                               -> formal index in the concrete advice that will run
	 * 
	 * This must always return a new Pointcut object (even if the concretized
	 * Pointcut is identical to the resolved one).  That behavior is
	 * assumed in many places.
	 * XXX fix implementors to handle state
	 */
	protected abstract Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings);
	
	
	//XXX implementors need to handle state
	/**
	 * This can be called from NotPointcut even for Pointcuts that
	 * don't match the shadow
	 */
	public abstract Test findResidue(Shadow shadow, ExposedState state);

	//XXX we're not sure whether or not this is needed
	//XXX currently it's unused  we're keeping it around as a stub
	public void postRead(ResolvedTypeX enclosingType) {}
	
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		byte kind = s.readByte();
		Pointcut ret;
		
		switch(kind) {
			case KINDED: ret = KindedPointcut.read(s, context); break;
			case WITHIN: ret = WithinPointcut.read(s, context); break;
			case THIS_OR_TARGET: ret = ThisOrTargetPointcut.read(s, context); break;
			case ARGS: ret = ArgsPointcut.read(s, context); break;
			case AND: ret = AndPointcut.read(s, context); break;
			case OR: ret = OrPointcut.read(s, context); break;
			case NOT: ret = NotPointcut.read(s, context); break;
			case REFERENCE: ret = ReferencePointcut.read(s, context); break;
			case IF: ret = IfPointcut.read(s, context); break;
			case CFLOW: ret = CflowPointcut.read(s, context); break;
			case WITHINCODE: ret = WithincodePointcut.read(s, context); break;
			case HANDLER: ret = HandlerPointcut.read(s, context); break;
			
			case NONE: ret = makeMatchesNothing(RESOLVED); break;
			default:
				throw new BCException("unknown kind: " + kind);
		}
		ret.state = RESOLVED;
		return ret;
	}
	

	//public void prepare(Shadow shadow) {}
    
    // ---- test method
    
    public static Pointcut fromString(String str) {
        PatternParser parser = new PatternParser(str);
        return parser.parsePointcut();
    }
    
    private static class MatchesNothingPointcut extends Pointcut {
    	public Test findResidue(Shadow shadow, ExposedState state) {
			return Literal.FALSE; // can only get here if an earlier error occurred
		}

		public FuzzyBoolean fastMatch(ResolvedTypeX type) {
			return FuzzyBoolean.NO;
		}
		
		public FuzzyBoolean match(Shadow shadow) {
			return FuzzyBoolean.NO;
		}

		public void resolveBindings(IScope scope, Bindings bindings) {
		}
	
		public void postRead(ResolvedTypeX enclosingType) {
		}

		public Pointcut concretize1(
			ResolvedTypeX inAspect,
			IntMap bindings) {
			return makeMatchesNothing(state);
		}


		public void write(DataOutputStream s) throws IOException {
			s.writeByte(NONE);
		}
		
		public String toString() { return ""; }
	}
    
    //public static Pointcut MatchesNothing = new MatchesNothingPointcut();
    //??? there could possibly be some good optimizations to be done at this point
    public static Pointcut makeMatchesNothing(State state) {
    	Pointcut ret = new MatchesNothingPointcut();
    	ret.state = state;
    	return ret;
    }
    
	public void assertState(State state) {
		if (this.state != state) {
			throw new BCException("expected state: " + state + " got: " + this.state);
		}
	}


}
