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
import java.util.ArrayList;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FileUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;

//XXX need to use dim in matching
public class WildTypePattern extends TypePattern {
	NamePattern[] namePatterns;
	int ellipsisCount;
	String[] importedPrefixes;
	String[] knownMatches;
	int dim;

	WildTypePattern(NamePattern[] namePatterns, boolean includeSubtypes, int dim) {
		super(includeSubtypes);
		this.namePatterns = namePatterns;
		this.dim = dim;
		ellipsisCount = 0;
		for (int i=0; i<namePatterns.length; i++) {
			if (namePatterns[i] == NamePattern.ELLIPSIS) ellipsisCount++;
		}
		setLocation(namePatterns[0].getSourceContext(), namePatterns[0].getStart(), namePatterns[namePatterns.length-1].getEnd());
	}

	public WildTypePattern(List names, boolean includeSubtypes, int dim) {
		this((NamePattern[])names.toArray(new NamePattern[names.size()]), includeSubtypes, dim);

	}
	
	public WildTypePattern(List names, boolean includeSubtypes, int dim, int endPos) {
		this(names, includeSubtypes, dim);
		this.end = endPos;
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
		String targetTypeName = type.getName();
		
		//System.err.println("match: " + targetTypeName + ", " + knownMatches); //Arrays.asList(importedPrefixes));
		
		//XXX hack
		if (knownMatches == null && importedPrefixes == null) {
			return innerMatchesExactly(targetTypeName);
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
    	if (isStar()) {
			return TypePattern.ANY;  //??? loses source location
		}

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
				
				BindingTypePattern binding = new BindingTypePattern(formalBinding);
				binding.copyLocationFrom(this);
				bindings.register(binding, scope);
				
				return binding;
			}
		}
		
		String cleanName = maybeGetCleanName();
		if (cleanName != null) {
			TypeX type;
			
			//System.out.println("resolve: " + cleanName);
			//??? this loop has too many inefficiencies to count
			while ((type = scope.lookupType(cleanName, this)) == ResolvedTypeX.MISSING) {
				int lastDot = cleanName.lastIndexOf('.');
				if (lastDot == -1) break;
				cleanName = cleanName.substring(0, lastDot) + '$' + cleanName.substring(lastDot+1);
			}
			if (type == ResolvedTypeX.MISSING) {
				if (requireExactType) {
					if (!allowBinding) {
						scope.getWorld().getMessageHandler().handleMessage(
							MessageUtil.error("can't bind type name '" + cleanName + "'",
											getSourceLocation()));
					} else if (scope.getWorld().getLint().invalidAbsoluteTypeName.isEnabled()) {
						scope.getWorld().getLint().invalidAbsoluteTypeName.signal(cleanName, getSourceLocation());
					}
					return NO;
				} else if (scope.getWorld().getLint().invalidAbsoluteTypeName.isEnabled()) {
					scope.getWorld().getLint().invalidAbsoluteTypeName.signal(cleanName, getSourceLocation());
				}
			} else {
				if (dim != 0) type = TypeX.makeArray(type, dim);
				TypePattern ret = new ExactTypePattern(type, includeSubtypes);
				ret.copyLocationFrom(this);
				return ret;
			}
		} else {
			if (requireExactType) {
				scope.getWorld().getMessageHandler().handleMessage(
					MessageUtil.error("wildcard type pattern not allowed, must use type name",
										getSourceLocation()));
				return NO;
			}
			//XXX need to implement behavior for Lint.invalidWildcardTypeName
		}
		
		importedPrefixes = scope.getImportedPrefixes();
		knownMatches = preMatch(scope.getImportedNames());
		
		return this;
	}
	
	public boolean isStar() {
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
    	for (int i=0, len=namePatterns.length; i < len; i++) {
    		NamePattern name = namePatterns[i];
    		if (name == null) {
    			buf.append(".");
    		} else {
    			if (i > 0) buf.append(".");
    			buf.append(name.toString());
    		}
    	}
    	return buf.toString();
    }
    
    public boolean equals(Object other) {
    	if (!(other instanceof WildTypePattern)) return false;
    	WildTypePattern o = (WildTypePattern)other;
    	int len = o.namePatterns.length;
    	if (len != this.namePatterns.length) return false;
    	for (int i=0; i < len; i++) {
    		if (!o.namePatterns[i].equals(this.namePatterns[i])) return false;
    	}
    	return true;
	}

    public int hashCode() {
        int result = 17;
        for (int i = 0, len = namePatterns.length; i < len; i++) {
            result = 37*result + namePatterns[i].hashCode();
        }
        return result;
    }
    
	/**
	 * @see org.aspectj.weaver.patterns.PatternNode#write(DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(TypePattern.WILD);
		s.writeShort(namePatterns.length);
		for (int i = 0; i < namePatterns.length; i++) {
			namePatterns[i].write(s);
		}
		s.writeBoolean(includeSubtypes);
		s.writeInt(dim);
		//??? storing this information with every type pattern is wasteful of .class
		//    file size. Storing it on enclosing types would be more efficient
		FileUtil.writeStringArray(knownMatches, s);
		FileUtil.writeStringArray(importedPrefixes, s);
		writeLocation(s);
	}
	
	public static TypePattern read(DataInputStream s, ISourceContext context) throws IOException {
		int len = s.readShort();
		NamePattern[] namePatterns = new NamePattern[len];
		for (int i=0; i < len; i++) {
			namePatterns[i] = NamePattern.read(s);
		}
		boolean includeSubtypes = s.readBoolean();
		int dim = s.readInt();
		WildTypePattern ret = new WildTypePattern(namePatterns, includeSubtypes, dim);
		ret.knownMatches = FileUtil.readStringArray(s);
		ret.importedPrefixes = FileUtil.readStringArray(s);
		ret.readLocation(context, s);
		return ret;
	}

}
