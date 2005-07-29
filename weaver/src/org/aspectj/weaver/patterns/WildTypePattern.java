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
import java.util.ArrayList;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FileUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.BoundedReferenceType;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeFactory;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.TypeVariableReference;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.UnresolvedTypeVariableReferenceType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;

/**
 * The PatternParser always creates WildTypePatterns for type patterns in pointcut
 * expressions (apart from *, which is sometimes directly turned into TypePattern.ANY).
 * resolveBindings() tries to work out what we've really got and turn it into a type
 * pattern that we can use for matching. This will normally be either an ExactTypePattern
 * or a WildTypePattern.
 * 
 * Here's how the process pans out for various generic and parameterized patterns:
 * (see GenericsWildTypePatternResolvingTestCase)
 * 
 * Foo where Foo exists and is generic
 *    Parser creates WildTypePattern namePatterns={Foo}
 *    resolveBindings resolves Foo to RT(Foo - raw)
 *                    return ExactTypePattern(LFoo;)
 * 
 * Foo<String> where Foo exists and String meets the bounds
 *    Parser creates WildTypePattern namePatterns = {Foo}, typeParameters=WTP{String}
 *    resolveBindings resolves typeParameters to ExactTypePattern(String)
 *                    resolves Foo to RT(Foo)
 *                    returns ExactTypePattern(PFoo<String>; - parameterized)
 *                    
 * Foo<Str*> where Foo exists and takes one bound
 *    Parser creates WildTypePattern namePatterns = {Foo}, typeParameters=WTP{Str*}
 *    resolveBindings resolves typeParameters to WTP{Str*}
 *                    resolves Foo to RT(Foo)
 *                    returns WildTypePattern(name = Foo, typeParameters = WTP{Str*} isGeneric=false)                    
 * 
 * Fo*<String> 
 *    Parser creates WildTypePattern namePatterns = {Fo*}, typeParameters=WTP{String}
 *    resolveBindings resolves typeParameters to ETP{String}
 *                    returns WildTypePattern(name = Fo*, typeParameters = ETP{String} isGeneric=false)                    
 *
 * 
 * Foo<?>
 * 
 * Foo<? extends Number>
 * 
 * Foo<? extends Number+>
 * 
 * Foo<? super Number>
 * 
 */
public class WildTypePattern extends TypePattern {
	private static final String GENERIC_WILDCARD_CHARACTER = "?";
	private NamePattern[] namePatterns;
	int ellipsisCount;
	String[] importedPrefixes;
	String[] knownMatches;
	int dim;
	
	// these next three are set if the type pattern is constrained by extends or super clauses, in which case the
	// namePatterns must have length 1
	// TODO AMC: read/write/resolve of these fields
	TypePattern upperBound;  //  extends Foo 
	TypePattern[] additionalInterfaceBounds;  // extends Foo & A,B,C
	TypePattern lowerBound;  // super Foo

	// if we have type parameters, these fields indicate whether we should be a generic type pattern or a parameterized
	// type pattern. We can only tell during resolve bindings.
	private boolean isGeneric = true;
	
	WildTypePattern(NamePattern[] namePatterns, boolean includeSubtypes, int dim, boolean isVarArgs, TypePatternList typeParams) {
		super(includeSubtypes,isVarArgs,typeParams);
		this.namePatterns = namePatterns;
		this.dim = dim;
		ellipsisCount = 0;
		for (int i=0; i<namePatterns.length; i++) {
			if (namePatterns[i] == NamePattern.ELLIPSIS) ellipsisCount++;
		}
		setLocation(namePatterns[0].getSourceContext(), namePatterns[0].getStart(), namePatterns[namePatterns.length-1].getEnd());
	}

	public WildTypePattern(List names, boolean includeSubtypes, int dim) {
		this((NamePattern[])names.toArray(new NamePattern[names.size()]), includeSubtypes, dim,false,TypePatternList.EMPTY);

	}
	
	public WildTypePattern(List names, boolean includeSubtypes, int dim, int endPos) {
		this(names, includeSubtypes, dim);
		this.end = endPos;
	}

	public WildTypePattern(List names, boolean includeSubtypes, int dim, int endPos, boolean isVarArg) {
		this(names, includeSubtypes, dim);
		this.end = endPos;
		this.isVarArgs = isVarArg;
	}
	
	public WildTypePattern(
			List names, 
			boolean includeSubtypes, 
			int dim, 
			int endPos, 
			boolean isVarArg, 
			TypePatternList typeParams,
			TypePattern upperBound,
			TypePattern[] additionalInterfaceBounds,
			TypePattern lowerBound) {
		this((NamePattern[])names.toArray(new NamePattern[names.size()]),includeSubtypes,dim,isVarArg,typeParams);
		this.end = endPos;
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
		this.additionalInterfaceBounds = additionalInterfaceBounds;
	}
	
	public WildTypePattern(
			List names, 
			boolean includeSubtypes, 
			int dim, 
			int endPos, 
			boolean isVarArg, 
			TypePatternList typeParams)
	{
		this((NamePattern[])names.toArray(new NamePattern[names.size()]),includeSubtypes,dim,isVarArg,typeParams);
		this.end = endPos;		
	}
	
    public NamePattern[] getNamePatterns() {
        return namePatterns;
    }
	
	public TypePattern getUpperBound() { return upperBound; }
	public TypePattern getLowerBound() { return lowerBound; }
	public TypePattern[] getAdditionalIntefaceBounds() { return additionalInterfaceBounds; }

