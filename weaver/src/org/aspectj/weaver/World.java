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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.aspectj.asm.IHierarchy;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;

public abstract class World implements Dump.INode {
	protected IMessageHandler messageHandler = IMessageHandler.SYSTEM_ERR;
	protected ICrossReferenceHandler xrefHandler = null;

    protected TypeMap typeMap = new TypeMap(); // Signature to ResolvedType
    
    protected CrosscuttingMembersSet crosscuttingMembersSet = new CrosscuttingMembersSet(this);
    
    protected IHierarchy model = null;
    
    protected Lint lint = new Lint(this);
    
    protected boolean XnoInline;
    protected boolean XlazyTjp;

    public boolean behaveInJava5Way = false;
    

    private List dumpState_cantFindTypeExceptions = null;
	
    protected World() {
        super();
        Dump.registerNode(this.getClass(),this);
        typeMap.put("B", ResolvedType.BYTE);
        typeMap.put("S", ResolvedType.SHORT);
        typeMap.put("I", ResolvedType.INT);
        typeMap.put("J", ResolvedType.LONG);
        typeMap.put("F", ResolvedType.FLOAT);
        typeMap.put("D", ResolvedType.DOUBLE);
        typeMap.put("C", ResolvedType.CHAR);
        typeMap.put("Z", ResolvedType.BOOLEAN);
        typeMap.put("V", ResolvedType.VOID);
    }
    
    public void accept (Dump.IVisitor visitor) {
		visitor.visitString("Shadow mungers:");
		visitor.visitList(crosscuttingMembersSet.getShadowMungers());
		visitor.visitString("Type mungers:");
		visitor.visitList(crosscuttingMembersSet.getTypeMungers());
        visitor.visitString("Late Type mungers:");
        visitor.visitList(crosscuttingMembersSet.getLateTypeMungers());
        if (dumpState_cantFindTypeExceptions!=null) {
          visitor.visitString("Cant find type problems:");
          visitor.visitList(dumpState_cantFindTypeExceptions);
          dumpState_cantFindTypeExceptions = null;
        }
    }

    public ResolvedType[] resolve(UnresolvedType[] types) {
        int len = types.length;
        ResolvedType[] ret = new ResolvedType[len];
        for (int i=0; i<len; i++) {
            ret[i] = resolve(types[i]);
        }
        return ret;
    }
    
