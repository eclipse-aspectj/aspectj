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


package org.aspectj.weaver;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.asm.IRelationship;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.WeaveMessage;
import org.aspectj.lang.JoinPoint;
import org.aspectj.util.PartialOrder;
import org.aspectj.util.TypeSafeEnum;
import org.aspectj.weaver.ast.Var;
import org.aspectj.weaver.bcel.BcelAdvice;

/*
 * The superclass of anything representing a the shadow of a join point.  A shadow represents
 * some bit of code, and encompasses both entry and exit from that code.  All shadows have a kind
 * and a signature.
 */

public abstract class Shadow {

	// every Shadow has a unique id, doesn't matter if it wraps...
	private static int nextShadowID = 100;  // easier to spot than zero.
	
	private final Kind kind; 
    private final Member signature;
    private Member matchingSignature;
    private ResolvedMember resolvedSignature;
	protected final Shadow enclosingShadow;
    protected List mungers = new ArrayList(1);

    public int shadowId = nextShadowID++;  // every time we build a shadow, it gets a new id

	// ----
    protected Shadow(Kind kind, Member signature, Shadow enclosingShadow) {
        this.kind = kind;
        this.signature = signature;
        this.enclosingShadow = enclosingShadow;
    }

	// ----

    public abstract World getIWorld();

	public List /*ShadowMunger*/ getMungers() {
		return mungers;
	}
	    
    /**
     * could this(*) pcd ever match
     */
    public final boolean hasThis() {
    	if (getKind().neverHasThis()) {
    		return false;
    	} else if (getKind().isEnclosingKind()) {
    		return !getSignature().isStatic();
    	} else if (enclosingShadow == null) {
    		return false;
    	} else {
    		return enclosingShadow.hasThis();
    	}
    }

    /**
     * the type of the this object here
     * 
     * @throws IllegalStateException if there is no this here
     */
    public final UnresolvedType getThisType() {
        if (!hasThis()) throw new IllegalStateException("no this");
        if (getKind().isEnclosingKind()) {
    		return getSignature().getDeclaringType();
    	} else {
    		return enclosingShadow.getThisType();
    	}
    }
    
    /**
     * a var referencing this
     * 
     * @throws IllegalStateException if there is no target here
     */
    public abstract Var getThisVar();
    
    
    
    /**
     * could target(*) pcd ever match
     */
    public final boolean hasTarget() {
    	if (getKind().neverHasTarget()) {
    		return false;
    	} else if (getKind().isTargetSameAsThis()) {
    		return hasThis();
    	} else {
    		return !getSignature().isStatic();
    	}
    }

    /**
     * the type of the target object here
     * 
     * @throws IllegalStateException if there is no target here
     */
    public final UnresolvedType getTargetType() {
        if (!hasTarget()) throw new IllegalStateException("no target");
        return getSignature().getDeclaringType();
    }
    
    /**
     * a var referencing the target
     * 
     * @throws IllegalStateException if there is no target here
     */
    public abstract Var getTargetVar();
    
    public UnresolvedType[] getArgTypes() {
    	if (getKind() == FieldSet) return new UnresolvedType[] { getSignature().getReturnType() };
        return getSignature().getParameterTypes();
    }
    
    public UnresolvedType[] getGenericArgTypes() {
    	if (getKind() == FieldSet) return new UnresolvedType[] { getResolvedSignature().getGenericReturnType() };
        return getResolvedSignature().getGenericParameterTypes();    	
    }
    
    public UnresolvedType getArgType(int arg) {
    	if (getKind() == FieldSet) return getSignature().getReturnType();
        return getSignature().getParameterTypes()[arg];
    }

    public int getArgCount() {
    	if (getKind() == FieldSet) return 1;
        return getSignature()
            .getParameterTypes().length;
    }
    	
	public abstract UnresolvedType getEnclosingType();	

	public abstract Var getArgVar(int i);
	
	public abstract Var getThisJoinPointVar();
	public abstract Var getThisJoinPointStaticPartVar();
	public abstract Var getThisEnclosingJoinPointStaticPartVar();
    
	// annotation variables
	public abstract Var getKindedAnnotationVar(UnresolvedType forAnnotationType);
	public abstract Var getWithinAnnotationVar(UnresolvedType forAnnotationType);
	public abstract Var getWithinCodeAnnotationVar(UnresolvedType forAnnotationType);
	public abstract Var getThisAnnotationVar(UnresolvedType forAnnotationType);
	public abstract Var getTargetAnnotationVar(UnresolvedType forAnnotationType);
	public abstract Var getArgAnnotationVar(int i, UnresolvedType forAnnotationType);
	