	// called by parser after parsing a type pattern, must bump dim as well as setting flag
	public void setIsVarArgs(boolean isVarArgs) {
		this.isVarArgs = isVarArgs;
		if (isVarArgs) this.dim += 1;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.TypePattern#couldEverMatchSameTypesAs(org.aspectj.weaver.patterns.TypePattern)
	 */
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		if (super.couldEverMatchSameTypesAs(other)) return true;
		// false is necessary but not sufficient
		UnresolvedType otherType = other.getExactType();
		if (otherType != ResolvedType.MISSING) {
			if (namePatterns.length > 0) {
				if (!namePatterns[0].matches(otherType.getName())) return false;
			}
		} 
		if (other instanceof WildTypePattern) {
			WildTypePattern owtp = (WildTypePattern) other;
			String mySimpleName = namePatterns[0].maybeGetSimpleName();
			String yourSimpleName = owtp.namePatterns[0].maybeGetSimpleName();
			if (mySimpleName != null && yourSimpleName != null) {
				return (mySimpleName.startsWith(yourSimpleName) ||
						yourSimpleName.startsWith(mySimpleName));
			}
		}
		return true;
	}
	
	//XXX inefficient implementation
	public static char[][] splitNames(String s) {
		List ret = new ArrayList();
		int startIndex = 0;
		while (true) {
		    int breakIndex = s.indexOf('.', startIndex);  // what about /
		    if (breakIndex == -1) breakIndex = s.indexOf('$', startIndex);  // we treat $ like . here
		    if (breakIndex == -1) break;
		    char[] name = s.substring(startIndex, breakIndex).toCharArray();
		    ret.add(name);
		    startIndex = breakIndex+1;
		}
		ret.add(s.substring(startIndex).toCharArray());
		return (char[][])ret.toArray(new char[ret.size()][]);
	}	
	

	/**
	 * @see org.aspectj.weaver.TypePattern#matchesExactly(IType)
	 */
	protected boolean matchesExactly(ResolvedType type) {
		return matchesExactly(type,type);
	}

	protected boolean matchesExactly(ResolvedType type, ResolvedType annotatedType) {
		String targetTypeName = type.getName();
		
		//System.err.println("match: " + targetTypeName + ", " + knownMatches); //Arrays.asList(importedPrefixes));
		// Ensure the annotation pattern is resolved
		annotationPattern.resolve(type.getWorld());
		
		return matchesExactlyByName(targetTypeName) &&
		        matchesParameters(type,STATIC) &&
		        matchesBounds(type,STATIC) &&
		       annotationPattern.matches(annotatedType).alwaysTrue();
	}
	
	
	// we've matched against the base (or raw) type, but if this type pattern specifies parameters or
	// type variables we need to make sure we match against them too
	private boolean matchesParameters(ResolvedType aType, MatchKind staticOrDynamic) {
		if (!isGeneric && typeParameters.size() > 0) {
			if(!aType.isParameterizedType()) return false;
			// we have to match type parameters
			return typeParameters.matches(aType.getResolvedTypeParameters(), staticOrDynamic).alwaysTrue();
		}
		return true;
	}
	
	// we've matched against the base (or raw) type, but if this type pattern specifies bounds because
	// it is a ? extends or ? super deal then we have to match them too.
	private boolean matchesBounds(ResolvedType aType, MatchKind staticOrDynamic) {
		if (upperBound == null && aType.getUpperBound() != null) {
			// for upper bound, null can also match against Object - but anything else and we're out.
			if (!aType.getUpperBound().getName().equals(UnresolvedType.OBJECT.getName())) {
				return false;
			}
		}
		if (lowerBound == null && aType.getLowerBound() != null) return false;
		if (upperBound != null) {
			// match ? extends
			if (aType.isGenericWildcardSuper()) return false;
			if (aType.getUpperBound() == null) return false;
			return upperBound.matches((ResolvedType)aType.getUpperBound(),staticOrDynamic).alwaysTrue();
		}
		if (lowerBound != null) {
			// match ? super
			if (!aType.isGenericWildcardSuper()) return false;
			return lowerBound.matches((ResolvedType)aType.getLowerBound(),staticOrDynamic).alwaysTrue();
		}
		return true;
	}
	
	/**
	 * Used in conjunction with checks on 'isStar()' to tell you if this pattern represents '*' or '*[]' which are 
	 * different !
	 */
	public int getDimensions() {
		return dim;
	}
	
	public boolean isArray() {
		return dim > 1;
	}
	
    /**
	 * @param targetTypeName
	 * @return
	 */
	private boolean matchesExactlyByName(String targetTypeName) {
		// we deal with parameter matching separately...
		if (targetTypeName.indexOf('<') != -1) {
			targetTypeName = targetTypeName.substring(0,targetTypeName.indexOf('<'));
		}
		// we deal with bounds matching separately too...
		if (targetTypeName.startsWith(GENERIC_WILDCARD_CHARACTER)) {
			targetTypeName = GENERIC_WILDCARD_CHARACTER;
		}
		//XXX hack
		if (knownMatches == null && importedPrefixes == null) {
			return innerMatchesExactly(targetTypeName);
		}
		
		if (isNamePatternStar()) {
			// we match if the dimensions match
			int numDimensionsInTargetType = 0;
			if (dim > 0) {
				int index;
				while((index = targetTypeName.indexOf('[')) != -1) {
					numDimensionsInTargetType++;
					targetTypeName = targetTypeName.substring(index+1);
				}
				if (numDimensionsInTargetType == dim) {
					return true;
				} else {
					return false;
				}
			}
		}
		
		// if our pattern is length 1, then known matches are exact matches
		// if it's longer than that, then known matches are prefixes of a sort
		if (namePatterns.length == 1) {
			for (int i=0, len=knownMatches.length; i < len; i++) {
				if (knownMatches[i].equals(targetTypeName)) return true;
			}
		} else {
			for (int i=0, len=knownMatches.length; i < len; i++) {
				String knownPrefix = knownMatches[i] + "$";
				if (targetTypeName.startsWith(knownPrefix)) {
					int pos = lastIndexOfDotOrDollar(knownMatches[i]);
					if (innerMatchesExactly(targetTypeName.substring(pos+1))) {
						return true;
					}
				}
			}
		}


		// if any prefixes match, strip the prefix and check that the rest matches
		// assumes that prefixes have a dot at the end
		for (int i=0, len=importedPrefixes.length; i < len; i++) {
			String prefix = importedPrefixes[i];
			//System.err.println("prefix match? " + prefix + " to " + targetTypeName);
			if (targetTypeName.startsWith(prefix)) {
				
				if (innerMatchesExactly(targetTypeName.substring(prefix.length()))) {
					return true;
				}
			}
		}
		
		return innerMatchesExactly(targetTypeName);
	}

