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
import java.util.*;
import java.util.List;

import org.aspectj.weaver.*;
import org.aspectj.weaver.ast.*;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.*;


public class IfPointcut extends Pointcut {
	public ResolvedMember testMethod;
	public int extraParameterFlags;
	
	public Pointcut residueSource;
	int baseArgsCount;
	
	//XXX some way to compute args

	
	public IfPointcut(ResolvedMember testMethod, int extraParameterFlags) {
		this.testMethod = testMethod;
		this.extraParameterFlags = extraParameterFlags;
	}
    
	public FuzzyBoolean match(Shadow shadow) {
		//??? this is not maximally efficient
		return FuzzyBoolean.MAYBE;
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.IF);
		testMethod.write(s);
		s.writeByte(extraParameterFlags);
		writeLocation(s);
	}
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		IfPointcut ret = new IfPointcut(ResolvedMember.readResolvedMember(s, context), s.readByte());
		ret.readLocation(context, s);
		return ret;
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		//??? all we need is good error messages in here in cflow contexts
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof IfPointcut)) return false;
		IfPointcut o = (IfPointcut)other;
		return o.testMethod.equals(this.testMethod);
	}
    public int hashCode() {
        int result = 17;
        result = 37*result + testMethod.hashCode();
        return result;
    }
	public String toString() {
		return "if(" + testMethod + ")";
	}

	private boolean findingResidue = false;
	public Test findResidue(Shadow shadow, ExposedState state) {
		if (findingResidue) return Literal.TRUE;
		findingResidue = true;
		try {
			ExposedState myState = new ExposedState(baseArgsCount);
			//System.out.println(residueSource);
			residueSource.findResidue(shadow, myState); // don't care about Test
			
			//System.out.println(myState);
			
			List args = new ArrayList();
	        for (int i=0; i < baseArgsCount; i++) {
	        	args.add(myState.get(i));
	        }
	
	        // handle thisJoinPoint parameters
	        if ((extraParameterFlags & Advice.ThisJoinPoint) != 0) {
	        	args.add(shadow.getThisJoinPointVar());
	        }
	        
	        if ((extraParameterFlags & Advice.ThisJoinPointStaticPart) != 0) {
	        	args.add(shadow.getThisJoinPointStaticPartVar());
	        }
	        
	        if ((extraParameterFlags & Advice.ThisEnclosingJoinPointStaticPart) != 0) {
	        	args.add(shadow.getThisEnclosingJoinPointStaticPartVar());
	        }
	
			return Test.makeCall(testMethod, (Expr[])args.toArray(new Expr[args.size()]));
		} finally {
			findingResidue = false;
		}
	}
	
	
	public Pointcut concretize(ResolvedTypeX inAspect, IntMap bindings) {
		return this.concretize1(inAspect, bindings);
	}
	
	private IfPointcut partiallyConcretized = null;
	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		//System.err.println("concretize: " + this + " already: " + partiallyConcretized);
		if (partiallyConcretized != null) {
			return partiallyConcretized;
		}
		IfPointcut ret = new IfPointcut(testMethod, extraParameterFlags);
		partiallyConcretized = ret;
		if (bindings.directlyInAdvice()) {
			Advice advice = bindings.getEnclosingAdvice();
			ret.baseArgsCount = advice.getBaseParameterCount();
			ret.residueSource = advice.getPointcut().concretize(inAspect, ret.baseArgsCount, advice);
		} else {
			ResolvedPointcutDefinition def = bindings.peekEnclosingDefinitition();
			if (def == CflowPointcut.CFLOW_MARKER) {
				inAspect.getWorld().getMessageHandler().handleMessage(
				    MessageUtil.error("if not supported lexically within cflow (compiler limitation)",
				    					null));
				return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
			}
			ret.baseArgsCount = def.getParameterTypes().length;
			
			IntMap newBindings = IntMap.idMap(ret.baseArgsCount);
			newBindings.copyContext(bindings);
			ret.residueSource = def.getPointcut().concretize(inAspect, newBindings);
		}
		
		return ret;
	}

}