	public abstract Member getEnclosingCodeSignature();
	

    /** returns the kind of shadow this is, representing what happens under this shadow
     */
    public Kind getKind() {
        return kind;
    }

    /** returns the signature of the thing under this shadow
     */
    public Member getSignature() {
        return signature;
    }
    
    /**
     * returns the signature of the thing under this shadow, with
     * any synthetic arguments removed
     */
    public Member getMatchingSignature() {
    	return matchingSignature != null ? matchingSignature : signature;
    }
    
    public void setMatchingSignature(Member member) {
    	this.matchingSignature = member;
    }
    
    /**
     * returns the resolved signature of the thing under this shadow
     * 
     */
    public ResolvedMember getResolvedSignature() {
    	if (resolvedSignature == null) {
    		resolvedSignature = signature.resolve(getIWorld());
    	}
    	return resolvedSignature;
    }
    
	
	public UnresolvedType getReturnType() {
		if (kind == ConstructorCall) return getSignature().getDeclaringType();
		else if (kind == FieldSet) return ResolvedType.VOID;
		return getResolvedSignature().getGenericReturnType();
	}

    
    /**
     * These names are the ones that will be returned by thisJoinPoint.getKind()
     * Those need to be documented somewhere
     */
    public static final Kind MethodCall           = new Kind(JoinPoint.METHOD_CALL, 1,  true);
    public static final Kind ConstructorCall      = new Kind(JoinPoint.CONSTRUCTOR_CALL, 2,  true);
    public static final Kind MethodExecution      = new Kind(JoinPoint.METHOD_EXECUTION, 3,  false);
    public static final Kind ConstructorExecution = new Kind(JoinPoint.CONSTRUCTOR_EXECUTION, 4,  false);
    public static final Kind FieldGet             = new Kind(JoinPoint.FIELD_GET, 5,  true);
    public static final Kind FieldSet             = new Kind(JoinPoint.FIELD_SET, 6,  true);
    public static final Kind StaticInitialization = new Kind(JoinPoint.STATICINITIALIZATION, 7,  false);
    public static final Kind PreInitialization    = new Kind(JoinPoint.PREINTIALIZATION, 8,  false);
    public static final Kind AdviceExecution      = new Kind(JoinPoint.ADVICE_EXECUTION, 9,  false);
    public static final Kind Initialization       = new Kind(JoinPoint.INITIALIZATION, 10,  false);
    public static final Kind ExceptionHandler     = new Kind(JoinPoint.EXCEPTION_HANDLER, 11,  true);
    
    public static final int MAX_SHADOW_KIND = 11;
    public static final Kind[] SHADOW_KINDS = new Kind[] {
    	MethodCall, ConstructorCall, MethodExecution, ConstructorExecution,
    	FieldGet, FieldSet, StaticInitialization, PreInitialization,
    	AdviceExecution, Initialization, ExceptionHandler,
    };

    public static final Set ALL_SHADOW_KINDS;   
    static {
    	HashSet aSet = new HashSet();
    	for (int i = 0; i < SHADOW_KINDS.length; i++) {
			aSet.add(SHADOW_KINDS[i]);
		}
    	ALL_SHADOW_KINDS = Collections.unmodifiableSet(aSet);
    }

    /** A type-safe enum representing the kind of shadows
     */
	public static final class Kind extends TypeSafeEnum {
//		private boolean argsOnStack;  //XXX unused

		public Kind(String name, int key, boolean argsOnStack) {
			super(name, key);
//			this.argsOnStack = argsOnStack;
		}

		public String toLegalJavaIdentifier() {
			return getName().replace('-', '_');
		}

		public boolean argsOnStack() {
			return !isTargetSameAsThis();
		}

		// !!! this is false for handlers!
		public boolean allowsExtraction() {
			return true;
		}
		
		// XXX revisit along with removal of priorities
		public boolean hasHighPriorityExceptions() {
			return !isTargetSameAsThis();
		}
		