	private int lastIndexOfDotOrDollar(String string) {
    	int dot = string.lastIndexOf('.');
    	int dollar = string.lastIndexOf('$');
    	return Math.max(dot, dollar);
    }

	
	private boolean innerMatchesExactly(String targetTypeName) {
		//??? doing this everytime is not very efficient
		char[][] names = splitNames(targetTypeName);

        return innerMatchesExactly(names);
	}

    private boolean innerMatchesExactly(char[][] names) {
        		
        		int namesLength = names.length;
        		int patternsLength = namePatterns.length;
        		
        		int namesIndex = 0;
        		int patternsIndex = 0;
        		
        		if (ellipsisCount == 0) {
        			if (namesLength != patternsLength) return false;
        			while (patternsIndex < patternsLength) {
        				if (!namePatterns[patternsIndex++].matches(names[namesIndex++])) {
        					return false;
        				}
        			}
        			return true;
        		} else if (ellipsisCount == 1) {
        			if (namesLength < patternsLength-1) return false;
        			while (patternsIndex < patternsLength) {
        				NamePattern p = namePatterns[patternsIndex++];
        				if (p == NamePattern.ELLIPSIS) {
        					namesIndex = namesLength - (patternsLength-patternsIndex);
        				} else {
        				    if (!p.matches(names[namesIndex++])) {
        					    return false;
        				    }
        				}
        			}
        			return true;
        		} else {
        //            System.err.print("match(\"" + Arrays.asList(namePatterns) + "\", \"" + Arrays.asList(names) + "\") -> ");
                    boolean b = outOfStar(namePatterns, names, 0, 0, patternsLength - ellipsisCount, namesLength, ellipsisCount);
        //            System.err.println(b);
                    return b;
        		}
    }
    private static boolean outOfStar(final NamePattern[] pattern, final char[][] target, 
                                              int           pi,            int       ti, 
                                              int           pLeft,         int       tLeft,
                                       final int            starsLeft) {
        if (pLeft > tLeft) return false;
        while (true) {
            // invariant: if (tLeft > 0) then (ti < target.length && pi < pattern.length) 
            if (tLeft == 0) return true;
            if (pLeft == 0) {
                return (starsLeft > 0);  
            }
            if (pattern[pi] == NamePattern.ELLIPSIS) {
                return inStar(pattern, target, pi+1, ti, pLeft, tLeft, starsLeft-1);
            }
            if (! pattern[pi].matches(target[ti])) {
                return false;
            }
            pi++; ti++; pLeft--; tLeft--;
        }
    }    
    private static boolean inStar(final NamePattern[] pattern, final char[][] target, 
                                            int          pi,            int      ti, 
                                     final int          pLeft,          int      tLeft,
                                            int         starsLeft) {
        // invariant: pLeft > 0, so we know we'll run out of stars and find a real char in pattern
        // of course, we probably can't parse multiple ..'s in a row, but this keeps the algorithm
        // exactly parallel with that in NamePattern
        NamePattern patternChar = pattern[pi];
        while (patternChar == NamePattern.ELLIPSIS) {
            starsLeft--;
            patternChar = pattern[++pi];
        }
        while (true) {
            // invariant: if (tLeft > 0) then (ti < target.length)
            if (pLeft > tLeft) return false;
            if (patternChar.matches(target[ti])) {
                if (outOfStar(pattern, target, pi+1, ti+1, pLeft-1, tLeft-1, starsLeft)) return true;
            }
            ti++; tLeft--;
        }
    }
	
	/**
	 * @see org.aspectj.weaver.TypePattern#matchesInstanceof(IType)
	 */
	public FuzzyBoolean matchesInstanceof(ResolvedType type) {
		//XXX hack to let unmatched types just silently remain so
		if (maybeGetSimpleName() != null) return FuzzyBoolean.NO;
		
		type.getWorld().getMessageHandler().handleMessage(
			new Message("can't do instanceof matching on patterns with wildcards",
				IMessage.ERROR, null, getSourceLocation()));
		return FuzzyBoolean.NO;
	}

	public NamePattern extractName() {
		//System.err.println("extract from : " + Arrays.asList(namePatterns));
		int len = namePatterns.length;
		NamePattern ret = namePatterns[len-1];
		NamePattern[] newNames = new NamePattern[len-1];
		System.arraycopy(namePatterns, 0, newNames, 0, len-1);
		namePatterns = newNames;
		//System.err.println("    left : " + Arrays.asList(namePatterns));
		return ret;
	}
	
