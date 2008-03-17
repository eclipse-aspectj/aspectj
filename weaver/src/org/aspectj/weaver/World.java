/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 *               2005 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation
 *     Adrian Colyer, Andy Clement, overhaul for generics 
 * ******************************************************************/

package org.aspectj.weaver;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import org.aspectj.asm.IHierarchy;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.context.PinpointingMessageHandler;
import org.aspectj.weaver.UnresolvedType.TypeKind;
import org.aspectj.weaver.bcel.BcelObjectType;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.reflect.ReflectionBasedReferenceTypeDelegate;
import org.aspectj.weaver.tools.PointcutDesignatorHandler;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

/**
 * A World is a collection of known types and crosscutting members.
 */
public abstract class World implements Dump.INode {
	/** handler for any messages produced during resolution etc. */
	private IMessageHandler messageHandler = IMessageHandler.SYSTEM_ERR;
	
	/** handler for cross-reference information produced during the weaving process */
	private ICrossReferenceHandler xrefHandler = null;

	/** Currently 'active' scope in which to lookup (resolve) typevariable references */
	private TypeVariableDeclaringElement typeVariableLookupScope;
	
	/** The heart of the world, a map from type signatures to resolved types */
    protected TypeMap typeMap = new TypeMap(this); // Signature to ResolvedType

    /** New pointcut designators this world supports */
    private Set pointcutDesignators;

    // see pr145963
    /** Should we create the hierarchy for binary classes and aspects*/
    public static boolean createInjarHierarchy = true;

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
    
    /** Xpinpoint controls whether we put out developer info showing the source of messages */
    private boolean Xpinpoint = false;
    
    /** When behaving in a Java 5 way autoboxing is considered */
    private boolean behaveInJava5Way = false;
    
    /** Determines if this world could be used for multiple compiles */
    private boolean incrementalCompileCouldFollow = false;
    
    /** The level of the aspectjrt.jar the code we generate needs to run on */
    private String targetAspectjRuntimeLevel = Constants.RUNTIME_LEVEL_DEFAULT;
    
    /** Flags for the new joinpoints that are 'optional' */
    private boolean optionalJoinpoint_ArrayConstruction = false;  // Command line flag: "-Xjoinpoints:arrayconstruction"
    private boolean optionalJoinpoint_Synchronization   = false;  // Command line flag: "-Xjoinpoints:synchronization"
    
    private boolean addSerialVerUID = false;
    
    
    private Properties extraConfiguration = null;
    private boolean checkedAdvancedConfiguration=false;
    private boolean synchronizationPointcutsInUse = false;
    // Xset'table options
    private boolean fastDelegateSupportEnabled = isASMAround;
	private boolean runMinimalMemory = false;
	private boolean shouldPipelineCompilation = true;
	protected boolean bcelRepositoryCaching = xsetBCEL_REPOSITORY_CACHING_DEFAULT.equalsIgnoreCase("true");
	private boolean completeBinaryTypes = false;
	public boolean forDEBUG_structuralChangesCode = false;
	public boolean forDEBUG_bridgingCode = false;
	
	private static Trace trace = TraceFactory.getTraceFactory().getTrace(World.class);
    
    // Records whether ASM is around ... so we might use it for delegates
    protected static boolean isASMAround = false;
    