		/**
		 * These shadow kinds have return values that can be bound in
		 * after returning(Dooberry doo) advice.
		 * @return
		 */
		public boolean hasReturnValue() {
			return 
				this == MethodCall ||
				this == ConstructorCall ||
				this == MethodExecution ||
				this == FieldGet ||
				this == AdviceExecution;
		}
		
		
		/**
		 * These are all the shadows that contains other shadows within them and
		 * are often directly associated with methods.
		 */
		public boolean isEnclosingKind() {
			return this == MethodExecution || this == ConstructorExecution ||
					this == AdviceExecution || this == StaticInitialization
					|| this == Initialization;
		}
		
		public boolean isTargetSameAsThis() {
			return this == MethodExecution 
				|| this == ConstructorExecution 
				|| this == StaticInitialization
				|| this == PreInitialization
				|| this == AdviceExecution
				|| this == Initialization;
		}
		
		public boolean neverHasTarget() {
			return this == ConstructorCall
				|| this == ExceptionHandler
				|| this == PreInitialization
				|| this == StaticInitialization;
		}
		
		public boolean neverHasThis() {
			return this == PreInitialization
				|| this == StaticInitialization;
		}
		
		
		public String getSimpleName() {
			int dash = getName().lastIndexOf('-');
			if (dash == -1) return getName();
			else return getName().substring(dash+1);
		}
		
		public static Kind read(DataInputStream s) throws IOException {
			int key = s.readByte();
			switch(key) {
				case 1: return MethodCall;
				case 2: return ConstructorCall;
				case 3: return MethodExecution;
				case 4: return ConstructorExecution;
				case 5: return FieldGet;
				case 6: return FieldSet;
				case 7: return StaticInitialization;
				case 8: return PreInitialization;
				case 9: return AdviceExecution;
				case 10: return Initialization;
				case 11: return ExceptionHandler;
			}
			throw new BCException("unknown kind: " + key);
		}		
	}

    /**
     * Only does the check if the munger requires it (@AJ aspects don't)
     *
     * @param munger
     * @return
     */
	protected boolean checkMunger(ShadowMunger munger) {
        if (munger.mustCheckExceptions()) {
            for (Iterator i = munger.getThrownExceptions().iterator(); i.hasNext(); ) {
                if (!checkCanThrow(munger,  (ResolvedType)i.next() )) return false;
            }
        }
        return true;
	}

	protected boolean checkCanThrow(ShadowMunger munger, ResolvedType resolvedTypeX) {
		if (getKind() == ExceptionHandler) {
			//XXX much too lenient rules here, need to walk up exception handlers
			return true;
		}
		
		if (!isDeclaredException(resolvedTypeX, getSignature())) {
			getIWorld().showMessage(IMessage.ERROR,
					WeaverMessages.format(WeaverMessages.CANT_THROW_CHECKED,resolvedTypeX,this), // from advice in \'" + munger. + "\'",
					getSourceLocation(), munger.getSourceLocation());
		}
		
		return true;
	}

	private boolean isDeclaredException(
		ResolvedType resolvedTypeX,
		Member member)
	{
		ResolvedType[] excs = getIWorld().resolve(member.getExceptions(getIWorld()));
		for (int i=0, len=excs.length; i < len; i++) {
			if (excs[i].isAssignableFrom(resolvedTypeX)) return true;
		}
		return false;
	}
	
	
    public void addMunger(ShadowMunger munger) {
    	if (checkMunger(munger)) this.mungers.add(munger);
    }
 
    public final void implement() {
    	sortMungers();
    	if (mungers == null) return;
    	prepareForMungers();
    	implementMungers();
    } 
    
	private void sortMungers() {
		
		List sorted = PartialOrder.sort(mungers);
		
		// Bunch of code to work out whether to report xlints for advice that isn't ordered at this Joinpoint
		possiblyReportUnorderedAdvice(sorted);
		
		if (sorted == null) {
			// this means that we have circular dependencies
			for (Iterator i = mungers.iterator(); i.hasNext(); ) {
				ShadowMunger m = (ShadowMunger)i.next();
				getIWorld().getMessageHandler().handleMessage(
					MessageUtil.error(
							WeaverMessages.format(WeaverMessages.CIRCULAR_DEPENDENCY,this), m.getSourceLocation()));
			}
		}
		mungers = sorted;
	}