	/**
	 * Method maybeExtractName.
	 * @param string
	 * @return boolean
	 */
	public boolean maybeExtractName(String string) {
		int len = namePatterns.length;
		NamePattern ret = namePatterns[len-1];
		String simple = ret.maybeGetSimpleName();
		if (simple != null && simple.equals(string)) {
			extractName();
			return true;
		}
		return false;
	}
    	
	/**
	 * If this type pattern has no '.' or '*' in it, then
	 * return a simple string
	 * 
	 * otherwise, this will return null;
	 */
	public String maybeGetSimpleName() {
		if (namePatterns.length == 1) {
			return namePatterns[0].maybeGetSimpleName();
		}
		return null;
	}
	
	/**
	 * If this type pattern has no '*' or '..' in it
	 */
	public String maybeGetCleanName() {
		if (namePatterns.length == 0) {
			throw new RuntimeException("bad name: " + namePatterns);
		}
		//System.out.println("get clean: " + this);
		StringBuffer buf = new StringBuffer();
		for (int i=0, len=namePatterns.length; i < len; i++) {
			NamePattern p = namePatterns[i];
			String simpleName = p.maybeGetSimpleName();
			if (simpleName == null) return null;
			if (i > 0) buf.append(".");
			buf.append(simpleName);
		}
		//System.out.println(buf);
		return buf.toString();
	}		


	/**
	 * Need to determine if I'm really a pattern or a reference to a formal
	 * 
	 * We may wish to further optimize the case of pattern vs. non-pattern
	 * 
	 * We will be replaced by what we return
	 */
	public TypePattern resolveBindings(IScope scope, Bindings bindings, 
    								boolean allowBinding, boolean requireExactType)
    { 		
    	if (isNamePatternStar()) {
    		TypePattern anyPattern = maybeResolveToAnyPattern(scope, bindings, allowBinding, requireExactType);
    		if (anyPattern != null) return anyPattern;
		}

    	TypePattern bindingTypePattern = maybeResolveToBindingTypePattern(scope, bindings, allowBinding, requireExactType);
    	if (bindingTypePattern != null) return bindingTypePattern;
    	
    	annotationPattern = annotationPattern.resolveBindings(scope,bindings,allowBinding);
    	
		// resolve any type parameters
		if (typeParameters!=null && typeParameters.size()>0) {
			typeParameters.resolveBindings(scope,bindings,allowBinding,requireExactType);
			// now we have to decide whether to create a "generic" type pattern or a "parameterized" type
			// pattern
			// start with the simple rule that if all parameters have resolved to a type variable based pattern
			// then it is generic, otherwise it is parameterized
			// if we have e.g. staticinitialization<T>(Foo<T,String>) then that's a parameterized type
			isGeneric = true;
			TypePattern[] tps = typeParameters.getTypePatterns();
			for (int i = 0; i < tps.length; i++) {
				if (!tps[i].getExactType().isTypeVariableReference()) {
					isGeneric = false;
					break;
				}
			}
		}
		
		// resolve any bounds
		if (upperBound != null) upperBound = upperBound.resolveBindings(scope, bindings, allowBinding, requireExactType);
		if (lowerBound != null) lowerBound = lowerBound.resolveBindings(scope, bindings, allowBinding, requireExactType);
		// amc - additional interface bounds only needed if we support type vars again.
		
		String fullyQualifiedName = maybeGetCleanName();
		if (fullyQualifiedName != null) {
			return resolveBindingsFromFullyQualifiedTypeName(fullyQualifiedName, scope, bindings, allowBinding, requireExactType);
		} else {
			if (requireExactType) {
				scope.getWorld().getMessageHandler().handleMessage(
					MessageUtil.error(WeaverMessages.format(WeaverMessages.WILDCARD_NOT_ALLOWED),
										getSourceLocation()));
				return NO;
			}
			importedPrefixes = scope.getImportedPrefixes();
			knownMatches = preMatch(scope.getImportedNames());			
			return this;  // pattern contains wildcards so can't be resolved to an ExactTypePattern...
			//XXX need to implement behavior for Lint.invalidWildcardTypeName
		}		
	}
	
	private TypePattern maybeResolveToAnyPattern(IScope scope, Bindings bindings, 
			boolean allowBinding, boolean requireExactType) {
 	    // If there is an annotation specified we have to
	    // use a special variant of Any TypePattern called
	    // AnyWithAnnotation
		if (annotationPattern == AnnotationTypePattern.ANY) {
		  if (dim == 0 && !isVarArgs && upperBound == null && lowerBound == null && (additionalInterfaceBounds == null || additionalInterfaceBounds.length==0)) { // pr72531
			return TypePattern.ANY;  //??? loses source location
		  } 
		} else {
	    	annotationPattern = annotationPattern.resolveBindings(scope,bindings,allowBinding);
			AnyWithAnnotationTypePattern ret = new AnyWithAnnotationTypePattern(annotationPattern); 			
			ret.setLocation(sourceContext,start,end);
			return ret;
		}
		return null; // can't resolve to a simple "any" pattern
	}
	
	private TypePattern maybeResolveToBindingTypePattern(IScope scope, Bindings bindings, 
			boolean allowBinding, boolean requireExactType) {
		String simpleName = maybeGetSimpleName();
		if (simpleName != null) {
			FormalBinding formalBinding = scope.lookupFormal(simpleName);
			if (formalBinding != null) {
				if (bindings == null) {
					scope.message(IMessage.ERROR, this, "negation doesn't allow binding");
					return this;
				}
				if (!allowBinding) {
					scope.message(IMessage.ERROR, this, 
						"name binding only allowed in target, this, and args pcds");
					return this;
				}
				
				BindingTypePattern binding = new BindingTypePattern(formalBinding,isVarArgs);
				binding.copyLocationFrom(this);
				bindings.register(binding, scope);
				
				return binding;
			}
		}
		return null; // not possible to resolve to a binding type pattern
	}
	
