/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import java.io.*;
import java.util.List;

import org.aspectj.weaver.*;
import org.aspectj.weaver.ResolvedTypeX;

public class DeclareDominates extends Declare {
	private TypePatternList patterns;
	

	public DeclareDominates(List patterns) {
		this(new TypePatternList(patterns));
	}
	
	private DeclareDominates(TypePatternList patterns) {
		this.patterns = patterns;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("declare dominates: ");
		buf.append(patterns);
		buf.append(";");
		return buf.toString();
	}
	
	public boolean equals(Object other) { 
		if (!(other instanceof DeclareDominates)) return false;
		DeclareDominates o = (DeclareDominates)other;
		return o.patterns.equals(patterns);
	}
    
    public int hashCode() {
    	return patterns.hashCode();
    }


	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Declare.DOMINATES);
		patterns.write(s);
		writeLocation(s);
	}

	public static Declare read(DataInputStream s, ISourceContext context) throws IOException {
		Declare ret = new DeclareDominates(TypePatternList.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}
	
    public void resolve(IScope scope) {
    	patterns = patterns.resolveBindings(scope, Bindings.NONE, false); 	
    }

	public TypePatternList getPatterns() {
		return patterns;
	}

	private int matchingIndex(ResolvedTypeX a) {
		int knownMatch = -1;
		int starMatch = -1;
		for (int i=0, len=patterns.size(); i < len; i++) {
			TypePattern p = patterns.get(i);
			if (p.isStar()) {
				starMatch = i;
			} else if (p.matchesExactly(a)) {
				if (knownMatch != -1) {
					throw new BCException("multiple matches: " + this + " with " + a);
				} else {
					knownMatch = i;
				}
			}
		}
		if (knownMatch == -1) return starMatch;
		else return knownMatch;
	}
	

	public int compare(ResolvedTypeX aspect1, ResolvedTypeX aspect2) {
		int index1 = matchingIndex(aspect1);
		int index2 = matchingIndex(aspect2);
		
		//System.out.println("a1: " + aspect1 + ", " + aspect2 + " = " + index1 + ", " + index2);
		
		if (index1 == -1 || index2 == -1) return 0;
		
		if (index1 == index2) return 0;
		else if (index1 > index2) return -1;
		else return +1;
	}
	
	public boolean isAdviceLike() {
		return false;
	}

}
