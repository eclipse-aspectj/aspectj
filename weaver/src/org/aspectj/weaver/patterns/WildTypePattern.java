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
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;

//XXX need to use dim in matching
public class WildTypePattern extends TypePattern {
	NamePattern[] namePatterns;
	int ellipsisCount;
	String[] importedPrefixes;
	String[] knownMatches;
	int dim;

	WildTypePattern(NamePattern[] namePatterns, boolean includeSubtypes, int dim, boolean isVarArgs) {
		super(includeSubtypes,isVarArgs);
		this.namePatterns = namePatterns;
		this.dim = dim;
		ellipsisCount = 0;
		for (int i=0; i<namePatterns.length; i++) {
			if (namePatterns[i] == NamePattern.ELLIPSIS) ellipsisCount++;
		}
		setLocation(namePatterns[0].getSourceContext(), namePatterns[0].getStart(), namePatterns[namePatterns.length-1].getEnd());
	}

	public WildTypePattern(List names, boolean includeSubtypes, int dim) {
		this((NamePattern[])names.toArray(new NamePattern[names.size()]), includeSubtypes, dim,false);

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
		TypeX otherType = other.getExactType();
		if (otherType != ResolvedTypeX.MISSING) {
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
	protected boolean matchesExactly(ResolvedTypeX type) {
		return matchesExactly(type,type);
	}

	protected boolean matchesExactly(ResolvedTypeX type, ResolvedTypeX annotatedType) {
		String targetTypeName = type.getName();
		
		//System.err.println("match: " + targetTypeName + ", " + knownMatches); //Arrays.asList(importedPrefixes));
		// Ensure the annotation pattern is resolved
		annotationPattern.resolve(type.getWorld());
		
		return matchesExactlyByName(targetTypeName) &&
		       annotationPattern.matches(annotatedType).alwaysTrue();
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
	public FuzzyBoolean matchesInstanceof(ResolvedTypeX type) {
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
    	    // If there is an annotation specified we have to
    	    // use a special variant of Any TypePattern called
    	    // AnyWithAnnotation
    		if (annotationPattern == AnnotationTypePattern.ANY) {
    		  if (dim == 0) { // pr72531
    			return TypePattern.ANY;  //??? loses source location
    		  } 
    		} else {
    	    	annotationPattern = annotationPattern.resolveBindings(scope,bindings,allowBinding);
    			AnyWithAnnotationTypePattern ret = new AnyWithAnnotationTypePattern(annotationPattern); 			
    			ret.setLocation(sourceContext,start,end);
    			return ret;
    		}
		}

    	annotationPattern = annotationPattern.resolveBindings(scope,bindings,allowBinding);
    	
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
		
		String cleanName = maybeGetCleanName();
		String originalName = cleanName;
		// if we discover it is 'MISSING' when searching via the scope, this next local var will
		// tell us if it is really missing or if it does exist in the world and we just can't
		// see it from the current scope.
		ResolvedTypeX resolvedTypeInTheWorld = null;
		if (cleanName != null) {
			TypeX type;
			
			//System.out.println("resolve: " + cleanName);
			//??? this loop has too many inefficiencies to count
			resolvedTypeInTheWorld = scope.getWorld().resolve(TypeX.forName(cleanName),true);
			while ((type = scope.lookupType(cleanName, this)) == ResolvedTypeX.MISSING) {
				int lastDot = cleanName.lastIndexOf('.');
				if (lastDot == -1) break;
				cleanName = cleanName.substring(0, lastDot) + '$' + cleanName.substring(lastDot+1);
				if (resolvedTypeInTheWorld == ResolvedTypeX.MISSING)
					resolvedTypeInTheWorld = scope.getWorld().resolve(TypeX.forName(cleanName),true);					
			}
			if (type == ResolvedTypeX.MISSING) {
				if (requireExactType) {
					if (!allowBinding) {
						scope.getWorld().getMessageHandler().handleMessage(
							MessageUtil.error(WeaverMessages.format(WeaverMessages.CANT_BIND_TYPE,originalName),
											getSourceLocation()));
					} else if (scope.getWorld().getLint().invalidAbsoluteTypeName.isEnabled()) {
						scope.getWorld().getLint().invalidAbsoluteTypeName.signal(originalName, getSourceLocation());
					}
					return NO;
				} else if (scope.getWorld().getLint().invalidAbsoluteTypeName.isEnabled()) {
					// Only put the lint warning out if we can't find it in the world
					if (resolvedTypeInTheWorld == ResolvedTypeX.MISSING)
					  scope.getWorld().getLint().invalidAbsoluteTypeName.signal(originalName, getSourceLocation());
				}
			} else {
				if (dim != 0) type = TypeX.makeArray(type, dim);
				TypePattern ret = new ExactTypePattern(type, includeSubtypes,isVarArgs);
				ret.setAnnotationTypePattern(annotationPattern);
				ret.copyLocationFrom(this);
				return ret;
			}
		} else {
			if (requireExactType) {
				scope.getWorld().getMessageHandler().handleMessage(
					MessageUtil.error(WeaverMessages.format(WeaverMessages.WILDCARD_NOT_ALLOWED),
										getSourceLocation()));
				return NO;
			}
			//XXX need to implement behavior for Lint.invalidWildcardTypeName
		}
		
		importedPrefixes = scope.getImportedPrefixes();
		knownMatches = preMatch(scope.getImportedNames());
		
		return this;
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
				TypeX type = TypeX.forName(clazz.getName());
				if (dim != 0) type = TypeX.makeArray(type,dim);
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
	
    
//	public void postRead(ResolvedTypeX enclosingType) {
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
		//??? storing this information with every type pattern is wasteful of .class
		//    file size. Storing it on enclosing types would be more efficient
		FileUtil.writeStringArray(knownMatches, s);
		FileUtil.writeStringArray(importedPrefixes, s);
		writeLocation(s);
		annotationPattern.write(s);
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
	  WildTypePattern ret = new WildTypePattern(namePatterns, includeSubtypes, dim, varArg);
	  ret.knownMatches = FileUtil.readStringArray(s);
	  ret.importedPrefixes = FileUtil.readStringArray(s);
	  ret.readLocation(context, s);
	  ret.setAnnotationTypePattern(AnnotationTypePattern.read(s,context));
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
		WildTypePattern ret = new WildTypePattern(namePatterns, includeSubtypes, dim, false);
		ret.knownMatches = FileUtil.readStringArray(s);
		ret.importedPrefixes = FileUtil.readStringArray(s);
		ret.readLocation(context, s);
		return ret;
	}

}