    /**
     * Attempt to resolve a type - the source location gives you some context in which
     * resolution is taking place.  In the case of an error where we can't find the
     * type - we can then at least report why (source location) we were trying to resolve it.
     */
    public ResolvedType resolve(UnresolvedType ty,ISourceLocation isl) {
        ResolvedType ret = resolve(ty,true);
        if (ty == ResolvedType.MISSING) {
            IMessage msg = null;
            if (isl!=null) {
              msg = MessageUtil.error(WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE,ty.getName()),isl);
            } else {
              msg = MessageUtil.error(WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE,ty.getName())); 
            }
            messageHandler.handleMessage(msg);
        }
        return ret;
    }
    
    public ResolvedType resolve(UnresolvedType ty) {
    	return resolve(ty, false);
    }
    
    // if we already have an rtx, don't re-resolve it
    public ResolvedType resolve(ResolvedType ty) {
    	return ty;
    }
    
    public ResolvedType getCoreType(UnresolvedType tx) {
    	ResolvedType coreTy = resolve(tx,true);
    	if (coreTy == ResolvedType.MISSING) {
    		 MessageUtil.error(messageHandler, 
            	WeaverMessages.format(WeaverMessages.CANT_FIND_CORE_TYPE,tx.getName()));
    	}
    	return coreTy;
    }
    
    /**
     * Attempt to resolve a type that should be a generic type.  
     */
    public ResolvedType resolveTheGenericType(UnresolvedType ty, boolean allowMissing) {
        // Look up the raw type by signature
    	String signature = ty.getRawType().getSignature();
    	ResolvedType ret = (ResolvedType) typeMap.get(signature);
    	if (ret==null) {
    		ret = resolve(UnresolvedType.forSignature(signature));
        	typeMap.put(signature,ret);
    	}
    	//TODO asc temporary guard - can this ever happen?
    	if (ret==null) throw new RuntimeException("RAW TYPE IS missing for "+ty);
    	
    	// Does the raw type know its generic form? (It will if we created the
    	// raw type from a source type, it won't if its been created just through
    	// being referenced, e.g. java.util.List
    	ResolvedType generic = ret.getGenericType();
    	if (generic != null) { generic.world = this; return generic; } 

    	// Fault in the generic that underpins the raw type ;)
    	ReferenceTypeDelegate thegen = resolveObjectType((ReferenceType)ret);
    	ReferenceType genericRefType = new ReferenceType(
    			UnresolvedType.forGenericTypeSignature(ret.getSignature(),thegen.getDeclaredGenericSignature()),this);
    	((ReferenceType)ret).setGenericType(genericRefType);
    	genericRefType.delegate=thegen;
    	((ReferenceType)ret).delegate=thegen;
    	return genericRefType;
    }

    public ResolvedType resolve(UnresolvedType ty, boolean allowMissing) {
    	if (ty instanceof ResolvedType) {
    		ResolvedType rty = (ResolvedType) ty;
    		rty.world = this; 
    		return rty;
    	}
    	//System.out.println("resolve: " + ty + " world " + typeMap.keySet());		
    	if (ty instanceof UnresolvedTypeVariableReferenceType) {
    		// AMC - don't like this instanceof test, suggests some refactoring needed...
    		return ((UnresolvedTypeVariableReferenceType)ty).resolve(this);
    	}
        String signature = ty.getSignature();
        ResolvedType ret = typeMap.get(signature);
        if (ret != null) { 
        	ret.world = this;  // Set the world for the RTX
        	return ret; 
        }
        
        if (ty.isArray()) {
            ret = new ResolvedType.Array(signature, this, resolve(ty.getComponentType(), allowMissing));
        } else {
            ret = resolveObjectType(ty);
            if (!allowMissing && ret == ResolvedType.MISSING) {
                MessageUtil.error(messageHandler, 
                		WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE,ty.getName()));
                if (dumpState_cantFindTypeExceptions==null) {
                  dumpState_cantFindTypeExceptions = new ArrayList();   
                }
                dumpState_cantFindTypeExceptions.add(new RuntimeException("Can't find type "+ty.getName()));
            }
        }
		if (ty.isParameterizedType()) {
			for (int i = 0; i < ty.typeParameters.length; i++) {
				ty.typeParameters[i] = resolve(ty.typeParameters[i],allowMissing);
			}
		}
        //System.out.println("ret: " + ret);
        // Pulling in the type may have already put the right entry in the map
		if (typeMap.get(signature)==null) {
	        typeMap.put(signature, ret);
		}
        return ret;
    }
    
    //XXX helper method might be bad
    public ResolvedType resolve(String name) {
    	return resolve(UnresolvedType.forName(name));
    }

	
	/*
     * Copes with parameterized types.  When it sees one then it discovers the
     * base type and reuses the delegate
     */
    protected final ResolvedType resolveObjectType(UnresolvedType ty) {
		if (ty.isParameterizedType()) {
			ReferenceType genericType = (ReferenceType)resolveTheGenericType(ty,false);
			ReferenceType parameterizedType = new ReferenceType(ty.getSignature(),this);
			parameterizedType.setGenericType(genericType);
			parameterizedType.setDelegate(genericType.getDelegate()); // move into setgenerictype
			return parameterizedType;
		} else if (ty.isGenericType()) {
			ReferenceType genericType = (ReferenceType)resolveTheGenericType(ty,false);
			return genericType;
		} else {
			String signature = ty.getRawTypeSignature();
    		ReferenceType name = new ReferenceType(signature/*was ty.getSignature()*/, this);
	    	ReferenceTypeDelegate concreteName = resolveObjectType(name);
	    	if (concreteName == null) return ResolvedType.MISSING;
	    	if (concreteName.isGeneric()) {
	    		// During resolution we have discovered that the underlying type is in fact generic, 
	    		// so the thing we thought was a simple type is in fact a raw type.

	        	ReferenceType genericRefType = new ReferenceType(
	        			UnresolvedType.forGenericTypeSignature(signature,concreteName.getDeclaredGenericSignature()),this);
	    		// name =  ReferenceType.fromTypeX(UnresolvedType.forRawTypeNames(ty.getName()),this);
		    	name.setDelegate(concreteName);
		    	genericRefType.setDelegate(concreteName);
		    	name.setGenericType(genericRefType);
		    	return name;
	    	} else {
		    	name.setDelegate(concreteName);
		    	return name;
	    	}
		}
    }
    
    protected abstract ReferenceTypeDelegate resolveObjectType(ReferenceType ty);
    

    protected final boolean isCoerceableFrom(ResolvedType type, ResolvedType other) {
        return type.isCoerceableFrom(other);
    }

    protected final boolean isAssignableFrom(ResolvedType type, ResolvedType other) {
        return type.isAssignableFrom(other);
    }

    public boolean needsNoConversionFrom(ResolvedType type, ResolvedType other) {
        return type.needsNoConversionFrom(other);
    }
    
    protected final boolean isInterface(UnresolvedType type) {
        return resolve(type).isInterface();
    }

    protected final ResolvedType getSuperclass(UnresolvedType type) {
        return resolve(type).getSuperclass();
    }

    protected final UnresolvedType[] getDeclaredInterfaces(UnresolvedType type) {
        return resolve(type).getDeclaredInterfaces();
    }

    protected final int getModifiers(UnresolvedType type) {
        return resolve(type).getModifiers();
    }

    protected final ResolvedMember[] getDeclaredFields(UnresolvedType type) {
        return resolve(type).getDeclaredFields();
    }

    protected final ResolvedMember[] getDeclaredMethods(UnresolvedType type) {
        return resolve(type).getDeclaredMethods();
    }

    protected final ResolvedMember[] getDeclaredPointcuts(UnresolvedType type) {
        return resolve(type).getDeclaredPointcuts();
    }

    // ---- members


    // XXX should we worry about dealing with context and looking up access?
    public ResolvedMember resolve(Member member) {
        ResolvedType declaring = member.getDeclaringType().resolve(this);
        ResolvedMember ret;
        if (member.getKind() == Member.FIELD) {
            ret = declaring.lookupField(member);
        } else {
            ret = declaring.lookupMethod(member);
        }
        
        if (ret != null) return ret;
        
        return declaring.lookupSyntheticMember(member);   
    }

    protected int getModifiers(Member member) {
    	ResolvedMember r = resolve(member);
    	if (r == null) throw new BCException("bad resolve of " + member);
        return r.getModifiers();
    }

    protected String[] getParameterNames(Member member) {
        return resolve(member).getParameterNames();
    }

    protected UnresolvedType[] getExceptions(Member member) {
        return resolve(member).getExceptions();
    }

    // ---- pointcuts

    public ResolvedPointcutDefinition findPointcut(UnresolvedType typeX, String name) {
        throw new RuntimeException("not implemented yet");
    }
    
    /**
     * Get the shadow mungers of this world.
     * 
     * @return a list of {@link IShadowMunger}s appropriate for this world.
     */      
    //public abstract List getShadowMungers();
    
    // ---- empty world
    
