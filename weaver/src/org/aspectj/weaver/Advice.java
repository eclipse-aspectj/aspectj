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


package org.aspectj.weaver;

import java.util.*;

import org.aspectj.bridge.*;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.patterns.*;

public abstract class Advice extends ShadowMunger {

    protected AdviceKind kind;
    protected Member signature;
    protected int extraParameterFlags;
    protected int lexicalPosition;
    
    // not necessarily declaring aspect, this is a semantics change from 1.0
    protected ResolvedTypeX concreteAspect; // null until after concretize
    
    protected List innerCflowEntries = Collections.EMPTY_LIST;  // just for cflow*Entry kinds
    protected int nFreeVars; // just for cflow*Entry kinds
    
    protected TypePattern exceptionType; // just for Softener kind

    public static Advice makeCflowEntry(World world, Pointcut entry, boolean isBelow, Member stackField, int nFreeVars, List innerCflowEntries) {
    	Advice ret = world.concreteAdvice(isBelow ? AdviceKind.CflowBelowEntry : AdviceKind.CflowEntry,
    	      entry, stackField, 0, entry);
    	      //0);
    	ret.innerCflowEntries = innerCflowEntries;
    	ret.nFreeVars = nFreeVars;
    	return ret;
    }

    public static Advice makePerCflowEntry(World world, Pointcut entry, boolean isBelow, 
    								Member stackField, ResolvedTypeX inAspect, List innerCflowEntries)
    {
    	Advice ret = world.concreteAdvice(isBelow ? AdviceKind.PerCflowBelowEntry : AdviceKind.PerCflowEntry,
    	      entry, stackField, 0, entry);
    	ret.innerCflowEntries = innerCflowEntries;
    	ret.concreteAspect = inAspect;
    	return ret;
    }

    public static Advice makePerObjectEntry(World world, Pointcut entry, boolean isThis, 
    								ResolvedTypeX inAspect)
    {
    	Advice ret = world.concreteAdvice(isThis ? AdviceKind.PerThisEntry : AdviceKind.PerTargetEntry,
    	      entry, null, 0, entry);
  
    	ret.concreteAspect = inAspect;
    	return ret;
    }
    
    public static Advice makeSoftener(World world, Pointcut entry, TypePattern exceptionType) {
    	Advice ret = world.concreteAdvice(AdviceKind.Softener,
    	      entry, null, 0, entry);  
  
    	ret.exceptionType = exceptionType;
    	//System.out.println("made ret: " + ret + " with " + exceptionType);
    	return ret;
    }
    	

    public Advice(AdviceKind kind, Pointcut pointcut, Member signature, 
    	int extraParameterFlags, int start, int end, ISourceContext sourceContext)
    {
    	super(pointcut, start, end, sourceContext);
		this.kind = kind;
		this.signature = signature;
		this.extraParameterFlags = extraParameterFlags;
		this.lexicalPosition = start;  //XXX should go away
    }    

	
	public boolean match(Shadow shadow, World world) {
		if (super.match(shadow, world)) {
			if (shadow.getKind() == Shadow.ExceptionHandler) {
				if (kind.isAfter() || kind == AdviceKind.Around) {
					world.showMessage(IMessage.WARNING,
	    				"Only before advice is supported on handler join points (compiler limitation)", 
	    				getSourceLocation(), shadow.getSourceLocation());
					return false;
				}
			}
			
			
    		if (hasExtraParameter() && kind == AdviceKind.AfterReturning) {
    			return getExtraParameterType().isConvertableFrom(shadow.getReturnType(), world);
    		} else if (kind == AdviceKind.PerTargetEntry) {
    			return shadow.hasTarget();
    		} else if (kind == AdviceKind.PerThisEntry) {
    			return shadow.hasThis();
    		} else if (kind == AdviceKind.Around) {
    			if (shadow.getKind() == Shadow.PreInitialization) {
	    			world.showMessage(IMessage.ERROR,
	    				"around on pre-initialization not supported (compiler limitation)", 
	    				getSourceLocation(), shadow.getSourceLocation());
					return false;
    			} else if (shadow.getKind() == Shadow.Initialization) {
	    			world.showMessage(IMessage.ERROR,
	    				"around on initialization not supported (compiler limitation)", 
	    				getSourceLocation(), shadow.getSourceLocation());
					return false;
    			} else {
    				if (!getSignature().getReturnType().isConvertableFrom(shadow.getReturnType(), world)) {
    					//System.err.println(this + ", " + sourceContext + ", " + start);
    					
    					world.getMessageHandler().handleMessage(
	    				MessageUtil.error("incompatible return type applying to " + shadow,
	    					getSourceLocation()));
	    				//XXX need a crosscutting error message here
	    				return false;
    				}
    			}
    		}
    		return true; 
    	} else {
    		return false;
    	}
	}


