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
import java.util.Iterator;

import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
/**
 *  On creation, type pattern only contains WildTypePattern nodes, not BindingType or ExactType. 
 * 
 * <p>Then we call resolveBindings() during compilation
 * During concretization of enclosing pointcuts, we call remapAdviceFormals
  * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public abstract class TypePattern extends PatternNode {
	public static class MatchKind {
		private String name;
		public MatchKind(String name) { this.name = name; }
		public String toString() { return name; }
	}
	
	public static final MatchKind STATIC = new MatchKind("STATIC");
	public static final MatchKind DYNAMIC = new MatchKind("DYNAMIC");
	
	public static final TypePattern ELLIPSIS = new EllipsisTypePattern();
	public static final TypePattern ANY = new AnyTypePattern();
	public static final TypePattern NO = new NoTypePattern();
	
	
	protected boolean includeSubtypes;
	
	protected TypePattern(boolean includeSubtypes) {
		this.includeSubtypes = includeSubtypes;
	}
	
	//XXX non-final for Not, && and ||
	public boolean matchesStatically(ResolvedTypeX type) {
		if (includeSubtypes) {
			return matchesSubtypes(type);
		} else {
			return matchesExactly(type);
		}
	}
	public abstract FuzzyBoolean matchesInstanceof(ResolvedTypeX type);	
	
	
	public final FuzzyBoolean matches(ResolvedTypeX type, MatchKind kind) {
		//??? This is part of gracefully handling missing references
		if (type == ResolvedTypeX.MISSING) return FuzzyBoolean.NO;
		
		if (kind == STATIC) {
			return FuzzyBoolean.fromBoolean(matchesStatically(type));
		} else if (kind == DYNAMIC) {
			//System.err.println("matching: " + this + " with " + type);
			FuzzyBoolean ret = matchesInstanceof(type);
			//System.err.println("    got: " + ret);
			return ret;
		} else {
			throw new IllegalArgumentException("kind must be DYNAMIC or STATIC");
		}
	}
	
	
	protected abstract boolean matchesExactly(ResolvedTypeX type);
	protected boolean matchesSubtypes(ResolvedTypeX type) {
		//System.out.println("matching: " + this + " to " + type);
		if (matchesExactly(type)) {
			//System.out.println("    true");
			return true;
		}
		
		FuzzyBoolean ret = FuzzyBoolean.NO; // ??? -eh
		for (Iterator i = type.getDirectSupertypes(); i.hasNext(); ) {
			ResolvedTypeX superType = (ResolvedTypeX)i.next();
			if (matchesSubtypes(superType)) return true;
		}
		return false;
	}
	
	public TypeX resolveExactType(IScope scope, Bindings bindings) {
		TypePattern p = resolveBindings(scope, bindings, false, true);
		if (p == NO) return ResolvedTypeX.MISSING;
		
		return ((ExactTypePattern)p).getType();
	}
	
	public TypeX getExactType() {
		if (this instanceof ExactTypePattern) return ((ExactTypePattern)this).getType();
		else return ResolvedTypeX.MISSING;
	}
	
	protected TypePattern notExactType(IScope s) {
		s.getMessageHandler().handleMessage(MessageUtil.error("exact type pattern required", getSourceLocation()));
		return NO;
	}
	
//	public boolean assertExactType(IMessageHandler m) {
//		if (this instanceof ExactTypePattern) return true;
//		
//		//XXX should try harder to avoid multiple errors for one problem
//		m.handleMessage(MessageUtil.error("exact type pattern required", getSourceLocation()));
//		return false;
//	}

	/**
	 * This can modify in place, or return a new TypePattern if the type changes.
	 */
    public TypePattern resolveBindings(IScope scope, Bindings bindings, 
    								boolean allowBinding, boolean requireExactType)
    { 
    	return this;
    }
    
	public void postRead(ResolvedTypeX enclosingType) {
	}
	
	public boolean isStar() {
		return false;
	}

    
    
    /**
     * This is called during concretization of pointcuts, it is used by BindingTypePattern
     * to return a new BindingTypePattern with a formal index appropiate for the advice,
     * rather than for the lexical declaration, i.e. this handles transforamtions through
     * named pointcuts.
     * <pre>
     * pointcut foo(String name): args(name);
     * --&gt; This makes a BindingTypePattern(0) pointing to the 0th formal
     * 
     * before(Foo f, String n): this(f) && foo(n) { ... }
     * --&gt; when resolveReferences is called on the args from the above, it
     *     will return a BindingTypePattern(1)
     * 
     * before(Foo f): this(f) && foo(*) { ... }
     * --&gt; when resolveReferences is called on the args from the above, it
     *     will return an ExactTypePattern(String)
     * </pre>
     */
	public TypePattern remapAdviceFormals(IntMap bindings) {
		return this;
	}


	public static final byte WILD = 1;
	public static final byte EXACT = 2;
	public static final byte BINDING = 3;
	public static final byte ELLIPSIS_KEY = 4; 
	public static final byte ANY_KEY = 5; 
	public static final byte NOT = 6;
	public static final byte OR = 7;
	public static final byte AND = 8;
	public static final byte NO_KEY = 9;

	public static TypePattern read(DataInputStream s, ISourceContext context) throws IOException {
		byte key = s.readByte();
		switch(key) {
			case WILD: return WildTypePattern.read(s, context);
			case EXACT: return ExactTypePattern.read(s, context);
			case BINDING: return BindingTypePattern.read(s, context);
			case ELLIPSIS_KEY: return ELLIPSIS;
			case ANY_KEY: return ANY;
			case NO_KEY: return NO;
			case NOT: return NotTypePattern.read(s, context);
			case OR: return OrTypePattern.read(s, context);
			case AND: return AndTypePattern.read(s, context);
		}
		throw new BCException("unknown TypePattern kind: " + key);
	}
}

