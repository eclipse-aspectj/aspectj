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


package org.aspectj.weaver.bcel;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.IndexedInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.RET;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.Type;
import org.aspectj.bridge.IMessage;
import org.aspectj.util.PartialOrder;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.IClassWeaver;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.NewFieldTypeMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.WeaverStateKind;

class BcelClassWeaver implements IClassWeaver {
    
    /**
     * This is called from {@link BcelWeaver} to perform the per-class weaving process.
     */
	public static boolean weave(
		BcelWorld world,
		LazyClassGen clazz,
		List shadowMungers,
		List typeMungers) 
	{
		boolean b =  new BcelClassWeaver(world, clazz, shadowMungers, typeMungers).weave();
		//System.err.println(clazz.getClassName() + ", " + clazz.getWeaverState());
		//clazz.print();
		return b;
	}
	
	// --------------------------------------------
    
    private final LazyClassGen clazz;
    private final List         shadowMungers;
    private final List         typeMungers;

    private final BcelObjectType  ty;    // alias of clazz.getType()
    private final BcelWorld       world; // alias of ty.getWorld()
    private final ConstantPoolGen cpg;   // alias of clazz.getConstantPoolGen()
    private final InstructionFactory fact; // alias of clazz.getFactory();

    
    private final List        addedLazyMethodGens           = new ArrayList();
    private final Set         addedDispatchTargets          = new HashSet();
    
    
    private        List addedSuperInitializersAsList = null; // List<IfaceInitList>
    private final Map  addedSuperInitializers = new HashMap(); // Interface -> IfaceInitList
    private        List addedThisInitializers  = new ArrayList(); // List<NewFieldMunger>
    private        List addedClassInitializers  = new ArrayList(); // List<NewFieldMunger>
    
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
		List typeMungers) 
	{
		super();
		// assert world == clazz.getType().getWorld()
		this.world = world;
		this.clazz = clazz;
		this.shadowMungers = shadowMungers;
		this.typeMungers = typeMungers;
		this.ty = clazz.getBcelObjectType();
		this.cpg = clazz.getConstantPoolGen();
		this.fact = clazz.getFactory();
		initializeSuperInitializerMap(ty.getResolvedTypeX());
	} 
    
    // --------------------------------------------
   
   	private void initializeSuperInitializerMap(ResolvedTypeX child) {
		ResolvedTypeX[] superInterfaces = child.getDeclaredInterfaces();
		for (int i=0, len=superInterfaces.length; i < len; i++) {
			if (ty.getResolvedTypeX().isTopmostImplementor(superInterfaces[i])) {
				if (addSuperInitializer(superInterfaces[i])) {
					initializeSuperInitializerMap(superInterfaces[i]);
				}
			}
		}
	}

	private boolean addSuperInitializer(ResolvedTypeX onType) {
		IfaceInitList l = (IfaceInitList) addedSuperInitializers.get(onType);
		if (l != null) return false;
		l = new IfaceInitList(onType);
		addedSuperInitializers.put(onType, l);
		return true;
	}
   
	public void addInitializer(ConcreteTypeMunger cm) {
		NewFieldTypeMunger m = (NewFieldTypeMunger) cm.getMunger();
		ResolvedTypeX onType = m.getSignature().getDeclaringType().resolve(world);
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
    	final ResolvedTypeX onType;
    	List list = new ArrayList();
    	IfaceInitList(ResolvedTypeX onType) {
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
    
    // ----
    
    public boolean weave() {
        if (clazz.getWeaverState().isWoven()) {
        	world.showMessage(IMessage.ERROR, 
				"class \'" + clazz.getType().getName() + "\' is already woven",
				ty.getSourceLocation(), null);
        	return false;
        }
        
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
        	isChanged |= munger.munge(this);
        }
        
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
            isChanged |= match(mg);
        }
        if (! isChanged) return false;
        
        // now we weave all but the initialization shadows
		for (Iterator i = methodGens.iterator(); i.hasNext();) {
			LazyMethodGen mg = (LazyMethodGen)i.next();
			if (! mg.hasBody()) continue;
			implement(mg);
		}
			
        
        // if we matched any initialization shadows, we inline and weave
		if (! initializationShadows.isEmpty()) {
			inlineSelfConstructors(methodGens);
			positionAndImplement(initializationShadows);
		}
		
		
		// finally, if we changed, we add in the introduced methods.
        if (isChanged) {
        	clazz.setWeaverState(WeaverStateKind.Woven);
			weaveInAddedMethods();
        }
        
        return isChanged;
    }


	private void inlineSelfConstructors(List methodGens) {
		for (Iterator i = methodGens.iterator(); i.hasNext();) {
			LazyMethodGen mg = (LazyMethodGen) i.next();
			if (! mg.getName().equals("<init>")) continue;
			InstructionHandle ih = findSuperOrThisCall(mg);
			if (ih != null && isThisCall(ih)) {
				LazyMethodGen donor = getCalledMethod(ih);
				inlineMethod(donor, mg, ih);
			}
		}
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
		InstructionHandle end = footer.append(fact.NOP);

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
			Instruction fresh = src.getInstruction().copy();
			InstructionHandle dest;
			if (fresh instanceof CPInstruction) {
				// need to reset index to go to new constant pool.  This is totally
				// a computation leak... we're testing this LOTS of times.  Sigh.
				if (isAcrossClass) {
					CPInstruction cpi = (CPInstruction) fresh;
					cpi.setIndex(recipientCpg.addConstant(donorCpg.getConstant(cpi.getIndex()), donorCpg));
				}
			}
			if (src.getInstruction() == Range.RANGEINSTRUCTION) {
				dest = ret.append(Range.RANGEINSTRUCTION);
			} else if (fresh instanceof ReturnInstruction) {
				if (keepReturns) {
					dest = ret.append(fresh);
				} else {
					dest = ret.append(fact.createBranchInstruction(Constants.GOTO, end));
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
			ret.insert(fact.createStore(Type.OBJECT, targetSlot));
			frameEnv.put(donorFramePos, targetSlot);
			donorFramePos += 1;
		}
		Type[] argTypes = donor.getArgumentTypes();
		for (int i = 0, len = argTypes.length; i < len; i++) {
			Type argType = argTypes[i];
			int argSlot = recipient.allocateLocal(argType);
			ret.insert(fact.createStore(argType, argSlot));
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
		setup.append(fact.createDup(1));
		setup.append(fact.createInvoke(
			aspectName, 
			"<init>", 
			Type.VOID, 
			new Type[0], 
			Constants.INVOKESPECIAL));
		setup.append(fact.createFieldAccess(aspectName, field.getName(), aspectType, Constants.PUTSTATIC));
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
		// we want to match ajsynthetic constructors...
		if (mg.getName().equals("<init>")) {
			// XXX the enclosing join point is wrong for things before ignoreMe.
			InstructionHandle superOrThisCall = findSuperOrThisCall(mg);

			// we don't walk bodies of things where it's a wrong constructor thingie
			if (superOrThisCall == null) return false;

			enclosingShadow = BcelShadow.makeConstructorExecution(world, mg, superOrThisCall);
			
			// walk the body
			boolean beforeSuperOrThisCall = true;
			if (shouldWeaveBody(mg)) { //!mg.isAjSynthetic()) {
				for (InstructionHandle h = mg.getBody().getStart();
					h != null;
					h = h.getNext()) {
					if (h == superOrThisCall) {
						beforeSuperOrThisCall = false;
						continue;
					}
					match(mg, h, beforeSuperOrThisCall ? null : enclosingShadow, shadowAccumulator);
				}
				match(enclosingShadow, shadowAccumulator);
			}
			
			// XXX we don't do pre-inits of interfaces
			
			// now add interface inits and cexecs
			if (superOrThisCall != null && ! isThisCall(superOrThisCall)) {
				InstructionHandle curr = enclosingShadow.getRange().getStart();
				for (Iterator i = addedSuperInitializersAsList.iterator(); i.hasNext(); ) {
					IfaceInitList l = (IfaceInitList) i.next();
					// generate the cexec jp
					Member ifaceInitSig = AjcMemberMaker.interfaceConstructor(l.onType);
					BcelShadow cexecShadow =
						BcelShadow.makeIfaceConstructorExecution(
							world,
							mg,
							curr,
							ifaceInitSig);
					if (match(cexecShadow, shadowAccumulator)) {
						cexecShadow.getRange().getBody().append(cexecShadow.getRange().getStart(), fact.NOP);
					}
					// generate the init jp around it
					BcelShadow initShadow =
						BcelShadow.makeIfaceInitialization(
							world,
							mg,
							cexecShadow,
							ifaceInitSig);
					match(initShadow, shadowAccumulator);
					// insert code in place
					InstructionList inits = genInitInstructions(l.list, false);
					initShadow.getRange().insert(inits, Range.InsideAfter);
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
		} else if (!shouldWeaveBody(mg)) { //.isAjSynthetic()) {
			return false;			
		} else {
			if (mg.getName().equals("<clinit>")) {
				clinitShadow = enclosingShadow = BcelShadow.makeStaticInitialization(world, mg);
				//System.err.println(enclosingShadow);
			} else if (mg.isAdviceMethod()) {
				enclosingShadow = BcelShadow.makeAdviceExecution(world, mg);
			} else {
				AjAttribute.EffectiveSignatureAttribute effective = mg.getEffectiveSignature();
				if (effective == null) {
					enclosingShadow = BcelShadow.makeMethodExecution(world, mg);
				} else if (effective.isWeaveBody()) {
					enclosingShadow = BcelShadow.makeShadowForMethod(world, mg, effective.getShadowKind(), effective.getEffectiveSignature());
				} else {
					return false;
				}
			}

			for (InstructionHandle h = mg.getBody().getStart();
				h != null;
				h = h.getNext()) {
				match(mg, h, enclosingShadow, shadowAccumulator);
			}
			match(enclosingShadow, shadowAccumulator);
			mg.matchedShadows = shadowAccumulator;
			return !shadowAccumulator.isEmpty();
		}
	}

	private boolean shouldWeaveBody(LazyMethodGen mg) {
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
			if (!isStatic) ret.append(fact.ALOAD_0);
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
		if (i instanceof FieldInstruction) {
			FieldInstruction fi = (FieldInstruction) i;
			

			
			if (i instanceof PUTFIELD || i instanceof PUTSTATIC) {
				// check for sets of constant fields.  We first check the previous 
				// instruction.  If the previous instruction is a LD_WHATEVER (push
				// constant on the stack) then we must resolve the field to determine
				// if it's final.  If it is final, then we don't generate a shadow.
				InstructionHandle prevHandle = ih.getPrev();
				Instruction prevI = prevHandle.getInstruction();
				if (Utility.isConstantPushInstruction(prevI)) {
					Member field = world.makeFieldSignature(clazz, (FieldInstruction) i);
					ResolvedMember resolvedField = field.resolve(world);
					if (resolvedField == null) {
						// we can't find the field, so it's not a join point.
					} else if (Modifier.isFinal(resolvedField.getModifiers())) {
						// it's final, so it's the set of a final constant, so it's
						// not a join point according to 1.0.6 and 1.1.
					} else {
						match(
							BcelShadow.makeFieldSet(world, mg, ih, enclosingShadow),
							shadowAccumulator);
					}						
				} else {
					match(
						BcelShadow.makeFieldSet(world, mg, ih, enclosingShadow),
						shadowAccumulator);
				}
			} else {
				match(
					BcelShadow.makeFieldGet(world, mg, ih, enclosingShadow),
					shadowAccumulator);
			}
		} else if (i instanceof InvokeInstruction) {
			InvokeInstruction ii = (InvokeInstruction) i;
			if (ii.getMethodName(clazz.getConstantPoolGen()).equals("<init>")) {
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
		} 
		// performance optimization... we only actually care about ASTORE instructions, 
		// since that's what every javac type thing ever uses to start a handler, but for
		// now we'll do this for everybody.
		if (Range.isRangeHandle(ih)) return;
		InstructionTargeter[] targeters = ih.getTargeters();
		if (targeters != null) {
			for (int j = 0; j < targeters.length; j++) {
				InstructionTargeter t = targeters[j];
				if (t instanceof ExceptionRange) {
					// assert t.getHandler() == ih
					ExceptionRange er = (ExceptionRange) t;
					if (er.getCatchType() == null) continue;
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

	private void matchInvokeInstruction(LazyMethodGen mg,
		InstructionHandle ih,
		InvokeInstruction invoke,
		BcelShadow enclosingShadow,
		List shadowAccumulator) 
	{
		String methodName = invoke.getName(cpg);
		if (methodName.startsWith(NameMangler.PREFIX)) {
			Member method =
				world.makeMethodSignature(clazz, invoke);
			ResolvedMember declaredSig = method.resolve(world);
			//System.err.println(method + ", declaredSig: "  +declaredSig);
			if (declaredSig == null) return;
			
			if (declaredSig.getKind() == Member.FIELD) {
				Shadow.Kind kind;
				if (method.getReturnType().equals(ResolvedTypeX.VOID)) {
					kind = Shadow.FieldSet;
				} else {
					kind = Shadow.FieldGet;
				}
				
				match(BcelShadow.makeShadowForMethodCall(world, mg, ih, enclosingShadow,
						kind, declaredSig),
					shadowAccumulator);
			} else {
				AjAttribute.EffectiveSignatureAttribute effectiveSig = declaredSig.getEffectiveSignature();
				if (effectiveSig == null) return;
				//System.err.println("call to inter-type member: " + effectiveSig);
				if (effectiveSig.isWeaveBody()) return;
				match(BcelShadow.makeShadowForMethodCall(world, mg, ih, enclosingShadow,
						effectiveSig.getShadowKind(), effectiveSig.getEffectiveSignature()),
					shadowAccumulator);
			}
		} else {
			match(
				BcelShadow.makeMethodCall(world, mg, ih, enclosingShadow),
				shadowAccumulator);
		}
	}
	
    private boolean match(BcelShadow shadow, List shadowAccumulator) {
    	//System.err.println("match: " + shadow);
        boolean isMatched = false;
        for (Iterator i = shadowMungers.iterator(); i.hasNext(); ) {
            ShadowMunger munger = (ShadowMunger)i.next();
            if (munger.match(shadow, world)) {
                shadow.addMunger(munger);
                isMatched = true;
            }
        }       
        if (isMatched) shadowAccumulator.add(shadow);
        return isMatched;
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
            shadow.implement();
        }
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

}
