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
import java.lang.reflect.Member;
import java.util.Collections;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.util.TypeSafeEnum;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.Checker;
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
 * 
 * A day in the life of a pointcut.... - AMC.
 * ==========================================
 * 
 * Pointcuts are created by the PatternParser, which is called by ajdt to
 * parse a pointcut from the PseudoTokens AST node (which in turn are part
 * of a PointcutDesignator AST node). 
 * 
 * Pointcuts are resolved by ajdt when an AdviceDeclaration or a 
 * PointcutDeclaration has its statements resolved. This happens as
 * part of completeTypeBindings in the AjLookupEnvironment which is
 * called after the diet parse phase of the compiler. Named pointcuts,
 * and references to named pointcuts are instances of ReferencePointcut. 
 * 
 * At the end of the compilation process, the pointcuts are serialized
 * (write method) into attributes in the class file.
 * 
 * When the weaver loads the class files, it unpacks the attributes
 * and deserializes the pointcuts (read). All aspects are added to the
 * world, by calling addOrReplaceAspect on
 * the crosscutting members set of the world. When aspects are added or
 * replaced, the crosscutting members in the aspect are extracted as
 * ShadowMungers (each holding a pointcut). The ShadowMungers are
 * concretized, which concretizes the pointcuts. At this stage
 * ReferencePointcuts are replaced by their declared content.
 * 
 * During weaving, the weaver processes type by type. It first culls
 * potentially matching ShadowMungers by calling the fastMatch method
 * on their pointcuts. Only those that might match make it through to
 * the next phase. At the next phase, all of the shadows within the
 * type are created and passed to the pointcut for matching (match). 
 * 
 * When the actual munging happens, matched pointcuts are asked for
 * their residue (findResidue) - the runtime test if any. Because of
 * negation, findResidue may be called on pointcuts that could never
 * match the shadow.
 * 
 */
public abstract class Pointcut extends PatternNode implements PointcutExpressionMatching {
	public static final class State extends TypeSafeEnum {
		public State(String name, int key) {
			super(name, key);
		}
	}
	
	public static final State SYMBOLIC = new State("symbolic", 0);
	public static final State RESOLVED = new State("resolved", 1);
	public static final State CONCRETE = new State("concrete", 2);

	protected byte pointcutKind;
	
	public State state;

	protected int lastMatchedShadowId;
	private FuzzyBoolean lastMatchedShadowResult;
	private Test lastMatchedShadowResidue;
	
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
	public abstract FuzzyBoolean fastMatch(FastMatchInfo info);

	/**
	 * The set of ShadowKinds that this Pointcut could possibly match  
	 */
	public abstract /*Enum*/Set/*<Shadow.Kind>*/ couldMatchKinds();
	
	/**
	 * Do I really match this shadow?
	 * XXX implementors need to handle state
	 */
	public final FuzzyBoolean match(Shadow shadow) {
		if (shadow.shadowId == lastMatchedShadowId) return lastMatchedShadowResult;
		FuzzyBoolean ret;
		// this next test will prevent a lot of un-needed matching going on....
		if (couldMatchKinds().contains(shadow.getKind())) {
			ret = matchInternal(shadow);
		} else {
			ret = FuzzyBoolean.NO;
		}
		lastMatchedShadowId = shadow.shadowId;
		lastMatchedShadowResult = ret;
		return ret;
	}
	
	protected abstract FuzzyBoolean matchInternal(Shadow shadow);
	
	/*
	 * for runtime / dynamic pointcuts.
	 * Default implementation delegates to StaticPart matcher
	 */
	public FuzzyBoolean match(JoinPoint jp, JoinPoint.StaticPart enclosingJoinPoint) {
		return match(jp.getStaticPart());
	}

