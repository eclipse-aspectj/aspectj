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
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
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
	protected boolean isVarArgs = false;
	protected AnnotationTypePattern annotationPattern = AnnotationTypePattern.ANY;
	
	protected TypePattern(boolean includeSubtypes,boolean isVarArgs) {
		this.includeSubtypes = includeSubtypes;
		this.isVarArgs = isVarArgs;
	}
	
	protected TypePattern(boolean includeSubtypes) {
		this(includeSubtypes,false);
	}
	
	public void setAnnotationTypePattern(AnnotationTypePattern annPatt) {
		this.annotationPattern = annPatt;
	}
	
	// answer conservatively...
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		if (this.includeSubtypes || other.includeSubtypes) return true;
		if (this.annotationPattern != AnnotationTypePattern.ANY) return true;
		if (other.annotationPattern != AnnotationTypePattern.ANY) return true;
		return false;
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
		FuzzyBoolean typeMatch = null;
		//??? This is part of gracefully handling missing references
		if (type == ResolvedTypeX.MISSING) return FuzzyBoolean.NO;
		
		if (kind == STATIC) {
//			typeMatch = FuzzyBoolean.fromBoolean(matchesStatically(type));
//			return typeMatch.and(annotationPattern.matches(type));
		    return FuzzyBoolean.fromBoolean(matchesStatically(type));
		} else if (kind == DYNAMIC) {
			//System.err.println("matching: " + this + " with " + type);
//			typeMatch = matchesInstanceof(type);
			//System.err.println("    got: " + ret);
//			return typeMatch.and(annotationPattern.matches(type));
		    return matchesInstanceof(type);
		} else {
			throw new IllegalArgumentException("kind must be DYNAMIC or STATIC");
		}
	}
	
	
	// methods for dynamic pc matching...
	public final FuzzyBoolean matches(Class toMatch, MatchKind kind) {
		if (kind == STATIC) {
			return FuzzyBoolean.fromBoolean(matchesStatically(toMatch));
		} else if (kind == DYNAMIC) {
			//System.err.println("matching: " + this + " with " + type);
			FuzzyBoolean ret = matchesInstanceof(toMatch);
			//System.err.println("    got: " + ret);
			return ret;
		} else {
			throw new IllegalArgumentException("kind must be DYNAMIC or STATIC");
		}
	}
		
	/**
	 * This variant is only called by the args and handler pcds when doing runtime
	 * matching. We need to handle primitive types correctly in this case (an Integer
	 * should match an int,...).
	 */
	public final FuzzyBoolean matches(Object o, MatchKind kind) {
		if (kind == STATIC) {  // handler pcd
			return FuzzyBoolean.fromBoolean(matchesStatically(o.getClass()));
		} else if (kind == DYNAMIC) {  // args pcd
//			Class clazz = o.getClass();
//			FuzzyBoolean ret = FuzzyBoolean.fromBoolean(matchesSubtypes(clazz));
//			if (ret == FuzzyBoolean.NO) {
//				// try primitive type instead
//				if (clazz == Integer.class) ret = FuzzyBoolean.fromBoolean(matchesExactly(int.class));
//			}
//			return ret;
			return matchesInstanceof(o.getClass());
		} else {
			throw new IllegalArgumentException("kind must be DYNAMIC or STATIC");			
		}
	}
	
	public boolean matchesStatically(Class toMatch) {
		if (includeSubtypes) {
			return matchesSubtypes(toMatch);
		} else {
			return matchesExactly(toMatch);
		}
	}
	public abstract FuzzyBoolean matchesInstanceof(Class toMatch);	
	
	protected abstract boolean matchesExactly(Class toMatch);
	protected boolean matchesSubtypes(Class toMatch) {
		if (matchesExactly(toMatch)) {
			return true;
		} 
		Class superClass = toMatch.getSuperclass();
		if (superClass != null) {
			return matchesSubtypes(superClass);
		}
		return false;
	}
	
	protected abstract boolean matchesExactly(ResolvedTypeX type);

	protected boolean matchesSubtypes(ResolvedTypeX type) {
		//System.out.println("matching: " + this + " to " + type);
		if (matchesExactly(type)) {
			//System.out.println("    true");
			return true;
		}
		
		// FuzzyBoolean ret = FuzzyBoolean.NO; // ??? -eh
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
		s.getMessageHandler().handleMessage(MessageUtil.error(
				WeaverMessages.format(WeaverMessages.EXACT_TYPE_PATTERN_REQD), getSourceLocation()));
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
    	annotationPattern = annotationPattern.resolveBindings(scope,bindings,allowBinding);
    	return this;
    }
    
    public TypePattern resolveBindingsFromRTTI(boolean allowBindng, boolean requireExactType) {
    	return this;
    }
    
    public void resolve(World world) {
        annotationPattern.resolve(world);
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
	public static final byte ANY_WITH_ANNO = 10;

	public static TypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
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
			case ANY_WITH_ANNO: return AnyWithAnnotationTypePattern.read(s,context);
		}
		throw new BCException("unknown TypePattern kind: " + key);
	}

	public boolean isIncludeSubtypes() {
		return includeSubtypes;
	}

}