	private TypePattern resolveBindingsFromFullyQualifiedTypeName(String fullyQualifiedName, IScope scope, Bindings bindings, 
			boolean allowBinding, boolean requireExactType) {
		String originalName = fullyQualifiedName;
		ResolvedType resolvedTypeInTheWorld = null;
		UnresolvedType type;
		
		//System.out.println("resolve: " + cleanName);
		//??? this loop has too many inefficiencies to count
		resolvedTypeInTheWorld = scope.getWorld().resolve(UnresolvedType.forName(fullyQualifiedName),true);
		while ((type = scope.lookupType(fullyQualifiedName, this)) == ResolvedType.MISSING) {
			int lastDot = fullyQualifiedName.lastIndexOf('.');
			if (lastDot == -1) break;
			fullyQualifiedName = fullyQualifiedName.substring(0, lastDot) + '$' + fullyQualifiedName.substring(lastDot+1);
			if (resolvedTypeInTheWorld == ResolvedType.MISSING)
				resolvedTypeInTheWorld = scope.getWorld().resolve(UnresolvedType.forName(fullyQualifiedName),true);					
		}
		if (type == ResolvedType.MISSING) {
			return resolveBindingsForMissingType(resolvedTypeInTheWorld, originalName, scope, bindings, allowBinding, requireExactType);
		} else {
			return resolveBindingsForExactType(scope,type,fullyQualifiedName,requireExactType);
		}
	}
	
	private TypePattern resolveBindingsForExactType(IScope scope, UnresolvedType aType, String fullyQualifiedName,boolean requireExactType) {
		TypePattern ret = null;
		if (aType.isTypeVariableReference()) {
			// we have to set the bounds on it based on the bounds of this pattern
			ret = resolveBindingsForTypeVariable(scope, (UnresolvedTypeVariableReferenceType) aType);
		} else if (typeParameters.size()>0) {
			ret = resolveParameterizedType(scope, aType, requireExactType);
		} else if (upperBound != null || lowerBound != null) {
			// this must be a generic wildcard with bounds
			ret = resolveGenericWildcard(scope, aType);			
		} else {
			if (dim != 0) aType = UnresolvedType.makeArray(aType, dim);
			ret = new ExactTypePattern(aType,includeSubtypes,isVarArgs);					
		}
		ret.setAnnotationTypePattern(annotationPattern);
		ret.copyLocationFrom(this);
		return ret;
	}

	private TypePattern resolveGenericWildcard(IScope scope, UnresolvedType aType) {
		if (!aType.getSignature().equals(GENERIC_WILDCARD_CHARACTER)) throw new IllegalStateException("Can only have bounds for a generic wildcard");
		boolean canBeExact = true;
		if ((upperBound != null) && (upperBound.getExactType() == ResolvedType.MISSING)) canBeExact = false;
		if ((lowerBound != null) && (lowerBound.getExactType() == ResolvedType.MISSING)) canBeExact = false;
		if (canBeExact) {
			ResolvedType type = null;
			if (upperBound != null) {
				if (upperBound.isIncludeSubtypes()) { 
					canBeExact = false;
				} else {
					ReferenceType upper = (ReferenceType) upperBound.getExactType().resolve(scope.getWorld());
					type = new BoundedReferenceType(upper,true,scope.getWorld());
				}
			} else {
				if (lowerBound.isIncludeSubtypes()) {
					canBeExact = false;
				} else {
					ReferenceType lower = (ReferenceType) lowerBound.getExactType().resolve(scope.getWorld());
					type = new BoundedReferenceType(lower,false,scope.getWorld());
				}
			}
			if (canBeExact) {
				// might have changed if we find out include subtypes is set on one of the bounds...
				return new ExactTypePattern(type,includeSubtypes,isVarArgs);
			}
		} 
	
		// we weren't able to resolve to an exact type pattern...
		// leave as wild type pattern
		importedPrefixes = scope.getImportedPrefixes();
		knownMatches = preMatch(scope.getImportedNames());
		return this;	
	}

	private TypePattern resolveParameterizedType(IScope scope, UnresolvedType aType, boolean requireExactType) {
		if (!verifyTypeParameters(aType.resolve(scope.getWorld()),scope,requireExactType)) return TypePattern.NO; // messages already isued
		// Only if the type is exact *and* the type parameters are exact should we create an 
		// ExactTypePattern for this WildTypePattern					
		if (typeParameters.areAllExact()) {
			TypePattern[] typePats = typeParameters.getTypePatterns();
			UnresolvedType[] typeParameterTypes = new UnresolvedType[typePats.length];
			for (int i = 0; i < typeParameterTypes.length; i++) {
				typeParameterTypes[i] = ((ExactTypePattern)typePats[i]).getExactType();
			}
			ResolvedType type = TypeFactory.createParameterizedType(aType.resolve(scope.getWorld()), typeParameterTypes, scope.getWorld());
			if (isGeneric) type = type.getGenericType();
//				UnresolvedType tx = UnresolvedType.forParameterizedTypes(aType,typeParameterTypes);
//				UnresolvedType type = scope.getWorld().resolve(tx,true); 
			if (dim != 0) type = ResolvedType.makeArray(type, dim);
			return new ExactTypePattern(type,includeSubtypes,isVarArgs);
		} else {
			// AMC... just leave it as a wild type pattern then?
			importedPrefixes = scope.getImportedPrefixes();
			knownMatches = preMatch(scope.getImportedNames());
			return this;
		}
	}
	