    // not quite optimal... but the xlint is ignore by default
	private void possiblyReportUnorderedAdvice(List sorted) {
		if (sorted!=null && getIWorld().getLint().unorderedAdviceAtShadow.isEnabled() && mungers.size()>1) {
			
			// Stores a set of strings of the form 'aspect1:aspect2' which indicates there is no
			// precedence specified between the two aspects at this shadow.
			Set clashingAspects = new HashSet();
			int max = mungers.size();
			
			// Compare every pair of advice mungers
			for (int i = max-1; i >=0; i--) {
				for (int j=0; j<i; j++) {
				  Object a = mungers.get(i);
				  Object b = mungers.get(j);
				  
				  // Make sure they are the right type
				  if (a instanceof BcelAdvice && b instanceof BcelAdvice) {
					  BcelAdvice adviceA = (BcelAdvice)a;
					  BcelAdvice adviceB = (BcelAdvice)b;
					  if (!adviceA.concreteAspect.equals(adviceB.concreteAspect)) {
						  AdviceKind adviceKindA = adviceA.getKind();
						  AdviceKind adviceKindB = adviceB.getKind();
						  
						  // make sure they are the nice ones (<6) and not any synthetic advice ones we
						  // create to support other features of the language.
						  if (adviceKindA.getKey()<(byte)6 && adviceKindB.getKey()<(byte)6 && 
						      adviceKindA.getPrecedence() == adviceKindB.getPrecedence()) {
							  
							  // Ask the world if it knows about precedence between these
							  Integer order = getIWorld().getPrecedenceIfAny(
									  adviceA.concreteAspect,
									  adviceB.concreteAspect);
							  
							  if (order!=null && 
							      order.equals(new Integer(0))) {
								  String key = adviceA.getDeclaringAspect()+":"+adviceB.getDeclaringAspect();
								  String possibleExistingKey = adviceB.getDeclaringAspect()+":"+adviceA.getDeclaringAspect();
								  if (!clashingAspects.contains(possibleExistingKey)) clashingAspects.add(key);
							  }
						  }
					  }
				  }
				}				
		    }
			for (Iterator iter = clashingAspects.iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				String aspect1 = element.substring(0,element.indexOf(":"));
				String aspect2 = element.substring(element.indexOf(":")+1);
				getIWorld().getLint().unorderedAdviceAtShadow.signal(
						  new String[]{this.toString(),aspect1,aspect2},
						  this.getSourceLocation(),null);
			}		  
		}
	}
	
	/** Prepare the shadow for implementation.  After this is done, the shadow
	 * should be in such a position that each munger simply needs to be implemented.
	 */
	protected void prepareForMungers() {
		throw new RuntimeException("Generic shadows cannot be prepared");		
	}
	
	/*
	 * Ensure we report a nice source location - particular in the case
	 * where the source info is missing (binary weave).
	 */
	private String beautifyLocation(ISourceLocation isl) {
		StringBuffer nice = new StringBuffer();
		if (isl==null || isl.getSourceFile()==null || isl.getSourceFile().getName().indexOf("no debug info available")!=-1) {
			nice.append("no debug info available");
	    } else {
	    	// can't use File.getName() as this fails when a Linux box encounters a path created on Windows and vice-versa
	    	int takeFrom = isl.getSourceFile().getPath().lastIndexOf('/');
	    	if (takeFrom == -1) {
	    		takeFrom = isl.getSourceFile().getPath().lastIndexOf('\\');
	    	}    			
	    	nice.append(isl.getSourceFile().getPath().substring(takeFrom +1));
	    	if (isl.getLine()!=0) nice.append(":").append(isl.getLine());
		}
		return nice.toString();
	}
	