//    public static final World EMPTY = new World() {
//        public List getShadowMungers() { return Collections.EMPTY_LIST; }
//        public ResolvedType.ConcreteName resolveObjectType(ResolvedType.Name ty) {
//            return null;
//        }
//        public Advice concreteAdvice(AjAttribute.AdviceAttribute attribute, Pointcut p, Member m) {
//            throw new RuntimeException("unimplemented");
//        }
//        public ConcreteTypeMunger concreteTypeMunger(ResolvedTypeMunger munger, ResolvedType aspectType) {
//            throw new RuntimeException("unimplemented");
//        }        
//    };
    
    
    public abstract Advice concreteAdvice(
    	AjAttribute.AdviceAttribute attribute,
    	Pointcut pointcut,
        Member signature);
        
    public final Advice concreteAdvice(
     	AdviceKind kind,
        Pointcut p,
        Member signature,
        int extraParameterFlags,
        IHasSourceLocation loc)
    {
    	AjAttribute.AdviceAttribute attribute = 
    		new AjAttribute.AdviceAttribute(kind, p, extraParameterFlags, loc.getStart(), loc.getEnd(), loc.getSourceContext());
		return concreteAdvice(attribute, p, signature);
    }
        
        

	public ConcreteTypeMunger makeCflowStackFieldAdder(ResolvedMember cflowField) {
		throw new RuntimeException("unimplemented");
	}
	
	public ConcreteTypeMunger makeCflowCounterFieldAdder(ResolvedMember cflowField) {
		throw new RuntimeException("unimplemented");
	}

    /**
     * Register a munger for perclause @AJ aspect so that we add aspectOf(..) to them as needed
     * @see org.aspectj.weaver.bcel.BcelWorld#makePerClauseAspect(ResolvedType, org.aspectj.weaver.patterns.PerClause.Kind)
     * 
     * @param aspect
     * @param kind
     * @return
     */
    public ConcreteTypeMunger makePerClauseAspect(ResolvedType aspect, PerClause.Kind kind) {
        throw new RuntimeException("unimplemented");
    }

    public abstract ConcreteTypeMunger concreteTypeMunger(ResolvedTypeMunger munger, ResolvedType aspectType);

	/**
	 * Nobody should hold onto a copy of this message handler, or setMessageHandler won't
	 * work right.
	 */
	public IMessageHandler getMessageHandler() {
		return messageHandler;
	}

	public void setMessageHandler(IMessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}
	
	public void setXRefHandler(ICrossReferenceHandler xrefHandler) {
		this.xrefHandler = xrefHandler;
	}
	
	public void showMessage(
		Kind kind,
		String message,
		ISourceLocation loc1,
		ISourceLocation loc2)
	{
		if (loc1 != null) {
			messageHandler.handleMessage(new Message(message, kind, null, loc1));
			if (loc2 != null) {
				messageHandler.handleMessage(new Message(message, kind, null, loc2));
			}
		} else {
			messageHandler.handleMessage(new Message(message, kind, null, loc2));
		}
	}


	