	/*
	 * for runtime / dynamic pointcuts.
	 * Not all pointcuts can be matched at runtime, those that can should overide either
	 * match(JoinPoint), or this method, or both.
	 */
	public FuzzyBoolean match(JoinPoint.StaticPart jpsp) {
		throw new UnsupportedOperationException("Pointcut expression " + this.toString() + "cannot be matched at runtime");
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesDynamically(java.lang.String, java.lang.reflect.Member, java.lang.Object, java.lang.Object, java.lang.reflect.Member)
	 */
	public boolean matchesDynamically(
			Object thisObject, Object targetObject, Object[] args) {
		throw new UnsupportedOperationException("Pointcut expression " + this.toString() + "cannot be matched by this operation");
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesStatically(java.lang.String, java.lang.reflect.Member, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesStatically(
			String joinpointKind, Member member, Class thisClass,
			Class targetClass, Member withinCode) {
		throw new UnsupportedOperationException("Pointcut expression " + this.toString() + "cannot be matched by this operation");
	}
	
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
	public static final byte IF_TRUE = 14;
	public static final byte IF_FALSE = 15;
	public static final byte ANNOTATION = 16;
	public static final byte ATWITHIN = 17;
	public static final byte ATWITHINCODE = 18;
	public static final byte ATTHIS_OR_TARGET = 19;
	public static final byte ATARGS = 20;
	
	public static final byte NONE = 40;

	public byte getPointcutKind() { return pointcutKind; }

	// internal, only called from resolve
	protected abstract void resolveBindings(IScope scope, Bindings bindings);
	
	// internal, only called from resolve
	protected abstract void resolveBindingsFromRTTI();
	
    /**
     * Returns this pointcut mutated
     */
    public final Pointcut resolve(IScope scope) {
    	assertState(SYMBOLIC);
    	Bindings bindingTable = new Bindings(scope.getFormalCount());
        this.resolveBindings(scope, bindingTable);
        bindingTable.checkAllBound(scope);
        this.state = RESOLVED;
        return this;  	
    }
    
    /**
     * Returns this pointcut with type patterns etc resolved based on available RTTI 
     */
    public Pointcut resolve() {
    	assertState(SYMBOLIC);
    	this.resolveBindingsFromRTTI();
    	this.state = RESOLVED;
    	return this;
    }
	
	/**
	 * Returns a new pointcut
	 * Only used by test cases
	 */
    public final Pointcut concretize(ResolvedTypeX inAspect, int arity) {
        return concretize(inAspect, IntMap.idMap(arity));
    }
	
	
	//XXX this is the signature we're moving to
	public final Pointcut concretize(ResolvedTypeX inAspect, int arity, ShadowMunger advice) {
		//if (state == CONCRETE) return this; //???
		IntMap map = IntMap.idMap(arity);
		map.setEnclosingAdvice(advice);
		map.setConcreteAspect(inAspect);
		return concretize(inAspect, map);
	}
	
	public boolean isDeclare(ShadowMunger munger) {
		if (munger == null) return false; // ??? Is it actually an error if we get a null munger into this method.
		if (munger instanceof Checker) return true;
		if (((Advice)munger).getKind().equals(AdviceKind.Softener)) return true;
		return false;
	}
	
	
	public final Pointcut concretize(ResolvedTypeX inAspect, IntMap bindings) {
		//!!! add this test -- assertState(RESOLVED);
		Pointcut ret = this.concretize1(inAspect, bindings);
        if (shouldCopyLocationForConcretize()) ret.copyLocationFrom(this);
		ret.state = CONCRETE;
		return ret;
	}
	
	
	protected boolean shouldCopyLocationForConcretize() {
		return true;
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
	public final Test findResidue(Shadow shadow, ExposedState state) {
//		if (shadow.shadowId == lastMatchedShadowId) return lastMatchedShadowResidue;
		Test ret = findResidueInternal(shadow,state);
//		lastMatchedShadowResidue = ret;
		lastMatchedShadowId = shadow.shadowId;
		return ret;
	}
	
	protected abstract Test findResidueInternal(Shadow shadow,ExposedState state);

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
			case IF_TRUE: ret = IfPointcut.makeIfTruePointcut(RESOLVED); break;
			case IF_FALSE: ret = IfPointcut.makeIfFalsePointcut(RESOLVED); break;
			case ANNOTATION: ret = AnnotationPointcut.read(s, context); break;
			case ATWITHIN: ret = WithinAnnotationPointcut.read(s, context); break;
			case ATWITHINCODE: ret = WithinCodeAnnotationPointcut.read(s, context); break;
			case ATTHIS_OR_TARGET: ret = ThisOrTargetAnnotationPointcut.read(s, context); break;
			case ATARGS: ret = ArgsAnnotationPointcut.read(s,context); break;
			case NONE: ret = makeMatchesNothing(RESOLVED); break;
			default:
				throw new BCException("unknown kind: " + kind);
		}
		ret.state = RESOLVED;
		ret.pointcutKind = kind;
		return ret;
		
	}
	

	//public void prepare(Shadow shadow) {}
    
    // ---- test method
    
    public static Pointcut fromString(String str) {
        PatternParser parser = new PatternParser(str);
        return parser.parsePointcut();
    }
    
    private static class MatchesNothingPointcut extends Pointcut {
    	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
			return Literal.FALSE; // can only get here if an earlier error occurred
		}

		public Set couldMatchKinds() {
			return Collections.EMPTY_SET;
		}
		
		public FuzzyBoolean fastMatch(FastMatchInfo type) {
			return FuzzyBoolean.NO;
		}
		
		protected FuzzyBoolean matchInternal(Shadow shadow) {
			return FuzzyBoolean.NO;
		}
		
		public FuzzyBoolean match(JoinPoint.StaticPart jpsp) {
			return FuzzyBoolean.NO;
		}

		/* (non-Javadoc)
		 * @see org.aspectj.weaver.patterns.Pointcut#matchesDynamically(java.lang.Object, java.lang.Object, java.lang.Object[])
		 */
		public boolean matchesDynamically(Object thisObject,
				Object targetObject, Object[] args) {
			return false;
		}

		/* (non-Javadoc)
		 * @see org.aspectj.weaver.patterns.Pointcut#matchesStatically(java.lang.String, java.lang.reflect.Member, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
		 */
		public FuzzyBoolean matchesStatically(
				String joinpointKind, Member member, Class thisClass,
				Class targetClass, Member withinCode) {
			return FuzzyBoolean.NO;
		}
		
		public void resolveBindings(IScope scope, Bindings bindings) {
		}
		
		public void resolveBindingsFromRTTI() {
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
