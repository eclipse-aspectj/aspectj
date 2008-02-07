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


package org.aspectj.weaver.bcel;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.apache.bcel.generic.ANEWARRAY;
import org.aspectj.apache.bcel.generic.BranchInstruction;
import org.aspectj.apache.bcel.generic.CPInstruction;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.generic.FieldGen;
import org.aspectj.apache.bcel.generic.FieldInstruction;
import org.aspectj.apache.bcel.generic.GOTO;
import org.aspectj.apache.bcel.generic.GOTO_W;
import org.aspectj.apache.bcel.generic.INVOKESPECIAL;
import org.aspectj.apache.bcel.generic.IndexedInstruction;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.InstructionTargeter;
import org.aspectj.apache.bcel.generic.InvokeInstruction;
import org.aspectj.apache.bcel.generic.LineNumberTag;
import org.aspectj.apache.bcel.generic.LocalVariableInstruction;
import org.aspectj.apache.bcel.generic.LocalVariableTag;
import org.aspectj.apache.bcel.generic.MONITORENTER;
import org.aspectj.apache.bcel.generic.MONITOREXIT;
import org.aspectj.apache.bcel.generic.MULTIANEWARRAY;
import org.aspectj.apache.bcel.generic.MethodGen;
import org.aspectj.apache.bcel.generic.NEW;
import org.aspectj.apache.bcel.generic.NEWARRAY;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.PUTFIELD;
import org.aspectj.apache.bcel.generic.PUTSTATIC;
import org.aspectj.apache.bcel.generic.RET;
import org.aspectj.apache.bcel.generic.ReturnInstruction;
import org.aspectj.apache.bcel.generic.Select;
import org.aspectj.apache.bcel.generic.Tag;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.generic.annotation.AnnotationGen;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.WeaveMessage;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.bridge.context.ContextToken;
import org.aspectj.util.PartialOrder;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.AsmRelationshipProvider;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.IClassWeaver;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.MissingResolvedTypeWithKnownSignature;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.NewConstructorTypeMunger;
import org.aspectj.weaver.NewFieldTypeMunger;
import org.aspectj.weaver.NewMethodTypeMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.WeaverMetrics;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.ExactTypePattern;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

class BcelClassWeaver implements IClassWeaver {

    private static Trace trace = TraceFactory.getTraceFactory().getTrace(BcelClassWeaver.class);
    
    /**
     * This is called from {@link BcelWeaver} to perform the per-class weaving process.
     */
	public static boolean weave(
		BcelWorld world,
		LazyClassGen clazz,
		List shadowMungers,
		List typeMungers,
        List lateTypeMungers)
	{
		boolean b =  new BcelClassWeaver(world, clazz, shadowMungers, typeMungers, lateTypeMungers).weave();
		//System.out.println(clazz.getClassName() + ", " + clazz.getType().getWeaverState());
		//clazz.print();
		return b;
	}
	
	// --------------------------------------------
    
    private final LazyClassGen clazz;
    private final List         shadowMungers;
    private final List         typeMungers;
    private final List         lateTypeMungers;

    private final BcelObjectType  ty;    // alias of clazz.getType()
    private final BcelWorld       world; // alias of ty.getWorld()
    private final ConstantPoolGen cpg;   // alias of clazz.getConstantPoolGen()
    private final InstructionFactory fact; // alias of clazz.getFactory();

    
    private final List        addedLazyMethodGens           = new ArrayList();
    private final Set         addedDispatchTargets          = new HashSet();
    

	// Static setting across BcelClassWeavers
	private static boolean inReweavableMode = false;
    
    
    private        List addedSuperInitializersAsList = null; // List<IfaceInitList>
    private final Map  addedSuperInitializers = new HashMap(); // Interface -> IfaceInitList
    private        List addedThisInitializers  = new ArrayList(); // List<NewFieldMunger>
    private        List addedClassInitializers  = new ArrayList(); // List<NewFieldMunger>
	
	private Map mapToAnnotations = new HashMap();
    
    private BcelShadow clinitShadow = null;
    
    /**
     * This holds the initialization and pre-initialization shadows for this class
     * that were actually matched by mungers (if no match, then we don't even create the
     * shadows really).
     */
    private final List        initializationShadows         = new ArrayList(1);
    
	private BcelClassWeaver(
		BcelWorld world,
		LazyClassGen clazz,
		List shadowMungers,
		List typeMungers,
        List lateTypeMungers)
	{
		super();
		// assert world == clazz.getType().getWorld()
		this.world = world;
		this.clazz = clazz;
		this.shadowMungers = shadowMungers;
		this.typeMungers = typeMungers;
        this.lateTypeMungers = lateTypeMungers;
		this.ty = clazz.getBcelObjectType();
		this.cpg = clazz.getConstantPoolGen();
		this.fact = clazz.getFactory();
		
		fastMatchShadowMungers(shadowMungers);
		
		initializeSuperInitializerMap(ty.getResolvedTypeX());
		if (!checkedXsetForLowLevelContextCapturing) {
			Properties p = world.getExtraConfiguration();
        	if (p!=null) {
        		String s = p.getProperty(World.xsetCAPTURE_ALL_CONTEXT,"false");
        		captureLowLevelContext = s.equalsIgnoreCase("true");
        		if (captureLowLevelContext) 
        			world.getMessageHandler().handleMessage(MessageUtil.info("["+World.xsetCAPTURE_ALL_CONTEXT+"=true] Enabling collection of low level context for debug/crash messages"));
        	}
			checkedXsetForLowLevelContextCapturing=true;
		}
	} 
	

    private List[] perKindShadowMungers;
    private boolean canMatchBodyShadows = false;
    private boolean canMatchInitialization = false;
    private void fastMatchShadowMungers(List shadowMungers) {
    	// beware the annoying property that SHADOW_KINDS[i].getKey == (i+1) !
    	
    	perKindShadowMungers = new List[Shadow.MAX_SHADOW_KIND + 1];
    	for (int i = 0; i < perKindShadowMungers.length; i++) {
			perKindShadowMungers[i] = new ArrayList(0);
    	}
    	for (Iterator iter = shadowMungers.iterator(); iter.hasNext();) {
			ShadowMunger munger = (ShadowMunger) iter.next();
			
			int couldMatchKinds = munger.getPointcut().couldMatchKinds();
			for (int i = 0; i < Shadow.SHADOW_KINDS.length; i++) {
				Shadow.Kind kind = Shadow.SHADOW_KINDS[i];
				if (kind.isSet(couldMatchKinds)) perKindShadowMungers[kind.getKey()].add(munger);
			}
			
//			Set couldMatchKinds = munger.getPointcut().couldMatchKinds();
//			for (Iterator kindIterator = couldMatchKinds.iterator(); 
//			     kindIterator.hasNext();) {
//				Shadow.Kind aKind = (Shadow.Kind) kindIterator.next();
//				perKindShadowMungers[aKind.getKey()].add(munger);
//			}
		}
    	
    	if (!perKindShadowMungers[Shadow.Initialization.getKey()].isEmpty())
    		canMatchInitialization = true;
    	
    	for (int i = 0; i < Shadow.SHADOW_KINDS.length; i++) {
			Shadow.Kind kind = Shadow.SHADOW_KINDS[i];
			if (!kind.isEnclosingKind() && !perKindShadowMungers[i+1].isEmpty()) {
				canMatchBodyShadows = true;
			}
			if (perKindShadowMungers[i+1].isEmpty()) {
				perKindShadowMungers[i+1] = null;
			}
    	}
    }
    
    private boolean canMatch(Shadow.Kind kind) {
    	return perKindShadowMungers[kind.getKey()] != null;
    }
    
//   	private void fastMatchShadowMungers(List shadowMungers, ArrayList mungers, Kind kind) {
//		FastMatchInfo info = new FastMatchInfo(clazz.getType(), kind);
//		for (Iterator i = shadowMungers.iterator(); i.hasNext();) {
//			ShadowMunger munger = (ShadowMunger) i.next();
//			FuzzyBoolean fb = munger.getPointcut().fastMatch(info);
//			WeaverMetrics.recordFastMatchResult(fb);// Could pass: munger.getPointcut().toString()
//			if (fb.maybeTrue()) mungers.add(munger);
//		}
//	}


	private void initializeSuperInitializerMap(ResolvedType child) {
		ResolvedType[] superInterfaces = child.getDeclaredInterfaces();
		for (int i=0, len=superInterfaces.length; i < len; i++) {
			if (ty.getResolvedTypeX().isTopmostImplementor(superInterfaces[i])) {
				if (addSuperInitializer(superInterfaces[i])) {
					initializeSuperInitializerMap(superInterfaces[i]);
				}
			}
		}
	}

	private boolean addSuperInitializer(ResolvedType onType) {
		if (onType.isRawType() || onType.isParameterizedType()) onType = onType.getGenericType();
		IfaceInitList l = (IfaceInitList) addedSuperInitializers.get(onType);
		if (l != null) return false;
		l = new IfaceInitList(onType);
		addedSuperInitializers.put(onType, l);
		return true;
	}
   
	public void addInitializer(ConcreteTypeMunger cm) {
		NewFieldTypeMunger m = (NewFieldTypeMunger) cm.getMunger();
		ResolvedType onType = m.getSignature().getDeclaringType().resolve(world);
		if (onType.isRawType()) onType = onType.getGenericType();

		if (m.getSignature().isStatic()) {
			addedClassInitializers.add(cm);
		} else {
			if (onType == ty.getResolvedTypeX()) {
				addedThisInitializers.add(cm);
			} else {
				IfaceInitList l = (IfaceInitList) addedSuperInitializers.get(onType);
				l.list.add(cm);
			}
		}
	}
    
    private static class IfaceInitList implements PartialOrder.PartialComparable {
    	final ResolvedType onType;
    	List list = new ArrayList();
    	IfaceInitList(ResolvedType onType) {
    		this.onType = onType;
    	}
    	
		public int compareTo(Object other) {
			IfaceInitList o = (IfaceInitList)other;
			if (onType.isAssignableFrom(o.onType)) return +1;
			else if (o.onType.isAssignableFrom(onType)) return -1;
			else return 0;
		}

		public int fallbackCompareTo(Object other) {
			return 0;
		}
    }
    
    // XXX this is being called, but the result doesn't seem to be being used
    public boolean addDispatchTarget(ResolvedMember m) {
    	return addedDispatchTargets.add(m);
    }
    
    public void addLazyMethodGen(LazyMethodGen gen) {
    	addedLazyMethodGens.add(gen);
    }
    	
	public void addOrReplaceLazyMethodGen(LazyMethodGen mg) {
		if (alreadyDefined(clazz, mg)) return;
		
		for (Iterator i = addedLazyMethodGens.iterator(); i.hasNext(); ) {
			LazyMethodGen existing = (LazyMethodGen)i.next();
			if (signaturesMatch(mg, existing)) {
				if (existing.definingType == null) {
					// this means existing was introduced on the class itself
					return;
				} else if (mg.definingType.isAssignableFrom(existing.definingType)) {
					// existing is mg's subtype and dominates mg
					return;
				} else if (existing.definingType.isAssignableFrom(mg.definingType)) {
					// mg is existing's subtype and dominates existing
					i.remove();
					addedLazyMethodGens.add(mg);
					return;
				} else {
					throw new BCException("conflict between: " + mg + " and " + existing);
				}
			} 
		}
		addedLazyMethodGens.add(mg);
	}

	private boolean alreadyDefined(LazyClassGen clazz, LazyMethodGen mg) {
		for (Iterator i = clazz.getMethodGens().iterator(); i.hasNext(); ) {
			LazyMethodGen existing = (LazyMethodGen)i.next();
			if (signaturesMatch(mg, existing)) {
				if (!mg.isAbstract() && existing.isAbstract()) {
					i.remove();
					return false;
				}
				return true;
			}
		}
		return false;
	}

	private boolean signaturesMatch(LazyMethodGen mg, LazyMethodGen existing) {
		return mg.getName().equals(existing.getName()) &&
			mg.getSignature().equals(existing.getSignature());
	}    	
    
	
	protected static LazyMethodGen makeBridgeMethod(LazyClassGen gen, ResolvedMember member) {

		// remove abstract modifier
		int mods = member.getModifiers();
		if (Modifier.isAbstract(mods)) mods = mods - Modifier.ABSTRACT;
		
		LazyMethodGen ret = new LazyMethodGen(
			mods,
			BcelWorld.makeBcelType(member.getReturnType()),
			member.getName(),
			BcelWorld.makeBcelTypes(member.getParameterTypes()),
			UnresolvedType.getNames(member.getExceptions()),
			gen);
        
        // 43972 : Static crosscutting makes interfaces unusable for javac
        // ret.makeSynthetic();    
		return ret;
	}
	
	
	/**
	 * Create a single bridge method called 'theBridgeMethod' that bridges to 'whatToBridgeTo'
	 */
	private static void createBridgeMethod(BcelWorld world, LazyMethodGen whatToBridgeToMethodGen, LazyClassGen clazz,ResolvedMember theBridgeMethod) {
		InstructionList body;
		InstructionFactory fact;
		int pos = 0;

		ResolvedMember whatToBridgeTo = whatToBridgeToMethodGen.getMemberView();
		
		if (whatToBridgeTo==null) {
			whatToBridgeTo = 
			  new ResolvedMemberImpl(Member.METHOD,
				whatToBridgeToMethodGen.getEnclosingClass().getType(),
				whatToBridgeToMethodGen.getAccessFlags(),
				whatToBridgeToMethodGen.getName(),
				whatToBridgeToMethodGen.getSignature());
		}
		LazyMethodGen bridgeMethod = makeBridgeMethod(clazz,theBridgeMethod); // The bridge method in this type will have the same signature as the one in the supertype
		bridgeMethod.setAccessFlags(bridgeMethod.getAccessFlags() | 0x00000040 /*BRIDGE    = 0x00000040*/ );
		Type returnType   = BcelWorld.makeBcelType(theBridgeMethod.getReturnType());
		Type[] paramTypes = BcelWorld.makeBcelTypes(theBridgeMethod.getParameterTypes());
		Type[] newParamTypes=whatToBridgeToMethodGen.getArgumentTypes();
		body = bridgeMethod.getBody();
		fact = clazz.getFactory();

		if (!whatToBridgeToMethodGen.isStatic()) {
		   body.append(InstructionFactory.createThis());
		   pos++;
		}
		for (int i = 0, len = paramTypes.length; i < len; i++) {
		  Type paramType = paramTypes[i];
		  body.append(InstructionFactory.createLoad(paramType, pos));
		  if (!newParamTypes[i].equals(paramTypes[i])) {
			  if (world.forDEBUG_bridgingCode) System.err.println("Bridging: Cast "+newParamTypes[i]+" from "+paramTypes[i]);
			  body.append(fact.createCast(paramTypes[i],newParamTypes[i]));
		  }
		  pos+=paramType.getSize();
		}

		body.append(Utility.createInvoke(fact, world,whatToBridgeTo));
		body.append(InstructionFactory.createReturn(returnType));
		clazz.addMethodGen(bridgeMethod);
	}
	
    // ----
    