	private TypePattern resolveBindingsForMissingType(ResolvedType typeFoundInWholeWorldSearch, String nameWeLookedFor, IScope scope, Bindings bindings, 
			boolean allowBinding, boolean requireExactType) {
		if (requireExactType) {
			if (!allowBinding) {
				scope.getWorld().getMessageHandler().handleMessage(
					MessageUtil.error(WeaverMessages.format(WeaverMessages.CANT_BIND_TYPE,nameWeLookedFor),
									getSourceLocation()));
			} else if (scope.getWorld().getLint().invalidAbsoluteTypeName.isEnabled()) {
				scope.getWorld().getLint().invalidAbsoluteTypeName.signal(nameWeLookedFor, getSourceLocation());
			}
			return NO;
		} else if (scope.getWorld().getLint().invalidAbsoluteTypeName.isEnabled()) {
			// Only put the lint warning out if we can't find it in the world
			if (typeFoundInWholeWorldSearch == ResolvedType.MISSING) {
			  scope.getWorld().getLint().invalidAbsoluteTypeName.signal(nameWeLookedFor, getSourceLocation());
			}
		}
		importedPrefixes = scope.getImportedPrefixes();
		knownMatches = preMatch(scope.getImportedNames());
		return this;		
	}
	
	/**
	 * We resolved the type to a type variable declared in the pointcut designator.
	 * Now we have to create either an exact type pattern or a wild type pattern for it,
	 * with upper and lower bounds set accordingly.
	 * XXX none of this stuff gets serialized yet
	 * @param scope
	 * @param tvrType
	 * @return
	 */
	private TypePattern resolveBindingsForTypeVariable(IScope scope, UnresolvedTypeVariableReferenceType tvrType) {
		Bindings emptyBindings = new Bindings(0);
		if (upperBound != null) {
			upperBound = upperBound.resolveBindings(scope, emptyBindings, false, false);
		}
		if (lowerBound != null) {
			lowerBound = lowerBound.resolveBindings(scope, emptyBindings, false, false);
		}
		if (additionalInterfaceBounds != null) {
			TypePattern[] resolvedIfBounds = new TypePattern[additionalInterfaceBounds.length];
			for (int i = 0; i < resolvedIfBounds.length; i++) {
				resolvedIfBounds[i] = additionalInterfaceBounds[i].resolveBindings(scope, emptyBindings, false, false);				
			}
			additionalInterfaceBounds = resolvedIfBounds;
		}
		if ( upperBound == null && lowerBound == null && additionalInterfaceBounds == null) {
			// no bounds to worry about...
			ResolvedType rType = tvrType.resolve(scope.getWorld());
			if (dim != 0) rType = ResolvedType.makeArray(rType, dim);
			return new ExactTypePattern(rType,includeSubtypes,isVarArgs);								
		} else {
			// we have to set bounds on the TypeVariable held by tvrType before resolving it
			boolean canCreateExactTypePattern = true;
			if (upperBound != null && upperBound.getExactType() == ResolvedType.MISSING) canCreateExactTypePattern = false;
			if (lowerBound != null && lowerBound.getExactType() == ResolvedType.MISSING) canCreateExactTypePattern = false;
			if (additionalInterfaceBounds != null) {
				for (int i = 0; i < additionalInterfaceBounds.length; i++) {
					if (additionalInterfaceBounds[i].getExactType() == ResolvedType.MISSING) canCreateExactTypePattern = false;
				}
			}
			if (canCreateExactTypePattern) {
				TypeVariable tv = tvrType.getTypeVariable();
				if (upperBound != null) tv.setUpperBound(upperBound.getExactType());
				if (lowerBound != null) tv.setLowerBound(lowerBound.getExactType());
				if (additionalInterfaceBounds != null) {
					UnresolvedType[] ifBounds = new UnresolvedType[additionalInterfaceBounds.length];
					for (int i = 0; i < ifBounds.length; i++) {
						ifBounds[i] = additionalInterfaceBounds[i].getExactType();
					}
					tv.setAdditionalInterfaceBounds(ifBounds);
				}
				ResolvedType rType = tvrType.resolve(scope.getWorld());
				if (dim != 0) rType = ResolvedType.makeArray(rType, dim);
				return new ExactTypePattern(rType,includeSubtypes,isVarArgs);								
			}
			return this;  // leave as wild type pattern then
		}
	}
	
