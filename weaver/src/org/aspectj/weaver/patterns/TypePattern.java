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

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;

import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeVariableReference;
import org.aspectj.weaver.UnresolvedType;
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
	protected TypePatternList typeParameters = TypePatternList.EMPTY;
	
	protected TypePattern(boolean includeSubtypes,boolean isVarArgs,TypePatternList typeParams) {
		this.includeSubtypes = includeSubtypes;
		this.isVarArgs = isVarArgs;
		this.typeParameters = (typeParams == null ? TypePatternList.EMPTY : typeParams);
	}
	
	protected TypePattern(boolean includeSubtypes, boolean isVarArgs) {
		this(includeSubtypes,isVarArgs,null);
	}

    public AnnotationTypePattern getAnnotationPattern() {
        return annotationPattern;
    }

    public boolean isVarArgs() {
        return isVarArgs;
    }

	public boolean isStarAnnotation() {
		return annotationPattern == AnnotationTypePattern.ANY;
	}
	
	public boolean isArray() {
		return false;
	}
	
	protected TypePattern(boolean includeSubtypes) {
		this(includeSubtypes,false);
	}
	
	public void setAnnotationTypePattern(AnnotationTypePattern annPatt) {
		this.annotationPattern = annPatt;
	}
	
	public void setTypeParameters(TypePatternList typeParams) {
		this.typeParameters = typeParams;
	}
	
	public TypePatternList getTypeParameters() {
		return this.typeParameters;
	}
	
	public void setIsVarArgs(boolean isVarArgs) {
		this.isVarArgs = isVarArgs;
	}
	
	// answer conservatively...
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		if (this.includeSubtypes || other.includeSubtypes) return true;
		if (this.annotationPattern != AnnotationTypePattern.ANY) return true;
		if (other.annotationPattern != AnnotationTypePattern.ANY) return true;
		return false;
	}
	
	//XXX non-final for Not, && and ||
	public boolean matchesStatically(ResolvedType type) {
		if (includeSubtypes) {
			return matchesSubtypes(type);
		} else {
			return matchesExactly(type);
		}
	}
	public abstract FuzzyBoolean matchesInstanceof(ResolvedType type);	
	
	public final FuzzyBoolean matches(ResolvedType type, MatchKind kind) {
		FuzzyBoolean typeMatch = null;
		//??? This is part of gracefully handling missing references
		if (type.isMissing()) return FuzzyBoolean.NO;
		
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
		
	protected abstract boolean matchesExactly(ResolvedType type);
	
	protected abstract boolean matchesExactly(ResolvedType type, ResolvedType annotatedType);

	protected boolean matchesSubtypes(ResolvedType type) {
		//System.out.println("matching: " + this + " to " + type);
		if (matchesExactly(type)) {
			//System.out.println("    true");
			return true;
		}
		// pr124808
		Iterator typesIterator = null;
		if (type.isTypeVariableReference()) {
			typesIterator = ((TypeVariableReference)type).getTypeVariable().getFirstBound().resolve(type.getWorld()).getDirectSupertypes();
		} else {
			typesIterator = type.getDirectSupertypes();
		}
		
		// FuzzyBoolean ret = FuzzyBoolean.NO; // ??? -eh
		for (Iterator i = typesIterator; i.hasNext(); ) {
			ResolvedType superType = (ResolvedType)i.next();
			// TODO asc generics, temporary whilst matching isnt aware..
			//if (superType.isParameterizedType()) superType = superType.getRawType().resolve(superType.getWorld());
			if (matchesSubtypes(superType,type)) return true;
		}
		return false;
	}
	
	protected boolean matchesSubtypes(ResolvedType superType, ResolvedType annotatedType) {
		//System.out.println("matching: " + this + " to " + type);
		if (matchesExactly(superType,annotatedType)) {
			//System.out.println("    true");
			return true;
		}
		
		// FuzzyBoolean ret = FuzzyBoolean.NO; // ??? -eh
		for (Iterator i = superType.getDirectSupertypes(); i.hasNext(); ) {
			ResolvedType superSuperType = (ResolvedType)i.next();
			if (matchesSubtypes(superSuperType,annotatedType)) return true;
		}
		return false;
	}
	
	public UnresolvedType resolveExactType(IScope scope, Bindings bindings) {
		TypePattern p = resolveBindings(scope, bindings, false, true);
		if (!(p instanceof ExactTypePattern)) return ResolvedType.MISSING;
		return ((ExactTypePattern)p).getType();
	}
	
	public UnresolvedType getExactType() {
		if (this instanceof ExactTypePattern) return ((ExactTypePattern)this).getType();
		else return ResolvedType.MISSING;
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
    
    public void resolve(World world) {
        annotationPattern.resolve(world);
    }
    
    /**
     * return a version of this type pattern in which all type variable references have been
     * replaced by their corresponding entry in the map.
     */
    public abstract TypePattern parameterizeWith(Map typeVariableMap,World w);
    
	public void postRead(ResolvedType enclosingType) {
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
	public static final byte HAS_MEMBER = 11;

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
			case HAS_MEMBER: return HasMemberTypePattern.read(s,context);
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
		super(false,false,new TypePatternList());
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
	protected boolean matchesExactly(ResolvedType type) {
		return false;
	}
	
	protected boolean matchesExactly(ResolvedType type, ResolvedType annotatedType) {
		return false;
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesInstanceof(IType)
	 */
	public FuzzyBoolean matchesInstanceof(ResolvedType type) {
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

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
    
    public TypePattern parameterizeWith(Map typeVariableMap,World w) {
    	return this;
    }


}

class AnyTypePattern extends TypePattern {
	
	/**
	 * Constructor for EllipsisTypePattern.
	 * @param includeSubtypes
	 */
	public AnyTypePattern() {
		super(false,false,new TypePatternList());
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
	protected boolean matchesExactly(ResolvedType type) {
		return true;
	}

	protected boolean matchesExactly(ResolvedType type, ResolvedType annotatedType) {
		return true;
	}
	
	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesInstanceof(IType)
	 */
	public FuzzyBoolean matchesInstanceof(ResolvedType type) {
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
	protected boolean matchesSubtypes(ResolvedType type) {
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

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
    
    public TypePattern parameterizeWith(Map arg0,World w) {
    	return this;
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

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this,data);
	}
	
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		return true;
	}

	protected boolean matchesExactly(ResolvedType type) {
		annotationPattern.resolve(type.getWorld());
		boolean b = false;
		if (type.temporaryAnnotationTypes!=null) {
			b = annotationPattern.matches(type,type.temporaryAnnotationTypes).alwaysTrue();
		} else {
			b = annotationPattern.matches(type).alwaysTrue();
		}
		return b;
	}
	
	
	protected boolean matchesExactly(ResolvedType type, ResolvedType annotatedType) {
		annotationPattern.resolve(type.getWorld());
		return annotationPattern.matches(annotatedType).alwaysTrue();		
	}

	public FuzzyBoolean matchesInstanceof(ResolvedType type) {
		if (Modifier.isFinal(type.getModifiers())) {
			return FuzzyBoolean.fromBoolean(matchesExactly(type));
		}
		return FuzzyBoolean.MAYBE;
	}

	public TypePattern parameterizeWith(Map typeVariableMap,World w) {
		AnyWithAnnotationTypePattern ret = new AnyWithAnnotationTypePattern(this.annotationPattern.parameterizeWith(typeVariableMap,w));
		ret.copyLocationFrom(this);
		return ret;
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

	protected boolean matchesSubtypes(ResolvedType type) {
		return true;
	}
	
	public boolean isStar() {
		return false;
	}
	
	public String toString() { return annotationPattern+" *"; }
	
	public boolean equals(Object obj) {
		if  (!(obj instanceof AnyWithAnnotationTypePattern)) return false;
		AnyWithAnnotationTypePattern awatp = (AnyWithAnnotationTypePattern) obj;
		return (annotationPattern.equals(awatp.annotationPattern));
	}
	
	public int hashCode() {
		return annotationPattern.hashCode();
	}
}

class NoTypePattern extends TypePattern {
	
	public NoTypePattern() {
		super(false,false,new TypePatternList());
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
	protected boolean matchesExactly(ResolvedType type) {
		return false;
	}
	
	protected boolean matchesExactly(ResolvedType type, ResolvedType annotatedType) {
		return false;
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesInstanceof(IType)
	 */
	public FuzzyBoolean matchesInstanceof(ResolvedType type) {
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
	protected boolean matchesSubtypes(ResolvedType type) {
		return false;
	}
	
	
	public boolean isStar() {
		return false;
	}
	
	public String toString() { return "<nothing>"; }//FIXME AV - bad! toString() cannot be parsed back (not idempotent)
	
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

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
    
    public TypePattern parameterizeWith(Map arg0,World w) {
    	return this;
    }
}

