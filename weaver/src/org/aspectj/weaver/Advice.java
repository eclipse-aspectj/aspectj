/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import java.util.Collections;
import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.bcel.Utility;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.TypePattern;

public abstract class Advice extends ShadowMunger {

	protected AjAttribute.AdviceAttribute attribute; // the pointcut field is ignored

    protected AdviceKind kind; // alias of attribute.getKind()
    protected Member signature;
    
    // not necessarily declaring aspect, this is a semantics change from 1.0
    protected ResolvedType concreteAspect; // null until after concretize
    
    protected List innerCflowEntries = Collections.EMPTY_LIST;  // just for cflow*Entry kinds
    protected int nFreeVars; // just for cflow*Entry kinds
    
    protected TypePattern exceptionType; // just for Softener kind
    
    // if we are parameterized, these type may be different to the advice signature types
    protected UnresolvedType[] bindingParameterTypes;
    
    protected List/*Lint.Kind*/ suppressedLintKinds = null; // based on annotations on this advice
    
    ISourceLocation lastReportedMonitorExitJoinpointLocation = null;

    public static Advice makeCflowEntry(World world, Pointcut entry, boolean isBelow, Member stackField, int nFreeVars, List innerCflowEntries, ResolvedType inAspect){
    	Advice ret = world.createAdviceMunger(isBelow ? AdviceKind.CflowBelowEntry : AdviceKind.CflowEntry,
    	      entry, stackField, 0, entry);
    	      //0);
    	ret.innerCflowEntries = innerCflowEntries;
    	ret.nFreeVars = nFreeVars;
    	ret.concreteAspect = inAspect;
    	return ret;
    }

    public static Advice makePerCflowEntry(World world, Pointcut entry, boolean isBelow, 
    								Member stackField, ResolvedType inAspect, List innerCflowEntries)
    {
    	Advice ret = world.createAdviceMunger(isBelow ? AdviceKind.PerCflowBelowEntry : AdviceKind.PerCflowEntry,
    	      entry, stackField, 0, entry);
    	ret.innerCflowEntries = innerCflowEntries;
    	ret.concreteAspect = inAspect;
    	return ret;
    }

    public static Advice makePerObjectEntry(World world, Pointcut entry, boolean isThis, 
    								ResolvedType inAspect)
    {
    	Advice ret = world.createAdviceMunger(isThis ? AdviceKind.PerThisEntry : AdviceKind.PerTargetEntry,
    	      entry, null, 0, entry);
  
    	ret.concreteAspect = inAspect;
    	return ret;
    }
    
    // PTWIMPL per type within entry advice is what initializes the aspect instance in the matched type
    public static Advice makePerTypeWithinEntry(World world, Pointcut p, ResolvedType inAspect) {
    	Advice ret = world.createAdviceMunger(AdviceKind.PerTypeWithinEntry,p,null,0,p);
    	ret.concreteAspect = inAspect;
    	return ret;
    }
    
    public static Advice makeSoftener(World world, Pointcut entry, TypePattern exceptionType,ResolvedType inAspect,IHasSourceLocation loc) {
    	Advice ret = world.createAdviceMunger(AdviceKind.Softener, entry, null, 0, loc);  
  
    	ret.exceptionType = exceptionType;
    	ret.concreteAspect = inAspect;
    	// System.out.println("made ret: " + ret + " with " + exceptionType);
    	return ret;
    }
    	

