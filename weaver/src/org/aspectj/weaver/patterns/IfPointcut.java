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
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.lang.JoinPoint;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;


public class IfPointcut extends Pointcut {
	public ResolvedMember testMethod;
	public int extraParameterFlags;
	
	public Pointcut residueSource;
	int baseArgsCount;
	
	//XXX some way to compute args

	
	public IfPointcut(ResolvedMember testMethod, int extraParameterFlags) {
		this.testMethod = testMethod;
		this.extraParameterFlags = extraParameterFlags;
		this.pointcutKind = IF;
	}
	
	public Set couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS;
	}

	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return FuzzyBoolean.MAYBE;
	}
    
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		//??? this is not maximally efficient
		return FuzzyBoolean.MAYBE;
	}

	public boolean alwaysFalse() {
		return false;
	}
	
	public boolean alwaysTrue() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesDynamically(java.lang.Object, java.lang.Object, java.lang.Object[])
	 */
	public boolean matchesDynamically(Object thisObject, Object targetObject,
			Object[] args) {
		throw new UnsupportedOperationException("If pointcut matching not supported by this operation");
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesStatically(java.lang.String, java.lang.reflect.Member, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesStatically(
			String joinpointKind, Member member, Class thisClass,
			Class targetClass, Member withinCode) {
		throw new UnsupportedOperationException("If pointcut matching not supported by this operation");
	}
	
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.IF);
		testMethod.write(s);
		s.writeByte(extraParameterFlags);
		writeLocation(s);
	}
	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		IfPointcut ret = new IfPointcut(ResolvedMember.readResolvedMember(s, context), s.readByte());
		ret.readLocation(context, s);
		return ret;
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		//??? all we need is good error messages in here in cflow contexts
	}
	
	public void resolveBindingsFromRTTI() {}
	
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
	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
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
	

	// amc - the only reason this override seems to be here is to stop the copy, but 
	// that can be prevented by overriding shouldCopyLocationForConcretization,
	// allowing me to make the method final in Pointcut.