    public boolean weave() {
        if (clazz.isWoven() && !clazz.isReweavable()) {
        	world.showMessage(IMessage.ERROR, 
        		  WeaverMessages.format(WeaverMessages.ALREADY_WOVEN,clazz.getType().getName()),
				ty.getSourceLocation(), null);
        	return false;
        }
       

        Set aspectsAffectingType = null;
        if (inReweavableMode || clazz.getType().isAspect()) aspectsAffectingType = new HashSet();
        
        boolean isChanged = false;
        
        // we want to "touch" all aspects
        if (clazz.getType().isAspect()) isChanged = true;

        // start by munging all typeMungers
        for (Iterator i = typeMungers.iterator(); i.hasNext(); ) {
        	Object o = i.next();
        	if ( !(o instanceof BcelTypeMunger) ) {
        		//???System.err.println("surprising: " + o);
        		continue;
        	}
        	BcelTypeMunger munger = (BcelTypeMunger)o;
        	boolean typeMungerAffectedType = munger.munge(this);
        	if (typeMungerAffectedType) {
        		isChanged = true;
        		if (inReweavableMode || clazz.getType().isAspect()) aspectsAffectingType.add(munger.getAspectType().getName());
        	}
        }



        // Weave special half type/half shadow mungers... 
        isChanged = weaveDeclareAtMethodCtor(clazz) || isChanged;
        isChanged = weaveDeclareAtField(clazz)      || isChanged;
        
        // XXX do major sort of stuff
        // sort according to:  Major:  type hierarchy
        //                     within each list:  dominates
        // don't forget to sort addedThisInitialiers according to dominates
        addedSuperInitializersAsList = new ArrayList(addedSuperInitializers.values());
        addedSuperInitializersAsList = PartialOrder.sort(addedSuperInitializersAsList);        
        if (addedSuperInitializersAsList == null) {
        	throw new BCException("circularity in inter-types");
        }
      
        // this will create a static initializer if there isn't one
        // this is in just as bad taste as NOPs
        LazyMethodGen staticInit = clazz.getStaticInitializer();
        staticInit.getBody().insert(genInitInstructions(addedClassInitializers, true));
        
        // now go through each method, and match against each method.  This
        // sets up each method's {@link LazyMethodGen#matchedShadows} field, 
        // and it also possibly adds to {@link #initializationShadows}.
        List methodGens = new ArrayList(clazz.getMethodGens());
        for (Iterator i = methodGens.iterator(); i.hasNext();) {
            LazyMethodGen mg = (LazyMethodGen)i.next();
			if (! mg.hasBody()) continue;
			if (world.isJoinpointSynchronizationEnabled() && 
				world.areSynchronizationPointcutsInUse() && 
				mg.getMethod().isSynchronized()) {
				transformSynchronizedMethod(mg);
			}
			boolean shadowMungerMatched = match(mg);
			if (shadowMungerMatched) {
				// For matching mungers, add their declaring aspects to the list that affected this type
				if (inReweavableMode || clazz.getType().isAspect()) aspectsAffectingType.addAll(findAspectsForMungers(mg));
              isChanged = true;
			}
        }

        // now we weave all but the initialization shadows
		for (Iterator i = methodGens.iterator(); i.hasNext();) {
			LazyMethodGen mg = (LazyMethodGen)i.next();
			if (! mg.hasBody()) continue;
			implement(mg);
		}
			
        // if we matched any initialization shadows, we inline and weave
		if (!initializationShadows.isEmpty()) {
			// Repeat next step until nothing left to inline...cant go on 
			// infinetly as compiler will have detected and reported 
			// "Recursive constructor invocation"
			while (inlineSelfConstructors(methodGens));
			positionAndImplement(initializationShadows);
		}
		
        // now proceed with late type mungers
        if (lateTypeMungers != null) {
            for (Iterator i = lateTypeMungers.iterator(); i.hasNext(); ) {
                BcelTypeMunger munger = (BcelTypeMunger)i.next();
                if (munger.matches(clazz.getType())) {
                    boolean typeMungerAffectedType = munger.munge(this);
                    if (typeMungerAffectedType) {
                        isChanged = true;
                        if (inReweavableMode || clazz.getType().isAspect()) aspectsAffectingType.add(munger.getAspectType().getName());
                    }
                }
            }
        }

        //FIXME AV - see #75442, for now this is not enough to fix the bug, comment that out until we really fix it
//        // flush to save some memory
//        PerObjectInterfaceTypeMunger.unregisterFromAsAdvisedBy(clazz.getType());

		// finally, if we changed, we add in the introduced methods.
        if (isChanged) {
        	clazz.getOrCreateWeaverStateInfo(inReweavableMode);
			weaveInAddedMethods(); // FIXME asc are these potentially affected by declare annotation?
        }
        
        if (inReweavableMode) {
        	WeaverStateInfo wsi = clazz.getOrCreateWeaverStateInfo(true);
        	wsi.addAspectsAffectingType(aspectsAffectingType);
        	wsi.setUnwovenClassFileData(ty.getJavaClass().getBytes());
        	wsi.setReweavable(true);
        } else {
        	clazz.getOrCreateWeaverStateInfo(false).setReweavable(false);
        }
        
        return isChanged;
    }
    
    
    
    
    // **************************** start of bridge method creation code *****************
    
	// FIXME asc tidy this lot up !!
	
    // FIXME asc refactor into ResolvedType or even ResolvedMember?
    /**
     * Check if a particular method is overriding another - refactored into this helper so it
     * can be used from multiple places.
     */
    private static ResolvedMember isOverriding(ResolvedType typeToCheck,ResolvedMember methodThatMightBeGettingOverridden,String mname,String mrettype,int mmods,boolean inSamePackage,UnresolvedType[] methodParamsArray) {
		// Check if we can be an override...
		if (methodThatMightBeGettingOverridden.isStatic()) return null; // we can't be overriding a static method
		if (methodThatMightBeGettingOverridden.isPrivate())  return null; // we can't be overriding a private method
		if (!methodThatMightBeGettingOverridden.getName().equals(mname))  return null; // names dont match (this will also skip <init> and <clinit> too)
		if (methodThatMightBeGettingOverridden.getParameterTypes().length!=methodParamsArray.length)  return null; // check same number of parameters
		if (!isVisibilityOverride(mmods,methodThatMightBeGettingOverridden,inSamePackage))  return null;
	
		if (typeToCheck.getWorld().forDEBUG_bridgingCode) System.err.println("  Bridging:seriously considering this might be getting overridden '"+methodThatMightBeGettingOverridden+"'");
		
		// Look at erasures of parameters (List<String> erased is List)
		boolean sameParams = true;
		for (int p = 0;p<methodThatMightBeGettingOverridden.getParameterTypes().length;p++) {
		  if (!methodThatMightBeGettingOverridden.getParameterTypes()[p].getErasureSignature().equals(methodParamsArray[p].getErasureSignature())) sameParams = false;
		}
		
		// If the 'typeToCheck' represents a parameterized type then the method will be the parameterized form of the
		// generic method in the generic type.  So if the method was 'void m(List<T> lt, T t)' and the parameterized type here
		// is I<String> then the method we are looking at will be 'void m(List<String> lt, String t)' which when erased
		// is 'void m(List lt,String t)' - so if the parameters *do* match then there is a generic method we are
		// overriding
		
	    if (sameParams) {		    	
	    	// check for covariance
	    	if (typeToCheck.isParameterizedType()) {
				return methodThatMightBeGettingOverridden.getBackingGenericMember();
	    	} else if (!methodThatMightBeGettingOverridden.getReturnType().getErasureSignature().equals(mrettype)) {
	    	    // addressing the wierd situation from bug 147801
	    		// just check whether these things are in the right relationship for covariance...
	    		ResolvedType superReturn = typeToCheck.getWorld().resolve(UnresolvedType.forSignature(methodThatMightBeGettingOverridden.getReturnType().getErasureSignature()));
	    		ResolvedType subReturn   = typeToCheck.getWorld().resolve(UnresolvedType.forSignature(mrettype));
	    		if (superReturn.isAssignableFrom(subReturn)) 
	    			return methodThatMightBeGettingOverridden;
	    	}
	    } 
	    return null;
    }
    
    /**
     * Looks at the visibility modifiers between two methods, and knows whether they are from classes in
     * the same package, and decides whether one overrides the other.
     * @return true if there is an overrides rather than a 'hides' relationship
     */
    static boolean isVisibilityOverride(int methodMods, ResolvedMember inheritedMethod,boolean inSamePackage) {
    	if (inheritedMethod.isStatic()) return false;
    	if (methodMods == inheritedMethod.getModifiers()) return true;

    	if (inheritedMethod.isPrivate()) return false;
    	
    	boolean isPackageVisible = !inheritedMethod.isPrivate() && !inheritedMethod.isProtected() 
    	                         && !inheritedMethod.isPublic();
    	if (isPackageVisible && !inSamePackage) return false;
    	
    	return true;
    }
    