//	public void addDeclare(ResolvedType onType, Declare declare, boolean forWeaving) {
//		// this is not extensible, oh well
//		if (declare instanceof DeclareErrorOrWarning) {
//			ShadowMunger m = new Checker((DeclareErrorOrWarning)declare);
//			onType.addShadowMunger(m);
//		} else if (declare instanceof DeclareDominates) {
//			declareDominates.add(declare);
//		} else if (declare instanceof DeclareParents) {
//			declareParents.add(declare);
//		} else if (declare instanceof DeclareSoft) {
//			DeclareSoft d = (DeclareSoft)declare;
//			declareSoft.add(d);
//			if (forWeaving) {
//				ShadowMunger m = Advice.makeSoftener(this, d.getPointcut().concretize(onType, 0), d.getException());
//				onType.addShadowMunger(m);
//			}
//		} else {
//			throw new RuntimeException("unimplemented");
//		}
//	}


	/**
	 * Same signature as org.aspectj.util.PartialOrder.PartialComparable.compareTo
	 */
	public int compareByDominates(ResolvedType aspect1, ResolvedType aspect2) {
		//System.out.println("dom compare: " + aspect1 + " with " + aspect2);
		//System.out.println(crosscuttingMembersSet.getDeclareDominates());
		
		//??? We probably want to cache this result.  This is order N where N is the
		//??? number of dominates declares in the whole system.
		//??? This method can be called a large number of times.
		int order = 0;
		DeclarePrecedence orderer = null; // Records the declare precedence statement that gives the first ordering
		for (Iterator i = crosscuttingMembersSet.getDeclareDominates().iterator(); i.hasNext(); ) {
			DeclarePrecedence d = (DeclarePrecedence)i.next();
			int thisOrder = d.compare(aspect1, aspect2);
			//System.out.println("comparing: " + thisOrder + ": " + d);
			if (thisOrder != 0) {
				if (orderer==null) orderer = d;
				if (order != 0 && order != thisOrder) {
					ISourceLocation[] isls = new ISourceLocation[2];
					isls[0]=orderer.getSourceLocation();
					isls[1]=d.getSourceLocation();
					Message m = 
					  new Message("conflicting declare precedence orderings for aspects: "+
					              aspect1.getName()+" and "+aspect2.getName(),null,true,isls);
					messageHandler.handleMessage(m);
					// throw new BCException("conflicting dominates orders"+d.getSourceLocation());
				} else {
					order = thisOrder;
				}
			}
		}
		
		
		return order; 
	}
	
	
	public int comparePrecedence(ResolvedType aspect1, ResolvedType aspect2) {
		//System.err.println("compare precedence " + aspect1 + ", " + aspect2);
		if (aspect1.equals(aspect2)) return 0;
		
		int ret = compareByDominates(aspect1, aspect2);
		if (ret != 0) return ret;
		
		if (aspect1.isAssignableFrom(aspect2)) return -1;
		else if (aspect2.isAssignableFrom(aspect1)) return +1;

		return 0;
	}


	
	
	public List getDeclareParents() {
		return crosscuttingMembersSet.getDeclareParents();
	}
	
	public List getDeclareAnnotationOnTypes() {
		return crosscuttingMembersSet.getDeclareAnnotationOnTypes();
	}
	
	public List getDeclareAnnotationOnFields() {
		return crosscuttingMembersSet.getDeclareAnnotationOnFields();
	}
	
	public List getDeclareAnnotationOnMethods() {
		return crosscuttingMembersSet.getDeclareAnnotationOnMethods();
	}

	public List getDeclareSoft() {
		return crosscuttingMembersSet.getDeclareSofts();
	}

	public CrosscuttingMembersSet getCrosscuttingMembersSet() {
		return crosscuttingMembersSet;
	}

	public IHierarchy getModel() {
		return model;
	}

	public void setModel(IHierarchy model) {
		this.model = model;
	}

	public Lint getLint() {
		return lint;
	}

	public void setLint(Lint lint) {
		this.lint = lint;
	}

	public boolean isXnoInline() {
		return XnoInline;
	}

	public void setXnoInline(boolean xnoInline) {
		XnoInline = xnoInline;
	}
	
	public boolean isXlazyTjp() {
		return XlazyTjp;
	}

	public void setXlazyTjp(boolean b) {
		XlazyTjp = b;
	}

	public ReferenceType lookupOrCreateName(UnresolvedType ty) {
		String signature = ty.getSignature();
        ReferenceType ret = (ReferenceType)typeMap.get(signature);
        if (ret == null) {
        	ret = ReferenceType.fromTypeX(ty, this);
        	typeMap.put(signature, ret);
        }
		return ret;
	}
	