    public Advice(AjAttribute.AdviceAttribute attribute, Pointcut pointcut, Member signature)
    {
    	super(pointcut, attribute.getStart(), attribute.getEnd(), attribute.getSourceContext());
		this.attribute = attribute;
		this.kind = attribute.getKind(); // alias
		this.signature = signature;
		if (signature != null) {
			this.bindingParameterTypes = signature.getParameterTypes();
		} else {
			this.bindingParameterTypes = new UnresolvedType[0];
		}
    }    

	
	public boolean match(Shadow shadow, World world) {
		if (super.match(shadow, world)) {
			if (shadow.getKind() == Shadow.ExceptionHandler) {
				if (kind.isAfter() || kind == AdviceKind.Around) {
					world.showMessage(IMessage.WARNING,
							WeaverMessages.format(WeaverMessages.ONLY_BEFORE_ON_HANDLER),
							getSourceLocation(), shadow.getSourceLocation());
					return false;
				}
			}
			if (shadow.getKind()==Shadow.SynchronizationLock ||
				shadow.getKind()==Shadow.SynchronizationUnlock) {
				if (kind==AdviceKind.Around
//					Don't work, see comments in SynchronizationTests
//					&& attribute.getProceedCallSignatures()!=null
//					&& attribute.getProceedCallSignatures().length!=0
					) {
					world.showMessage(IMessage.WARNING,
							WeaverMessages.format(WeaverMessages.NO_AROUND_ON_SYNCHRONIZATION),
							getSourceLocation(), shadow.getSourceLocation());
					return false;
				}
			}
			

			if (hasExtraParameter() && kind == AdviceKind.AfterReturning) {
    			ResolvedType resolvedExtraParameterType = getExtraParameterType().resolve(world);
    			ResolvedType shadowReturnType = shadow.getReturnType().resolve(world);
    			boolean matches = 
    				(resolvedExtraParameterType.isConvertableFrom(shadowReturnType) &&
    				 shadow.getKind().hasReturnValue());
    			if (matches && resolvedExtraParameterType.isParameterizedType()) {
    				maybeIssueUncheckedMatchWarning(resolvedExtraParameterType,shadowReturnType,shadow,world);
    			}
    			return matches;
			} else if (hasExtraParameter() && kind==AdviceKind.AfterThrowing) { // pr119749
	    			ResolvedType exceptionType = getExtraParameterType().resolve(world);
	    			if (!exceptionType.isCheckedException()) return true;
	    			UnresolvedType[] shadowThrows = shadow.getSignature().getExceptions(world);
	    			boolean matches = false;
	    			for (int i = 0; i < shadowThrows.length && !matches; i++) {
						ResolvedType type = shadowThrows[i].resolve(world);
						if (exceptionType.isAssignableFrom(type)) matches=true;
					}
	    			return matches;
    		} else if (kind == AdviceKind.PerTargetEntry) {
    			return shadow.hasTarget();
    		} else if (kind == AdviceKind.PerThisEntry) {
    			return shadow.hasThis();
    		} else if (kind == AdviceKind.Around) {
    			if (shadow.getKind() == Shadow.PreInitialization) {
	    			world.showMessage(IMessage.ERROR,
	    					WeaverMessages.format(WeaverMessages.AROUND_ON_PREINIT),
							getSourceLocation(), shadow.getSourceLocation());
					return false;
				} else if (shadow.getKind() == Shadow.Initialization) {
					world.showMessage(IMessage.ERROR,
							WeaverMessages.format(WeaverMessages.AROUND_ON_INIT),
							getSourceLocation(), shadow.getSourceLocation());
					return false;
				} else if (shadow.getKind() == Shadow.StaticInitialization && 
							shadow.getEnclosingType().resolve(world).isInterface())
				{
					world.showMessage(IMessage.ERROR,
							WeaverMessages.format(WeaverMessages.AROUND_ON_INTERFACE_STATICINIT,shadow.getEnclosingType().getName()),
							getSourceLocation(), shadow.getSourceLocation());
					return false;
    			} else {
    				//System.err.println(getSignature().getReturnType() + " from " + shadow.getReturnType());
    				if (getSignature().getReturnType() == ResolvedType.VOID) {
    					if (shadow.getReturnType() != ResolvedType.VOID) {
    						world.showMessage(IMessage.ERROR, 
    							WeaverMessages.format(WeaverMessages.NON_VOID_RETURN,shadow),	
    							getSourceLocation(), shadow.getSourceLocation());
    						return false;
    					}
    				} else if (getSignature().getReturnType().equals(UnresolvedType.OBJECT)) {
    					return true;
    				} else {
    					ResolvedType shadowReturnType = shadow.getReturnType().resolve(world);
    					ResolvedType adviceReturnType = getSignature().getGenericReturnType().resolve(world);
    					
    					if (shadowReturnType.isParameterizedType() && adviceReturnType.isRawType()) { // Set<Integer> and Set
    						ResolvedType shadowReturnGenericType = shadowReturnType.getGenericType(); // Set
    						ResolvedType adviceReturnGenericType = adviceReturnType.getGenericType(); // Set
    						if (shadowReturnGenericType.isAssignableFrom(adviceReturnGenericType) && 
    								world.getLint().uncheckedAdviceConversion.isEnabled()) {
    							world.getLint().uncheckedAdviceConversion.signal(
    								new String[]{shadow.toString(),shadowReturnType.getName(),adviceReturnType.getName()},
    								shadow.getSourceLocation(),
    								new ISourceLocation[]{getSourceLocation()});
    						}
    					} else if(!shadowReturnType.isAssignableFrom(adviceReturnType)) {
	    					//System.err.println(this + ", " + sourceContext + ", " + start);
							world.showMessage(IMessage.ERROR,
									WeaverMessages.format(WeaverMessages.INCOMPATIBLE_RETURN_TYPE,shadow),
									getSourceLocation(), shadow.getSourceLocation());
		    				return false;
    					}
    				}
    			}
    		}
    		return true; 
    	} else {
    		return false;
    	}
	}