    /**
     * This method recurses up a specified type looking for a method that overrides the one passed in.
     * 
     * @return the method being overridden or null if none is found
     */
    public static ResolvedMember checkForOverride(ResolvedType typeToCheck,String mname,String mparams,String mrettype,int mmods,String mpkg,UnresolvedType[] methodParamsArray) {

    	if (typeToCheck==null) return null;
    	if (typeToCheck instanceof MissingResolvedTypeWithKnownSignature) return null; // we just can't tell !
    	
    	if (typeToCheck.getWorld().forDEBUG_bridgingCode) System.err.println("  Bridging:checking for override of "+mname+" in "+typeToCheck);
    	
    	String packageName = typeToCheck.getPackageName();
    	if (packageName==null) packageName="";
    	boolean inSamePackage = packageName.equals(mpkg); // used when looking at visibility rules
    	
    	ResolvedMember [] methods = typeToCheck.getDeclaredMethods();
    	for (int ii=0;ii<methods.length;ii++) {
			ResolvedMember methodThatMightBeGettingOverridden = methods[ii]; // the method we are going to check			
			ResolvedMember isOverriding = isOverriding(typeToCheck,methodThatMightBeGettingOverridden,mname,mrettype,mmods,inSamePackage,methodParamsArray);
			if (isOverriding!=null) return isOverriding;
		}
		List l = typeToCheck.getInterTypeMungers();
		for (Iterator iterator = l.iterator(); iterator.hasNext();) {
			Object o = iterator.next();
			// FIXME asc if its not a BcelTypeMunger then its an EclipseTypeMunger ... do I need to worry about that?
			if (o instanceof BcelTypeMunger) {
				BcelTypeMunger element = (BcelTypeMunger)o;
				if (element.getMunger() instanceof NewMethodTypeMunger) {
					if (typeToCheck.getWorld().forDEBUG_bridgingCode) System.err.println("Possible ITD candidate "+element);
					ResolvedMember aMethod = element.getSignature();
					ResolvedMember isOverriding = isOverriding(typeToCheck,aMethod,mname,mrettype,mmods,inSamePackage,methodParamsArray);
					if (isOverriding!=null) return isOverriding;
				}
			}
		}
		
		
		if (typeToCheck.equals(UnresolvedType.OBJECT)) return null; 
		
	    ResolvedType superclass = typeToCheck.getSuperclass();
		ResolvedMember overriddenMethod = checkForOverride(superclass,mname,mparams,mrettype,mmods,mpkg,methodParamsArray);
		if (overriddenMethod!=null) return overriddenMethod;
	    
		ResolvedType[] interfaces = typeToCheck.getDeclaredInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			ResolvedType anInterface = interfaces[i];
			overriddenMethod = checkForOverride(anInterface,mname,mparams,mrettype,mmods,mpkg,methodParamsArray);
			if (overriddenMethod!=null) return overriddenMethod;
		}
		return null;
    }

    /**
     * We need to determine if any methods in this type require bridge methods - this method should only
     * be called if necessary to do this calculation, i.e. we are on a 1.5 VM (where covariance/generics exist) and
     * the type hierarchy for the specified class has changed (via decp/itd).
     * 
     * See pr108101
     */
	public static boolean calculateAnyRequiredBridgeMethods(BcelWorld world,LazyClassGen clazz) {
	    world.ensureAdvancedConfigurationProcessed();
		if (!world.isInJava5Mode()) return false; // just double check... the caller should have already verified this
		if (clazz.isInterface()) return false; // dont bother if we're an interface
		boolean didSomething=false; // set if we build any bridge methods
		
		// So what methods do we have right now in this class?
		List /*LazyMethodGen*/ methods = clazz.getMethodGens();

		// Keep a set of all methods from this type - it'll help us to check if bridge methods 
		// have already been created, we don't want to do it twice!
		Set methodsSet = new HashSet();
		for (int i = 0; i < methods.size(); i++) {
			LazyMethodGen aMethod = (LazyMethodGen)methods.get(i);
			methodsSet.add(aMethod.getName()+aMethod.getSignature()); // e.g. "foo(Ljava/lang/String;)V"
		}
		
		// Now go through all the methods in this type
		for (int i = 0; i < methods.size(); i++) {
			
			// This is the local method that we *might* have to bridge to
			LazyMethodGen bridgeToCandidate  = (LazyMethodGen)methods.get(i);
			if (bridgeToCandidate.isBridgeMethod()) continue; // Doh!
			String name  = bridgeToCandidate.getName();
			String psig  = bridgeToCandidate.getParameterSignature();
			String rsig  = bridgeToCandidate.getReturnType().getSignature();
			
			//if (bridgeToCandidate.isAbstract()) continue;
			if (bridgeToCandidate.isStatic())      continue; // ignore static methods
			if (name.endsWith("init>")) continue; // Skip constructors and static initializers

			if (world.forDEBUG_bridgingCode) System.err.println("Bridging: Determining if we have to bridge to "+clazz.getName()+"."+name+""+bridgeToCandidate.getSignature());
			
			// Let's take a look at the superclass
			ResolvedType theSuperclass= clazz.getSuperClass();
			if (world.forDEBUG_bridgingCode) System.err.println("Bridging: Checking supertype "+theSuperclass);
			String pkgName = clazz.getPackageName();
			UnresolvedType[] bm = BcelWorld.fromBcel(bridgeToCandidate.getArgumentTypes());
			ResolvedMember overriddenMethod = checkForOverride(theSuperclass,name,psig,rsig,bridgeToCandidate.getAccessFlags(),pkgName,bm);
			if (overriddenMethod!=null) { 
				boolean alreadyHaveABridgeMethod = methodsSet.contains(overriddenMethod.getName()+overriddenMethod.getSignature());
				if (!alreadyHaveABridgeMethod) {
					if (world.forDEBUG_bridgingCode) System.err.println("Bridging:bridging to '"+overriddenMethod+"'");
					createBridgeMethod(world, bridgeToCandidate, clazz, overriddenMethod);
					didSomething = true;
					continue; // look at the next method
				}
			}

			// Check superinterfaces
			String[] interfaces = clazz.getInterfaceNames();
			for (int j = 0; j < interfaces.length; j++) {
				if (world.forDEBUG_bridgingCode) System.err.println("Bridging:checking superinterface "+interfaces[j]);
				ResolvedType interfaceType = world.resolve(interfaces[j]);
				overriddenMethod = checkForOverride(interfaceType,name,psig,rsig,bridgeToCandidate.getAccessFlags(),clazz.getPackageName(),bm);
				if (overriddenMethod!=null) { 
					boolean alreadyHaveABridgeMethod = methodsSet.contains(overriddenMethod.getName()+overriddenMethod.getSignature());
					if (!alreadyHaveABridgeMethod) {
						createBridgeMethod(world, bridgeToCandidate, clazz, overriddenMethod);
						didSomething=true;
						if (world.forDEBUG_bridgingCode) System.err.println("Bridging:bridging to "+overriddenMethod);
						continue; // look at the next method
					}	
				}
			}
		}

		return didSomething;
	}
	
    // **************************** end of bridge method creation code *****************

    /**
     * Weave any declare @method/@ctor statements into the members of the supplied class
     */
    private boolean weaveDeclareAtMethodCtor(LazyClassGen clazz) {
		List reportedProblems = new ArrayList();
		
		List allDecams = world.getDeclareAnnotationOnMethods();
		if (allDecams.isEmpty()) return false; // nothing to do
		
		boolean isChanged = false;

		// deal with ITDs
		List itdMethodsCtors = getITDSubset(clazz,ResolvedTypeMunger.Method);
		itdMethodsCtors.addAll(getITDSubset(clazz,ResolvedTypeMunger.Constructor));		
		if (!itdMethodsCtors.isEmpty()) {
			// Can't use the subset called 'decaMs' as it won't be right for ITDs...
 	        isChanged = weaveAtMethodOnITDSRepeatedly(allDecams,itdMethodsCtors,reportedProblems);
		}
		
		// deal with all the other methods...
        List members = clazz.getMethodGens();
		List decaMs = getMatchingSubset(allDecams,clazz.getType());		
		if (decaMs.isEmpty()) return false; // nothing to do
		if (!members.isEmpty()) {
		  Set unusedDecams = new HashSet();
		  unusedDecams.addAll(decaMs);
          for (int memberCounter = 0;memberCounter<members.size();memberCounter++) {
            LazyMethodGen mg = (LazyMethodGen)members.get(memberCounter);
            if (!mg.getName().startsWith(NameMangler.PREFIX)) {
            	
            	// Single first pass
            	List worthRetrying = new ArrayList();
            	boolean modificationOccured = false;
            	List /*AnnotationGen*/ annotationsToAdd = null;
            	for (Iterator iter = decaMs.iterator(); iter.hasNext();) {
            		DeclareAnnotation decaM = (DeclareAnnotation) iter.next();
            		
            		if (decaM.matches(mg.getMemberView(),world)) {
            			if (doesAlreadyHaveAnnotation(mg.getMemberView(),decaM,reportedProblems)) {
            				// remove the declare @method since don't want an error when 
            				// the annotation is already there
            				unusedDecams.remove(decaM);
            				continue; // skip this one...
            			}

            			if (annotationsToAdd==null) annotationsToAdd = new ArrayList();
            			Annotation a = decaM.getAnnotationX().getBcelAnnotation();
            			AnnotationGen ag = new AnnotationGen(a,clazz.getConstantPoolGen(),true);
            			annotationsToAdd.add(ag);
            			mg.addAnnotation(decaM.getAnnotationX());
            			
            			AsmRelationshipProvider.getDefault().addDeclareAnnotationRelationship(decaM.getSourceLocation(),clazz.getName(),mg.getMethod());
            			reportMethodCtorWeavingMessage(clazz, mg.getMemberView(), decaM,mg.getDeclarationLineNumber());
            			isChanged = true;
            			modificationOccured = true;
						// remove the declare @method since have matched against it
            			unusedDecams.remove(decaM);           			
            		} else {
            			if (!decaM.isStarredAnnotationPattern()) 
            				worthRetrying.add(decaM); // an annotation is specified that might be put on by a subsequent decaf
            		}
            	}
            	
            	// Multiple secondary passes
            	while (!worthRetrying.isEmpty() && modificationOccured) {
            		modificationOccured = false;
            		// lets have another go
            		List forRemoval = new ArrayList();
            		for (Iterator iter = worthRetrying.iterator(); iter.hasNext();) {
            			DeclareAnnotation decaM = (DeclareAnnotation) iter.next();
            			if (decaM.matches(mg.getMemberView(),world)) {
            				if (doesAlreadyHaveAnnotation(mg.getMemberView(),decaM,reportedProblems)) {
            					// remove the declare @method since don't want an error when 
                				// the annotation is already there
                				unusedDecams.remove(decaM);
            					continue; // skip this one...
            				}
            				
            				if (annotationsToAdd==null) annotationsToAdd = new ArrayList();
                			Annotation a = decaM.getAnnotationX().getBcelAnnotation();
                			AnnotationGen ag = new AnnotationGen(a,clazz.getConstantPoolGen(),true);
                			annotationsToAdd.add(ag);
                			
            				mg.addAnnotation(decaM.getAnnotationX());
            				AsmRelationshipProvider.getDefault().addDeclareAnnotationRelationship(decaM.getSourceLocation(),clazz.getName(),mg.getMethod());
            				isChanged = true;
            				modificationOccured = true;
            				forRemoval.add(decaM);
    						// remove the declare @method since have matched against it
            				unusedDecams.remove(decaM);
            			}
            		}
            		worthRetrying.removeAll(forRemoval);
            	}
            	if (annotationsToAdd!=null) {
            		Method oldMethod = mg.getMethod();
        			MethodGen myGen = new MethodGen(oldMethod,clazz.getClassName(),clazz.getConstantPoolGen(),false);// dont use tags, they won't get repaired like for woven methods.
            		for (Iterator iter = annotationsToAdd.iterator(); iter.hasNext();) {
						AnnotationGen a = (AnnotationGen) iter.next();
						myGen.addAnnotation(a);						
					}
            		Method newMethod = myGen.getMethod();
            		members.set(memberCounter,new LazyMethodGen(newMethod,clazz));
            	}
            	
            }
          }
	  	  checkUnusedDeclareAtTypes(unusedDecams, false);
        }
		return isChanged;
    }

	
    // TAG: WeavingMessage
	private void reportMethodCtorWeavingMessage(LazyClassGen clazz, ResolvedMember member, DeclareAnnotation decaM,int memberLineNumber) {
		if (!getWorld().getMessageHandler().isIgnoring(IMessage.WEAVEINFO)){
			StringBuffer parmString = new StringBuffer("(");
			UnresolvedType[] paramTypes = member.getParameterTypes();
			for (int i = 0; i < paramTypes.length; i++) {
				UnresolvedType type = paramTypes[i];
				String s = org.aspectj.apache.bcel.classfile.Utility.signatureToString(type.getSignature());
				if (s.lastIndexOf(".")!=-1) s =s.substring(s.lastIndexOf(".")+1);
				parmString.append(s);
				if ((i+1)<paramTypes.length) parmString.append(",");
			}
			parmString.append(")");
			String methodName = member.getName();
			StringBuffer sig = new StringBuffer();
			sig.append(org.aspectj.apache.bcel.classfile.Utility.accessToString(member.getModifiers()));
			sig.append(" ");
			sig.append(member.getReturnType().toString());
			sig.append(" ");
			sig.append(member.getDeclaringType().toString());
			sig.append(".");
			sig.append(methodName.equals("<init>")?"new":methodName);
			sig.append(parmString);
			
			StringBuffer loc = new StringBuffer();
			if (clazz.getFileName()==null) {
				loc.append("no debug info available");
			} else {
				loc.append(clazz.getFileName());
				if (memberLineNumber!=-1) {
					loc.append(":"+memberLineNumber);
				}
			}
			getWorld().getMessageHandler().handleMessage(
					WeaveMessage.constructWeavingMessage(WeaveMessage.WEAVEMESSAGE_ANNOTATES,
							new String[]{
							sig.toString(),
							loc.toString(),
							decaM.getAnnotationString(),
							methodName.startsWith("<init>")?"constructor":"method",
							decaM.getAspect().toString(),
							Utility.beautifyLocation(decaM.getSourceLocation())
							}));	
		}
	}
    
	/**
	 * Looks through a list of declare annotation statements and only returns
	 * those that could possibly match on a field/method/ctor in type.
	 */
	private List getMatchingSubset(List declareAnnotations, ResolvedType type) {
	    List subset = new ArrayList();
	    for (Iterator iter = declareAnnotations.iterator(); iter.hasNext();) {
			DeclareAnnotation da = (DeclareAnnotation) iter.next();
			if (da.couldEverMatch(type)) {
				subset.add(da);
			}
		}
		return subset;
	}

	
    /**
     * Get a subset of all the type mungers defined on this aspect
     */
	private List getITDSubset(LazyClassGen clazz,ResolvedTypeMunger.Kind wantedKind) {
		List subset = new ArrayList();
		Collection c = clazz.getBcelObjectType().getTypeMungers();
		for (Iterator iter = c.iterator();iter.hasNext();) {
			BcelTypeMunger typeMunger = (BcelTypeMunger)iter.next();
			if (typeMunger.getMunger().getKind()==wantedKind) 
				subset.add(typeMunger);
		}
		return subset;
	}
	
	public LazyMethodGen locateAnnotationHolderForFieldMunger(LazyClassGen clazz,BcelTypeMunger fieldMunger) {
		NewFieldTypeMunger nftm = (NewFieldTypeMunger)fieldMunger.getMunger();
		ResolvedMember lookingFor =AjcMemberMaker.interFieldInitializer(nftm.getSignature(),clazz.getType());
		List meths = clazz.getMethodGens();
		for (Iterator iter = meths.iterator(); iter.hasNext();) {
			LazyMethodGen element = (LazyMethodGen) iter.next();
			if (element.getName().equals(lookingFor.getName())) return element;
		}
		return null;
	}
	
	// FIXME asc refactor this to neaten it up
	public LazyMethodGen locateAnnotationHolderForMethodCtorMunger(LazyClassGen clazz,BcelTypeMunger methodCtorMunger) {
		if (methodCtorMunger.getMunger() instanceof NewMethodTypeMunger) {
			NewMethodTypeMunger nftm = (NewMethodTypeMunger)methodCtorMunger.getMunger();
			
			ResolvedMember lookingFor = AjcMemberMaker.interMethodDispatcher(nftm.getSignature(),methodCtorMunger.getAspectType());
			
			List meths = clazz.getMethodGens();
			for (Iterator iter = meths.iterator(); iter.hasNext();) {
				LazyMethodGen element = (LazyMethodGen) iter.next();
				if (element.getName().equals(lookingFor.getName()) && element.getParameterSignature().equals(lookingFor.getParameterSignature())) return element;
			}
			return null;
		} else if (methodCtorMunger.getMunger() instanceof NewConstructorTypeMunger) {
			NewConstructorTypeMunger nftm = (NewConstructorTypeMunger)methodCtorMunger.getMunger();
			ResolvedMember lookingFor =AjcMemberMaker.postIntroducedConstructor(methodCtorMunger.getAspectType(),nftm.getSignature().getDeclaringType(),nftm.getSignature().getParameterTypes());
			List meths = clazz.getMethodGens();
			for (Iterator iter = meths.iterator(); iter.hasNext();) {
				LazyMethodGen element = (LazyMethodGen) iter.next();
				if (element.getName().equals(lookingFor.getName()) && element.getParameterSignature().equals(lookingFor.getParameterSignature())) return element;
			}
			return null;
		} else {
			throw new RuntimeException("Not sure what this is: "+methodCtorMunger);
		}
	}
	
    /**
     * Applies some set of declare @field constructs (List<DeclareAnnotation>) to some bunch 
     * of ITDfields (List<BcelTypeMunger>.  It will iterate over the fields repeatedly until
     * everything has been applied.
     * 
     */
	private boolean weaveAtFieldRepeatedly(List decaFs, List itdFields,List reportedErrors) {
		boolean isChanged = false;
		for (Iterator iter = itdFields.iterator(); iter.hasNext();) {
			BcelTypeMunger fieldMunger = (BcelTypeMunger) iter.next();
			ResolvedMember itdIsActually = fieldMunger.getSignature();
			List worthRetrying = new ArrayList();
			boolean modificationOccured = false;
			
			for (Iterator iter2 = decaFs.iterator(); iter2.hasNext();) {
				DeclareAnnotation decaF = (DeclareAnnotation) iter2.next();
				
				if (decaF.matches(itdIsActually,world)) {
					LazyMethodGen annotationHolder = locateAnnotationHolderForFieldMunger(clazz,fieldMunger);
					if (doesAlreadyHaveAnnotation(annotationHolder,itdIsActually,decaF,reportedErrors)) continue; // skip this one...
					annotationHolder.addAnnotation(decaF.getAnnotationX());
					AsmRelationshipProvider.getDefault().addDeclareAnnotationRelationship(decaF.getSourceLocation(),itdIsActually.getSourceLocation());
					isChanged = true;
					modificationOccured = true;
					
				} else {
					if (!decaF.isStarredAnnotationPattern()) 
						worthRetrying.add(decaF); // an annotation is specified that might be put on by a subsequent decaf
				}
			}
			
		    while (!worthRetrying.isEmpty() && modificationOccured) {
				modificationOccured = false;
                List forRemoval = new ArrayList();
                for (Iterator iter2 = worthRetrying.iterator(); iter2.hasNext();) {
				  DeclareAnnotation decaF = (DeclareAnnotation) iter2.next();
				  if (decaF.matches(itdIsActually,world)) {
					LazyMethodGen annotationHolder = locateAnnotationHolderForFieldMunger(clazz,fieldMunger);
					if (doesAlreadyHaveAnnotation(annotationHolder,itdIsActually,decaF,reportedErrors)) continue; // skip this one...
					annotationHolder.addAnnotation(decaF.getAnnotationX());
					AsmRelationshipProvider.getDefault().addDeclareAnnotationRelationship(decaF.getSourceLocation(),itdIsActually.getSourceLocation());
					isChanged = true;
					modificationOccured = true;
					forRemoval.add(decaF);
				  }
				  worthRetrying.removeAll(forRemoval);
                }
		    }
	      }
	      return isChanged;
	}
	
	
	/**
     * Applies some set of declare @method/@ctor constructs (List<DeclareAnnotation>) to some bunch 
     * of ITDmembers (List<BcelTypeMunger>.  It will iterate over the fields repeatedly until
     * everything has been applied.
     */
	private boolean weaveAtMethodOnITDSRepeatedly(List decaMCs, List itdMethodsCtors,List reportedErrors) {
		boolean isChanged = false;
		for (Iterator iter = itdMethodsCtors.iterator(); iter.hasNext();) {
			BcelTypeMunger methodctorMunger = (BcelTypeMunger) iter.next();
			ResolvedMember unMangledInterMethod = methodctorMunger.getSignature();
			List worthRetrying = new ArrayList();
			boolean modificationOccured = false;
			
			for (Iterator iter2 = decaMCs.iterator(); iter2.hasNext();) {
				DeclareAnnotation decaMC = (DeclareAnnotation) iter2.next();
				if (decaMC.matches(unMangledInterMethod,world)) {
					LazyMethodGen annotationHolder = locateAnnotationHolderForMethodCtorMunger(clazz,methodctorMunger);
					if (annotationHolder == null || doesAlreadyHaveAnnotation(annotationHolder,unMangledInterMethod,decaMC,reportedErrors)){
						continue; // skip this one...
					}
					annotationHolder.addAnnotation(decaMC.getAnnotationX());
					isChanged=true;
					AsmRelationshipProvider.getDefault().addDeclareAnnotationRelationship(decaMC.getSourceLocation(),unMangledInterMethod.getSourceLocation());
					reportMethodCtorWeavingMessage(clazz, unMangledInterMethod, decaMC,-1);
					modificationOccured = true;					
				} else {
					if (!decaMC.isStarredAnnotationPattern()) 
						worthRetrying.add(decaMC); // an annotation is specified that might be put on by a subsequent decaf
				}
			}
			
		    while (!worthRetrying.isEmpty() && modificationOccured) {
				modificationOccured = false;
                List forRemoval = new ArrayList();
                for (Iterator iter2 = worthRetrying.iterator(); iter2.hasNext();) {
				  DeclareAnnotation decaMC = (DeclareAnnotation) iter2.next();
				  if (decaMC.matches(unMangledInterMethod,world)) {
					LazyMethodGen annotationHolder = locateAnnotationHolderForFieldMunger(clazz,methodctorMunger);
					if (doesAlreadyHaveAnnotation(annotationHolder,unMangledInterMethod,decaMC,reportedErrors)) continue; // skip this one...
					annotationHolder.addAnnotation(decaMC.getAnnotationX());
					unMangledInterMethod.addAnnotation(decaMC.getAnnotationX());
					AsmRelationshipProvider.getDefault().addDeclareAnnotationRelationship(decaMC.getSourceLocation(),unMangledInterMethod.getSourceLocation());
					isChanged = true;
					modificationOccured = true;
					forRemoval.add(decaMC);
				  }
				  worthRetrying.removeAll(forRemoval);
                }
		    }
	      }
	      return isChanged;
	}
	
	private boolean dontAddTwice(DeclareAnnotation decaF, Annotation [] dontAddMeTwice){
		for (int i = 0; i < dontAddMeTwice.length; i++){
			Annotation ann = dontAddMeTwice[i];
			if (ann != null && decaF.getAnnotationX().getTypeName().equals(ann.getTypeName())){
				//dontAddMeTwice[i] = null; // incase it really has been added twice!
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Weave any declare @field statements into the fields of the supplied class
	 * 
	 * Interesting case relating to public ITDd fields.  The annotations are really stored against
     * the interfieldinit method in the aspect, but the public field is placed in the target
     * type and then is processed in the 2nd pass over fields that occurs.  I think it would be
     * more expensive to avoid putting the annotation on that inserted public field than just to
     * have it put there as well as on the interfieldinit method.
	 */
	private boolean weaveDeclareAtField(LazyClassGen clazz) {
	  
        // BUGWARNING not getting enough warnings out on declare @field ?
        // There is a potential problem here with warnings not coming out - this
        // will occur if they are created on the second iteration round this loop.
        // We currently deactivate error reporting for the second time round.
        // A possible solution is to record what annotations were added by what
        // decafs and check that to see if an error needs to be reported - this
        // would be expensive so lets skip it for now

		List reportedProblems = new ArrayList();

		List allDecafs = world.getDeclareAnnotationOnFields();
		if (allDecafs.isEmpty()) return false; // nothing to do
		
		
		boolean isChanged = false;
		List itdFields = getITDSubset(clazz,ResolvedTypeMunger.Field);
		if (itdFields!=null) {
			isChanged = weaveAtFieldRepeatedly(allDecafs,itdFields,reportedProblems);
		}
		
        List decaFs = getMatchingSubset(allDecafs,clazz.getType());
		if (decaFs.isEmpty()) return false; // nothing more to do
		Field[] fields = clazz.getFieldGens();
		if (fields!=null) {
		  Set unusedDecafs = new HashSet();
		  unusedDecafs.addAll(decaFs);
          for (int fieldCounter = 0;fieldCounter<fields.length;fieldCounter++) {
            BcelField aBcelField = new BcelField(clazz.getBcelObjectType(),fields[fieldCounter]);
			if (!aBcelField.getName().startsWith(NameMangler.PREFIX)) {				
            // Single first pass
            List worthRetrying = new ArrayList();
            boolean modificationOccured = false;
            
            Annotation [] dontAddMeTwice = fields[fieldCounter].getAnnotations();
            
            // go through all the declare @field statements
            for (Iterator iter = decaFs.iterator(); iter.hasNext();) {
				DeclareAnnotation decaF = (DeclareAnnotation) iter.next();
				if (decaF.matches(aBcelField,world)) {
					
					if (!dontAddTwice(decaF,dontAddMeTwice)){
						if (doesAlreadyHaveAnnotation(aBcelField,decaF,reportedProblems)){
            				// remove the declare @field since don't want an error when 
            				// the annotation is already there
            				unusedDecafs.remove(decaF);
							continue;
						}
						
						if(decaF.getAnnotationX().isRuntimeVisible()){ // isAnnotationWithRuntimeRetention(clazz.getJavaClass(world))){
						//if(decaF.getAnnotationTypeX().isAnnotationWithRuntimeRetention(world)){						
							// it should be runtime visible, so put it on the Field
							Annotation a = decaF.getAnnotationX().getBcelAnnotation();
							AnnotationGen ag = new AnnotationGen(a,clazz.getConstantPoolGen(),true);
							FieldGen myGen = new FieldGen(fields[fieldCounter],clazz.getConstantPoolGen());
							myGen.addAnnotation(ag);
							Field newField = myGen.getField();
							
							aBcelField.addAnnotation(decaF.getAnnotationX());
							clazz.replaceField(fields[fieldCounter],newField);
							fields[fieldCounter]=newField;
							
						} else{
							aBcelField.addAnnotation(decaF.getAnnotationX());
						}
					}
					
					AsmRelationshipProvider.getDefault().addDeclareAnnotationRelationship(decaF.getSourceLocation(),clazz.getName(),fields[fieldCounter]);
					reportFieldAnnotationWeavingMessage(clazz, fields, fieldCounter, decaF);		
					isChanged = true;
					modificationOccured = true;
					// remove the declare @field since have matched against it
        			unusedDecafs.remove(decaF); 
				} else {
					if (!decaF.isStarredAnnotationPattern()) 
						worthRetrying.add(decaF); // an annotation is specified that might be put on by a subsequent decaf
				}
			}
			
            // Multiple secondary passes
            while (!worthRetrying.isEmpty() && modificationOccured) {
              modificationOccured = false;
              // lets have another go
              List forRemoval = new ArrayList();
              for (Iterator iter = worthRetrying.iterator(); iter.hasNext();) {
				DeclareAnnotation decaF = (DeclareAnnotation) iter.next();
				if (decaF.matches(aBcelField,world)) {
					// below code is for recursive things
					if (doesAlreadyHaveAnnotation(aBcelField,decaF,reportedProblems)) {
        				// remove the declare @field since don't want an error when 
        				// the annotation is already there
        				unusedDecafs.remove(decaF);
						continue; // skip this one...
					}
					aBcelField.addAnnotation(decaF.getAnnotationX());
					AsmRelationshipProvider.getDefault().addDeclareAnnotationRelationship(decaF.getSourceLocation(),clazz.getName(),fields[fieldCounter]);
					isChanged = true;
					modificationOccured = true;
					forRemoval.add(decaF);
					// remove the declare @field since have matched against it
        			unusedDecafs.remove(decaF); 
				}
			  }
			  worthRetrying.removeAll(forRemoval);
            }
			}
          }
	  	  checkUnusedDeclareAtTypes(unusedDecafs,true);
        }
		return isChanged;
	}

	// bug 99191 - put out an error message if the type doesn't exist
	/**
	 * Report an error if the reason a "declare @method/ctor/field" was not used was because the member
	 * specified does not exist.  This method is passed some set of declare statements that didn't
	 * match and a flag indicating whether the set contains declare @field or declare @method/ctor
	 * entries.
	 */
	private void checkUnusedDeclareAtTypes(Set unusedDecaTs, boolean isDeclareAtField) {
		for (Iterator iter = unusedDecaTs.iterator(); iter.hasNext();) {
	  		DeclareAnnotation declA = (DeclareAnnotation) iter.next();
	  		
	  		// Error if an exact type pattern was specified 
	  		if ((declA.isExactPattern() || 
					(declA.getSignaturePattern().getDeclaringType() instanceof ExactTypePattern))
					&& (!declA.getSignaturePattern().getName().isAny()
							|| (declA.getKind() == DeclareAnnotation.AT_CONSTRUCTOR))) {
	  			
	  			// Quickly check if an ITD meets supplies the 'missing' member
	  			boolean itdMatch = false;
	  			List lst = clazz.getType().getInterTypeMungers();
	  			for (Iterator iterator = lst.iterator(); iterator.hasNext() && !itdMatch;) {
					BcelTypeMunger element = (BcelTypeMunger) iterator.next();
					if (element.getMunger() instanceof NewFieldTypeMunger) { 
						NewFieldTypeMunger nftm = (NewFieldTypeMunger)element.getMunger();
						itdMatch = declA.getSignaturePattern().matches(nftm.getSignature(),world,false);
	  				}else if (element.getMunger() instanceof NewMethodTypeMunger) {
						NewMethodTypeMunger nmtm = (NewMethodTypeMunger)element.getMunger();
						itdMatch = declA.getSignaturePattern().matches(nmtm.getSignature(),world,false);							
					} else if (element.getMunger() instanceof NewConstructorTypeMunger) {
						NewConstructorTypeMunger nctm = (NewConstructorTypeMunger)element.getMunger();
						itdMatch = declA.getSignaturePattern().matches(nctm.getSignature(),world,false);							
					}
				}
	  			if (!itdMatch) {
	  				IMessage message = null;
	  				if (isDeclareAtField) {
	  					message = new Message(
								"The field '"+ declA.getSignaturePattern().toString() + 
								"' does not exist", declA.getSourceLocation() , true);
					} else { 
						message = new Message(
								"The method '"+ declA.getSignaturePattern().toString() + 
								"' does not exist", declA.getSourceLocation() , true);
					}
					world.getMessageHandler().handleMessage(message);					
				}
	  		}
	  	}
	}
	
	// TAG: WeavingMessage
	private void reportFieldAnnotationWeavingMessage(LazyClassGen clazz, Field[] fields, int fieldCounter, DeclareAnnotation decaF) {
		if (!getWorld().getMessageHandler().isIgnoring(IMessage.WEAVEINFO)){
		  Field theField = fields[fieldCounter];
		  world.getMessageHandler().handleMessage(
		          WeaveMessage.constructWeavingMessage(WeaveMessage.WEAVEMESSAGE_ANNOTATES,
		              new String[]{
						  theField.toString() + "' of type '" + clazz.getName(),
						  clazz.getFileName(),
						  decaF.getAnnotationString(),
						  "field",
						  decaF.getAspect().toString(),
						  Utility.beautifyLocation(decaF.getSourceLocation())}));
		}
	}
    
    /**
     * Check if a resolved member (field/method/ctor) already has an annotation, if it
     * does then put out a warning and return true
     */
	private boolean doesAlreadyHaveAnnotation(ResolvedMember rm,DeclareAnnotation deca,List reportedProblems) {
	  if (rm.hasAnnotation(deca.getAnnotationTypeX())) {
	    if (world.getLint().elementAlreadyAnnotated.isEnabled()) {
		  Integer uniqueID = new Integer(rm.hashCode()*deca.hashCode());
		  if (!reportedProblems.contains(uniqueID)) {
		    reportedProblems.add(uniqueID);
	        world.getLint().elementAlreadyAnnotated.signal(
      		    new String[]{rm.toString(),deca.getAnnotationTypeX().toString()},
      		    rm.getSourceLocation(),new ISourceLocation[]{deca.getSourceLocation()});
	      }
	    }
		return true;
	  }
	  return false;
	}
	
	private boolean doesAlreadyHaveAnnotation(LazyMethodGen rm,ResolvedMember itdfieldsig,DeclareAnnotation deca,List reportedProblems) {
		  if (rm != null && rm.hasAnnotation(deca.getAnnotationTypeX())) {
			  if (world.getLint().elementAlreadyAnnotated.isEnabled()) {
				  Integer uniqueID = new Integer(rm.hashCode()*deca.hashCode());
				  if (!reportedProblems.contains(uniqueID)) {
					  reportedProblems.add(uniqueID);
					  reportedProblems.add(new Integer(itdfieldsig.hashCode()*deca.hashCode()));
					  world.getLint().elementAlreadyAnnotated.signal(
						new String[]{itdfieldsig.toString(),deca.getAnnotationTypeX().toString()},
						rm.getSourceLocation(),new ISourceLocation[]{deca.getSourceLocation()});
				  }
			  }
	      	return true;
		  }
		  return false;
		}
	
	private Set findAspectsForMungers(LazyMethodGen mg) {
		Set aspectsAffectingType = new HashSet();
		for (Iterator iter = mg.matchedShadows.iterator(); iter.hasNext();) {
			BcelShadow aShadow = (BcelShadow) iter.next();	
			// Mungers in effect on that shadow
			for (Iterator iter2 = aShadow.getMungers().iterator();iter2.hasNext();) {
				ShadowMunger aMunger = (ShadowMunger) iter2.next();
				if (aMunger instanceof BcelAdvice) {
					BcelAdvice bAdvice = (BcelAdvice)aMunger;
					if(bAdvice.getConcreteAspect() != null){
						aspectsAffectingType.add(bAdvice.getConcreteAspect().getName());
					}
				} else {
				// It is a 'Checker' - we don't need to remember aspects that only contributed Checkers...
				}		
			}
		}
		return aspectsAffectingType;
	}


	private boolean inlineSelfConstructors(List methodGens) {
		boolean inlinedSomething = false;
		for (Iterator i = methodGens.iterator(); i.hasNext();) {
			LazyMethodGen mg = (LazyMethodGen) i.next();
			if (! mg.getName().equals("<init>")) continue;
			InstructionHandle ih = findSuperOrThisCall(mg);
			if (ih != null && isThisCall(ih)) {
				LazyMethodGen donor = getCalledMethod(ih);
				inlineMethod(donor, mg, ih);
				inlinedSomething = true;
			}
		}
		return inlinedSomething;
	}

	private void positionAndImplement(List initializationShadows) {
		for (Iterator i = initializationShadows.iterator(); i.hasNext(); ) {
			BcelShadow s = (BcelShadow) i.next();
			positionInitializationShadow(s);
			//s.getEnclosingMethod().print();
			s.implement();
		}			
	}		

	private void positionInitializationShadow(BcelShadow s) {
		LazyMethodGen mg = s.getEnclosingMethod();
		InstructionHandle call = findSuperOrThisCall(mg);
		InstructionList body = mg.getBody();
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow((BcelShadow) s);
		if (s.getKind() == Shadow.PreInitialization) {
			// XXX assert first instruction is an ALOAD_0.
			// a pre shadow goes from AFTER the first instruction (which we believe to
			// be an ALOAD_0) to just before the call to super
			r.associateWithTargets(
				Range.genStart(body, body.getStart().getNext()),
				Range.genEnd(body, call.getPrev()));
		} else {
			// assert s.getKind() == Shadow.Initialization
			r.associateWithTargets(
				Range.genStart(body, call.getNext()),
				Range.genEnd(body));
		}
	}

	private boolean isThisCall(InstructionHandle ih) {
		INVOKESPECIAL inst = (INVOKESPECIAL) ih.getInstruction();
		return inst.getClassName(cpg).equals(clazz.getName());
	}


	/** inline a particular call in bytecode.
	 * 
	 * @param donor the method we want to inline
 	 * @param recipient the method containing the call we want to inline
	 * @param call the instructionHandle in recipient's body holding the call we want to 
	 * 			inline.
	 */
	public static void inlineMethod(
		LazyMethodGen donor,
		LazyMethodGen recipient,
		InstructionHandle call)
	{
		// assert recipient.contains(call)
		
		/* Implementation notes:  
		 *
		 * We allocate two slots for every tempvar so we don't screw up 
		 * longs and doubles which may share space.  This could be conservatively avoided 
		 * (no reference to a long/double instruction, don't do it) or packed later.  
		 * Right now we don't bother to pack.
		 * 
		 * Allocate a new var for each formal param of the inlined.  Fill with stack
		 * contents.  Then copy the inlined instructions in with the appropriate remap
		 * table.  Any framelocs used by locals in inlined are reallocated to top of 
		 * frame,
		 */
		final InstructionFactory fact = recipient.getEnclosingClass().getFactory();

		IntMap frameEnv = new IntMap();

		// this also sets up the initial environment
		InstructionList argumentStores = 
			genArgumentStores(donor, recipient, frameEnv, fact);

		InstructionList inlineInstructions = 
			genInlineInstructions(donor, recipient, frameEnv, fact, false);

		inlineInstructions.insert(argumentStores);
		
		recipient.getBody().append(call, inlineInstructions);
		Utility.deleteInstruction(call, recipient);
	}
	
//	public BcelVar genTempVar(UnresolvedType typeX) {
//	   return new BcelVar(typeX.resolve(world), genTempVarIndex(typeX.getSize()));
//	}
//	 
//    private int genTempVarIndex(int size) {
//        return enclosingMethod.allocateLocal(size);
//    }
	
	
	
	/**
	 * Input method is a synchronized method, we remove the bit flag for synchronized and
	 * then insert a try..finally block
	 * 
	 * Some jumping through firey hoops required - depending on the input code level (1.5 or not)
	 * we may or may not be able to use the LDC instruction that takes a class literal (doesnt on
	 * <1.5).
	 * 
	 * FIXME asc Before promoting -Xjoinpoints:synchronization to be a standard option, this needs a bunch of
	 * tidying up - there is some duplication that can be removed.  
	 */
	public static void transformSynchronizedMethod(LazyMethodGen synchronizedMethod) {
        if (trace.isTraceEnabled()) trace.enter("transformSynchronizedMethod",synchronizedMethod);
//		System.err.println("DEBUG: Transforming synchronized method: "+synchronizedMethod.getName());
		final InstructionFactory fact	= synchronizedMethod.getEnclosingClass().getFactory();
		InstructionList body    = synchronizedMethod.getBody();
		InstructionList prepend = new InstructionList();
		Type enclosingClassType = BcelWorld.makeBcelType(synchronizedMethod.getEnclosingClass().getType());
		Type javaLangClassType  = Type.getType(Class.class);
		
		
		// STATIC METHOD TRANSFORMATION
		if (synchronizedMethod.isStatic()) {
			
			// What to do here depends on the level of the class file!
			// LDC can handle class literals in Java5 and above *sigh*
			if (synchronizedMethod.getEnclosingClass().isAtLeastJava5()) {
				// MONITORENTER logic:
				// 0:   ldc     #2; //class C
				// 2:   dup
				// 3:   astore_0
				// 4:   monitorenter
				int slotForLockObject = synchronizedMethod.allocateLocal(enclosingClassType);
				prepend.append(fact.createConstant(enclosingClassType));
				prepend.append(InstructionFactory.createDup(1));
				prepend.append(InstructionFactory.createStore(enclosingClassType, slotForLockObject));
				prepend.append(InstructionFactory.MONITORENTER);
				
				// MONITOREXIT logic:
				
				// We basically need to wrap the code from the method in a finally block that
				// will ensure monitorexit is called.  Content on the finally block seems to
				// be always:
				// 
				// E1: ALOAD_1
				//     MONITOREXIT
				//     ATHROW
				//
				// so lets build that:
				InstructionList finallyBlock = new InstructionList();
				finallyBlock.append(InstructionFactory.createLoad(Type.getType(java.lang.Class.class),slotForLockObject));
				finallyBlock.append(InstructionConstants.MONITOREXIT);
				finallyBlock.append(InstructionConstants.ATHROW);
				
//				finally -> E1
//				|               GETSTATIC java.lang.System.out Ljava/io/PrintStream;   (line 21)
//				|               LDC "hello"
//				|               INVOKEVIRTUAL java.io.PrintStream.println (Ljava/lang/String;)V
//				|               ALOAD_1   (line 20)
//				|               MONITOREXIT
//				finally -> E1
//				                GOTO L0
//				finally -> E1
//				|           E1: ALOAD_1
//				|               MONITOREXIT
//				finally -> E1
//				                ATHROW
//				            L0: RETURN   (line 23)
				
				// search for 'returns' and make them jump to the aload_<n>,monitorexit
				InstructionHandle walker = body.getStart();
				List rets = new ArrayList();
				while (walker!=null) { 
					if (walker.getInstruction() instanceof ReturnInstruction) {
						rets.add(walker);
					}
					walker = walker.getNext();
				}
				if (rets.size()>0) {
					// need to ensure targeters for 'return' now instead target the load instruction
					// (so we never jump over the monitorexit logic)
					
					for (Iterator iter = rets.iterator(); iter.hasNext();) {
						InstructionHandle element = (InstructionHandle) iter.next();
						InstructionList monitorExitBlock = new InstructionList();
						monitorExitBlock.append(InstructionFactory.createLoad(enclosingClassType,slotForLockObject));
						monitorExitBlock.append(InstructionConstants.MONITOREXIT);
						//monitorExitBlock.append(Utility.copyInstruction(element.getInstruction()));
						//element.setInstruction(InstructionFactory.createLoad(classType,slotForThis));
						InstructionHandle monitorExitBlockStart = body.insert(element,monitorExitBlock);
						
						// now move the targeters from the RET to the start of the monitorexit block
						InstructionTargeter[] targeters = element.getTargeters();
						if (targeters!=null) {
							for (int i = 0; i < targeters.length; i++) {
						
								InstructionTargeter targeter = targeters[i];
								// what kinds are there?
								if (targeter instanceof LocalVariableTag) {
									// ignore
								} else if (targeter instanceof LineNumberTag) {
									// ignore
								} else if (targeter instanceof GOTO || targeter instanceof GOTO_W) {
									// move it...
									targeter.updateTarget(element, monitorExitBlockStart);	
								} else if (targeter instanceof BranchInstruction) {
									// move it
									targeter.updateTarget(element, monitorExitBlockStart);
								} else {
									throw new RuntimeException("Unexpected targeter encountered during transform: "+targeter);
								}
							}		
						}
					}
				}
			
				// now the magic, putting the finally block around the code
		        InstructionHandle finallyStart = finallyBlock.getStart();

				InstructionHandle tryPosition   = body.getStart();
				InstructionHandle catchPosition = body.getEnd();
				body.insert(body.getStart(),prepend); // now we can put the monitorenter stuff on
				synchronizedMethod.getBody().append(finallyBlock);			
				synchronizedMethod.addExceptionHandler(tryPosition, catchPosition,finallyStart,null/*==finally*/,false);
				synchronizedMethod.addExceptionHandler(finallyStart,finallyStart.getNext(),finallyStart,null,false);
			} else {

				// TRANSFORMING STATIC METHOD ON PRE JAVA5
				
				// Hideous nightmare, class literal references prior to Java5
			
			// YIKES! this is just the code for MONITORENTER !
//		0:   getstatic       #59; //Field class$1:Ljava/lang/Class;
//		3:   dup
//		4:   ifnonnull       32
//		7:   pop
// try
//		8:   ldc     #61; //String java.lang.String
//		10:  invokestatic    #44; //Method java/lang/Class.forName:(Ljava/lang/String;)Ljava/lang/Class;
//		13:  dup
// catch
//		14:  putstatic       #59; //Field class$1:Ljava/lang/Class;
//		17:  goto    32
//		20:  new     #46; //class java/lang/NoClassDefFoundError
//		23:  dup_x1
//		24:  swap
//		25:  invokevirtual   #52; //Method java/lang/Throwable.getMessage:()Ljava/lang/String;
//		28:  invokespecial   #54; //Method java/lang/NoClassDefFoundError."<init>":(Ljava/lang/String;)V
//		31:  athrow
//		32:  dup <-- partTwo (branch target)
//		33:  astore_0
//		34:  monitorenter
//			
//			plus exceptiontable entry!
//			  8    13    20   Class java/lang/ClassNotFoundException
			Type classType = BcelWorld.makeBcelType(synchronizedMethod.getEnclosingClass().getType());
			Type clazzType = Type.getType(Class.class);
		
			InstructionList parttwo = new InstructionList();
			parttwo.append(InstructionFactory.createDup(1));
			int slotForThis = synchronizedMethod.allocateLocal(classType);
			parttwo.append(InstructionFactory.createStore(clazzType, slotForThis)); // ? should be the real type ? String or something?
			parttwo.append(InstructionFactory.MONITORENTER);
		
			String fieldname = synchronizedMethod.getEnclosingClass().allocateField("class$");
			Field f = new FieldGen(Modifier.STATIC | Modifier.PRIVATE,
				    Type.getType(Class.class),fieldname,synchronizedMethod.getEnclosingClass().getConstantPoolGen()).getField();
			synchronizedMethod.getEnclosingClass().addField(f, null);
			
//			10:  invokestatic    #44; //Method java/lang/Class.forName:(Ljava/lang/String;)Ljava/lang/Class;
//			13:  dup
//			14:  putstatic       #59; //Field class$1:Ljava/lang/Class;
//			17:  goto    32
//			20:  new     #46; //class java/lang/NoClassDefFoundError
//			23:  dup_x1
//			24:  swap
//			25:  invokevirtual   #52; //Method java/lang/Throwable.getMessage:()Ljava/lang/String;
//			28:  invokespecial   #54; //Method java/lang/NoClassDefFoundError."<init>":(Ljava/lang/String;)V
//			31:  athrow
			String name = synchronizedMethod.getEnclosingClass().getName();
			
			prepend.append(fact.createGetStatic(name, fieldname, Type.getType(Class.class)));
			prepend.append(InstructionFactory.createDup(1));
			prepend.append(InstructionFactory.createBranchInstruction(Constants.IFNONNULL, parttwo.getStart()));
			prepend.append(InstructionFactory.POP);
			
			prepend.append(fact.createConstant(name));
			InstructionHandle tryInstruction = prepend.getEnd();
			prepend.append(fact.createInvoke("java.lang.Class", "forName", clazzType,new Type[]{ Type.getType(String.class)}, Constants.INVOKESTATIC));
			InstructionHandle catchInstruction = prepend.getEnd();
			prepend.append(InstructionFactory.createDup(1));
			
			prepend.append(fact.createPutStatic(synchronizedMethod.getEnclosingClass().getType().getName(), fieldname, Type.getType(Class.class)));
			prepend.append(InstructionFactory.createBranchInstruction(Constants.GOTO, parttwo.getStart()));
			
			// start of catch block 
			InstructionList catchBlockForLiteralLoadingFail = new InstructionList();
			catchBlockForLiteralLoadingFail.append(fact.createNew((ObjectType)Type.getType(NoClassDefFoundError.class)));
			catchBlockForLiteralLoadingFail.append(InstructionFactory.createDup_1(1));
			catchBlockForLiteralLoadingFail.append(InstructionFactory.SWAP);
			catchBlockForLiteralLoadingFail.append(fact.createInvoke("java.lang.Throwable", "getMessage", Type.getType(String.class),new Type[]{}, Constants.INVOKEVIRTUAL));
			catchBlockForLiteralLoadingFail.append(fact.createInvoke("java.lang.NoClassDefFoundError", "<init>", Type.VOID,new Type[]{ Type.getType(String.class)}, Constants.INVOKESPECIAL));
			catchBlockForLiteralLoadingFail.append(InstructionFactory.ATHROW);
			InstructionHandle catchBlockStart = catchBlockForLiteralLoadingFail.getStart();
			prepend.append(catchBlockForLiteralLoadingFail);
			prepend.append(parttwo);
//			 MONITORENTER
			// pseudocode: load up 'this' (var0), dup it, store it in a new local var (for use with monitorexit) and call monitorenter:
			// ALOAD_0, DUP, ASTORE_<n>, MONITORENTER
//			prepend.append(InstructionFactory.createLoad(classType,0));
//			prepend.append(InstructionFactory.createDup(1));
//			int slotForThis = synchronizedMethod.allocateLocal(classType);
//			prepend.append(InstructionFactory.createStore(classType, slotForThis));
//			prepend.append(InstructionFactory.MONITORENTER);
			
			// MONITOREXIT
			// here be dragons
			
			// We basically need to wrap the code from the method in a finally block that
			// will ensure monitorexit is called.  Content on the finally block seems to
			// be always:
			// 
			// E1: ALOAD_1
			//     MONITOREXIT
			//     ATHROW
			//
			// so lets build that:
			InstructionList finallyBlock = new InstructionList();
			finallyBlock.append(InstructionFactory.createLoad(Type.getType(java.lang.Class.class),slotForThis));
			finallyBlock.append(InstructionConstants.MONITOREXIT);
			finallyBlock.append(InstructionConstants.ATHROW);
			
//			finally -> E1
//			|               GETSTATIC java.lang.System.out Ljava/io/PrintStream;   (line 21)
//			|               LDC "hello"
//			|               INVOKEVIRTUAL java.io.PrintStream.println (Ljava/lang/String;)V
//			|               ALOAD_1   (line 20)
//			|               MONITOREXIT
//			finally -> E1
//			                GOTO L0
//			finally -> E1
//			|           E1: ALOAD_1
//			|               MONITOREXIT
//			finally -> E1
//			                ATHROW
//			            L0: RETURN   (line 23)
			//frameEnv.put(donorFramePos, thisSlot);
			
			// search for 'returns' and make them to the aload_<n>,monitorexit
			InstructionHandle walker = body.getStart();
			List rets = new ArrayList();
			while (walker!=null) { //!walker.equals(body.getEnd())) {
				if (walker.getInstruction() instanceof ReturnInstruction) {
					rets.add(walker);
				}
				walker = walker.getNext();
			}
			if (rets.size()>0) {
				// need to ensure targeters for 'return' now instead target the load instruction
				// (so we never jump over the monitorexit logic)
				
				for (Iterator iter = rets.iterator(); iter.hasNext();) {
					InstructionHandle element = (InstructionHandle) iter.next();
//					System.err.println("Adding monitor exit block at "+element);
					InstructionList monitorExitBlock = new InstructionList();
					monitorExitBlock.append(InstructionFactory.createLoad(classType,slotForThis));
					monitorExitBlock.append(InstructionConstants.MONITOREXIT);
					//monitorExitBlock.append(Utility.copyInstruction(element.getInstruction()));
					//element.setInstruction(InstructionFactory.createLoad(classType,slotForThis));
					InstructionHandle monitorExitBlockStart = body.insert(element,monitorExitBlock);
					
					// now move the targeters from the RET to the start of the monitorexit block
					InstructionTargeter[] targeters = element.getTargeters();
					if (targeters!=null) {
						for (int i = 0; i < targeters.length; i++) {
					
							InstructionTargeter targeter = targeters[i];
							// what kinds are there?
							if (targeter instanceof LocalVariableTag) {
								// ignore
							} else if (targeter instanceof LineNumberTag) {
								// ignore
							} else if (targeter instanceof GOTO || targeter instanceof GOTO_W) {
								// move it...
								targeter.updateTarget(element, monitorExitBlockStart);
							} else if (targeter instanceof BranchInstruction) {
								// move it
								targeter.updateTarget(element, monitorExitBlockStart);
							} else {
								throw new RuntimeException("Unexpected targeter encountered during transform: "+targeter);
							}
						}		
					}
				}
			}
//			body = rewriteWithMonitorExitCalls(body,fact,true,slotForThis,classType);
//			synchronizedMethod.setBody(body);
		
			// now the magic, putting the finally block around the code
	        InstructionHandle finallyStart = finallyBlock.getStart();

			InstructionHandle tryPosition   = body.getStart();
			InstructionHandle catchPosition = body.getEnd();
			body.insert(body.getStart(),prepend); // now we can put the monitorenter stuff on

			synchronizedMethod.getBody().append(finallyBlock);			
			synchronizedMethod.addExceptionHandler(tryPosition, catchPosition,finallyStart,null/*==finally*/,false);
			synchronizedMethod.addExceptionHandler(tryInstruction, catchInstruction,catchBlockStart,(ObjectType)Type.getType(ClassNotFoundException.class),true);
			synchronizedMethod.addExceptionHandler(finallyStart,finallyStart.getNext(),finallyStart,null,false);
			}
		} else {
			
			// TRANSFORMING NON STATIC METHOD 
			Type classType = BcelWorld.makeBcelType(synchronizedMethod.getEnclosingClass().getType());
			// MONITORENTER
			// pseudocode: load up 'this' (var0), dup it, store it in a new local var (for use with monitorexit) and call monitorenter:
			// ALOAD_0, DUP, ASTORE_<n>, MONITORENTER
			prepend.append(InstructionFactory.createLoad(classType,0));
			prepend.append(InstructionFactory.createDup(1));
			int slotForThis = synchronizedMethod.allocateLocal(classType);
			prepend.append(InstructionFactory.createStore(classType, slotForThis));
			prepend.append(InstructionFactory.MONITORENTER);
//			body.insert(body.getStart(),prepend);
			
			// MONITOREXIT
			
			// We basically need to wrap the code from the method in a finally block that
			// will ensure monitorexit is called.  Content on the finally block seems to
			// be always:
			// 
			// E1: ALOAD_1
			//     MONITOREXIT
			//     ATHROW
			//
			// so lets build that:
			InstructionList finallyBlock = new InstructionList();
			finallyBlock.append(InstructionFactory.createLoad(classType,slotForThis));
			finallyBlock.append(InstructionConstants.MONITOREXIT);
			finallyBlock.append(InstructionConstants.ATHROW);
			
//			finally -> E1
//			|               GETSTATIC java.lang.System.out Ljava/io/PrintStream;   (line 21)
//			|               LDC "hello"
//			|               INVOKEVIRTUAL java.io.PrintStream.println (Ljava/lang/String;)V
//			|               ALOAD_1   (line 20)
//			|               MONITOREXIT
//			finally -> E1
//			                GOTO L0
//			finally -> E1
//			|           E1: ALOAD_1
//			|               MONITOREXIT
//			finally -> E1
//			                ATHROW
//			            L0: RETURN   (line 23)
			//frameEnv.put(donorFramePos, thisSlot);
			
			// search for 'returns' and make them to the aload_<n>,monitorexit
			InstructionHandle walker = body.getStart();
			List rets = new ArrayList();
			while (walker!=null) { //!walker.equals(body.getEnd())) {
				if (walker.getInstruction() instanceof ReturnInstruction) {
					rets.add(walker);
				}
				walker = walker.getNext();
			}
			if (rets.size()>0) {
				// need to ensure targeters for 'return' now instead target the load instruction
				// (so we never jump over the monitorexit logic)
				
				for (Iterator iter = rets.iterator(); iter.hasNext();) {
					InstructionHandle element = (InstructionHandle) iter.next();
//					System.err.println("Adding monitor exit block at "+element);
					InstructionList monitorExitBlock = new InstructionList();
					monitorExitBlock.append(InstructionFactory.createLoad(classType,slotForThis));
					monitorExitBlock.append(InstructionConstants.MONITOREXIT);
					//monitorExitBlock.append(Utility.copyInstruction(element.getInstruction()));
					//element.setInstruction(InstructionFactory.createLoad(classType,slotForThis));
					InstructionHandle monitorExitBlockStart = body.insert(element,monitorExitBlock);
					
					// now move the targeters from the RET to the start of the monitorexit block
					InstructionTargeter[] targeters = element.getTargeters();
					if (targeters!=null) {
						for (int i = 0; i < targeters.length; i++) {
					
							InstructionTargeter targeter = targeters[i];
							// what kinds are there?
							if (targeter instanceof LocalVariableTag) {
								// ignore
							} else if (targeter instanceof LineNumberTag) {
								// ignore
							} else if (targeter instanceof GOTO || targeter instanceof GOTO_W) {
								// move it...
								targeter.updateTarget(element, monitorExitBlockStart);
							} else if (targeter instanceof BranchInstruction) {
								// move it
								targeter.updateTarget(element, monitorExitBlockStart);
							} else {
								throw new RuntimeException("Unexpected targeter encountered during transform: "+targeter);
							}
						}		
					}
				}
			}
		
			// now the magic, putting the finally block around the code
	        InstructionHandle finallyStart = finallyBlock.getStart();

			InstructionHandle tryPosition   = body.getStart();
			InstructionHandle catchPosition = body.getEnd();
			body.insert(body.getStart(),prepend); // now we can put the monitorenter stuff on
			synchronizedMethod.getBody().append(finallyBlock);			
			synchronizedMethod.addExceptionHandler(tryPosition, catchPosition,finallyStart,null/*==finally*/,false);
			synchronizedMethod.addExceptionHandler(finallyStart,finallyStart.getNext(),finallyStart,null,false);
			// also the exception handling for the finally block jumps to itself
			
			// max locals will already have been modified in the allocateLocal() call
			
//			synchronized bit is removed on LazyMethodGen.pack()
		}
		
		// gonna have to go through and change all aload_0s to load the var from a variable,
		// going to add a new variable for the this var

        if (trace.isTraceEnabled()) trace.exit("transformSynchronizedMethod");
	}
	
	

	/** generate the instructions to be inlined.
	 * 
	 * @param donor the method from which we will copy (and adjust frame and jumps) 
	 * 			instructions.
	 * @param recipient the method the instructions will go into.  Used to get the frame
	 * 			size so we can allocate new frame locations for locals in donor.
	 * @param frameEnv an environment to map from donor frame to recipient frame,
	 * 			initially populated with argument locations.
	 * @param fact an instruction factory for recipient
	 */
	static InstructionList genInlineInstructions(
		LazyMethodGen donor,
		LazyMethodGen recipient,
		IntMap frameEnv,
		InstructionFactory fact,
		boolean keepReturns) 
	{
		InstructionList footer = new InstructionList();
		InstructionHandle end = footer.append(InstructionConstants.NOP);

		InstructionList ret = new InstructionList();
		InstructionList sourceList = donor.getBody();

		Map srcToDest  = new HashMap();
		ConstantPoolGen donorCpg = donor.getEnclosingClass().getConstantPoolGen();
		ConstantPoolGen recipientCpg = recipient.getEnclosingClass().getConstantPoolGen();
		
		boolean isAcrossClass = donorCpg != recipientCpg;
		
		// first pass: copy the instructions directly, populate the srcToDest map,
		// fix frame instructions
		for (InstructionHandle src = sourceList.getStart();
			src != null;
			src = src.getNext()) 
		{
			Instruction fresh = Utility.copyInstruction(src.getInstruction());
			InstructionHandle dest;
			if (fresh instanceof CPInstruction) {
				// need to reset index to go to new constant pool.  This is totally
				// a computation leak... we're testing this LOTS of times.  Sigh.
				if (isAcrossClass) {
					CPInstruction cpi = (CPInstruction) fresh;
					cpi.setIndex(
						recipientCpg.addConstant(
							donorCpg.getConstant(cpi.getIndex()),
							donorCpg));
				}
			}
			if (src.getInstruction() == Range.RANGEINSTRUCTION) {
				dest = ret.append(Range.RANGEINSTRUCTION);
			} else if (fresh instanceof ReturnInstruction) {
				if (keepReturns) {
					dest = ret.append(fresh);
				} else {
					dest = 
						ret.append(InstructionFactory.createBranchInstruction(Constants.GOTO, end));
				}
			} else if (fresh instanceof BranchInstruction) {
				dest = ret.append((BranchInstruction) fresh);
			} else if (
				fresh instanceof LocalVariableInstruction || fresh instanceof RET) {
				IndexedInstruction indexed = (IndexedInstruction) fresh;
				int oldIndex = indexed.getIndex();
				int freshIndex;
				if (!frameEnv.hasKey(oldIndex)) {
					freshIndex = recipient.allocateLocal(2);
					frameEnv.put(oldIndex, freshIndex);
				} else {
					freshIndex = frameEnv.get(oldIndex);
				}
				indexed.setIndex(freshIndex);
				dest = ret.append(fresh);
			} else {
				dest = ret.append(fresh);
			}
			srcToDest.put(src, dest);
		}
		
		// second pass: retarget branch instructions, copy ranges and tags
		Map tagMap = new HashMap();
		Map shadowMap = new HashMap();		
		for (InstructionHandle dest = ret.getStart(), src = sourceList.getStart(); 
				dest != null; 
				dest = dest.getNext(), src = src.getNext()) {
			Instruction inst = dest.getInstruction();
			
			// retarget branches
			if (inst instanceof BranchInstruction) {
				BranchInstruction branch = (BranchInstruction) inst;
				InstructionHandle oldTarget = branch.getTarget();
				InstructionHandle newTarget =
					(InstructionHandle) srcToDest.get(oldTarget);
				if (newTarget == null) {
					// assert this is a GOTO
					// this was a return instruction we previously replaced
				} else {
					branch.setTarget(newTarget);
					if (branch instanceof Select) {
						Select select = (Select) branch;
						InstructionHandle[] oldTargets = select.getTargets();
						for (int k = oldTargets.length - 1; k >= 0; k--) {
							select.setTarget(
								k,
								(InstructionHandle) srcToDest.get(oldTargets[k]));
						}
					}
				}
			}			
			
			//copy over tags and range attributes
	        InstructionTargeter[] srcTargeters = src.getTargeters();
	        if (srcTargeters != null) { 
	            for (int j = srcTargeters.length - 1; j >= 0; j--) {
	                InstructionTargeter old = srcTargeters[j];
	                if (old instanceof Tag) {
	                	Tag oldTag = (Tag) old;
	                	Tag fresh = (Tag) tagMap.get(oldTag);
	                	if (fresh == null) {
	                		fresh = oldTag.copy();
	                		tagMap.put(oldTag, fresh);
	                	}
	                	dest.addTargeter(fresh);
	                } else if (old instanceof ExceptionRange) {
	                	ExceptionRange er = (ExceptionRange) old;
	                	if (er.getStart() == src) {
	                		ExceptionRange freshEr =
	                			new ExceptionRange(
	                				recipient.getBody(),
	                				er.getCatchType(),
	                				er.getPriority());
	                		freshEr.associateWithTargets(
								dest,
								(InstructionHandle)srcToDest.get(er.getEnd()),
								(InstructionHandle)srcToDest.get(er.getHandler()));
	                	}
					} else if (old instanceof ShadowRange) {
						ShadowRange oldRange = (ShadowRange) old;
						if (oldRange.getStart() == src) {
							BcelShadow oldShadow = oldRange.getShadow();
							BcelShadow freshEnclosing =
								oldShadow.getEnclosingShadow() == null
									? null
									: (BcelShadow) shadowMap.get(oldShadow.getEnclosingShadow());
							BcelShadow freshShadow =
								oldShadow.copyInto(recipient, freshEnclosing);
							ShadowRange freshRange = new ShadowRange(recipient.getBody());
							freshRange.associateWithShadow(freshShadow);
							freshRange.associateWithTargets(
								dest,
								(InstructionHandle) srcToDest.get(oldRange.getEnd()));
							shadowMap.put(oldRange, freshRange);
							//recipient.matchedShadows.add(freshShadow);
							// XXX should go through the NEW copied shadow and update
							// the thisVar, targetVar, and argsVar
							// ??? Might want to also go through at this time and add
							// "extra" vars to the shadow. 
						}
					}
	            }
	        }			
		}
		if (!keepReturns) ret.append(footer);
		return ret;
	}

	static InstructionList rewriteWithMonitorExitCalls(InstructionList sourceList,InstructionFactory fact,boolean keepReturns,int monitorVarSlot,Type monitorVarType)
		{
			InstructionList footer = new InstructionList();
			InstructionHandle end = footer.append(InstructionConstants.NOP);

			InstructionList newList = new InstructionList();

			Map srcToDest  = new HashMap();
			
			// first pass: copy the instructions directly, populate the srcToDest map,
			// fix frame instructions
			for (InstructionHandle src = sourceList.getStart(); src != null; src = src.getNext()) {
				Instruction fresh = Utility.copyInstruction(src.getInstruction());
				InstructionHandle dest;
				if (src.getInstruction() == Range.RANGEINSTRUCTION) {
					dest = newList.append(Range.RANGEINSTRUCTION);
				} else if (fresh instanceof ReturnInstruction) {
					if (keepReturns) {
						newList.append(InstructionFactory.createLoad(monitorVarType,monitorVarSlot));
						newList.append(InstructionConstants.MONITOREXIT);
						dest = newList.append(fresh);
					} else {
						dest = 
							newList.append(InstructionFactory.createBranchInstruction(Constants.GOTO, end));
					}
				} else if (fresh instanceof BranchInstruction) {
					dest = newList.append((BranchInstruction) fresh);
				} else if (
					fresh instanceof LocalVariableInstruction || fresh instanceof RET) {
					IndexedInstruction indexed = (IndexedInstruction) fresh;
					int oldIndex = indexed.getIndex();
					int freshIndex;
//					if (!frameEnv.hasKey(oldIndex)) {
//						freshIndex = recipient.allocateLocal(2);
//						frameEnv.put(oldIndex, freshIndex);
//					} else {
						freshIndex = oldIndex;//frameEnv.get(oldIndex);
//					}
					indexed.setIndex(freshIndex);
					dest = newList.append(fresh);
				} else {
					dest = newList.append(fresh);
				}
				srcToDest.put(src, dest);
			}
			
			// second pass: retarget branch instructions, copy ranges and tags
			Map tagMap = new HashMap();
			Map shadowMap = new HashMap();		
			for (InstructionHandle dest = newList.getStart(), src = sourceList.getStart(); 
					dest != null; 
					dest = dest.getNext(), src = src.getNext()) {
				Instruction inst = dest.getInstruction();
				
				// retarget branches
				if (inst instanceof BranchInstruction) {
					BranchInstruction branch = (BranchInstruction) inst;
					InstructionHandle oldTarget = branch.getTarget();
					InstructionHandle newTarget =
						(InstructionHandle) srcToDest.get(oldTarget);
					if (newTarget == null) {
						// assert this is a GOTO
						// this was a return instruction we previously replaced
					} else {
						branch.setTarget(newTarget);
						if (branch instanceof Select) {
							Select select = (Select) branch;
							InstructionHandle[] oldTargets = select.getTargets();
							for (int k = oldTargets.length - 1; k >= 0; k--) {
								select.setTarget(
									k,
									(InstructionHandle) srcToDest.get(oldTargets[k]));
							}
						}
					}
				}			
				
				//copy over tags and range attributes
		        InstructionTargeter[] srcTargeters = src.getTargeters();
		        if (srcTargeters != null) { 
		            for (int j = srcTargeters.length - 1; j >= 0; j--) {
		                InstructionTargeter old = srcTargeters[j];
		                if (old instanceof Tag) {
		                	 Tag oldTag = (Tag) old;
		                	 Tag fresh = (Tag) tagMap.get(oldTag);
		                	 if (fresh == null) {
		                		fresh = oldTag.copy();
		                		tagMap.put(oldTag, fresh);
		                	 }
		                	 dest.addTargeter(fresh);
		                } else if (old instanceof ExceptionRange) {
		                	 ExceptionRange er = (ExceptionRange) old;
		                	 if (er.getStart() == src) {
		                		ExceptionRange freshEr =
		                			new ExceptionRange(newList/*recipient.getBody()*/,er.getCatchType(),er.getPriority());
		                		freshEr.associateWithTargets(
									dest,
									(InstructionHandle)srcToDest.get(er.getEnd()),
									(InstructionHandle)srcToDest.get(er.getHandler()));
		                	}
						}
/*else if (old instanceof ShadowRange) {
							ShadowRange oldRange = (ShadowRange) old;
							if (oldRange.getStart() == src) {
								BcelShadow oldShadow = oldRange.getShadow();
								BcelShadow freshEnclosing =
									oldShadow.getEnclosingShadow() == null
										? null
										: (BcelShadow) shadowMap.get(oldShadow.getEnclosingShadow());
								BcelShadow freshShadow =
									oldShadow.copyInto(recipient, freshEnclosing);
								ShadowRange freshRange = new ShadowRange(recipient.getBody());
								freshRange.associateWithShadow(freshShadow);
								freshRange.associateWithTargets(
									dest,
									(InstructionHandle) srcToDest.get(oldRange.getEnd()));
								shadowMap.put(oldRange, freshRange);
								//recipient.matchedShadows.add(freshShadow);
								// XXX should go through the NEW copied shadow and update
								// the thisVar, targetVar, and argsVar
								// ??? Might want to also go through at this time and add
								// "extra" vars to the shadow. 
							}
						}*/
		            }
		        }			
			}
			if (!keepReturns) newList.append(footer);
			return newList;
		}

	/** generate the argument stores in preparation for inlining.
	 * 
	 * @param donor the method we will inline from.  Used to get the signature.
	 * @param recipient the method we will inline into.  Used to get the frame size 
	 * 			so we can allocate fresh locations.
	 * @param frameEnv an empty environment we populate with a map from donor frame to
	 * 			recipient frame.
	 * @param fact an instruction factory for recipient
	 */
	private static InstructionList genArgumentStores(
		LazyMethodGen donor,
		LazyMethodGen recipient,
		IntMap frameEnv,
		InstructionFactory fact) 
	{
		InstructionList ret = new InstructionList();

		int donorFramePos = 0;

		// writing ret back to front because we're popping. 
		if (! donor.isStatic()) {
			int targetSlot = recipient.allocateLocal(Type.OBJECT);
			ret.insert(InstructionFactory.createStore(Type.OBJECT, targetSlot));
			frameEnv.put(donorFramePos, targetSlot);
			donorFramePos += 1;
		}
		Type[] argTypes = donor.getArgumentTypes();
		for (int i = 0, len = argTypes.length; i < len; i++) {
			Type argType = argTypes[i];
			int argSlot = recipient.allocateLocal(argType);
			ret.insert(InstructionFactory.createStore(argType, argSlot));
			frameEnv.put(donorFramePos, argSlot);
			donorFramePos += argType.getSize();
		}
		return ret;
	}

	/** get a called method:  Assumes the called method is in this class,
	 * and the reference to it is exact (a la INVOKESPECIAL). 
	 * 
	 * @param ih The InvokeInstruction instructionHandle pointing to the called method.
	 */
	private LazyMethodGen getCalledMethod(
		InstructionHandle ih) 
	{
		InvokeInstruction inst = (InvokeInstruction) ih.getInstruction();

		String methodName = inst.getName(cpg);
		String signature = inst.getSignature(cpg);

		return clazz.getLazyMethodGen(methodName, signature);
	}

	private void weaveInAddedMethods() {
		Collections.sort(addedLazyMethodGens, 
			new Comparator() {
				public int compare(Object a, Object b) {
					LazyMethodGen aa = (LazyMethodGen) a;
					LazyMethodGen bb = (LazyMethodGen) b;
					int i = aa.getName().compareTo(bb.getName());
					if (i != 0) return i;
					return aa.getSignature().compareTo(bb.getSignature());
				}
			}
		);
		
		for (Iterator i = addedLazyMethodGens.iterator(); i.hasNext(); ) {
			clazz.addMethodGen((LazyMethodGen)i.next());
		}
	}

    void addPerSingletonField(Member field) {
    	ObjectType aspectType = (ObjectType) BcelWorld.makeBcelType(field.getReturnType());
    	String aspectName = field.getReturnType().getName();

		LazyMethodGen clinit = clazz.getStaticInitializer();
		InstructionList setup = new InstructionList();
		InstructionFactory fact = clazz.getFactory();

		setup.append(fact.createNew(aspectType));
		setup.append(InstructionFactory.createDup(1));
		setup.append(fact.createInvoke(
			aspectName, 
			"<init>", 
			Type.VOID, 
			new Type[0], 
			Constants.INVOKESPECIAL));
		setup.append(
			fact.createFieldAccess(
				aspectName,
				field.getName(),
				aspectType,
				Constants.PUTSTATIC));
		clinit.getBody().insert(setup);
    }

	/**
	 * Returns null if this is not a Java constructor, and then we won't 
	 * weave into it at all
	 */
	private InstructionHandle findSuperOrThisCall(LazyMethodGen mg) {
		int depth = 1;
		InstructionHandle start = mg.getBody().getStart();
		while (true) {
			if (start == null) return null;
			
			Instruction inst = start.getInstruction();
			if (inst instanceof INVOKESPECIAL
				&& ((INVOKESPECIAL) inst).getName(cpg).equals("<init>")) {
				depth--;
				if (depth == 0) return start;
			} else if (inst instanceof NEW) {
				depth++;
			} 
			start = start.getNext();
		}
	}

	// ----
	
	private boolean match(LazyMethodGen mg) {
		BcelShadow enclosingShadow;
		List shadowAccumulator = new ArrayList();
		
		boolean startsAngly = mg.getName().charAt(0)=='<';
		// we want to match ajsynthetic constructors...
		if (startsAngly && mg.getName().equals("<init>")) {
			return matchInit(mg, shadowAccumulator);
		} else if (!shouldWeaveBody(mg)) { //.isAjSynthetic()) {
			return false;			
		} else {
			if (startsAngly && mg.getName().equals("<clinit>")) {
				clinitShadow = enclosingShadow = BcelShadow.makeStaticInitialization(world, mg);
				//System.err.println(enclosingShadow);
			} else if (mg.isAdviceMethod()) {
				enclosingShadow = BcelShadow.makeAdviceExecution(world, mg);
			} else {
				AjAttribute.EffectiveSignatureAttribute effective = mg.getEffectiveSignature();
				if (effective == null) {
					enclosingShadow = BcelShadow.makeMethodExecution(world, mg, !canMatchBodyShadows);
				} else if (effective.isWeaveBody()) {
				  ResolvedMember rm = effective.getEffectiveSignature();

				  // Annotations for things with effective signatures are never stored in the effective 
				  // signature itself -  we have to hunt for them.  Storing them in the effective signature
				  // would mean keeping two sets up to date (no way!!)
				  
				  fixAnnotationsForResolvedMember(rm,mg.getMemberView());
				  				  
				  enclosingShadow =
					BcelShadow.makeShadowForMethod(world,mg,effective.getShadowKind(),rm);
				} else {
					return false;
				}
			}

			if (canMatchBodyShadows) {
				for (InstructionHandle h = mg.getBody().getStart();
					h != null;
					h = h.getNext()) {
					match(mg, h, enclosingShadow, shadowAccumulator);
				}
			}
			// FIXME asc change from string match if we can, rather brittle.  this check actually prevents field-exec jps
			if (canMatch(enclosingShadow.getKind()) && !(mg.getName().charAt(0)=='a' && mg.getName().startsWith("ajc$interFieldInit"))) {
				if (match(enclosingShadow, shadowAccumulator)) {
					enclosingShadow.init();
				}
			}
			mg.matchedShadows = shadowAccumulator;
			return !shadowAccumulator.isEmpty();
		}
	}

	private boolean matchInit(LazyMethodGen mg, List shadowAccumulator) {
		BcelShadow enclosingShadow;
		// XXX the enclosing join point is wrong for things before ignoreMe.
		InstructionHandle superOrThisCall = findSuperOrThisCall(mg);

		// we don't walk bodies of things where it's a wrong constructor thingie
		if (superOrThisCall == null) return false;

		enclosingShadow = BcelShadow.makeConstructorExecution(world, mg, superOrThisCall);
		if (mg.getEffectiveSignature() != null) {
			enclosingShadow.setMatchingSignature(mg.getEffectiveSignature().getEffectiveSignature());
		}
		
		// walk the body
		boolean beforeSuperOrThisCall = true;
		if (shouldWeaveBody(mg)) {
			if (canMatchBodyShadows) {
				for (InstructionHandle h = mg.getBody().getStart();
					h != null;
					h = h.getNext()) {
					if (h == superOrThisCall) {
						beforeSuperOrThisCall = false;
						continue;
					}
					match(mg, h, beforeSuperOrThisCall ? null : enclosingShadow, shadowAccumulator);
				}
			}
			if (canMatch(Shadow.ConstructorExecution))
				match(enclosingShadow, shadowAccumulator);
		}
		
		// XXX we don't do pre-inits of interfaces
		
		// now add interface inits
		if (superOrThisCall != null && ! isThisCall(superOrThisCall)) {
			InstructionHandle curr = enclosingShadow.getRange().getStart();
			for (Iterator i = addedSuperInitializersAsList.iterator(); i.hasNext(); ) {
				IfaceInitList l = (IfaceInitList) i.next();

				Member ifaceInitSig = AjcMemberMaker.interfaceConstructor(l.onType);
				
				BcelShadow initShadow =
					BcelShadow.makeIfaceInitialization(world, mg, ifaceInitSig);
				
				// insert code in place
				InstructionList inits = genInitInstructions(l.list, false);
				if (match(initShadow, shadowAccumulator) || !inits.isEmpty()) {
					initShadow.initIfaceInitializer(curr);
					initShadow.getRange().insert(inits, Range.OutsideBefore);
				}
			}
			
			// now we add our initialization code
			InstructionList inits = genInitInstructions(addedThisInitializers, false);
			enclosingShadow.getRange().insert(inits, Range.OutsideBefore);				
		}

		// actually, you only need to inline the self constructors that are 
		// in a particular group (partition the constructors into groups where members
		// call or are called only by those in the group).  Then only inline 
		// constructors
		// in groups where at least one initialization jp matched.  Future work.
		boolean addedInitialization = 
			match(
				BcelShadow.makeUnfinishedInitialization(world, mg), 
				initializationShadows);
		addedInitialization |=
			match(
				BcelShadow.makeUnfinishedPreinitialization(world, mg),
				initializationShadows);
		mg.matchedShadows = shadowAccumulator;
		return addedInitialization || !shadowAccumulator.isEmpty();
	}

	private boolean shouldWeaveBody(LazyMethodGen mg) {	
		if (mg.isBridgeMethod()) return false;
		if (mg.isAjSynthetic()) return mg.getName().equals("<clinit>");
		AjAttribute.EffectiveSignatureAttribute a = mg.getEffectiveSignature();
		if (a != null) return a.isWeaveBody();
		return true;
	}


	/**
	 * first sorts the mungers, then gens the initializers in the right order
	 */
	private InstructionList genInitInstructions(List list, boolean isStatic) {
		list = PartialOrder.sort(list);
        if (list == null) {
        	throw new BCException("circularity in inter-types");
        }
		
		InstructionList ret = new InstructionList();
		
		for (Iterator i = list.iterator(); i.hasNext();) {
			ConcreteTypeMunger cmunger = (ConcreteTypeMunger) i.next();
			NewFieldTypeMunger munger = (NewFieldTypeMunger) cmunger.getMunger();
			ResolvedMember initMethod = munger.getInitMethod(cmunger.getAspectType());
			if (!isStatic) ret.append(InstructionConstants.ALOAD_0);
			ret.append(Utility.createInvoke(fact, world, initMethod));
		}
		return ret;
	}

    
	private void match(
		LazyMethodGen mg,
		InstructionHandle ih,
		BcelShadow enclosingShadow,
		List shadowAccumulator) 
	{
		Instruction i = ih.getInstruction();
		if ((i instanceof FieldInstruction) && 
			(canMatch(Shadow.FieldGet) || canMatch(Shadow.FieldSet))
		) {
			FieldInstruction fi = (FieldInstruction) i;
						
			if (fi instanceof PUTFIELD || fi instanceof PUTSTATIC) {
				// check for sets of constant fields.  We first check the previous 
				// instruction.  If the previous instruction is a LD_WHATEVER (push
				// constant on the stack) then we must resolve the field to determine
				// if it's final.  If it is final, then we don't generate a shadow.
				InstructionHandle prevHandle = ih.getPrev();
				Instruction prevI = prevHandle.getInstruction();
				if (Utility.isConstantPushInstruction(prevI)) {
					Member field = BcelWorld.makeFieldJoinPointSignature(clazz, (FieldInstruction) i);
					ResolvedMember resolvedField = field.resolve(world);
					if (resolvedField == null) {
						// we can't find the field, so it's not a join point.
					} else if (Modifier.isFinal(resolvedField.getModifiers())) {
						// it's final, so it's the set of a final constant, so it's
						// not a join point according to 1.0.6 and 1.1.
					} else {
						if (canMatch(Shadow.FieldSet))
							matchSetInstruction(mg, ih, enclosingShadow, shadowAccumulator);
					}						
				} else {
					if (canMatch(Shadow.FieldSet))
						matchSetInstruction(mg, ih, enclosingShadow, shadowAccumulator);
				}
			} else {
				if (canMatch(Shadow.FieldGet))
					matchGetInstruction(mg, ih, enclosingShadow, shadowAccumulator);
			}
		} else if (i instanceof InvokeInstruction) {
			InvokeInstruction ii = (InvokeInstruction) i;
			if (ii.getMethodName(clazz.getConstantPoolGen()).equals("<init>")) {
				if (canMatch(Shadow.ConstructorCall))
					match(
							BcelShadow.makeConstructorCall(world, mg, ih, enclosingShadow),
							shadowAccumulator);
			} else if (ii instanceof INVOKESPECIAL) {
				String onTypeName = ii.getClassName(cpg);
				if (onTypeName.equals(mg.getEnclosingClass().getName())) {
					// we are private
					matchInvokeInstruction(mg, ih, ii, enclosingShadow, shadowAccumulator);
				} else {
					// we are a super call, and this is not a join point in AspectJ-1.{0,1}
				}
			} else {
					matchInvokeInstruction(mg, ih, ii, enclosingShadow, shadowAccumulator);
			}
		} else if (world.isJoinpointArrayConstructionEnabled() && 
				   (i instanceof NEWARRAY || i instanceof ANEWARRAY || i instanceof MULTIANEWARRAY)) {
			if (canMatch(Shadow.ConstructorCall)) {
				boolean debug = false;
				if (debug) System.err.println("Found new array instruction: "+i);
				if (i instanceof ANEWARRAY) {
					ANEWARRAY arrayInstruction = (ANEWARRAY)i;
					ObjectType arrayType = arrayInstruction.getLoadClassType(clazz.getConstantPoolGen());
					if (debug) System.err.println("Array type is "+arrayType);
					BcelShadow ctorCallShadow = BcelShadow.makeArrayConstructorCall(world,mg,ih,enclosingShadow);
					match(ctorCallShadow,shadowAccumulator);
				} else if (i instanceof NEWARRAY) {
					NEWARRAY arrayInstruction = (NEWARRAY)i;
					Type arrayType = arrayInstruction.getType();
					if (debug) System.err.println("Array type is "+arrayType);
					BcelShadow ctorCallShadow = BcelShadow.makeArrayConstructorCall(world,mg,ih,enclosingShadow);
					match(ctorCallShadow,shadowAccumulator);
				} else if (i instanceof MULTIANEWARRAY) {
					MULTIANEWARRAY arrayInstruction = (MULTIANEWARRAY)i;
					ObjectType arrayType = arrayInstruction.getLoadClassType(clazz.getConstantPoolGen());
					if (debug) System.err.println("Array type is "+arrayType);
					BcelShadow ctorCallShadow = BcelShadow.makeArrayConstructorCall(world,mg,ih,enclosingShadow);
					match(ctorCallShadow,shadowAccumulator);
				}
			}
// see pr77166 if you are thinking about implementing this
//		} else if (i instanceof AALOAD ) {
//			AALOAD arrayLoad = (AALOAD)i;
//			Type arrayType = arrayLoad.getType(clazz.getConstantPoolGen());
//			BcelShadow arrayLoadShadow = BcelShadow.makeArrayLoadCall(world,mg,ih,enclosingShadow);
//			match(arrayLoadShadow,shadowAccumulator);
//		} else if (i instanceof AASTORE) {
//			// ... magic required
		} else if ( world.isJoinpointSynchronizationEnabled() &&
				   ((i instanceof MONITORENTER) || (i instanceof MONITOREXIT))) {
			// if (canMatch(Shadow.Monitoring)) {
			  if (i instanceof MONITORENTER) {
				  BcelShadow monitorEntryShadow = BcelShadow.makeMonitorEnter(world,mg,ih,enclosingShadow);
				  match(monitorEntryShadow,shadowAccumulator);
			  } else {
				  BcelShadow monitorExitShadow = BcelShadow.makeMonitorExit(world,mg,ih,enclosingShadow);
				  match(monitorExitShadow,shadowAccumulator);
			  }
			// }
		}
		// performance optimization... we only actually care about ASTORE instructions, 
		// since that's what every javac type thing ever uses to start a handler, but for
		// now we'll do this for everybody.
		if (!canMatch(Shadow.ExceptionHandler)) return;
		if (Range.isRangeHandle(ih)) return;
		InstructionTargeter[] targeters = ih.getTargeters();
		if (targeters != null) {
			for (int j = 0; j < targeters.length; j++) {
				InstructionTargeter t = targeters[j];
				if (t instanceof ExceptionRange) {
					// assert t.getHandler() == ih
					ExceptionRange er = (ExceptionRange) t;
					if (er.getCatchType() == null) continue;
					if (isInitFailureHandler(ih)) return;
					
					match(
						BcelShadow.makeExceptionHandler(
							world, 
							er,
							mg, ih, enclosingShadow),
						shadowAccumulator);
				}
			}
		}
	}

	private boolean isInitFailureHandler(InstructionHandle ih) {
		// Skip the astore_0 and aload_0 at the start of the handler and 
		// then check if the instruction following these is 
		// 'putstatic ajc$initFailureCause'.  If it is then we are 
		// in the handler we created in AspectClinit.generatePostSyntheticCode()
		InstructionHandle twoInstructionsAway = ih.getNext().getNext();
		if (twoInstructionsAway.getInstruction() instanceof PUTSTATIC) {
			String name = ((PUTSTATIC)twoInstructionsAway.getInstruction()).getFieldName(cpg);
			if (name.equals(NameMangler.INITFAILURECAUSE_FIELD_NAME)) return true;
		}
		return false;
	}


	private void matchSetInstruction(
		LazyMethodGen mg,
		InstructionHandle ih,
		BcelShadow enclosingShadow,
		List shadowAccumulator) {
		FieldInstruction fi = (FieldInstruction) ih.getInstruction();
		Member field = BcelWorld.makeFieldJoinPointSignature(clazz, fi);
		
		// synthetic fields are never join points
		if (field.getName().startsWith(NameMangler.PREFIX)) return;
		
		ResolvedMember resolvedField = field.resolve(world);
		if (resolvedField == null) {
			// we can't find the field, so it's not a join point.
			return;
		} else if (
			Modifier.isFinal(resolvedField.getModifiers())
				&& Utility.isConstantPushInstruction(ih.getPrev().getInstruction())) {
			// it's the set of a final constant, so it's
			// not a join point according to 1.0.6 and 1.1.
			return;
		} else if (resolvedField.isSynthetic()) {
			// sets of synthetics aren't join points in 1.1
			return;
		} else {
			// Fix for bug 172107 (similar the "get" fix for bug 109728)
			BcelShadow bs=
				BcelShadow.makeFieldSet(world, resolvedField, mg, ih, enclosingShadow);
			String cname = fi.getClassName(cpg);
			if (!resolvedField.getDeclaringType().getName().equals(cname)) {
				bs.setActualTargetType(cname);
			}
			match(bs, shadowAccumulator);
		}
	}

	private void matchGetInstruction(LazyMethodGen mg, InstructionHandle ih, BcelShadow enclosingShadow, List shadowAccumulator) {
		FieldInstruction fi = (FieldInstruction) ih.getInstruction();
		Member field = BcelWorld.makeFieldJoinPointSignature(clazz, fi);

		// synthetic fields are never join points
		if (field.getName().startsWith(NameMangler.PREFIX)) return;
		
		ResolvedMember resolvedField = field.resolve(world);
		if (resolvedField == null) {
			// we can't find the field, so it's not a join point.
			return;
		} else if (resolvedField.isSynthetic()) {
			// sets of synthetics aren't join points in 1.1
			return;
		} else {
			BcelShadow bs = BcelShadow.makeFieldGet(world,resolvedField,mg,ih,enclosingShadow);
			String cname = fi.getClassName(cpg);
			if (!resolvedField.getDeclaringType().getName().equals(cname)) {
				bs.setActualTargetType(cname);
			}
			match(bs, shadowAccumulator);
		}
	}
	
	/**
     * For some named resolved type, this method looks for a member with a particular name -
     * it should only be used when you truly believe there is only one member with that 
     * name in the type as it returns the first one it finds.
     */
	private ResolvedMember findResolvedMemberNamed(ResolvedType type,String methodName) {
		ResolvedMember[] allMethods = type.getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			ResolvedMember member = allMethods[i];
			if (member.getName().equals(methodName)) return member;
		}
		return null;
	}
	
	/**
	 * For a given resolvedmember, this will discover the real annotations for it.
	 * <b>Should only be used when the resolvedmember is the contents of an effective signature
	 * attribute, as thats the only time when the annotations aren't stored directly in the
	 * resolvedMember</b>
	 * @param rm the sig we want it to pretend to be 'int A.m()' or somesuch ITD like thing
	 * @param declaredSig the real sig 'blah.ajc$xxx'
	 */
	private void fixAnnotationsForResolvedMember(ResolvedMember rm,ResolvedMember declaredSig) {
	  try {
		UnresolvedType memberHostType = declaredSig.getDeclaringType();
		ResolvedType[] annotations = (ResolvedType[])mapToAnnotations.get(rm);
		String methodName = declaredSig.getName();
		// FIXME asc shouldnt really rely on string names !
		if (annotations == null) {
			if (rm.getKind()==Member.FIELD) {
				if (methodName.startsWith("ajc$inlineAccessField")) {
					ResolvedMember resolvedDooberry = world.resolve(rm);
					annotations = resolvedDooberry.getAnnotationTypes();
				} else {
					ResolvedMember realthing = AjcMemberMaker.interFieldInitializer(rm,memberHostType);
					ResolvedMember resolvedDooberry = world.resolve(realthing);
					annotations = resolvedDooberry.getAnnotationTypes();
				}
			} else if (rm.getKind()==Member.METHOD && !rm.isAbstract()) {
				if (methodName.startsWith("ajc$inlineAccessMethod") || methodName.startsWith("ajc$superDispatch")) {
					ResolvedMember resolvedDooberry = world.resolve(declaredSig);
					annotations = resolvedDooberry.getAnnotationTypes();
				} else {
					ResolvedMember realthing = AjcMemberMaker.interMethodDispatcher(rm.resolve(world),memberHostType).resolve(world);
					// ResolvedMember resolvedDooberry = world.resolve(realthing);
					ResolvedMember theRealMember = findResolvedMemberNamed(memberHostType.resolve(world),realthing.getName());
					// AMC temp guard for M4
					if (theRealMember == null) {
						throw new UnsupportedOperationException("Known limitation in M4 - can't find ITD members when type variable is used as an argument and has upper bound specified");
					}
					annotations = theRealMember.getAnnotationTypes();
				}
			} else if (rm.getKind()==Member.CONSTRUCTOR) {
				ResolvedMember realThing = AjcMemberMaker.postIntroducedConstructor(memberHostType.resolve(world),rm.getDeclaringType(),rm.getParameterTypes());
				ResolvedMember resolvedDooberry = world.resolve(realThing);
				// AMC temp guard for M4
				if (resolvedDooberry == null) {
					throw new UnsupportedOperationException("Known limitation in M4 - can't find ITD members when type variable is used as an argument and has upper bound specified");
				}
				annotations = resolvedDooberry.getAnnotationTypes();
			}
			if (annotations == null) 
		      annotations = new ResolvedType[0];
			mapToAnnotations.put(rm,annotations);
		}
		rm.setAnnotationTypes(annotations);
		} 
	  	catch (UnsupportedOperationException ex) {
	  	  throw ex;	
	  	} catch (Throwable t) {
		  //FIXME asc remove this catch after more testing has confirmed the above stuff is OK
		  throw new BCException("Unexpectedly went bang when searching for annotations on "+rm,t);
		}
	}


	private void matchInvokeInstruction(LazyMethodGen mg,
		InstructionHandle ih,
		InvokeInstruction invoke,
		BcelShadow enclosingShadow,
		List shadowAccumulator) 
	{
		String methodName = invoke.getName(cpg);
		if (methodName.startsWith(NameMangler.PREFIX)) {
			Member jpSig =
				world.makeJoinPointSignatureForMethodInvocation(clazz, invoke);
			ResolvedMember declaredSig = jpSig.resolve(world);
			//System.err.println(method + ", declaredSig: "  +declaredSig);
			if (declaredSig == null) return;
			
			if (declaredSig.getKind() == Member.FIELD) {
				Shadow.Kind kind;
				if (jpSig.getReturnType().equals(ResolvedType.VOID)) {
					kind = Shadow.FieldSet;
				} else {
					kind = Shadow.FieldGet;
				}
				
				if (canMatch(Shadow.FieldGet) || canMatch(Shadow.FieldSet))
					match(BcelShadow.makeShadowForMethodCall(world, mg, ih, enclosingShadow,
							kind, declaredSig),
							shadowAccumulator);
			} else {
				AjAttribute.EffectiveSignatureAttribute effectiveSig =
					declaredSig.getEffectiveSignature();
				if (effectiveSig == null) return;
				//System.err.println("call to inter-type member: " + effectiveSig);
				if (effectiveSig.isWeaveBody()) return;

				
			    ResolvedMember rm = effectiveSig.getEffectiveSignature();
				
				fixAnnotationsForResolvedMember(rm,declaredSig); // abracadabra
			  
				if (canMatch(effectiveSig.getShadowKind())) 
					match(BcelShadow.makeShadowForMethodCall(world, mg, ih, enclosingShadow,
							effectiveSig.getShadowKind(), rm), shadowAccumulator);
			}
		} else {
			if (canMatch(Shadow.MethodCall))
				match(
						BcelShadow.makeMethodCall(world, mg, ih, enclosingShadow),
						shadowAccumulator);
		}
	}
	
	// static ... so all worlds will share the config for the first one created...
	private static boolean checkedXsetForLowLevelContextCapturing = false;
	private static boolean captureLowLevelContext = false;
	
    private boolean match(BcelShadow shadow, List shadowAccumulator) {
    	//System.err.println("match: " + shadow);
    	if (captureLowLevelContext) { // duplicate blocks - one with context capture, one without, seems faster than multiple 'ifs()'
	    	ContextToken shadowMatchToken = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.MATCHING_SHADOW, shadow);
	        boolean isMatched = false;
	        for (Iterator i = shadowMungers.iterator(); i.hasNext(); ) {
	            ShadowMunger munger = (ShadowMunger)i.next();
	            ContextToken mungerMatchToken = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.MATCHING_POINTCUT, munger.getPointcut());
	            if (munger.match(shadow, world)) {
					WeaverMetrics.recordMatchResult(true);// Could pass: munger
	                shadow.addMunger(munger);
	                isMatched = true;
				    if (shadow.getKind() == Shadow.StaticInitialization) {
					  clazz.warnOnAddedStaticInitializer(shadow,munger.getSourceLocation());
				    }
	            } else {
	            	WeaverMetrics.recordMatchResult(false); // Could pass: munger
	        	}
	            CompilationAndWeavingContext.leavingPhase(mungerMatchToken);
	        }       
	
	        if (isMatched) shadowAccumulator.add(shadow);
	        CompilationAndWeavingContext.leavingPhase(shadowMatchToken);
	        return isMatched;
    	} else {
	        boolean isMatched = false;
	        for (Iterator i = shadowMungers.iterator(); i.hasNext(); ) {
	            ShadowMunger munger = (ShadowMunger)i.next();
	            if (munger.match(shadow, world)) {
	                shadow.addMunger(munger);
	                isMatched = true;
				    if (shadow.getKind() == Shadow.StaticInitialization) {
					  clazz.warnOnAddedStaticInitializer(shadow,munger.getSourceLocation());
				    }
	        	}
	        }       	
	        if (isMatched) shadowAccumulator.add(shadow);
	        return isMatched;
    	}
    }
    
    // ----
    
    private void implement(LazyMethodGen mg) {
    	List shadows = mg.matchedShadows;
    	if (shadows == null) return;
        // We depend on a partial order such that inner shadows are earlier on the list
        // than outer shadows.  That's fine.  This order is preserved if:
        
        // A preceeds B iff B.getStart() is LATER THAN A.getStart().
        
        for (Iterator i = shadows.iterator(); i.hasNext(); ) {
            BcelShadow shadow = (BcelShadow)i.next();
            ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.IMPLEMENTING_ON_SHADOW,shadow);
            shadow.implement();
            CompilationAndWeavingContext.leavingPhase(tok);
        }
		int ii = mg.getMaxLocals();
		mg.matchedShadows = null;
    }
    
    // ----
    
	public LazyClassGen getLazyClassGen() {
		return clazz;
	}

	public List getShadowMungers() {
		return shadowMungers;
	}

	public BcelWorld getWorld() {
		return world;
	}
	
	// Called by the BcelWeaver to let us know all BcelClassWeavers need to collect reweavable info
	public static void setReweavableMode(boolean mode) {
		inReweavableMode = mode;
	}
	
	public static boolean getReweavableMode() { 
		return inReweavableMode;
	}
	
	public String toString() {
		return "BcelClassWeaver instance for : "+clazz;
	}
    

}