//	public void clearUnexposed() {
//		List toRemove = new ArrayList();
//		for (Iterator iter = typeMap.keySet().iterator(); iter.hasNext();) {
//			String sig = (String) iter.next();
//			ResolvedType x = (ResolvedType) typeMap.get(sig);
//			if (!x.isExposedToWeaver() && (!x.isPrimitive())) toRemove.add(sig);
//		}		
//		for (Iterator iter = toRemove.iterator(); iter.hasNext();) {
//			typeMap.remove(iter.next());		
//		}
//	}
//	
//	// for testing...
//	public void dumpTypeMap() {
//		int exposed = 0;
//		for (Iterator iter = typeMap.keySet().iterator(); iter.hasNext();) {
//			String sig = (String) iter.next();
//			ResolvedType x = (ResolvedType) typeMap.get(sig);
//			if (x.isExposedToWeaver()) exposed++;
//		}
//		System.out.println("type map contains " + typeMap.size() + " entries, " + exposed + " exposed to weaver");
//	}
//	
//	public void deepDumpTypeMap() {
//		for (Iterator iter = typeMap.keySet().iterator(); iter.hasNext();) {
//			String sig = (String) iter.next();
//			ResolvedType x = (ResolvedType) typeMap.get(sig);
//			if (! (x instanceof ResolvedType.Name)) {
//				System.out.println(sig + " -> " + x.getClass().getName() + ", " + x.getClassName());
//			} else {
//				ResolvedType.ConcreteName cname = ((ResolvedType.Name)x).getDelegate();
//				System.out.println(sig + " -> " + cname.getClass().getName() + ", " + cname.toString());
//			}
//		}
//		
//	}
	
	// Map of types in the world, with soft links to expendable ones
	protected static class TypeMap {
		private Map tMap = new HashMap();
		private Map expendableMap = new WeakHashMap();
					
		public ResolvedType put(String key, ResolvedType type) { 
			if (isExpendable(type))  {
				return (ResolvedType) expendableMap.put(key,type);
			} else {
				return (ResolvedType) tMap.put(key,type);
			}
		}
		
		public ResolvedType get(String key) {
			ResolvedType ret = (ResolvedType) tMap.get(key);
			if (ret == null) ret = (ResolvedType) expendableMap.get(key);
			return ret;
		}
		
		public ResolvedType remove(String key) {
			ResolvedType ret = (ResolvedType) tMap.remove(key);
			if (ret == null) ret = (ResolvedType) expendableMap.remove(key);
			return ret;
		}
		
		private boolean isExpendable(ResolvedType type) {
			return (
					  (type != null) &&
					  (!type.isExposedToWeaver()) &&
					  (!type.isPrimitiveType())
					);
		}
	}	
	
	public void setBehaveInJava5Way(boolean b) {
    	behaveInJava5Way = b;
    }
}