class EllipsisTypePattern extends TypePattern {
	
	/**
	 * Constructor for EllipsisTypePattern.
	 * @param includeSubtypes
	 */
	public EllipsisTypePattern() {
		super(false,false);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.TypePattern#couldEverMatchSameTypesAs(org.aspectj.weaver.patterns.TypePattern)
	 */
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		return true;
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
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesExactly(IType)
	 */
	protected boolean matchesExactly(Class type) {
		return false;
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesInstanceof(IType)
	 */
	public FuzzyBoolean matchesInstanceof(Class type) {
		return FuzzyBoolean.NO;
	}
	/**
	 * @see org.aspectj.weaver.patterns.PatternNode#write(DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(ELLIPSIS_KEY);
	}
	
	public String toString() { return ".."; }
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return (obj instanceof EllipsisTypePattern);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return 17 * 37;
	}
}

class AnyTypePattern extends TypePattern {
	
	/**
	 * Constructor for EllipsisTypePattern.
	 * @param includeSubtypes
	 */
	public AnyTypePattern() {
		super(false,false);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.TypePattern#couldEverMatchSameTypesAs(org.aspectj.weaver.patterns.TypePattern)
	 */
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		return true;
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
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesExactly(IType)
	 */
	protected boolean matchesExactly(Class type) {
		return true;
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesInstanceof(IType)
	 */
	public FuzzyBoolean matchesInstanceof(Class type) {
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
	
	public boolean equals(Object obj) {
		return (obj instanceof AnyTypePattern);
	}
	
	public int hashCode() {
		return 37;
	}
}

/**
 * This type represents a type pattern of '*' but with an annotation specified,
 * e.g. '@Color *'
 */
class AnyWithAnnotationTypePattern extends TypePattern {
	
	public AnyWithAnnotationTypePattern(AnnotationTypePattern atp) {
		super(false,false);
		annotationPattern = atp;
	}

	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		return true;
	}

	protected boolean matchesExactly(ResolvedTypeX type) {
		annotationPattern.resolve(type.getWorld());
		return annotationPattern.matches(type).alwaysTrue();
	}

	public FuzzyBoolean matchesInstanceof(ResolvedTypeX type) {
		return FuzzyBoolean.YES;
	}

	protected boolean matchesExactly(Class type) {
		return true;
	}

	public FuzzyBoolean matchesInstanceof(Class type) {
		return FuzzyBoolean.YES;
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(TypePattern.ANY_WITH_ANNO);
		annotationPattern.write(s);
		writeLocation(s);
	}
	
	public static TypePattern read(VersionedDataInputStream s,ISourceContext c) throws IOException {
		 AnnotationTypePattern annPatt = AnnotationTypePattern.read(s,c);
		 AnyWithAnnotationTypePattern ret = new AnyWithAnnotationTypePattern(annPatt);
		 ret.readLocation(c, s);
		 return ret;
	}

//	public FuzzyBoolean matches(IType type, MatchKind kind) {
//		return FuzzyBoolean.YES;
//	}

	protected boolean matchesSubtypes(ResolvedTypeX type) {
		return true;
	}
	
	public boolean isStar() {
		return true;
	}
	
	public String toString() { return annotationPattern+" *"; }
	
	public boolean equals(Object obj) {
		if  (!(obj instanceof AnyWithAnnotationTypePattern)) return false;
		AnyWithAnnotationTypePattern awatp = (AnyWithAnnotationTypePattern) obj;
		return (annotationPattern.equals(awatp));
	}
	
	public int hashCode() {
		return annotationPattern.hashCode();
	}
}

class NoTypePattern extends TypePattern {
	
	public NoTypePattern() {
		super(false,false);
	}

	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.TypePattern#couldEverMatchSameTypesAs(org.aspectj.weaver.patterns.TypePattern)
	 */
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		return false;
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
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesExactly(IType)
	 */
	protected boolean matchesExactly(Class type) {
		return false;
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesInstanceof(IType)
	 */
	public FuzzyBoolean matchesInstanceof(Class type) {
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return (obj instanceof NoTypePattern);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return 17 * 37 * 37;
	}
}