//	public Pointcut concretize(ResolvedTypeX inAspect, IntMap bindings) {
//		return this.concretize1(inAspect, bindings);
//	}
	
	protected boolean shouldCopyLocationForConcretize() {
		return false;
	}
	
	private IfPointcut partiallyConcretized = null;
	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		//System.err.println("concretize: " + this + " already: " + partiallyConcretized);
		
		if (isDeclare(bindings.getEnclosingAdvice())) {
			// Enforce rule about which designators are supported in declare
			inAspect.getWorld().showMessage(IMessage.ERROR,
					WeaverMessages.format(WeaverMessages.IF_IN_DECLARE),
					bindings.getEnclosingAdvice().getSourceLocation(),
					null);
			return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}
		
		if (partiallyConcretized != null) {
			return partiallyConcretized;
		}
		IfPointcut ret = new IfPointcut(testMethod, extraParameterFlags);
		ret.copyLocationFrom(this);
		partiallyConcretized = ret;
		
		// It is possible to directly code your pointcut expression in a per clause
		// rather than defining a pointcut declaration and referencing it in your
		// per clause.  If you do this, we have problems (bug #62458).  For now,
		// let's police that you are trying to code a pointcut in a per clause and
		// put out a compiler error.
		if (bindings.directlyInAdvice() && bindings.getEnclosingAdvice()==null) {
			// Assumption: if() is in a per clause if we say we are directly in advice
			// but we have no enclosing advice.
			inAspect.getWorld().showMessage(IMessage.ERROR,
					WeaverMessages.format(WeaverMessages.IF_IN_PERCLAUSE),
					this.getSourceLocation(),null);
			return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}
		
		if (bindings.directlyInAdvice()) {
			ShadowMunger advice = bindings.getEnclosingAdvice();
			if (advice instanceof Advice) {
				ret.baseArgsCount = ((Advice)advice).getBaseParameterCount();
			} else {
				ret.baseArgsCount = 0;
			}
			ret.residueSource = advice.getPointcut().concretize(inAspect, ret.baseArgsCount, advice);
		} else {
			ResolvedPointcutDefinition def = bindings.peekEnclosingDefinitition();
			if (def == CflowPointcut.CFLOW_MARKER) {
				inAspect.getWorld().showMessage(IMessage.ERROR,
						WeaverMessages.format(WeaverMessages.IF_LEXICALLY_IN_CFLOW),
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

//	public static Pointcut MatchesNothing = new MatchesNothingPointcut();
//	??? there could possibly be some good optimizations to be done at this point
	public static IfPointcut makeIfFalsePointcut(State state) {
		IfPointcut ret = new IfFalsePointcut();
		ret.state = state;
		return ret;
	}

	private static class IfFalsePointcut extends IfPointcut {
		
		public IfFalsePointcut() {
			super(null,0);
		}
		
		public Set couldMatchKinds() {
			return Collections.EMPTY_SET;
		}
		
		public boolean alwaysFalse() {
			return true;
		}
		
		protected Test findResidueInternal(Shadow shadow, ExposedState state) {
			return Literal.FALSE; // can only get here if an earlier error occurred
		}

		public FuzzyBoolean fastMatch(FastMatchInfo type) {
			return FuzzyBoolean.NO;
		}
		
		protected FuzzyBoolean matchInternal(Shadow shadow) {
			return FuzzyBoolean.NO;
		}
		
		public FuzzyBoolean match(JoinPoint.StaticPart jpsp) {
			return FuzzyBoolean.NO;
		}

		public void resolveBindings(IScope scope, Bindings bindings) {
		}
		
		public void resolveBindingsFromRTTI() {
		}

		public void postRead(ResolvedTypeX enclosingType) {
		}

		public Pointcut concretize1(
			ResolvedTypeX inAspect,
			IntMap bindings) {
			if (isDeclare(bindings.getEnclosingAdvice())) {
				// Enforce rule about which designators are supported in declare
				inAspect.getWorld().showMessage(IMessage.ERROR,
				  WeaverMessages.format(WeaverMessages.IF_IN_DECLARE),
				  bindings.getEnclosingAdvice().getSourceLocation(),
				  null);
				return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
			}
			return makeIfFalsePointcut(state);
		}


		public void write(DataOutputStream s) throws IOException {
			s.writeByte(Pointcut.IF_FALSE);
		}
		
	    public int hashCode() {
	        int result = 17;
	        return result;
	    }
		
	    public String toString() {
			return "if(false)";
		}	
	}

	public static IfPointcut makeIfTruePointcut(State state) {
		IfPointcut ret = new IfTruePointcut();
		ret.state = state;
		return ret;
	}

	private static class IfTruePointcut extends IfPointcut {		
			
		public IfTruePointcut() {
			super(null,0);
		}

		public boolean alwaysTrue() {
			return true;
		}
		
		protected Test findResidueInternal(Shadow shadow, ExposedState state) {
			return Literal.TRUE; // can only get here if an earlier error occurred
		}

		public FuzzyBoolean fastMatch(FastMatchInfo type) {
			return FuzzyBoolean.YES;
		}
		
		protected FuzzyBoolean matchInternal(Shadow shadow) {
			return FuzzyBoolean.YES;
		}
		
		public FuzzyBoolean match(JoinPoint.StaticPart jpsp) {
			return FuzzyBoolean.YES;
		}

		public void resolveBindings(IScope scope, Bindings bindings) {
		}
		
		public void resolveBindingsFromRTTI() {
		}

		public void postRead(ResolvedTypeX enclosingType) {
		}

		public Pointcut concretize1(
			ResolvedTypeX inAspect,
			IntMap bindings) {
			if (isDeclare(bindings.getEnclosingAdvice())) {
				// Enforce rule about which designators are supported in declare
				inAspect.getWorld().showMessage(IMessage.ERROR,
						WeaverMessages.format(WeaverMessages.IF_IN_DECLARE),
						bindings.getEnclosingAdvice().getSourceLocation(),
						null);
				return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
			}
			return makeIfTruePointcut(state);
		}


		public void write(DataOutputStream s) throws IOException {
			s.writeByte(IF_TRUE);
		}
		
	    public int hashCode() {
	        int result = 37;
	        return result;
	    }
		
	    public String toString() {
			return "if(true)";
		}	
	}

}