class EllipsisTypePattern extends TypePattern {
	
	/**
	 * Constructor for EllipsisTypePattern.
	 * @param includeSubtypes
	 */
	public EllipsisTypePattern() {
		super(false);
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesExactly(IType)
	 */
	protected boolean matchesExactly(ResolvedTypeX type) {
		return false;
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesInstanceof(IType)
	 */
	public FuzzyBoolean matchesInstanceof(ResolvedTypeX type) {
		return FuzzyBoolean.NO;
	}

	/**
	 * @see org.aspectj.weaver.patterns.PatternNode#write(DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(ELLIPSIS_KEY);
	}
	
	public String toString() { return ".."; }
}

class AnyTypePattern extends TypePattern {
	
	/**
	 * Constructor for EllipsisTypePattern.
	 * @param includeSubtypes
	 */
	public AnyTypePattern() {
		super(false);
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesExactly(IType)
	 */
	protected boolean matchesExactly(ResolvedTypeX type) {
		return true;
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesInstanceof(IType)
	 */
	public FuzzyBoolean matchesInstanceof(ResolvedTypeX type) {
		return FuzzyBoolean.YES;
	}

	/**
	 * @see org.aspectj.weaver.patterns.PatternNode#write(DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(ANY_KEY);
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matches(IType, MatchKind)
	 */
//	public FuzzyBoolean matches(IType type, MatchKind kind) {
//		return FuzzyBoolean.YES;
//	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesSubtypes(IType)
	 */
	protected boolean matchesSubtypes(ResolvedTypeX type) {
		return true;
	}
	
	
	public boolean isStar() {
		return true;
	}
	
	public String toString() { return "*"; }
}

class NoTypePattern extends TypePattern {
	
	public NoTypePattern() {
		super(false);
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesExactly(IType)
	 */
	protected boolean matchesExactly(ResolvedTypeX type) {
		return false;
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesInstanceof(IType)
	 */
	public FuzzyBoolean matchesInstanceof(ResolvedTypeX type) {
		return FuzzyBoolean.NO;
	}

	/**
	 * @see org.aspectj.weaver.patterns.PatternNode#write(DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(NO_KEY);
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matches(IType, MatchKind)
	 */
//	public FuzzyBoolean matches(IType type, MatchKind kind) {
//		return FuzzyBoolean.YES;
//	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesSubtypes(IType)
	 */
	protected boolean matchesSubtypes(ResolvedTypeX type) {
		return false;
	}
	
	
	public boolean isStar() {
		return false;
	}
	
	public String toString() { return "<nothing>"; }
}