	/**
	 * In after returning advice if we are binding the extra parameter to a parameterized
	 * type we may not be able to do a type-safe conversion.
	 * @param resolvedExtraParameterType  the type in the after returning declaration
	 * @param shadowReturnType the type at the shadow
	 * @param world
	 */
	private void maybeIssueUncheckedMatchWarning(ResolvedType afterReturningType, ResolvedType shadowReturnType, Shadow shadow, World world) {
		boolean inDoubt = !afterReturningType.isAssignableFrom(shadowReturnType);				
		if (inDoubt && world.getLint().uncheckedArgument.isEnabled()) {
			String uncheckedMatchWith = afterReturningType.getSimpleBaseName();
			if (shadowReturnType.isParameterizedType() && (shadowReturnType.getRawType() == afterReturningType.getRawType())) {
				uncheckedMatchWith = shadowReturnType.getSimpleName();
			}
			if (!Utility.isSuppressing(getSignature().getAnnotations(), "uncheckedArgument")) {
				world.getLint().uncheckedArgument.signal(
						new String[] {
								afterReturningType.getSimpleName(),
								uncheckedMatchWith,
								afterReturningType.getSimpleBaseName(),
								shadow.toResolvedString(world)},
						getSourceLocation(),
						new ISourceLocation[] {shadow.getSourceLocation()});
			}			
		}		
	}

	// ----

	public AdviceKind getKind() {
		return kind;
	}

	public Member getSignature() {
		return signature;
	}
	
	// only called as part of parameterization....
	public void setSignature(Member signature) {
		this.signature = signature;
		// reset hashCode value so it can be recalculated next time
		hashCode = 0;
	}
	
	public boolean hasExtraParameter() {
		return (getExtraParameterFlags() & ExtraArgument) != 0;
	}

	protected int getExtraParameterFlags() {
		return attribute.getExtraParameterFlags();
	}

	protected int getExtraParameterCount() {
		return countOnes(getExtraParameterFlags() & ParameterMask);
	}
	
	public UnresolvedType[] getBindingParameterTypes() { return this.bindingParameterTypes; }
	
	public void setBindingParameterTypes(UnresolvedType[] types) { this.bindingParameterTypes = types; }
	
	public static int countOnes(int bits) {
		int ret = 0;
		while (bits != 0) {
			if ((bits & 1) != 0) ret += 1;
			bits = bits >> 1;
		}
		return ret;
	}
	
	public int getBaseParameterCount() {
		return getSignature().getParameterTypes().length - getExtraParameterCount();
	}

	public String[] getBaseParameterNames(World world) {
		String[] allNames = getSignature().getParameterNames(world);
		int extras = getExtraParameterCount();
		if (extras == 0) return allNames;
		String[] result = new String[getBaseParameterCount()];
		for (int i = 0; i < result.length; i++) {
			result[i] = allNames[i];
		}
		return result;
	}
	