	/**
	 * When this method is called, we have resolved the base type to an exact type.
	 * We also have a set of type patterns for the parameters.
	 * Time to perform some basic checks:
	 *  - can the base type be parameterized? (is it generic)
	 *  - can the type parameter pattern list match the number of parameters on the base type
	 *  - do all parameter patterns meet the bounds of the respective type variables
	 *  If any of these checks fail, a warning message is issued and we return false.
	 * @return
	 */
	private boolean verifyTypeParameters(ResolvedType baseType,IScope scope, boolean requireExactType) {
		ResolvedType genericType = baseType.getGenericType();
		if (genericType == null) {
			// issue message "does not match because baseType.getName() is not generic"
			scope.message(MessageUtil.warn(
					WeaverMessages.format(WeaverMessages.NOT_A_GENERIC_TYPE,genericType.getName()),
					getSourceLocation()));
			return false;
		}
		int minRequiredTypeParameters = typeParameters.size();
		boolean foundEllipsis = false;
		TypePattern[] typeParamPatterns = typeParameters.getTypePatterns();
		for (int i = 0; i < typeParamPatterns.length; i++) {
			if (typeParamPatterns[i] instanceof WildTypePattern) {
				WildTypePattern wtp = (WildTypePattern) typeParamPatterns[i];
				if (wtp.ellipsisCount > 0) {
					foundEllipsis = true;
					minRequiredTypeParameters--;
				}
			}
		}
		TypeVariable[] tvs = genericType.getTypeVariables();
		if ((tvs.length < minRequiredTypeParameters) ||
			(!foundEllipsis && minRequiredTypeParameters != tvs.length))
		{
			// issue message "does not match because wrong no of type params"
			String msg = WeaverMessages.format(WeaverMessages.INCORRECT_NUMBER_OF_TYPE_ARGUMENTS,
					                           genericType.getName(),new Integer(tvs.length));
			if (requireExactType)   scope.message(MessageUtil.error(msg,getSourceLocation()));
			else					scope.message(MessageUtil.warn(msg,getSourceLocation()));
			return false;
		} 
		
		// now check that each typeParameter pattern, if exact, matches the bounds
		// of the type variable.
		if (typeParameters.areAllExact()) {
			for (int i = 0; i < tvs.length; i++) {
				UnresolvedType ut = typeParamPatterns[i].getExactType();
				if (!tvs[i].canBeBoundTo(ut.resolve(scope.getWorld()))) {
					// issue message that type parameter does not meet specification
					String parameterName = ut.getName();
					if (ut.isTypeVariableReference()) parameterName = ((TypeVariableReference)ut).getTypeVariable().getDisplayName();
					String msg = 
						WeaverMessages.format(
							WeaverMessages.VIOLATES_TYPE_VARIABLE_BOUNDS,
							parameterName,
							new Integer(i+1),
							tvs[i].getDisplayName(),
							genericType.getName());
					if (requireExactType)  scope.message(MessageUtil.error(msg,getSourceLocation()));	
					else				   scope.message(MessageUtil.warn(msg,getSourceLocation()));	
					return false;
				}
			}
		}
		return true;
	}
	
	public TypePattern resolveBindingsFromRTTI(boolean allowBinding, boolean requireExactType) {
	   	if (isStar()) {
			return TypePattern.ANY;  //??? loses source location
		}

		String cleanName = maybeGetCleanName();
		if (cleanName != null) {
			Class clazz = null;
			clazz = maybeGetPrimitiveClass(cleanName);

			while (clazz == null) {
				try {
					clazz = Class.forName(cleanName);
				} catch (ClassNotFoundException cnf) {
					int lastDotIndex = cleanName.lastIndexOf('.');
					if (lastDotIndex == -1) break;
					cleanName = cleanName.substring(0, lastDotIndex) + '$' + cleanName.substring(lastDotIndex+1);
				}
			}
			
			if (clazz == null) {
				try {
					clazz = Class.forName("java.lang." + cleanName);
				} catch (ClassNotFoundException cnf) {
				}
			}

			if (clazz == null) {
				if (requireExactType) {
					return NO;
				}
			} else {
				UnresolvedType type = UnresolvedType.forName(clazz.getName());
				if (dim != 0) type = UnresolvedType.makeArray(type,dim);
				TypePattern ret = new ExactTypePattern(type, includeSubtypes,isVarArgs);
				ret.copyLocationFrom(this);
				return ret;
			}
		} else if (requireExactType) {
		 	return NO;
		}
					
		importedPrefixes = SimpleScope.javaLangPrefixArray;
		knownMatches = new String[0];
		
		return this;	
	}

	private Class maybeGetPrimitiveClass(String typeName) {
		return (Class) ExactTypePattern.primitiveTypesMap.get(typeName);
	}
	
	public boolean isStar() {
		boolean annPatternStar = annotationPattern == AnnotationTypePattern.ANY;
		return (isNamePatternStar() && annPatternStar);
	}
	
	private boolean isNamePatternStar() {
		return namePatterns.length == 1 && namePatterns[0].isAny();
	}