	// ----

	public AdviceKind getKind() {
		return kind;
	}

	public Member getSignature() {
		return signature;
	}
	
	public boolean hasExtraParameter() {
		return (extraParameterFlags & ExtraArgument) != 0;
	}

	protected int getExtraParameterCount() {
		return countOnes(extraParameterFlags & ParameterMask);
	}
	
	public static int countOnes(int bits) {
		int ret = 0;
		while (bits != 0) {
			if ((bits & 1) != 0) ret += 1;
			bits = bits >> 1;
		}
		return ret;
	}
	
	public int getBaseParameterCount() {
		return signature.getParameterTypes().length - getExtraParameterCount();
	}

	public TypeX getExtraParameterType() {
		if (!hasExtraParameter()) return ResolvedTypeX.MISSING;
		return signature.getParameterTypes()[getBaseParameterCount()];
	}
	
	public TypeX getDeclaringAspect() {
		return signature.getDeclaringType();
	}

	protected String extraParametersToString() {
		if (extraParameterFlags == 0) {
			return "";
		} else {
			return "(extraFlags: " + extraParameterFlags + ")";
		}
    }

	public Pointcut getPointcut() {
		return pointcut;
	}

	// ----
 
    /** @param fromType is guaranteed to be a non-abstract aspect
     *  @param perClause has been concretized at a higher level
     */
    public ShadowMunger concretize(ResolvedTypeX fromType, World world, PerClause clause) {
    	// assert !fromType.isAbstract();
        Pointcut p = pointcut.concretize(fromType, signature.getArity(), this);
        if (clause != null) {
        	p = new AndPointcut(clause, p);
        	p.state = Pointcut.CONCRETE;
        }
        
		Advice munger = world.concreteAdvice(kind, p, signature, extraParameterFlags, start, end, sourceContext);
		munger.concreteAspect = fromType;
    	//System.err.println("concretizing here " + p + " with clause " + clause);
        return munger;
    }

	// ---- from object

	public String toString() {
		return "("
			+ getKind()
            + extraParametersToString() 
			+ ": "
			+ pointcut
			+ "->"
			+ signature
			+ ")";
	}
    public boolean equals(Object other) {
        if (! (other instanceof Advice)) return false;
        Advice o = (Advice) other;
        return o.kind == kind && o.pointcut.equals(pointcut) && o.signature.equals(signature) &&
            o.extraParameterFlags == extraParameterFlags;
    }
    private volatile int hashCode = 0;
    public int hashCode() {
        if (hashCode == 0) {
            int result = 17;
            result = 37*result + kind.hashCode();
            result = 37*result + pointcut.hashCode();
            if (signature != null) result = 37*result + signature.hashCode();
            hashCode = result;
        }
        return hashCode;
    }
 
    // ---- fields


	public static final int ExtraArgument = 1;
	public static final int ThisJoinPoint = 2;
	public static final int ThisJoinPointStaticPart = 4;
	public static final int ThisEnclosingJoinPointStaticPart = 8;
	public static final int ParameterMask = 0xf;
	
	public static final int CanInline = 0x40;
	
	public void setLexicalPosition(int lexicalPosition) {
		this.lexicalPosition = lexicalPosition;
	}

	public ResolvedTypeX getConcreteAspect() {
		return concreteAspect;
	}

}
