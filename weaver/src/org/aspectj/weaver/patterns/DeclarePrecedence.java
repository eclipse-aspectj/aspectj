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
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;

public class DeclarePrecedence extends Declare {
	private TypePatternList patterns;
	

	public DeclarePrecedence(List patterns) {
		this(new TypePatternList(patterns));
	}
	
	private DeclarePrecedence(TypePatternList patterns) {
		this.patterns = patterns;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("declare precedence: ");
		buf.append(patterns);
		buf.append(";");
		return buf.toString();
	}
	
	public boolean equals(Object other) { 
		if (!(other instanceof DeclarePrecedence)) return false;
		DeclarePrecedence o = (DeclarePrecedence)other;
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

	public static Declare read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		Declare ret = new DeclarePrecedence(TypePatternList.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}
	
    public void resolve(IScope scope) {
    	patterns = patterns.resolveBindings(scope, Bindings.NONE, false, false); 
    	boolean seenStar = false;
    	
    	for (int i=0; i < patterns.size(); i++) {
    		TypePattern pi = patterns.get(i);
    		if (pi.isStar()) {
    			if (seenStar) {
    				scope.getWorld().showMessage(IMessage.ERROR,
    						WeaverMessages.format(WeaverMessages.TWO_STARS_IN_PRECEDENCE),
							pi.getSourceLocation(), null);    				
    			}
    			seenStar = true;
    			continue;
    		}
    		ResolvedTypeX exactType = pi.getExactType().resolve(scope.getWorld());
    		if (exactType == ResolvedTypeX.MISSING) continue;
    		
    		// Cannot do a dec prec specifying a non-aspect types unless suffixed with a '+'
    		if (!exactType.isAspect() && !pi.isIncludeSubtypes()) {
    			scope.getWorld().showMessage(IMessage.ERROR,
    					WeaverMessages.format(WeaverMessages.CLASSES_IN_PRECEDENCE,exactType.getName()),
						pi.getSourceLocation(),null);
    		}
    		
    		for (int j=0; j < patterns.size(); j++) {
    			if (j == i) continue;
    			TypePattern pj = patterns.get(j);
    			if (pj.isStar()) continue;
    			if (pj.matchesStatically(exactType)) {
    				scope.getWorld().showMessage(IMessage.ERROR,
    						WeaverMessages.format(WeaverMessages.TWO_PATTERN_MATCHES_IN_PRECEDENCE,exactType.getName()), 
							pi.getSourceLocation(), pj.getSourceLocation());
    			}
    		}
    	}    	
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
			} else if (p.matchesStatically(a)) {
				if (knownMatch != -1) {
					a.getWorld().showMessage(IMessage.ERROR,
							WeaverMessages.format(WeaverMessages.MULTIPLE_MATCHES_IN_PRECEDENCE,a,patterns.get(knownMatch),p),
							patterns.get(knownMatch).getSourceLocation(), p.getSourceLocation());
					return -1;
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