	/*
	 * Report a message about the advice weave that has occurred.  Some messing about
	 * to make it pretty !  This code is just asking for an NPE to occur ...
	 */
	private void reportWeavingMessage(ShadowMunger munger) {
		Advice advice = (Advice)munger;
		AdviceKind aKind = advice.getKind();
		// Only report on interesting advice kinds ...
		if (aKind == null || advice.getConcreteAspect()==null) {
			// We suspect someone is programmatically driving the weaver 
			// (e.g. IdWeaveTestCase in the weaver testcases)
			return;
		}
		if (!( aKind.equals(AdviceKind.Before) ||
		       aKind.equals(AdviceKind.After) ||
		       aKind.equals(AdviceKind.AfterReturning) ||
		       aKind.equals(AdviceKind.AfterThrowing) ||
		       aKind.equals(AdviceKind.Around) ||
		       aKind.equals(AdviceKind.Softener))) return;
		
		String description = advice.getKind().toString();
		String advisedType = this.getEnclosingType().getName();
		String advisingType= advice.getConcreteAspect().getName();
		Message msg = null;
		if (advice.getKind().equals(AdviceKind.Softener)) {
			msg = WeaveMessage.constructWeavingMessage(
			  WeaveMessage.WEAVEMESSAGE_SOFTENS,
			    new String[]{advisedType,beautifyLocation(getSourceLocation()),
			    			 advisingType,beautifyLocation(munger.getSourceLocation())},
				advisedType,
				advisingType);
		} else {
			boolean runtimeTest = ((BcelAdvice)advice).hasDynamicTests();
			String joinPointDescription = this.toString();
		    msg = WeaveMessage.constructWeavingMessage(WeaveMessage.WEAVEMESSAGE_ADVISES,
		    		new String[]{ joinPointDescription, advisedType, beautifyLocation(getSourceLocation()),
				            description,
				            advisingType,beautifyLocation(munger.getSourceLocation()),
				            (runtimeTest?" [with runtime test]":"")},
					advisedType,
					advisingType);
				         // Boolean.toString(runtimeTest)});
		}
		getIWorld().getMessageHandler().handleMessage(msg);
	}


	public IRelationship.Kind determineRelKind(ShadowMunger munger) {
		AdviceKind ak = ((Advice)munger).getKind();
		if (ak.getKey()==AdviceKind.Before.getKey())
				return IRelationship.Kind.ADVICE_BEFORE;
		else if (ak.getKey()==AdviceKind.After.getKey())
				return IRelationship.Kind.ADVICE_AFTER;
		else if (ak.getKey()==AdviceKind.AfterThrowing.getKey())
				return IRelationship.Kind.ADVICE_AFTERTHROWING;
		else if (ak.getKey()==AdviceKind.AfterReturning.getKey())
				return IRelationship.Kind.ADVICE_AFTERRETURNING;
		else if (ak.getKey()==AdviceKind.Around.getKey())
				return IRelationship.Kind.ADVICE_AROUND;
		else if (ak.getKey()==AdviceKind.CflowEntry.getKey() ||
                ak.getKey()==AdviceKind.CflowBelowEntry.getKey() ||
                ak.getKey()==AdviceKind.InterInitializer.getKey() ||
                ak.getKey()==AdviceKind.PerCflowEntry.getKey() ||
                ak.getKey()==AdviceKind.PerCflowBelowEntry.getKey() ||
                ak.getKey()==AdviceKind.PerThisEntry.getKey() ||
                ak.getKey()==AdviceKind.PerTargetEntry.getKey() ||
                ak.getKey()==AdviceKind.Softener.getKey() ||
                ak.getKey()==AdviceKind.PerTypeWithinEntry.getKey()) {
            //System.err.println("Dont want a message about this: "+ak);
            return null;
		}
		throw new RuntimeException("Shadow.determineRelKind: What the hell is it? "+ak);
	}

 	/** Actually implement the (non-empty) mungers associated with this shadow */
	private void implementMungers() {
		World world = getIWorld();
		for (Iterator iter = mungers.iterator(); iter.hasNext();) {
			ShadowMunger munger = (ShadowMunger) iter.next();
			munger.implementOn(this);
			
			if (world.getCrossReferenceHandler() != null) {
				world.getCrossReferenceHandler().addCrossReference(
				  munger.getSourceLocation(), // What is being applied
				  this.getSourceLocation(),   // Where is it being applied
				  determineRelKind(munger),   // What kind of advice?
				  ((BcelAdvice)munger).hasDynamicTests() // Is a runtime test being stuffed in the code?
				  );
			}
			
			// TAG: WeavingMessage
			if (!getIWorld().getMessageHandler().isIgnoring(IMessage.WEAVEINFO)) {
				reportWeavingMessage(munger);
			}			
			
			if (world.getModel() != null) {
				//System.err.println("munger: " + munger + " on " + this);
				AsmRelationshipProvider.getDefault().adviceMunger(world.getModel(), this, munger);
			}
		}
	}

	public String makeReflectiveFactoryString() {
		return null; //XXX
	}
	
	public abstract ISourceLocation getSourceLocation();

	// ---- utility
    
    public String toString() {
        return getKind() + "(" + getSignature() + ")"; // + getSourceLines();
    }
    
    public String toResolvedString(World world) {
    	return getKind() + "(" + world.resolve(getSignature()).toGenericString() + ")";
    }
}
