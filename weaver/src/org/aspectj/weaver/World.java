/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 *               2005 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation
 *     Adrian Colyer, Andy Clement, overhaul for generics 
 * ******************************************************************/

package org.aspectj.weaver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.aspectj.asm.IHierarchy;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.weaver.UnresolvedType.TypeKind;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;

/**
 * A World is a collection of known types and crosscutting members.
 */
public abstract class World implements Dump.INode {
	/** handler for any messages produced during resolution etc. */
	private IMessageHandler messageHandler = IMessageHandler.SYSTEM_ERR;
	
	/** handler for cross-reference information produced during the weaving process */
	private ICrossReferenceHandler xrefHandler = null;

	/** The heart of the world, a map from type signatures to resolved types */
    protected TypeMap typeMap = new TypeMap(); // Signature to ResolvedType

    /** Calculator for working out aspect precedence */
    private AspectPrecedenceCalculator precedenceCalculator;
    
    /** All of the type and shadow mungers known to us */
    private CrosscuttingMembersSet crosscuttingMembersSet = 
    	new CrosscuttingMembersSet(this);
    
    /** Model holds ASM relationships */
    private IHierarchy model = null;
    
    /** for processing Xlint messages */
    private Lint lint = new Lint(this);
    
    /** XnoInline option setting passed down to weaver */
    private boolean XnoInline;
    
    /** XlazyTjp option setting passed down to weaver */
    private boolean XlazyTjp;

    /** XhasMember option setting passed down to weaver */
    private boolean XhasMember = false;
    
    /** When behaving in a Java 5 way autoboxing is considered */
    private boolean behaveInJava5Way = false;
    
    /** 
     * A list of RuntimeExceptions containing full stack information for every
     * type we couldn't find.
     */
    private List dumpState_cantFindTypeExceptions = null;
	
