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
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.ast.*;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;


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
	
    public FuzzyBoolean fastMatch(ResolvedTypeX type) {
		return FuzzyBoolean.MAYBE;
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


	//??? The implementation of name binding and type checking in if PCDs is very convoluted
	//    There has to be a better way...
	private boolean findingResidue = false;
	public Test findResidue(Shadow shadow, ExposedState state) {
		if (findingResidue) return Literal.TRUE;
		findingResidue = true;
		try {
			ExposedState myState = new ExposedState(baseArgsCount);
			//System.out.println(residueSource);
			//??? we throw out the test that comes from this walk.  All we want here
			//    is bindings for the arguments
			residueSource.findResidue(shadow, myState);
			
			//System.out.println(myState);
			
			Test ret = Literal.TRUE;
			
			List args = new ArrayList();
	        for (int i=0; i < baseArgsCount; i++) {
	        	Var v = myState.get(i);
	        	args.add(v);
	        	ret = Test.makeAnd(ret, 
	        		Test.makeInstanceof(v, 
	        			testMethod.getParameterTypes()[i].resolve(shadow.getIWorld())));
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
	        
	        ret = Test.makeAnd(ret, Test.makeCall(testMethod, (Expr[])args.toArray(new Expr[args.size()])));

			return ret; 
			
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
			ShadowMunger advice = bindings.getEnclosingAdvice();
			ret.baseArgsCount = ((Advice)advice).getBaseParameterCount();
			ret.residueSource = advice.getPointcut().concretize(inAspect, ret.baseArgsCount, advice);
		} else {
			ResolvedPointcutDefinition def = bindings.peekEnclosingDefinitition();
			if (def == CflowPointcut.CFLOW_MARKER) {
				inAspect.getWorld().showMessage(IMessage.ERROR,
					"if not supported lexically within cflow (compiler limitation)",
					getSourceLocation(), null);
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
