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


package org.aspectj.weaver.bcel;

import org.apache.bcel.generic.*;
import org.aspectj.weaver.*;
import org.aspectj.weaver.ast.*;
import org.aspectj.weaver.patterns.*;

/**
 * Advice implemented for bcel.
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public class BcelAdvice extends Advice {
	private Test pointcutTest;
	private ExposedState exposedState;

	public BcelAdvice(AdviceKind kind, Pointcut pointcut, Member signature,
		int extraArgumentFlags,
        int start, int end, ISourceContext sourceContext, ResolvedTypeX concreteAspect)
    {
		super(kind, pointcut, signature, extraArgumentFlags, start, end, sourceContext);
		this.concreteAspect = concreteAspect;
	}

    // ---- implementations of ShadowMunger's methods
    
    public void specializeOn(Shadow shadow) {
	  	if (getKind() == AdviceKind.Around) {
	  		((BcelShadow)shadow).initializeForAroundClosure();
	  	}
    	
    	//XXX this case is just here for supporting lazy test code
    	if (getKind() == null) {
			exposedState = new ExposedState(0);
    		return;
    	}
    	if (getKind().isPerEntry()) {
    		exposedState = new ExposedState(0);
    	} else if (getKind().isCflow()) {
    		exposedState = new ExposedState(nFreeVars);
    	} else if (getSignature() != null) {
			exposedState = new ExposedState(getSignature());
    	} else {
    		exposedState = new ExposedState(0);
    		return;  //XXX this case is just here for supporting lazy test code
    	}
		pointcutTest = getPointcut().findResidue(shadow, exposedState);
		
        // make sure thisJoinPoint parameters are initialized
        if ((extraParameterFlags & ThisJoinPoint) != 0) {
        	((BcelShadow)shadow).getThisJoinPointVar();
        }
        
        if ((extraParameterFlags & ThisJoinPointStaticPart) != 0) {
        	((BcelShadow)shadow).getThisJoinPointStaticPartVar();
        }
        
        if ((extraParameterFlags & ThisEnclosingJoinPointStaticPart) != 0) {
        	((BcelShadow)shadow).getThisEnclosingJoinPointStaticPartVar();
        }
    }   
       
    public void implementOn(Shadow s) {
        BcelShadow shadow = (BcelShadow) s;       
        if (getKind() == AdviceKind.Before) {
            shadow.weaveBefore(this);
        } else if (getKind() == AdviceKind.AfterReturning) {
            shadow.weaveAfterReturning(this);
        } else if (getKind() == AdviceKind.AfterThrowing) {
            TypeX catchType = 
                hasExtraParameter()
                ? getExtraParameterType()
                : TypeX.THROWABLE;
            shadow.weaveAfterThrowing(this, catchType);
        } else if (getKind() == AdviceKind.After) {   
            shadow.weaveAfter(this);
        } else if (getKind() == AdviceKind.Around) {
            shadow.weaveAroundClosure(this, hasDynamicTests());
        } else if (getKind() == AdviceKind.InterInitializer) {
        	 shadow.weaveAfterReturning(this);
        } else if (getKind().isCflow()) {
        	 shadow.weaveCflowEntry(this, getSignature());
        } else if (getKind() == AdviceKind.PerThisEntry) {
        	 shadow.weavePerObjectEntry(this, (BcelVar)shadow.getThisVar());
        } else if (getKind() == AdviceKind.PerTargetEntry) {
        	 shadow.weavePerObjectEntry(this, (BcelVar)shadow.getTargetVar());
        } else if (getKind() == AdviceKind.Softener) {
        	 shadow.weaveSoftener(this, ((ExactTypePattern)exceptionType).getType());
        } else {
            throw new BCException("unimplemented kind: " + getKind());
        }
    }

    // ---- implementations

    // only call me after prepare has been called
    public boolean hasDynamicTests() {
//    	if (hasExtraParameter() && getKind() == AdviceKind.AfterReturning) {
//            TypeX extraParameterType = getExtraParameterType();
//            if (! extraParameterType.equals(TypeX.OBJECT) 
//            		&& ! extraParameterType.isPrimitive())
//            	return true;
//    	}
    	
    	
        return pointcutTest != null && 
        	!(pointcutTest == Literal.TRUE);// || pointcutTest == Literal.NO_TEST);
    }


	/**
	 * get the instruction list for the really simple version of this advice.  
	 * Is broken apart
	 * for other advice, but if you want it in one block, this is the method to call.
	 * 
	 * @param s The shadow around which these instructions will eventually live.
	 * @param extraArgVar The var that will hold the return value or thrown exception 
	 * 			for afterX advice
	 * @param ifNoAdvice The instructionHandle to jump to if the dynamic 
	 * 			tests for this munger fails.
	 */
	InstructionList getAdviceInstructions(
		BcelShadow s,
		BcelVar extraArgVar,
		InstructionHandle ifNoAdvice) 
	{
        BcelShadow shadow = (BcelShadow) s;
        InstructionFactory fact = shadow.getFactory();
        BcelWorld world = shadow.getWorld();
		
		InstructionList il = new InstructionList();

        // we test to see if we have the right kind of thing...
        // after throwing does this just by the exception mechanism.
        if (hasExtraParameter() && getKind() == AdviceKind.AfterReturning) {
            TypeX extraParameterType = getExtraParameterType();
            if (! extraParameterType.equals(TypeX.OBJECT) 
            		&& ! extraParameterType.isPrimitive()) {
                il.append(
                    BcelRenderer.renderTest(
                        fact, 
                        world,
                        Test.makeInstanceof(
                            extraArgVar, getExtraParameterType().resolve(world)),
                        null,
                        ifNoAdvice,
                        null));
            }
        }
        il.append(getAdviceArgSetup(shadow, extraArgVar, null));
	    il.append(getNonTestAdviceInstructions(shadow));
	    
        InstructionHandle ifYesAdvice = il.getStart();
        il.insert(getTestInstructions(shadow, ifYesAdvice, ifNoAdvice, ifYesAdvice));	
        return il;
	}

	public InstructionList getAdviceArgSetup(
		BcelShadow shadow,
		BcelVar extraVar,
		InstructionList closureInstantiation) 
	{
        InstructionFactory fact = shadow.getFactory();
        BcelWorld world = shadow.getWorld();
        InstructionList il = new InstructionList();

//        if (targetAspectField != null) {
//        	il.append(fact.createFieldAccess(
//        		targetAspectField.getDeclaringType().getName(),
//        		targetAspectField.getName(),
//        		BcelWorld.makeBcelType(targetAspectField.getType()),
//        		Constants.GETSTATIC));
//        }
//        
		//System.err.println("BcelAdvice: " + exposedState);


		if (exposedState.getAspectInstance() != null) {
			il.append(
				BcelRenderer.renderExpr(fact, world, exposedState.getAspectInstance()));
		}
        for (int i = 0, len = exposedState.size(); i < len; i++) {
            BcelVar v = (BcelVar) exposedState.get(i);
            if (v == null) continue;
            TypeX desiredTy = getSignature().getParameterTypes()[i];
            v.appendLoadAndConvert(il, fact, desiredTy.resolve(world));
        }

        
		if (getKind() == AdviceKind.Around) {
			il.append(closureInstantiation);
		} else if (hasExtraParameter()) {
			extraVar.appendLoadAndConvert(
				il,
				fact,
				getExtraParameterType().resolve(world));
		}
        
        // handle thisJoinPoint parameters
        if ((extraParameterFlags & ThisJoinPoint) != 0) {
        	shadow.getThisJoinPointBcelVar().appendLoad(il, fact);
        }
        
        if ((extraParameterFlags & ThisJoinPointStaticPart) != 0) {
        	shadow.getThisJoinPointStaticPartBcelVar().appendLoad(il, fact);
        }
        
        if ((extraParameterFlags & ThisEnclosingJoinPointStaticPart) != 0) {
        	shadow.getThisEnclosingJoinPointStaticPartBcelVar().appendLoad(il, fact);
        }
        
        
        return il;
    }
    
    public InstructionList getNonTestAdviceInstructions(BcelShadow shadow) {
        return new InstructionList(
            Utility.createInvoke(shadow.getFactory(), shadow.getWorld(), getSignature()));
    }

    public InstructionList getTestInstructions(
        BcelShadow shadow,
        InstructionHandle sk,
        InstructionHandle fk,
        InstructionHandle next) 
	{
		//System.err.println("test: " + pointcutTest);
		return BcelRenderer.renderTest(
			shadow.getFactory(),
			shadow.getWorld(),
			pointcutTest,
			sk,
			fk,
			next);
	}

	public int compareTo(Object other) {
		if (!(other instanceof BcelAdvice)) return 0;
		BcelAdvice o = (BcelAdvice)other;
		
		//System.err.println("compareTo: " + this + ", " + o);
		if (kind.getPrecedence() != o.kind.getPrecedence()) {
			if (kind.getPrecedence() > o.kind.getPrecedence()) return +1;
			else return -1;
		}
		
		if (kind.isCflow()) {
			if (this.innerCflowEntries.contains(o)) return -1;
			else if (o.innerCflowEntries.contains(this)) return +1;
			else return 0;
		}
		
		
		if (kind.isPerEntry() || kind == AdviceKind.Softener) {
			return 0;
		}
		
		//System.out.println("compare: " + this + " with " + other);
		World world = concreteAspect.getWorld();
		
		int ret =
			concreteAspect.getWorld().compareByDominates(
				concreteAspect,
				o.concreteAspect);
		if (ret != 0) return ret;
		
		
		ResolvedTypeX declaringAspect = getDeclaringAspect().resolve(world);
		ResolvedTypeX o_declaringAspect = o.getDeclaringAspect().resolve(world);
		
		
		if (declaringAspect == o_declaringAspect) {
		    if (kind.isAfter() || o.kind.isAfter()) {
		    	return this.lexicalPosition < o.lexicalPosition ? -1: +1;
		    } else {
		    	return this.lexicalPosition < o.lexicalPosition ? +1: -1;
		    }
		} else if (declaringAspect.isAssignableFrom(o_declaringAspect)) {
			return -1;
		} else if (o_declaringAspect.isAssignableFrom(declaringAspect)) {
			return +1;
		} else {
			return 0;
		}
	}

	public BcelVar[] getExposedStateAsBcelVars() {
		//System.out.println("vars: " + Arrays.asList(exposedState.vars));
		if (exposedState == null) return BcelVar.NONE;
		int len = exposedState.vars.length;
		BcelVar[] ret = new BcelVar[len];
		for (int i=0; i < len; i++) {
			ret[i] = (BcelVar)exposedState.vars[i];
		}
		return ret; //(BcelVar[]) exposedState.vars;
	}

}