	/**
	 * returns those possible matches which I match exactly the last element of
	 */
	private String[] preMatch(String[] possibleMatches) {
		//if (namePatterns.length != 1) return CollectionUtil.NO_STRINGS;
		
		List ret = new ArrayList();
		for (int i=0, len=possibleMatches.length; i < len; i++) {
			char[][] names = splitNames(possibleMatches[i]); //??? not most efficient
			if (namePatterns[0].matches(names[names.length-1])) {
				ret.add(possibleMatches[i]);
			}
		}
		return (String[])ret.toArray(new String[ret.size()]);
	}
	
    
//	public void postRead(ResolvedType enclosingType) {
//		this.importedPrefixes = enclosingType.getImportedPrefixes();
//		this.knownNames = prematch(enclosingType.getImportedNames());
//	}


    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	if (annotationPattern != AnnotationTypePattern.ANY) {
    		buf.append('(');
    		buf.append(annotationPattern.toString());
    		buf.append(' ');
    	}
    	for (int i=0, len=namePatterns.length; i < len; i++) {
    		NamePattern name = namePatterns[i];
    		if (name == null) {
    			buf.append(".");
    		} else {
    			if (i > 0) buf.append(".");
    			buf.append(name.toString());
    		}
    	}
    	if (upperBound != null) {
    		buf.append(" extends ");
    		buf.append(upperBound.toString());
    	}
    	if (lowerBound != null) {
    		buf.append(" super ");
    		buf.append(lowerBound.toString());
    	}
		if (typeParameters!=null && typeParameters.size()!=0) {
			buf.append("<");
			buf.append(typeParameters.toString());
			buf.append(">");
		}
    	if (includeSubtypes) buf.append('+');
		if (isVarArgs) buf.append("...");
    	if (annotationPattern != AnnotationTypePattern.ANY) {
    		buf.append(')');
    	}
    	return buf.toString();
    }
    
    public boolean equals(Object other) {
    	if (!(other instanceof WildTypePattern)) return false;
    	WildTypePattern o = (WildTypePattern)other;
    	int len = o.namePatterns.length;
    	if (len != this.namePatterns.length) return false;
    	if (this.includeSubtypes != o.includeSubtypes) return false;
    	if (this.dim != o.dim) return false;
    	if (this.isVarArgs != o.isVarArgs) return false;
    	if (this.upperBound != null) {
    		if (o.upperBound == null) return false;
    	   	if (!this.upperBound.equals(o.upperBound)) return false;
    	} else {
    		if (o.upperBound != null) return false;
    	}
    	if (this.lowerBound != null) {
    		if (o.lowerBound == null) return false;
    	   	if (!this.lowerBound.equals(o.lowerBound)) return false;
    	} else {
    		if (o.lowerBound != null) return false;
    	}
    	if (!typeParameters.equals(o.typeParameters)) return false;
    	for (int i=0; i < len; i++) {
    		if (!o.namePatterns[i].equals(this.namePatterns[i])) return false;
    	}
    	return (o.annotationPattern.equals(this.annotationPattern));    	
	}

    public int hashCode() {
        int result = 17;
        for (int i = 0, len = namePatterns.length; i < len; i++) {
            result = 37*result + namePatterns[i].hashCode();
        }
        result = 37*result + annotationPattern.hashCode();
        if (upperBound != null) result = 37*result + upperBound.hashCode();
        if (lowerBound != null) result = 37*result + lowerBound.hashCode();
        return result;
    }

    
    public FuzzyBoolean matchesInstanceof(Class type) {
    	return FuzzyBoolean.NO;
    }
    
    public boolean matchesExactly(Class type) {
    	return matchesExactlyByName(type.getName());
    }
    
    
    private static final byte VERSION = 1; // rev on change
	/**
	 * @see org.aspectj.weaver.patterns.PatternNode#write(DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(TypePattern.WILD);
		s.writeByte(VERSION);
		s.writeShort(namePatterns.length);
		for (int i = 0; i < namePatterns.length; i++) {
			namePatterns[i].write(s);
		}
		s.writeBoolean(includeSubtypes);
		s.writeInt(dim);
		s.writeBoolean(isVarArgs);
		typeParameters.write(s);  // ! change from M2
		//??? storing this information with every type pattern is wasteful of .class
		//    file size. Storing it on enclosing types would be more efficient
		FileUtil.writeStringArray(knownMatches, s);
		FileUtil.writeStringArray(importedPrefixes, s);
		writeLocation(s);
		annotationPattern.write(s);
		// generics info, new in M3
		s.writeBoolean(isGeneric);
		s.writeBoolean(upperBound != null);
		if (upperBound != null) upperBound.write(s);
		s.writeBoolean(lowerBound != null);
		if (lowerBound != null) lowerBound.write(s);
		s.writeInt(additionalInterfaceBounds == null ? 0 : additionalInterfaceBounds.length);
		if (additionalInterfaceBounds != null) {
			for (int i = 0; i < additionalInterfaceBounds.length; i++) {
				additionalInterfaceBounds[i].write(s);
			}
		}
	}
	
	public static TypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		if (s.getMajorVersion()>=AjAttribute.WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ150) {
			return readTypePattern150(s,context);
		} else {
			return readTypePatternOldStyle(s,context);
	    }
    }

    public static TypePattern readTypePattern150(VersionedDataInputStream s, ISourceContext context) throws IOException {
	  byte version = s.readByte();
	  if (version > VERSION) {
		throw new BCException("WildTypePattern was written by a more recent version of AspectJ, cannot read");
	  }
	  int len = s.readShort();
	  NamePattern[] namePatterns = new NamePattern[len];
	  for (int i=0; i < len; i++) {
		namePatterns[i] = NamePattern.read(s);
	  }
	  boolean includeSubtypes = s.readBoolean();
	  int dim = s.readInt();
	  boolean varArg = s.readBoolean();
	  TypePatternList typeParams = TypePatternList.read(s, context);
	  WildTypePattern ret = new WildTypePattern(namePatterns, includeSubtypes, dim, varArg,typeParams);
	  ret.knownMatches = FileUtil.readStringArray(s);
	  ret.importedPrefixes = FileUtil.readStringArray(s);
	  ret.readLocation(context, s);
	  ret.setAnnotationTypePattern(AnnotationTypePattern.read(s,context));
	  // generics info, new in M3
	  ret.isGeneric = s.readBoolean();
	  if (s.readBoolean()) {
		  ret.upperBound = TypePattern.read(s,context);
	  }
	  if (s.readBoolean()) {
		  ret.lowerBound = TypePattern.read(s,context);
	  }
	  int numIfBounds = s.readInt();
	  if (numIfBounds > 0) {
		  ret.additionalInterfaceBounds = new TypePattern[numIfBounds];
		  for (int i = 0; i < numIfBounds; i++) {
			ret.additionalInterfaceBounds[i] = TypePattern.read(s,context);
		}
	  }
	  return ret;
	}
    
	public static TypePattern readTypePatternOldStyle(VersionedDataInputStream s, ISourceContext context) throws IOException {
		int len = s.readShort();
		NamePattern[] namePatterns = new NamePattern[len];
		for (int i=0; i < len; i++) {
			namePatterns[i] = NamePattern.read(s);
		}
		boolean includeSubtypes = s.readBoolean();
		int dim = s.readInt();
		WildTypePattern ret = new WildTypePattern(namePatterns, includeSubtypes, dim, false,null);
		ret.knownMatches = FileUtil.readStringArray(s);
		ret.importedPrefixes = FileUtil.readStringArray(s);
		ret.readLocation(context, s);
		return ret;
	}

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

}