    /**
     * Play God.
     * On the first day, God created the primitive types and put them in the type
     * map.
     */
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
        precedenceCalculator = new AspectPrecedenceCalculator(this);
    }
    
    /**
     * Dump processing when a fatal error occurs
     */
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
    
    
    // =============================================================================
    // T Y P E   R E S O L U T I O N
    // =============================================================================

    /**
     * Resolve a type that we require to be present in the world
     */
    public ResolvedType resolve(UnresolvedType ty) {
    	return resolve(ty, false);
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
    
    /**
     * Convenience method for resolving an array of unresolved types
     * in one hit. Useful for e.g. resolving type parameters in signatures.
     */
    public ResolvedType[] resolve(UnresolvedType[] types) {
    	if (types == null) return new ResolvedType[0];
    	
        ResolvedType[] ret = new ResolvedType[types.length];
        for (int i=0; i<types.length; i++) {
            ret[i] = resolve(types[i]);
        }
        return ret;
    }

    /**
     * Resolve a type. This the hub of type resolution. The resolved type is added
     * to the type map by signature.
     */
    public ResolvedType resolve(UnresolvedType ty, boolean allowMissing) {
    	
    	// special resolution processing for already resolved types.
    	if (ty instanceof ResolvedType) {
    		ResolvedType rty = (ResolvedType) ty;
    		rty = resolve(rty);
    		return rty;
    	}

    	// dispatch back to the type variable reference to resolve its constituent parts
    	// don't do this for other unresolved types otherwise you'll end up in a loop
    	if (ty.isTypeVariableReference()) {
    		return ty.resolve(this);
    	}
    	
    	// if we've already got a resolved type for the signature, just return it
    	// after updating the world
        String signature = ty.getSignature();
        ResolvedType ret = typeMap.get(signature);
        if (ret != null) { 
        	ret.world = this;  // Set the world for the RTX
        	return ret; 
        } else if ( signature.equals("?") || signature.equals("*")) {
        // might be a problem here, not sure '?' should make it to here as a signature, the 
        // proper signature for wildcard '?' is '*'
        	// fault in generic wildcard, can't be done earlier because of init issues
        	ResolvedType something = new BoundedReferenceType("?",this);
        	typeMap.put("?",something);
        	return something;
        }
        
        // no existing resolved type, create one
        if (ty.isArray()) {
            ret = new ResolvedType.Array(signature, 
            		                     this, 
            		                     resolve(ty.getComponentType(), 
            		                     allowMissing));
        } else {
            ret = resolveToReferenceType(ty);
            if (!allowMissing && ret == ResolvedType.MISSING) {
                handleRequiredMissingTypeDuringResolution(ty);
            }
        }        
  
		// Pulling in the type may have already put the right entry in the map
		if (typeMap.get(signature)==null && ret != ResolvedType.MISSING) {
	        typeMap.put(signature, ret);
		}
        return ret;
    }

    /**
     * We tried to resolve a type and couldn't find it...
     */
	private void handleRequiredMissingTypeDuringResolution(UnresolvedType ty) {
		MessageUtil.error(messageHandler, 
				WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE,ty.getName()));
		if (dumpState_cantFindTypeExceptions==null) {
		  dumpState_cantFindTypeExceptions = new ArrayList();   
		}
		dumpState_cantFindTypeExceptions.add(new RuntimeException("Can't find type "+ty.getName()));
	}
    
    /**
     * Some TypeFactory operations create resolved types directly, but these won't be
     * in the typeMap - this resolution process puts them there. Resolved types are
     * also told their world which is needed for the special autoboxing resolved types.
     */
    public ResolvedType resolve(ResolvedType ty) {
    	if (ty.isTypeVariableReference()) return ty; // until type variables have proper sigs...
    	ResolvedType resolved = typeMap.get(ty.getSignature());
    	if (resolved == null) {
    		typeMap.put(ty.getSignature(), ty);
    		resolved = ty;
    	}
    	resolved.world = this;
    	return resolved;
    }
    
    /**
     * Convenience method for finding a type by name and resolving it in one step.
     */
    public ResolvedType resolve(String name) {
    	return resolve(UnresolvedType.forName(name));
    }
    
    public ResolvedType resolve(String name,boolean allowMissing) {
    	return resolve(UnresolvedType.forName(name),allowMissing);
    }
    
	
	/**
	 * Resolve to a ReferenceType - simple, raw, parameterized, or generic.
     * Raw, parameterized, and generic versions of a type share a delegate.
     */
    private final ResolvedType resolveToReferenceType(UnresolvedType ty) {
		if (ty.isParameterizedType()) {
			// ======= parameterized types ================
			ReferenceType genericType = (ReferenceType)resolveGenericTypeFor(ty,false);
			ReferenceType parameterizedType = 
				TypeFactory.createParameterizedType(genericType, ty.typeParameters, this);
			return parameterizedType;
			
		} else if (ty.isGenericType()) {
			// ======= generic types ======================
			ReferenceType genericType = (ReferenceType)resolveGenericTypeFor(ty,false);
			return genericType;
			
		} else if (ty.isGenericWildcard()) {
			// ======= generic wildcard types =============
			return resolveGenericWildcardFor(ty);
    	} else {
			// ======= simple and raw types ===============
			String erasedSignature = ty.getErasureSignature();
    		ReferenceType simpleOrRawType = new ReferenceType(erasedSignature, this);
	    	ReferenceTypeDelegate delegate = resolveDelegate(simpleOrRawType);
	    	if (delegate == null) return ResolvedType.MISSING;
	    	
	    	if (delegate.isGeneric()) {
	    		// ======== raw type ===========
	    		simpleOrRawType.typeKind = TypeKind.RAW;
	        	ReferenceType genericType = new ReferenceType(
	        			UnresolvedType.forGenericTypeSignature(erasedSignature,delegate.getDeclaredGenericSignature()),this);
	    		// name =  ReferenceType.fromTypeX(UnresolvedType.forRawTypeNames(ty.getName()),this);
		    	simpleOrRawType.setDelegate(delegate);
		    	genericType.setDelegate(delegate);
		    	simpleOrRawType.setGenericType(genericType);
		    	return simpleOrRawType;
		    	
	    	} else {
	    		// ======== simple type =========
		    	simpleOrRawType.setDelegate(delegate);
		    	return simpleOrRawType;
	    	}
		}
    }

    /**
     * Attempt to resolve a type that should be a generic type.  
     */
    public ResolvedType resolveGenericTypeFor(UnresolvedType anUnresolvedType, boolean allowMissing) {
        // Look up the raw type by signature
    	String rawSignature = anUnresolvedType.getRawType().getSignature();
    	ResolvedType rawType = (ResolvedType) typeMap.get(rawSignature);
    	if (rawType==null) {
    		rawType = resolve(UnresolvedType.forSignature(rawSignature),false);
        	typeMap.put(rawSignature,rawType);
    	}
    	
    	// Does the raw type know its generic form? (It will if we created the
    	// raw type from a source type, it won't if its been created just through
    	// being referenced, e.g. java.util.List
    	ResolvedType genericType = rawType.getGenericType();
    	
    	// There is a special case to consider here (testGenericsBang_pr95993 highlights it)
    	// You may have an unresolvedType for a parameterized type but it
    	// is backed by a simple type rather than a generic type.  This occurs for
    	// inner types of generic types that inherit their enclosing types
    	// type variables.
    	if (rawType.isSimpleType() && anUnresolvedType.typeParameters.length==0) {
    		rawType.world = this;
    		return rawType; 
    	}
    	
    	if (genericType != null) { 
    		genericType.world = this;
    		return genericType; 
    	} else {
	    	// Fault in the generic that underpins the raw type ;)
	    	ReferenceTypeDelegate delegate = resolveDelegate((ReferenceType)rawType);
	    	ReferenceType genericRefType = new ReferenceType(
	    			UnresolvedType.forGenericTypeSignature(rawType.getSignature(),delegate.getDeclaredGenericSignature()),this);
	    	((ReferenceType)rawType).setGenericType(genericRefType);
	    	genericRefType.setDelegate(delegate);
	    	((ReferenceType)rawType).setDelegate(delegate);
	    	return genericRefType;
    	}
    }

    /**
     * Go from an unresolved generic wildcard (represented by UnresolvedType) to a resolved version (BoundedReferenceType).
     */
    private ReferenceType resolveGenericWildcardFor(UnresolvedType aType) {
    	BoundedReferenceType ret = null;
    	// FIXME asc doesnt take account of additional interface bounds (e.g. ? super R & Serializable - can you do that?)
    	if (aType.isExtends()) {
    		ReferenceType upperBound = (ReferenceType)resolve(aType.getUpperBound());
    		ret = new BoundedReferenceType(upperBound,true,this);
       	} else if (aType.isSuper()) {
    		ReferenceType lowerBound = (ReferenceType) resolve(aType.getLowerBound());
    		ret = new BoundedReferenceType(lowerBound,false,this);
    	} else {
    		// must be ? on its own!
    	}
    	return ret;
    }
    
    /**
     * Find the ReferenceTypeDelegate behind this reference type so that it can 
     * fulfill its contract.
     */
    protected abstract ReferenceTypeDelegate resolveDelegate(ReferenceType ty);
    
    /**
     * Special resolution for "core" types like OBJECT. These are resolved just like
     * any other type, but if they are not found it is more serious and we issue an
     * error message immediately.
     */
    public ResolvedType getCoreType(UnresolvedType tx) {
    	ResolvedType coreTy = resolve(tx,true);
    	if (coreTy == ResolvedType.MISSING) {
    		 MessageUtil.error(messageHandler, 
            	WeaverMessages.format(WeaverMessages.CANT_FIND_CORE_TYPE,tx.getName()));
    	}
    	return coreTy;
    }

    /**
     * Lookup a type by signature, if not found then build one and put it in the
     * map.
     */
	public ReferenceType lookupOrCreateName(UnresolvedType ty) {
		String signature = ty.getSignature();
        ReferenceType ret = lookupBySignature(signature);
        if (ret == null) {
        	ret = ReferenceType.fromTypeX(ty, this);
        	typeMap.put(signature, ret);
        }
		return ret;
	}

	/**
	 * Lookup a reference type in the world by its signature. Returns
	 * null if not found.
	 */
	public ReferenceType lookupBySignature(String signature) {
		return (ReferenceType) typeMap.get(signature);
	}
	

    // =============================================================================
    // T Y P E   R E S O L U T I O N  -- E N D
    // =============================================================================

    /**
     * Member resolution is achieved by resolving the declaring type and then
     * looking up the member in the resolved declaring type.
     */
    public ResolvedMember resolve(Member member) {
        ResolvedType declaring = member.getDeclaringType().resolve(this);
        if (declaring.isRawType()) declaring = declaring.getGenericType();
        ResolvedMember ret;
        if (member.getKind() == Member.FIELD) {
            ret = declaring.lookupField(member);
        } else {
            ret = declaring.lookupMethod(member);
        }
        
        if (ret != null) return ret;
        
        return declaring.lookupSyntheticMember(member);   
    }
    
    // Methods for creating various cross-cutting members...
    // ===========================================================
    
    /**
     * Create an advice shadow munger from the given advice attribute 
     */
    public abstract Advice createAdviceMunger(
    	AjAttribute.AdviceAttribute attribute,
    	Pointcut pointcut,
        Member signature);
        
    /**
     * Create an advice shadow munger for the given advice kind
     */
    public final Advice createAdviceMunger(
     	AdviceKind kind,
        Pointcut p,
        Member signature,
        int extraParameterFlags,
        IHasSourceLocation loc)
    {
    	AjAttribute.AdviceAttribute attribute = 
    		new AjAttribute.AdviceAttribute(kind, p, extraParameterFlags, loc.getStart(), loc.getEnd(), loc.getSourceContext());
		return createAdviceMunger(attribute, p, signature);
    }
        
	public abstract ConcreteTypeMunger makeCflowStackFieldAdder(ResolvedMember cflowField);
	
	public abstract ConcreteTypeMunger makeCflowCounterFieldAdder(ResolvedMember cflowField);

    /**
     * Register a munger for perclause @AJ aspect so that we add aspectOf(..) to them as needed
     * @see org.aspectj.weaver.bcel.BcelWorld#makePerClauseAspect(ResolvedType, org.aspectj.weaver.patterns.PerClause.Kind)
     */
    public abstract ConcreteTypeMunger makePerClauseAspect(ResolvedType aspect, PerClause.Kind kind);

    public abstract ConcreteTypeMunger concreteTypeMunger(ResolvedTypeMunger munger, ResolvedType aspectType);

	/**
	 * Same signature as org.aspectj.util.PartialOrder.PartialComparable.compareTo
	 */
	public int compareByPrecedence(ResolvedType aspect1, ResolvedType aspect2) {
		return precedenceCalculator.compareByPrecedence(aspect1, aspect2);
	}
		
	/**
	 * compares by precedence with the additional rule that a super-aspect is 
	 * sorted before its sub-aspects
	 */
	public int compareByPrecedenceAndHierarchy(ResolvedType aspect1, ResolvedType aspect2) {
		return precedenceCalculator.compareByPrecedenceAndHierarchy(aspect1, aspect2);
	}

    // simple property getter and setters
    // ===========================================================
    
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

	/**
	 * convenenience method for creating and issuing messages via the message handler
	 */
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


	public void setCrossReferenceHandler(ICrossReferenceHandler xrefHandler) {
		this.xrefHandler = xrefHandler;
	}

    /**
     * Get the cross-reference handler for the world, may be null.
     */
    public ICrossReferenceHandler getCrossReferenceHandler() {
    	return this.xrefHandler;
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
	
	public boolean isHasMemberSupportEnabled() {
		return XhasMember;
	}
	
	public void setXHasMemberSupportEnabled(boolean b) {
		XhasMember = b;
	}

	public void setBehaveInJava5Way(boolean b) {
    	behaveInJava5Way = b;
    }
	
	public boolean isInJava5Mode() {
		return behaveInJava5Way;
	}
	
	/*
	 *  Map of types in the world, with soft links to expendable ones.
	 *  An expendable type is a reference type that is not exposed to the weaver (ie
	 *  just pulled in for type resolution purposes).
	 */
	protected static class TypeMap {
		/** Map of types that never get thrown away */
		private Map tMap = new HashMap();
		/** Map of types that may be ejected from the cache if we need space */
		private Map expendableMap = new WeakHashMap();
		
		private static final boolean debug = false;
		/** 
		 * Add a new type into the map, the key is the type signature.
		 * Some types do *not* go in the map, these are ones involving
		 * *member* type variables.  The reason is that when all you have is the
		 * signature which gives you a type variable name, you cannot 
		 * guarantee you are using the type variable in the same way 
		 * as someone previously working with a similarly
		 * named type variable.  So, these do not go into the map:
		 * - TypeVariableReferenceType.
		 * - ParameterizedType where a member type variable is involved.
		 * - BoundedReferenceType when one of the bounds is a type variable.
		 * 
		 * definition: "member type variables" - a tvar declared on a generic 
		 * method/ctor as opposed to those you see declared on a generic type.
		 */
		public ResolvedType put(String key, ResolvedType type) { 
			if (type.isParameterizedType() && type.isParameterizedWithAMemberTypeVariable()) {
				if (debug) 
					System.err.println("Not putting a parameterized type that utilises member declared type variables into the typemap: key="+key+" type="+type);
				return type;
			}
			if (type.isTypeVariableReference()) {
				if (debug) 
					System.err.println("Not putting a type variable reference type into the typemap: key="+key+" type="+type);
				return type;
			}
			// this test should be improved - only avoid putting them in if one of the
			// bounds is a member type variable
			if (type instanceof BoundedReferenceType) {
				if (debug) 
					System.err.println("Not putting a bounded reference type into the typemap: key="+key+" type="+type);
				return type;
			}
						
			if (isExpendable(type))  {
				return (ResolvedType) expendableMap.put(key,type);
			} else {
				return (ResolvedType) tMap.put(key,type);
			}
		}
		
		/** Lookup a type by its signature */
		public ResolvedType get(String key) {
			ResolvedType ret = (ResolvedType) tMap.get(key);
			if (ret == null) ret = (ResolvedType) expendableMap.get(key);
			return ret;
		}
		
		/** Remove a type from the map */
		public ResolvedType remove(String key) {
			ResolvedType ret = (ResolvedType) tMap.remove(key);
			if (ret == null) ret = (ResolvedType) expendableMap.remove(key);
			return ret;
		}
		
		/** Reference types we don't intend to weave may be ejected from
		 * the cache if we need the space.
		 */
		private boolean isExpendable(ResolvedType type) {
			return (
					  (type != null) &&
					  (!type.isExposedToWeaver()) &&
					  (!type.isPrimitiveType())
					);
		}
		
	    public String toString() {
	    	StringBuffer sb = new StringBuffer();
	    	sb.append("types:\n");
	    	sb.append(dumpthem(tMap));
	    	sb.append("expendables:\n");
	    	sb.append(dumpthem(expendableMap));
	    	return sb.toString();
	    }
	    
	    private String dumpthem(Map m) {
	    	StringBuffer sb = new StringBuffer();
	    	Set keys = m.keySet();
	    	for (Iterator iter = keys.iterator(); iter.hasNext();) {
				String k = (String) iter.next();
				sb.append(k+"="+m.get(k)).append("\n");
			}
	    	return sb.toString();
	    }
	}	

	/**
	 * This class is used to compute and store precedence relationships between
	 * aspects.
	 */
	private static class AspectPrecedenceCalculator {
		
		private World world;
		private Map cachedResults;
		
		public AspectPrecedenceCalculator(World forSomeWorld) {
			this.world = forSomeWorld;
			this.cachedResults = new HashMap();
		}
		
		/**
		 * Ask every declare precedence in the world to order the two aspects.
		 * If more than one declare precedence gives an ordering, and the orderings
		 * conflict, then that's an error. 
		 */
		public int compareByPrecedence(ResolvedType firstAspect, ResolvedType secondAspect) {
			PrecedenceCacheKey key = new PrecedenceCacheKey(firstAspect,secondAspect);
			if (cachedResults.containsKey(key)) {
				return ((Integer) cachedResults.get(key)).intValue();
			} else {
				int order = 0;
				DeclarePrecedence orderer = null; // Records the declare precedence statement that gives the first ordering
				for (Iterator i = world.getCrosscuttingMembersSet().getDeclareDominates().iterator(); i.hasNext(); ) {
					DeclarePrecedence d = (DeclarePrecedence)i.next();
					int thisOrder = d.compare(firstAspect, secondAspect);
					if (thisOrder != 0) {
						if (orderer==null) orderer = d;
						if (order != 0 && order != thisOrder) {
							ISourceLocation[] isls = new ISourceLocation[2];
							isls[0]=orderer.getSourceLocation();
							isls[1]=d.getSourceLocation();
							Message m = 
							  new Message("conflicting declare precedence orderings for aspects: "+
							              firstAspect.getName()+" and "+secondAspect.getName(),null,true,isls);
							world.getMessageHandler().handleMessage(m);
						} else {
							order = thisOrder;
						}
					}
				}		
				cachedResults.put(key, new Integer(order));
				return order;
			}
		}
		
		public int compareByPrecedenceAndHierarchy(ResolvedType firstAspect, ResolvedType secondAspect) {
			if (firstAspect.equals(secondAspect)) return 0;
			
			int ret = compareByPrecedence(firstAspect, secondAspect);
			if (ret != 0) return ret;
			
			if (firstAspect.isAssignableFrom(secondAspect)) return -1;
			else if (secondAspect.isAssignableFrom(firstAspect)) return +1;

			return 0;
		}
		
		
		private static class PrecedenceCacheKey {
			public ResolvedType aspect1;
			public ResolvedType aspect2;
			
			public PrecedenceCacheKey(ResolvedType a1, ResolvedType a2) {
				this.aspect1 = a1;
				this.aspect2 = a2;
			}
			
			public boolean equals(Object obj) {
				if (!(obj instanceof PrecedenceCacheKey)) return false;
				PrecedenceCacheKey other = (PrecedenceCacheKey) obj;
				return (aspect1 == other.aspect1 && aspect2 == other.aspect2);
			}
			
			public int hashCode() {
				return aspect1.hashCode() + aspect2.hashCode();
			}
		}
	}
}
