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
import java.util.Iterator;
import java.util.List;

import org.aspectj.bridge.*;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.util.PartialOrder;
import org.aspectj.util.TypeSafeEnum;
import org.aspectj.weaver.ast.Var;

/*
 * The superclass of anything representing a the shadow of a join point.  A shadow represents
 * some bit of code, and encompasses both entry and exit from that code.  All shadows have a kind
 * and a signature.
 */

public abstract class Shadow {
	private final Kind kind; 
    private final Member signature;
	protected final Shadow enclosingShadow;
    protected List mungers = new ArrayList(1);

	// ----
    protected Shadow(Kind kind, Member signature, Shadow enclosingShadow) {
        this.kind = kind;
        this.signature = signature;
        this.enclosingShadow = enclosingShadow;
    }

	// ----

    public abstract World getIWorld();
    
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
    public final TypeX getThisType() {
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
    public final TypeX getTargetType() {
        if (!hasTarget()) throw new IllegalStateException("no target");
        return getSignature().getDeclaringType();
    }
    
    /**
     * a var referencing the target
     * 
     * @throws IllegalStateException if there is no target here
     */
    public abstract Var getTargetVar();
    
    public TypeX[] getArgTypes() {
    	if (getKind() == FieldSet) return new TypeX[] { getSignature().getReturnType() };
        return getSignature().getParameterTypes();
    }
    
    public TypeX getArgType(int arg) {
    	if (getKind() == FieldSet) return getSignature().getReturnType();
        return getSignature().getParameterTypes()[arg];
    }

    public int getArgCount() {
    	if (getKind() == FieldSet) return 1;
        return getSignature()
            .getParameterTypes().length;
    }
    	
	public abstract TypeX getEnclosingType();	

	public abstract Var getArgVar(int i);
	
	public abstract Var getThisJoinPointVar();
	public abstract Var getThisJoinPointStaticPartVar();
	public abstract Var getThisEnclosingJoinPointStaticPartVar();
    
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
    
	
	public TypeX getReturnType() {
		if (kind == ConstructorCall) return getSignature().getDeclaringType();
		else if (kind == FieldSet) return ResolvedTypeX.VOID;
		return getSignature().getReturnType();
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


    /** A type-safe enum representing the kind of shadows
     */
	public static final class Kind extends TypeSafeEnum {
		private boolean argsOnStack;  //XXX unused

		public Kind(String name, int key, boolean argsOnStack) {
			super(name, key);
			this.argsOnStack = argsOnStack;
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
	
	protected boolean checkMunger(ShadowMunger munger) {
		for (Iterator i = munger.getThrownExceptions().iterator(); i.hasNext(); ) {
			if (!checkCanThrow(munger,  (ResolvedTypeX)i.next() )) return false;
		}
		return true;
	}

	protected boolean checkCanThrow(ShadowMunger munger, ResolvedTypeX resolvedTypeX) {
		if (getKind() == ExceptionHandler) {
			//XXX much too lenient rules here, need to walk up exception handlers
			return true;
		}
		
		if (!isDeclaredException(resolvedTypeX, getSignature())) {
			getIWorld().showMessage(IMessage.ERROR, "can't throw checked exception \'" + resolvedTypeX +
							"\' at this join point \'" + this +"\'", // from advice in \'" + munger. + "\'",
							getSourceLocation(), munger.getSourceLocation());
		}
		
		return true;
	}

	private boolean isDeclaredException(
		ResolvedTypeX resolvedTypeX,
		Member member)
	{
		ResolvedTypeX[] excs = getIWorld().resolve(member.getExceptions(getIWorld()));
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
		if (sorted == null) {
			// this means that we have circular dependencies
			for (Iterator i = mungers.iterator(); i.hasNext(); ) {
				ShadowMunger m = (ShadowMunger)i.next();
				getIWorld().getMessageHandler().handleMessage(
					MessageUtil.error("circular dependency at " + this, m.getSourceLocation()));
			}
		}
		mungers = sorted;
	}
	
	/** Prepare the shadow for implementation.  After this is done, the shadow
	 * should be in such a position that each munger simply needs to be implemented.
	 */
	protected void prepareForMungers() {
		throw new RuntimeException("Generic shadows cannot be prepared");		
	}

 	/** Actually implement the (non-empty) mungers associated with this shadow */
	private void implementMungers() {
		World world = getIWorld();
		for (Iterator iter = mungers.iterator(); iter.hasNext();) {
			ShadowMunger munger = (ShadowMunger) iter.next();
			munger.implementOn(this);
			if (world.getModel() != null) {
				//System.err.println("munger: " + munger + " on " + this);
				AsmAdaptor.noteMunger(world.getModel(), this, munger);
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
}