	public UnresolvedType getExtraParameterType() {
		if (!hasExtraParameter()) return ResolvedType.MISSING;
		if (signature instanceof ResolvedMember) {
			if (getConcreteAspect().isAnnotationStyleAspect()) {
				// bug 122742 - if we're an annotation style aspect then one 
				// of the extra parameters could be JoinPoint which we want
				// to ignore
				int baseParmCnt = getBaseParameterCount();
				UnresolvedType[] genericParameterTypes = ((ResolvedMember)signature).getGenericParameterTypes();
				while ((baseParmCnt + 1 < genericParameterTypes.length)
						&& (genericParameterTypes[baseParmCnt].equals(AjcMemberMaker.TYPEX_JOINPOINT)
								|| genericParameterTypes[baseParmCnt].equals(AjcMemberMaker.TYPEX_STATICJOINPOINT)
								|| genericParameterTypes[baseParmCnt].equals(AjcMemberMaker.TYPEX_ENCLOSINGSTATICJOINPOINT))) {
					baseParmCnt++;
				}
				return ((ResolvedMember)signature).getGenericParameterTypes()[baseParmCnt];
			}
			return ((ResolvedMember)signature).getGenericParameterTypes()[getBaseParameterCount()];
		} else {
			return signature.getParameterTypes()[getBaseParameterCount()];
		}
	}
	
	public UnresolvedType getDeclaringAspect() {
		return getOriginalSignature().getDeclaringType();
	}

	protected Member getOriginalSignature() { 
		return signature;
	}
	
	protected String extraParametersToString() {
		if (getExtraParameterFlags() == 0) {
			return "";
		} else {
			return "(extraFlags: " + getExtraParameterFlags() + ")";
		}
    }

	public Pointcut getPointcut() {
		return pointcut;
	}

	// ----
 
    /** @param fromType is guaranteed to be a non-abstract aspect
     *  @param clause has been concretized at a higher level
     */
    public ShadowMunger concretize(ResolvedType fromType, World world, PerClause clause) {
    	// assert !fromType.isAbstract();
        Pointcut p = pointcut.concretize(fromType, getDeclaringType(), signature.getArity(), this);
        if (clause != null) {
        	Pointcut oldP = p;
        	p = new AndPointcut(clause, p);
        	p.copyLocationFrom(oldP);
        	p.state = Pointcut.CONCRETE;

            //FIXME ? ATAJ copy unbound bindings to ignore
            p.m_ignoreUnboundBindingForNames  =oldP.m_ignoreUnboundBindingForNames;
        }
        
		Advice munger = world.createAdviceMunger(attribute, p, signature);
		munger.concreteAspect = fromType;
		munger.bindingParameterTypes = this.bindingParameterTypes;
		munger.setDeclaringType(getDeclaringType());
    	//System.err.println("concretizing here " + p + " with clause " + clause);
        return munger;
    }

	// ---- from object

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("(").append(getKind()).append(extraParametersToString());
		sb.append(": ").append(pointcut).append("->").append(signature).append(")");
		return sb.toString();
//		return "("
//			+ getKind()
//            + extraParametersToString() 
//			+ ": "
//			+ pointcut
//			+ "->"
//			+ signature
//			+ ")";
	}
	
	// XXX this perhaps ought to take account of the other fields in advice ...
    public boolean equals(Object other) {
        if (! (other instanceof Advice)) return false;
        Advice o = (Advice) other;
        return o.kind.equals(kind) 
        	&& ((o.pointcut == null) ? (pointcut == null) : o.pointcut.equals(pointcut))
        	&& ((o.signature == null) ? (signature == null) : o.signature.equals(signature))
        	&& (AsmManager.getDefault().getHandleProvider().dependsOnLocation()
        			?((o.getSourceLocation()==null) ? (getSourceLocation()==null): o.getSourceLocation().equals(getSourceLocation())):true) // pr134471 - remove when handles are improved to be independent of location
        	;

    }

	private volatile int hashCode = 0;
    public int hashCode() {
    	if (hashCode == 0) {
            int result = 17;
            result = 37*result + kind.hashCode();
            result = 37*result + ((pointcut == null) ? 0 : pointcut.hashCode());
            result = 37*result + ((signature == null) ? 0 : signature.hashCode());
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


	// for testing only	
	public void setLexicalPosition(int lexicalPosition) {
		start = lexicalPosition;
	}

	public ResolvedType getConcreteAspect() {
		return concreteAspect;
	}
	
}