	private long errorThreshold;
	private long warningThreshold;
    
	
//    static {
//    	try {
//    		Class c = Class.forName("org.aspectj.org.objectweb.asm.ClassVisitor");
//    		isASMAround = true;
//    	} catch (ClassNotFoundException cnfe) {
//    		isASMAround = false;
//    	}
//    }
    
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
        if (trace.isTraceEnabled()) trace.enter("<init>", this);
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
        if (trace.isTraceEnabled()) trace.exit("<init>");
    }
    
    /**
     * Dump processing when a fatal error occurs
     */
    public void accept (Dump.IVisitor visitor) {
//		visitor.visitObject("Extra configuration:");
//		visitor.visitList(extraConfiguration.);
		visitor.visitObject("Shadow mungers:");
		visitor.visitList(crosscuttingMembersSet.getShadowMungers());
		visitor.visitObject("Type mungers:");
		visitor.visitList(crosscuttingMembersSet.getTypeMungers());
        visitor.visitObject("Late Type mungers:");
        visitor.visitList(crosscuttingMembersSet.getLateTypeMungers());
        if (dumpState_cantFindTypeExceptions!=null) {
          visitor.visitObject("Cant find type problems:");
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
        if (ResolvedType.isMissing(ty)) {
            //IMessage msg = null;
            getLint().cantFindType.signal(WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE,ty.getName()),isl);
            //if (isl!=null) {
              //msg = MessageUtil.error(WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE,ty.getName()),isl);
            //} else {
              //msg = MessageUtil.error(WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE,ty.getName())); 
            //}
            //messageHandler.handleMessage(msg);
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
        	ResolvedType something = new BoundedReferenceType("?","Ljava/lang/Object",this);
        	typeMap.put("?",something);
        	return something;
        }
        
        // no existing resolved type, create one
        if (ty.isArray()) {
        	ResolvedType componentType = resolve(ty.getComponentType(),allowMissing);
        	//String brackets = signature.substring(0,signature.lastIndexOf("[")+1);
            ret = new ArrayReferenceType(signature, "["+componentType.getErasureSignature(),
            		                     this, 
            		                     componentType);
        } else {
            ret = resolveToReferenceType(ty,allowMissing);
            if (!allowMissing && ret.isMissing()) {
                ret = handleRequiredMissingTypeDuringResolution(ty);
            }
            if (completeBinaryTypes) {
            	completeBinaryType(ret);
            }
        }        
  
		// Pulling in the type may have already put the right entry in the map
		if (typeMap.get(signature)==null && !ret.isMissing()) {
	        typeMap.put(signature, ret);
		}
        return ret;
    }
        
	/**
     * Called when a type is resolved - enables its type hierarchy to be finished off before we
     * proceed
     */
    protected void completeBinaryType(ResolvedType ret) {}
    
    
    /**
     * Return true if the classloader relating to this world is definetly the one that will
     * define the specified class.  Return false otherwise or we don't know for certain.
     */
    public boolean isLocallyDefined(String classname) {
    	return false;
    }
        
    /**
     * We tried to resolve a type and couldn't find it...
     */
	private ResolvedType handleRequiredMissingTypeDuringResolution(UnresolvedType ty) {
		// defer the message until someone asks a question of the type that we can't answer
		// just from the signature.
//		MessageUtil.error(messageHandler, 
//				WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE,ty.getName()));
		if (dumpState_cantFindTypeExceptions==null) {
		  dumpState_cantFindTypeExceptions = new ArrayList();   
		}
		dumpState_cantFindTypeExceptions.add(new RuntimeException("Can't find type "+ty.getName()));
		return new MissingResolvedTypeWithKnownSignature(ty.getSignature(),this);
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
//    	trace.enter("resolve", this, new Object[] {name});
    	ResolvedType ret = resolve(UnresolvedType.forName(name));
//    	trace.exit("resolve", ret);
    	return ret;
    }
    
    public ResolvedType resolve(String name,boolean allowMissing) {
    	return resolve(UnresolvedType.forName(name),allowMissing);
    }
    
	private ResolvedType currentlyResolvingBaseType;

	/**
	 * Resolve to a ReferenceType - simple, raw, parameterized, or generic.
     * Raw, parameterized, and generic versions of a type share a delegate.
     */
    private final ResolvedType resolveToReferenceType(UnresolvedType ty,boolean allowMissing) {
		if (ty.isParameterizedType()) {
			// ======= parameterized types ================
			ResolvedType rt = resolveGenericTypeFor(ty,allowMissing);
			if (rt.isMissing()) return rt;
			ReferenceType genericType = (ReferenceType)rt;
			currentlyResolvingBaseType = genericType;
						ReferenceType parameterizedType = 
				TypeFactory.createParameterizedType(genericType, ty.typeParameters, this);
			currentlyResolvingBaseType = null;
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
    		if (ty.needsModifiableDelegate()) simpleOrRawType.setNeedsModifiableDelegate(true);
	    	ReferenceTypeDelegate delegate = resolveDelegate(simpleOrRawType);
	    	// 117854
//	    	if (delegate == null) return ResolvedType.MISSING;
	    	if (delegate == null) return new MissingResolvedTypeWithKnownSignature(ty.getSignature(),erasedSignature,this);//ResolvedType.MISSING;
	    	
	    	if (delegate.isGeneric() && behaveInJava5Way) {
	    		// ======== raw type ===========
	    		simpleOrRawType.typeKind = TypeKind.RAW;
	        	ReferenceType genericType = makeGenericTypeFrom(delegate,simpleOrRawType); 
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
    		rawType = resolve(UnresolvedType.forSignature(rawSignature),allowMissing);
        	typeMap.put(rawSignature,rawType);
    	}
    	if (rawType.isMissing()) return rawType;
    	
    	// Does the raw type know its generic form? (It will if we created the
    	// raw type from a source type, it won't if its been created just through
    	// being referenced, e.g. java.util.List
    	ResolvedType genericType = rawType.getGenericType();
    	
    	// There is a special case to consider here (testGenericsBang_pr95993 highlights it)
    	// You may have an unresolvedType for a parameterized type but it
    	// is backed by a simple type rather than a generic type.  This occurs for
    	// inner types of generic types that inherit their enclosing types
    	// type variables.
    	if (rawType.isSimpleType() && (anUnresolvedType.typeParameters==null || anUnresolvedType.typeParameters.length==0)) {
    		rawType.world = this;
    		return rawType; 
    	}
    	
    	if (genericType != null) { 
    		genericType.world = this;
    		return genericType; 
    	} else {
	    	// Fault in the generic that underpins the raw type ;)
	    	ReferenceTypeDelegate delegate = resolveDelegate((ReferenceType)rawType);
	    	ReferenceType genericRefType = makeGenericTypeFrom(delegate,((ReferenceType)rawType));
	    	((ReferenceType)rawType).setGenericType(genericRefType);
	    	genericRefType.setDelegate(delegate);
	    	((ReferenceType)rawType).setDelegate(delegate);
	    	return genericRefType;
    	}
    }
    
    private ReferenceType makeGenericTypeFrom(ReferenceTypeDelegate delegate, ReferenceType rawType) {
    	String genericSig = delegate.getDeclaredGenericSignature();
    	if (genericSig != null) {
    		return new ReferenceType(
	    			UnresolvedType.forGenericTypeSignature(rawType.getSignature(),delegate.getDeclaredGenericSignature()),this);
    	} else {
    		return new ReferenceType(
    				UnresolvedType.forGenericTypeVariables(rawType.getSignature(), delegate.getTypeVariables()),this);
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
    	if (coreTy.isMissing()) {
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
	public Integer getPrecedenceIfAny(ResolvedType aspect1, ResolvedType aspect2) {
		return precedenceCalculator.getPrecedenceIfAny(aspect1, aspect2);
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
		if (this.isInPinpointMode()) {
			this.messageHandler = new PinpointingMessageHandler(messageHandler);
		} else {
			this.messageHandler = messageHandler;			
		}
	}

	/**
	 * convenenience method for creating and issuing messages via the message handler - 
	 * if you supply two locations you will get two messages.
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

	public boolean debug (String message) {
		return MessageUtil.debug(messageHandler,message);
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
    
    public void setTypeVariableLookupScope(TypeVariableDeclaringElement scope) {
    	this.typeVariableLookupScope = scope;
    }

    public TypeVariableDeclaringElement getTypeVariableLookupScope() {
    	return typeVariableLookupScope;
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

	public boolean isInPinpointMode() {
		return Xpinpoint;
	}
	
	public void setPinpointMode(boolean b) {
		this.Xpinpoint = b;
	}
	
	public void setBehaveInJava5Way(boolean b) {
    	behaveInJava5Way = b;
    }
	
	/**
	 * Set the error and warning threashold which can be taken from 
	 * CompilerOptions (see bug 129282)
	 * 
	 * @param errorThreshold
	 * @param warningThreshold
	 */
	public void setErrorAndWarningThreshold(long errorThreshold, long warningThreshold) {
		this.errorThreshold = errorThreshold;
		this.warningThreshold = warningThreshold;
	}
	
	/**
	 * @return true if ignoring the UnusedDeclaredThrownException and false if
	 *         this compiler option is set to error or warning
	 */
	public boolean isIgnoringUnusedDeclaredThrownException() {
		// the 0x800000 is CompilerOptions.UnusedDeclaredThrownException
		// which is ASTNode.bit24
		if((this.errorThreshold & 0x800000) != 0 
				|| (this.warningThreshold & 0x800000) != 0)
			return false;
		return true;
	}
	
	public void performExtraConfiguration(String config) {
		if (config==null) return;
		// Bunch of name value pairs to split
		extraConfiguration = new Properties();
		int pos =-1;
		while ((pos=config.indexOf(","))!=-1) {
			String nvpair = config.substring(0,pos);
			int pos2 = nvpair.indexOf("=");
			if (pos2!=-1) {
				String n = nvpair.substring(0,pos2);
				String v = nvpair.substring(pos2+1);
				extraConfiguration.setProperty(n,v);
			}
			config = config.substring(pos+1);
		}
		if (config.length()>0) {
			int pos2 = config.indexOf("=");
			if (pos2!=-1) {
				String n = config.substring(0,pos2);
				String v = config.substring(pos2+1);
				extraConfiguration.setProperty(n,v);
			}
		}
		ensureAdvancedConfigurationProcessed();
	}
	
	/**
	 * may return null
	 */
	public Properties getExtraConfiguration() {
		return extraConfiguration;
	}
    public final static String xsetWEAVE_JAVA_PACKAGES = "weaveJavaPackages"; // default false - controls LTW
    public final static String xsetWEAVE_JAVAX_PACKAGES = "weaveJavaxPackages"; // default false - controls LTW
	public final static String xsetCAPTURE_ALL_CONTEXT = "captureAllContext"; // default false
	public final static String xsetACTIVATE_LIGHTWEIGHT_DELEGATES = "activateLightweightDelegates"; // default true
	public final static String xsetRUN_MINIMAL_MEMORY ="runMinimalMemory"; // default true
	public final static String xsetDEBUG_STRUCTURAL_CHANGES_CODE = "debugStructuralChangesCode"; // default false
	public final static String xsetDEBUG_BRIDGING = "debugBridging"; // default false
	public final static String xsetBCEL_REPOSITORY_CACHING = "bcelRepositoryCaching";
	public final static String xsetPIPELINE_COMPILATION = "pipelineCompilation";
	public final static String xsetPIPELINE_COMPILATION_DEFAULT = "true"; 
	public final static String xsetCOMPLETE_BINARY_TYPES = "completeBinaryTypes";
	public final static String xsetCOMPLETE_BINARY_TYPES_DEFAULT = "false"; 
	public final static String xsetBCEL_REPOSITORY_CACHING_DEFAULT = "true"; 
	
	public boolean isInJava5Mode() {
		return behaveInJava5Way;
	}
	
	public void setTargetAspectjRuntimeLevel(String s) {
		targetAspectjRuntimeLevel = s;
	}
	
	public void setOptionalJoinpoints(String jps) {
		if (jps==null) return;
		if (jps.indexOf("arrayconstruction")!=-1) optionalJoinpoint_ArrayConstruction = true;
		if (jps.indexOf("synchronization")!=-1)   optionalJoinpoint_Synchronization = true;
	}
	
	public boolean isJoinpointArrayConstructionEnabled() {
		return optionalJoinpoint_ArrayConstruction;
	}
	public boolean isJoinpointSynchronizationEnabled() {
		return optionalJoinpoint_Synchronization;
	}
	
	public String getTargetAspectjRuntimeLevel() {
		return targetAspectjRuntimeLevel;
	}
	
	public boolean isTargettingAspectJRuntime12() {
		boolean b = false; // pr116679
		if (!isInJava5Mode()) b=true;
		else b = getTargetAspectjRuntimeLevel().equals(org.aspectj.weaver.Constants.RUNTIME_LEVEL_12);
		//System.err.println("Asked if targetting runtime 1.2 , returning: "+b);
		return b;
	}
	
	/*
	 *  Map of types in the world, can have 'references' to expendable ones which 
	 *  can be garbage collected to recover memory.
	 *  An expendable type is a reference type that is not exposed to the weaver (ie
	 *  just pulled in for type resolution purposes).
	 */
	protected static class TypeMap {
		
		private static boolean debug = false;

		// Strategy for entries in the expendable map
		public static int DONT_USE_REFS = 0; // Hang around forever
		public static int USE_WEAK_REFS = 1; // Collected asap
		public static int USE_SOFT_REFS = 2; // Collected when short on memory
		
		// SECRETAPI - Can switch to a policy of choice ;)
		public static int policy  = USE_SOFT_REFS; 

		// Map of types that never get thrown away
		private Map /* String -> ResolvedType */ tMap = new HashMap();
		
		// Map of types that may be ejected from the cache if we need space
		private Map expendableMap = Collections.synchronizedMap(new WeakHashMap());
		
		private World w;

		// profiling tools...
		private boolean memoryProfiling = false;
		private int maxExpendableMapSize = -1;
		private int collectedTypes = 0;
		private ReferenceQueue rq = new ReferenceQueue();
		
		private static Trace trace = TraceFactory.getTraceFactory().getTrace(World.TypeMap.class);
		
		TypeMap(World w) {
			if (trace.isTraceEnabled()) trace.enter("<init>",this,w);
			this.w = w;
			memoryProfiling = false;// !w.getMessageHandler().isIgnoring(Message.INFO);
			if (trace.isTraceEnabled()) trace.exit("<init>");
		}
		
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
			if (type instanceof MissingResolvedTypeWithKnownSignature) {
				if (debug) 
					System.err.println("Not putting a missing type into the typemap: key="+key+" type="+type);
				return type;
			}
			
			if ((type instanceof ReferenceType) && (((ReferenceType)type).getDelegate()==null) && w.isExpendable(type)) {
				if (debug) 
				    System.err.println("Not putting expendable ref type with null delegate into typemap: key="+key+" type="+type);
				return type;
			}
						
			if (w.isExpendable(type))  {
				// Dont use reference queue for tracking if not profiling...
				if (policy==USE_WEAK_REFS) {
				    if (memoryProfiling) expendableMap.put(key,new WeakReference(type,rq));
				    else                 expendableMap.put(key,new WeakReference(type));
				} else if (policy==USE_SOFT_REFS) {
					if (memoryProfiling) expendableMap.put(key,new SoftReference(type,rq));
					else                 expendableMap.put(key,new SoftReference(type));
				} else {
				  expendableMap.put(key,type);
				}
				if (memoryProfiling && expendableMap.size()>maxExpendableMapSize) {
					maxExpendableMapSize = expendableMap.size();
				}
			    return type;
			} else {
				return (ResolvedType) tMap.put(key,type);
			}
		}
		
		public void report() {
			if (!memoryProfiling) return;
			checkq();
			w.getMessageHandler().handleMessage(MessageUtil.info("MEMORY: world expendable type map reached maximum size of #"+maxExpendableMapSize+" entries"));
			w.getMessageHandler().handleMessage(MessageUtil.info("MEMORY: types collected through garbage collection #"+collectedTypes+" entries"));
		}
		
		public void checkq() {
			if (!memoryProfiling) return;
			while (rq.poll()!=null) collectedTypes++;
		}
		
		/** 
		 * Lookup a type by its signature, always look 
		 * in the real map before the expendable map 
		 */
		public ResolvedType get(String key) {
			checkq();
			ResolvedType ret = (ResolvedType) tMap.get(key);
			if (ret == null) {
				if (policy==USE_WEAK_REFS) {
					WeakReference ref = (WeakReference)expendableMap.get(key);
					if (ref != null) {
						ret = (ResolvedType) ref.get();
					}
				} else if (policy==USE_SOFT_REFS) {
					SoftReference ref = (SoftReference)expendableMap.get(key);
					if (ref != null) {
						ret = (ResolvedType) ref.get();
					}
				} else {
				    return (ResolvedType)expendableMap.get(key);
				}
			}
			return ret;
		}
		
		/** Remove a type from the map */
		public ResolvedType remove(String key) {
			ResolvedType ret = (ResolvedType) tMap.remove(key);
			if (ret == null) {
				if (policy==USE_WEAK_REFS) { 
					WeakReference wref = (WeakReference)expendableMap.remove(key);
					if (wref!=null) ret = (ResolvedType)wref.get();
				} else if (policy==USE_SOFT_REFS) {
					SoftReference wref = (SoftReference)expendableMap.remove(key);
					if (wref!=null) ret = (ResolvedType)wref.get();
				} else {
					ret = (ResolvedType)expendableMap.remove(key);
				}
			}
			return ret;
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
	    	
	    	int otherTypes = 0;
	    	int bcelDel = 0;
	    	int refDel = 0;
	    	
	    	for (Iterator iter = m.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object val = entry.getValue();
				if (val instanceof WeakReference) {
					val = ((WeakReference)val).get();
				} else 
				if (val instanceof SoftReference) {
					val = ((SoftReference)val).get();
				} 
				sb.append(entry.getKey()+"="+val).append("\n");
				if (val instanceof ReferenceType) {
					ReferenceType refType = (ReferenceType)val;
					if (refType.getDelegate() instanceof BcelObjectType) {
						bcelDel++;
					} else if (refType.getDelegate() instanceof ReflectionBasedReferenceTypeDelegate) {
						refDel++;
					} else {
						otherTypes++;
					}
				} else {
					otherTypes++;
				}
			}
	    	sb.append("# BCEL = "+bcelDel+", # REF = "+refDel+", # Other = "+otherTypes);
	    	
	    	return sb.toString();
	    }
	    
	    public int totalSize() {
	    	return tMap.size()+expendableMap.size();
	    }
	    public int hardSize() {
	    	return tMap.size();
	    }
	    
		public ResolvedType[] getAllTypes() {
			List/*ResolvedType*/ results = new ArrayList();

			collectTypes(expendableMap, results);
			collectTypes(tMap, results);
			return (ResolvedType[]) results.toArray(new ResolvedType[results.size()]);
		}

		private void collectTypes(Map map, List/*ResolvedType*/ results) {
			for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				ResolvedType type = get((String)key);
				if (type!=null) results.add(type);
				else System.err.println("null!:"+key);
			}
		}


	}	
	
	/** Reference types we don't intend to weave may be ejected from
	 * the cache if we need the space.
	 */
	protected boolean isExpendable(ResolvedType type) {
		return (
				!type.equals(UnresolvedType.OBJECT) && 
				  (type != null) &&
				  (!type.isExposedToWeaver()) &&
				  (!type.isPrimitiveType())
				);
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
		
		public Integer getPrecedenceIfAny(ResolvedType aspect1,ResolvedType aspect2) {
			return (Integer)cachedResults.get(new PrecedenceCacheKey(aspect1,aspect2));
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

	public void validateType(UnresolvedType type) { }

    // --- with java5 we can get into a recursive mess if we aren't careful when resolving types (*cough* java.lang.Enum) ---
	
    // --- this first map is for java15 delegates which may try and recursively access the same type variables.
	// --- I would rather stash this against a reference type - but we don't guarantee referencetypes are unique for
	//     so we can't :(
	private Map workInProgress1 = new HashMap();
	public TypeVariable[] getTypeVariablesCurrentlyBeingProcessed(Class baseClass) {
		return (TypeVariable[])workInProgress1.get(baseClass);
	}
	public void recordTypeVariablesCurrentlyBeingProcessed(Class baseClass, TypeVariable[] typeVariables) {
		workInProgress1.put(baseClass,typeVariables);
	}
	public void forgetTypeVariablesCurrentlyBeingProcessed(Class baseClass) {
		workInProgress1.remove(baseClass);
	}

    public void setAddSerialVerUID(boolean b) { addSerialVerUID=b;}
    public boolean isAddSerialVerUID() { return addSerialVerUID;}
    
    /** be careful calling this - pr152257 */
	public void flush() {
		typeMap.expendableMap.clear();
	}
	
	 public void ensureAdvancedConfigurationProcessed() {
	    	// Check *once* whether the user has switched asm support off
	    	if (!checkedAdvancedConfiguration) {
	        	Properties p = getExtraConfiguration();
	        	if (p!=null) {
	        		
				if (isASMAround) { // dont bother if its not...
	        		String s = p.getProperty(xsetACTIVATE_LIGHTWEIGHT_DELEGATES,"true");
	        		fastDelegateSupportEnabled = s.equalsIgnoreCase("true");
	        		if (!fastDelegateSupportEnabled) 
	        			getMessageHandler().handleMessage(MessageUtil.info("[activateLightweightDelegates=false] Disabling optimization to use lightweight delegates for non-woven types"));
				}
				
				String s = p.getProperty(xsetBCEL_REPOSITORY_CACHING,xsetBCEL_REPOSITORY_CACHING_DEFAULT);
				bcelRepositoryCaching = s.equalsIgnoreCase("true");
				if (!bcelRepositoryCaching) {
					getMessageHandler().handleMessage(MessageUtil.info("[bcelRepositoryCaching=false] AspectJ will not use a bcel cache for class information"));
				}
				
				s = p.getProperty(xsetPIPELINE_COMPILATION,xsetPIPELINE_COMPILATION_DEFAULT);
				shouldPipelineCompilation = s.equalsIgnoreCase("true");

				s = p.getProperty(xsetCOMPLETE_BINARY_TYPES,xsetCOMPLETE_BINARY_TYPES_DEFAULT);
				completeBinaryTypes = s.equalsIgnoreCase("true");
				if (completeBinaryTypes) {
					getMessageHandler().handleMessage(MessageUtil.info("[completeBinaryTypes=true] Completion of binary types activated"));
				}
				
				s = p.getProperty(xsetRUN_MINIMAL_MEMORY,"false");
        		runMinimalMemory = s.equalsIgnoreCase("true");
//	        	if (runMinimalMemory) 
//	        		getMessageHandler().handleMessage(MessageUtil.info("[runMinimalMemory=true] Optimizing bcel processing (and cost of performance) to use less memory"));
        		
        		
        		s = p.getProperty(xsetDEBUG_STRUCTURAL_CHANGES_CODE,"false");
        		forDEBUG_structuralChangesCode = s.equalsIgnoreCase("true");
        		
        		s = p.getProperty(xsetDEBUG_BRIDGING,"false");
        		forDEBUG_bridgingCode = s.equalsIgnoreCase("true");
	        		
    		}
        	checkedAdvancedConfiguration=true;
        }
     }
	    
	    public boolean isRunMinimalMemory() {
	      ensureAdvancedConfigurationProcessed();
	    	  return runMinimalMemory;
	    }
	    
	    public boolean shouldPipelineCompilation() {
	    	ensureAdvancedConfigurationProcessed();
	    	return shouldPipelineCompilation;
	    }
	    
	    public void setFastDelegateSupport(boolean b) { 
	    	  if (b && !isASMAround) {
	    		throw new BCException("Unable to activate fast delegate support, ASM classes cannot be found");
	    	  }
	    	  fastDelegateSupportEnabled = b; 
	    }
	    
	    public boolean isFastDelegateSupportEnabled() {
	    	  return false; // ASM not currently being used
//	    	  ensureAdvancedConfigurationProcessed();
//	    	  return fastDelegateSupportEnabled;
	    }
	        
	    public void setIncrementalCompileCouldFollow(boolean b) {incrementalCompileCouldFollow = b;}
	    public boolean couldIncrementalCompileFollow()           {return incrementalCompileCouldFollow;}
	
	    public void setSynchronizationPointcutsInUse() {
	    	if (trace.isTraceEnabled()) trace.enter("setSynchronizationPointcutsInUse", this);
	    	synchronizationPointcutsInUse =true;
	    	if (trace.isTraceEnabled()) trace.exit("setSynchronizationPointcutsInUse");
	    }
	    public boolean areSynchronizationPointcutsInUse() {return synchronizationPointcutsInUse;}
	    
	    public boolean isASMAround() { 
	    	return isASMAround;
	    }
	    
		public ResolvedType[] getAllTypes() {
			return typeMap.getAllTypes();
		}

        /**
         * Register a new pointcut designator handler with the world - this can be used by any pointcut parsers attached
         * to the world.
         * 
         * @param designatorHandler handler for the new pointcut
         */
        public void registerPointcutHandler(PointcutDesignatorHandler designatorHandler) {
            if (pointcutDesignators == null) pointcutDesignators = new HashSet();
            pointcutDesignators.add(designatorHandler);
        }
        
        public Set getRegisteredPointcutHandlers() {
            if (pointcutDesignators == null) return Collections.EMPTY_SET;
            return pointcutDesignators;
        }
        
}