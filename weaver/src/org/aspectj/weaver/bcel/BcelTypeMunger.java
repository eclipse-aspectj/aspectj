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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.generic.FieldGen;
import org.aspectj.apache.bcel.generic.INVOKESPECIAL;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.WeaveMessage;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.AsmRelationshipProvider;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.NewConstructorTypeMunger;
import org.aspectj.weaver.NewFieldTypeMunger;
import org.aspectj.weaver.NewMethodTypeMunger;
import org.aspectj.weaver.NewParentTypeMunger;
import org.aspectj.weaver.PerObjectInterfaceTypeMunger;
//import org.aspectj.weaver.PerTypeWithinTargetTypeMunger;
import org.aspectj.weaver.PrivilegedAccessMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.patterns.Pointcut;


//XXX addLazyMethodGen is probably bad everywhere
public class BcelTypeMunger extends ConcreteTypeMunger {

	public BcelTypeMunger(ResolvedTypeMunger munger, ResolvedTypeX aspectType) {
		super(munger, aspectType);
	}

	public String toString() {
		return "(BcelTypeMunger " + getMunger() + ")";
	}

	public boolean munge(BcelClassWeaver weaver) {
		boolean changed = false;
		boolean worthReporting = true;
		
		if (munger.getKind() == ResolvedTypeMunger.Field) {
			changed = mungeNewField(weaver, (NewFieldTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.Method) {
			changed = mungeNewMethod(weaver, (NewMethodTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.PerObjectInterface) {
			changed = mungePerObjectInterface(weaver, (PerObjectInterfaceTypeMunger)munger);
			worthReporting = false;
		} else if (munger.getKind() == ResolvedTypeMunger.PerTypeWithinInterface) {
			// PTWIMPL Transform the target type (add the aspect instance field)
			changed = mungePerTypeWithinTransformer(weaver);
			worthReporting = false;
		} else if (munger.getKind() == ResolvedTypeMunger.PrivilegedAccess) {
			changed = mungePrivilegedAccess(weaver, (PrivilegedAccessMunger)munger);
			worthReporting = false;
		} else if (munger.getKind() == ResolvedTypeMunger.Constructor) {
			changed = mungeNewConstructor(weaver, (NewConstructorTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.Parent) {
			changed = mungeNewParent(weaver, (NewParentTypeMunger)munger);
		} else {
			throw new RuntimeException("unimplemented");
		}
		
		if (changed && munger.changesPublicSignature()) {
			WeaverStateInfo info = 
				weaver.getLazyClassGen().getOrCreateWeaverStateInfo();
			info.addConcreteMunger(this);
		}
		// Whilst type mungers aren't persisting their source locations, we add this relationship during
		// compilation time (see other reference to ResolvedTypeMunger.persist)
		if (ResolvedTypeMunger.persistSourceLocation) {
			if (changed) {
				if (munger.getKind().equals(ResolvedTypeMunger.Parent)) {
			  	  AsmRelationshipProvider.getDefault().addRelationship(weaver.getLazyClassGen().getType(), munger,getAspectType());
				} else {
			  	  AsmRelationshipProvider.getDefault().addRelationship(weaver.getLazyClassGen().getType(), munger,getAspectType());	
				}
			}
		}
		
		// TAG: WeavingMessage
		if (changed && worthReporting  && munger!=null && !weaver.getWorld().getMessageHandler().isIgnoring(IMessage.WEAVEINFO)) {
			String tName = weaver.getLazyClassGen().getType().getSourceLocation().getSourceFile().getName();
			if (tName.indexOf("no debug info available")!=-1) tName = "no debug info available";
			else tName = getShortname(weaver.getLazyClassGen().getType().getSourceLocation().getSourceFile().getPath());
			String fName = getShortname(getAspectType().getSourceLocation().getSourceFile().getPath());
        	if (munger.getKind().equals(ResolvedTypeMunger.Parent)) {
        		// This message could come out of AjLookupEnvironment.addParent if doing parents
        		// munging at compile time only...
        		NewParentTypeMunger parentTM = (NewParentTypeMunger)munger;
        		if (parentTM.getNewParent().isInterface()) {
					weaver.getWorld().getMessageHandler().handleMessage(WeaveMessage.constructWeavingMessage(WeaveMessage.WEAVEMESSAGE_DECLAREPARENTSIMPLEMENTS,
					new String[]{weaver.getLazyClassGen().getType().getName(),
					tName,parentTM.getNewParent().getName(),fName},
					weaver.getLazyClassGen().getClassName(), getAspectType().getName()));
        		} else {
                    weaver.getWorld().getMessageHandler().handleMessage(
                    WeaveMessage.constructWeavingMessage(WeaveMessage.WEAVEMESSAGE_DECLAREPARENTSEXTENDS,
                        new String[]{weaver.getLazyClassGen().getType().getName(),
                            tName,parentTM.getNewParent().getName(),fName
                                }));
//                  TAG: WeavingMessage    DECLARE PARENTS: EXTENDS
//                  reportDeclareParentsMessage(WeaveMessage.WEAVEMESSAGE_DECLAREPARENTSEXTENDS,sourceType,parent);
                    
        		}
        	} else {
        		weaver.getWorld().getMessageHandler().handleMessage(WeaveMessage.constructWeavingMessage(WeaveMessage.WEAVEMESSAGE_ITD,
        		new String[]{weaver.getLazyClassGen().getType().getName(),
        			         tName,munger.getKind().toString().toLowerCase(),
        			         getAspectType().getName(),
        					 fName+":'"+munger.getSignature()+"'"},
							 weaver.getLazyClassGen().getClassName(), getAspectType().getName()));
        	}	
		}
		
		return changed;
	}
	
	private String getShortname(String path)  {
		int takefrom = path.lastIndexOf('/');
		if (takefrom == -1) {
			takefrom = path.lastIndexOf('\\');
		}
		return path.substring(takefrom+1);
	}

	/** 
     * For a long time, AspectJ did not allow binary weaving of declare parents.  This restriction is now lifted
     * but could do with more testing!
	 */
	private boolean mungeNewParent(BcelClassWeaver weaver, NewParentTypeMunger munger) {
		LazyClassGen  newParentTarget = weaver.getLazyClassGen();
		ResolvedTypeX newParent       = munger.getNewParent();
        
        boolean cont = true; // Set to false when we error, so we don't actually *do* the munge           
        cont = enforceDecpRule1_abstractMethodsImplemented(weaver, munger.getSourceLocation(),newParentTarget, newParent);
        cont = enforceDecpRule2_cantExtendFinalClass(weaver,munger.getSourceLocation(),newParentTarget,newParent) && cont;
                
        List methods = newParent.getMethodsWithoutIterator();
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
		  BcelMethod    superMethod = (BcelMethod) iter.next();
          if (!superMethod.getName().equals("<init>")) {
		    LazyMethodGen   subMethod = findMatchingMethod(newParentTarget, superMethod);
            if (subMethod!=null) {
              cont = enforceDecpRule3_visibilityChanges(weaver, newParent, superMethod, subMethod) && cont;
              cont = enforceDecpRule4_compatibleReturnTypes(weaver, superMethod, subMethod)        && cont;
              cont = enforceDecpRule5_cantChangeFromStaticToNonstatic(weaver,munger.getSourceLocation(),superMethod,subMethod) && cont;
            }                
          }
        }
        if (!cont) return false; // A rule was violated and an error message already reported
             
        if (newParent.isClass()) { // Changing the supertype
             if (!attemptToModifySuperCalls(weaver,newParentTarget,newParent)) return false;
             newParentTarget.setSuperClass(newParent);
		} else { // Adding a new interface
			newParentTarget.addInterface(newParent,getSourceLocation());
		}
		return true;
	}


    /**
     * Rule 1: For the declare parents to be allowed, the target type must override and implement 
     *         inherited abstract methods (if the type is not declared abstract)
     */
    private boolean enforceDecpRule1_abstractMethodsImplemented(BcelClassWeaver weaver, ISourceLocation mungerLoc,LazyClassGen newParentTarget, ResolvedTypeX newParent) {
        boolean ruleCheckingSucceeded = true;
        if (!(newParentTarget.isAbstract() || newParentTarget.isInterface())) { // Ignore abstract classes or interfaces
            List methods = newParent.getMethodsWithoutIterator();
            for (Iterator i = methods.iterator(); i.hasNext();) {
                BcelMethod o = (BcelMethod)i.next();
                if (o.isAbstract() && !o.getName().startsWith("ajc$interField")) { // Ignore abstract methods of ajc$interField prefixed methods
                    BcelMethod discoveredImpl = null;
                    List newParentTargetMethods = newParentTarget.getType().getMethodsWithoutIterator();
                    for (Iterator ii = newParentTargetMethods.iterator(); ii.hasNext() && discoveredImpl==null;) {
                        BcelMethod gen2 = (BcelMethod) ii.next();
                        if (gen2.getName().equals(o.getName()) && 
                            gen2.getParameterSignature().equals(o.getParameterSignature()) && !gen2.isAbstract()) {
                            discoveredImpl = gen2; // Found a valid implementation !
                        }
                    }     
                    if (discoveredImpl == null) {
                        // didnt find a valid implementation, lets check the ITDs on this type to see if they satisfy it
                        boolean satisfiedByITD = false;
                        for (Iterator ii = newParentTarget.getType().getInterTypeMungersIncludingSupers().iterator(); ii.hasNext(); ) {
                            ConcreteTypeMunger m = (ConcreteTypeMunger)ii.next();
                            if (m.getMunger() instanceof NewMethodTypeMunger) {
                            ResolvedMember sig = m.getSignature();
                            if (!Modifier.isAbstract(sig.getModifiers())) {
                                if (ResolvedTypeX
                                    .matches(
                                        AjcMemberMaker.interMethod(
                                            sig,m.getAspectType(),sig.getDeclaringType().isInterface(weaver.getWorld())),o)) {
                                    satisfiedByITD = true;
                                }
                            }
                            }
                        }
                        if (!satisfiedByITD) {
                          error(weaver,
                                "The type " + newParentTarget.getName() + " must implement the inherited abstract method "+o.getDeclaringType()+"."+o.getName()+o.getParameterSignature(),
                                newParentTarget.getType().getSourceLocation(),new ISourceLocation[]{o.getSourceLocation(),mungerLoc});
                          ruleCheckingSucceeded=false;
                        }
                    }
                }
            }
        }
        return ruleCheckingSucceeded;
    }
    
    /**
     * Rule 2. Can't extend final types
     */
	private boolean enforceDecpRule2_cantExtendFinalClass(BcelClassWeaver weaver, ISourceLocation mungerLoc, 
            LazyClassGen newParentTarget, ResolvedTypeX newParent) {
        if (newParent.isFinal()) {
            error(weaver,"Cannot make type "+newParentTarget.getName()+" extend final class "+newParent.getName(),
                  newParentTarget.getType().getSourceLocation(),
                  new ISourceLocation[]{mungerLoc});
            return false;
        }
		return true;
	}


	/**
     * Rule 3. Can't narrow visibility of methods when overriding
	 */
	private boolean enforceDecpRule3_visibilityChanges(BcelClassWeaver weaver, ResolvedTypeX newParent, BcelMethod superMethod, LazyMethodGen subMethod) {
        boolean cont = true;
		  if (superMethod.isPublic()) {
		    if (subMethod.isProtected() || subMethod.isDefault() || subMethod.isPrivate()) {
		      weaver.getWorld().getMessageHandler().handleMessage(MessageUtil.error(
		            "Cannot reduce the visibility of the inherited method '"+superMethod+"' from "+newParent.getName(),
		            superMethod.getSourceLocation()));
		      cont=false;
		    }
		  } else if (superMethod.isProtected()) {
		    if (subMethod.isDefault() || subMethod.isPrivate()) {
		      weaver.getWorld().getMessageHandler().handleMessage(MessageUtil.error(
		            "Cannot reduce the visibility of the inherited method '"+superMethod+"' from "+newParent.getName(),
		            superMethod.getSourceLocation()));
		      cont=false;
		    }
		  } else if (superMethod.isDefault()) {
		    if (subMethod.isPrivate()) {
		      weaver.getWorld().getMessageHandler().handleMessage(MessageUtil.error(
		            "Cannot reduce the visibility of the inherited method '"+superMethod+"' from "+newParent.getName(),
		            superMethod.getSourceLocation()));
		      cont=false;
		    }
		  }
		return cont;
	}
    
    /**
     * Rule 4. Can't have incompatible return types
     */
    private boolean enforceDecpRule4_compatibleReturnTypes(BcelClassWeaver weaver, BcelMethod superMethod, LazyMethodGen subMethod) {
        boolean cont = true;
        String superReturnTypeSig = superMethod.getReturnType().getSignature();
          String subReturnTypeSig   = subMethod.getReturnType().getSignature();
          if (!superReturnTypeSig.equals(subReturnTypeSig)) {
            // Allow for covariance - wish I could test this (need Java5...)
            ResolvedTypeX subType   = weaver.getWorld().resolve(subMethod.getReturnType());
            ResolvedTypeX superType = weaver.getWorld().resolve(superMethod.getReturnType());
            if (!subType.isAssignableFrom(superType)) {
                ISourceLocation sloc = subMethod.getSourceLocation();
                weaver.getWorld().getMessageHandler().handleMessage(MessageUtil.error(
                        "The return type is incompatible with "+superMethod.getDeclaringType()+"."+superMethod.getName()+superMethod.getParameterSignature(),
                        subMethod.getSourceLocation()));
                 cont=false;
            }
          }
        return cont;
    }
    
    /**
     * Rule5. Method overrides can't change the staticality (word?) - you can't override and make an instance
     *        method static or override and make a static method an instance method.
     */
    private boolean enforceDecpRule5_cantChangeFromStaticToNonstatic(BcelClassWeaver weaver,ISourceLocation mungerLoc,BcelMethod superMethod, LazyMethodGen subMethod ) {
      if (superMethod.isStatic() && !subMethod.isStatic()) { 
        error(weaver,"This instance method "+subMethod.getName()+subMethod.getParameterSignature()+
                     " cannot override the static method from "+superMethod.getDeclaringType().getName(),
              subMethod.getSourceLocation(),new ISourceLocation[]{mungerLoc});
        return false;
      } else if (!superMethod.isStatic() && subMethod.isStatic()) {
        error(weaver,"The static method "+subMethod.getName()+subMethod.getParameterSignature()+
                     " cannot hide the instance method from "+superMethod.getDeclaringType().getName(),
                subMethod.getSourceLocation(),new ISourceLocation[]{mungerLoc});
        return false;
      }
      return true;
    }

    public void error(BcelClassWeaver weaver,String text,ISourceLocation primaryLoc,ISourceLocation[] extraLocs) {
        IMessage msg = new Message(text, primaryLoc, true, extraLocs);
        weaver.getWorld().getMessageHandler().handleMessage(msg);
    }
   

	private LazyMethodGen findMatchingMethod(LazyClassGen newParentTarget, BcelMethod m) {
        LazyMethodGen found = null;
		// Search the type for methods overriding super methods (methods that come from the new parent)
		// Don't use the return value in the comparison as overriding doesnt
		for (Iterator i = newParentTarget.getMethodGens().iterator(); i.hasNext() && found==null;) {
		    LazyMethodGen gen = (LazyMethodGen) i.next();
		    if (gen.getName().equals(m.getName()) && 
		        gen.getParameterSignature().equals(m.getParameterSignature())) {
		        found = gen;
		    }
		}        
		return found;
	}

    /**
     * The main part of implementing declare parents extends.  Modify super ctor calls to target the new type.
     */
	public boolean attemptToModifySuperCalls(BcelClassWeaver weaver,LazyClassGen newParentTarget, ResolvedTypeX newParent) {
        String currentParent = newParentTarget.getSuperClassname();
        List mgs = newParentTarget.getMethodGens();
        
        // Look for ctors to modify
        for (Iterator iter = mgs.iterator(); iter.hasNext();) {
        	LazyMethodGen aMethod = (LazyMethodGen) iter.next();     
        
        	if (aMethod.getName().equals("<init>")) { 
        		InstructionList insList = aMethod.getBody();
        		InstructionHandle handle = insList.getStart();
        		while (handle!= null) {
        			if (handle.getInstruction() instanceof INVOKESPECIAL) {
        				ConstantPoolGen cpg = newParentTarget.getConstantPoolGen();
        				INVOKESPECIAL invokeSpecial = (INVOKESPECIAL)handle.getInstruction();
        				if (invokeSpecial.getClassName(cpg).equals(currentParent) && invokeSpecial.getMethodName(cpg).equals("<init>")) {
        					// System.err.println("Transforming super call '<init>"+sp.getSignature(cpg)+"'");
                     
        					// 1. Check there is a ctor in the new parent with the same signature
        					ResolvedMember newCtor = getConstructorWithSignature(newParent,invokeSpecial.getSignature(cpg));
                        
        					if (newCtor == null) {
                                
                                // 2. Check ITDCs to see if the necessary ctor is provided that way
                                boolean satisfiedByITDC = false;
                                for (Iterator ii = newParentTarget.getType().getInterTypeMungersIncludingSupers().iterator(); ii.hasNext() && !satisfiedByITDC; ) {
                                    ConcreteTypeMunger m = (ConcreteTypeMunger)ii.next();
                                    if (m.getMunger() instanceof NewConstructorTypeMunger) {
                                        if (m.getSignature().getSignature().equals(invokeSpecial.getSignature(cpg))) {
                                            satisfiedByITDC = true;
                                        }
                                    }
                                }
                                
                                if (!satisfiedByITDC) {
        						  String csig = createReadableCtorSig(newParent, cpg, invokeSpecial);
        						  weaver.getWorld().getMessageHandler().handleMessage(MessageUtil.error(
        								"Unable to modify hierarchy for "+newParentTarget.getClassName()+" - the constructor "+
										csig+" is missing",this.getSourceLocation()));
        						  return false;
                                }
        					}
                     
        					int idx = cpg.addMethodref(newParent.getClassName(), invokeSpecial.getMethodName(cpg), invokeSpecial.getSignature(cpg));
        					invokeSpecial.setIndex(idx);
        				}
        			}
        			handle = handle.getNext();   
        		}
        	}
        }
        return true;
    }


	/**
     * Creates a nice signature for the ctor, something like "(int,Integer,String)"
	 */
	private String createReadableCtorSig(ResolvedTypeX newParent, ConstantPoolGen cpg, INVOKESPECIAL invokeSpecial) {
        StringBuffer sb = new StringBuffer();
		Type[] ctorArgs = invokeSpecial.getArgumentTypes(cpg);
		  sb.append(newParent.getClassName());
		  sb.append("(");
		  for (int i = 0; i < ctorArgs.length; i++) {
			String argtype = ctorArgs[i].toString();
			if (argtype.lastIndexOf(".")!=-1) 
				sb.append(argtype.substring(argtype.lastIndexOf(".")+1));
			else
				sb.append(argtype);
			if (i+1<ctorArgs.length) sb.append(",");
		  }
		  sb.append(")");
          return sb.toString();
	}

	private ResolvedMember getConstructorWithSignature(ResolvedTypeX tx,String signature) {
        ResolvedMember[] mems = tx.getDeclaredJavaMethods();
        for (int i = 0; i < mems.length; i++) {
          ResolvedMember rm = mems[i];
          if (rm.getName().equals("<init>")) {
            if (rm.getSignature().equals(signature)) return rm;
          }
        }
        return null;
    }
    
    
	private boolean mungePrivilegedAccess(
		BcelClassWeaver weaver,
		PrivilegedAccessMunger munger)
	{
		LazyClassGen gen = weaver.getLazyClassGen();
		ResolvedMember member = munger.getMember();
		
		ResolvedTypeX onType = weaver.getWorld().resolve(member.getDeclaringType(),munger.getSourceLocation());
		//System.out.println("munging: " + gen + " with " + member);
		if (onType.equals(gen.getType())) {
			if (member.getKind() == Member.FIELD) {
				//System.out.println("matched: " + gen);
				addFieldGetter(gen, member,
					AjcMemberMaker.privilegedAccessMethodForFieldGet(aspectType, member));
				addFieldSetter(gen, member,
					AjcMemberMaker.privilegedAccessMethodForFieldSet(aspectType, member));
				return true;
			} else if (member.getKind() == Member.METHOD) {
				addMethodDispatch(gen, member,
					AjcMemberMaker.privilegedAccessMethodForMethod(aspectType, member));
				return true;
			} else if (member.getKind() == Member.CONSTRUCTOR) {
				for (Iterator i = gen.getMethodGens().iterator(); i.hasNext(); ) {
					LazyMethodGen m = (LazyMethodGen)i.next();
					if (m.getMemberView() != null
						&& m.getMemberView().getKind() == Member.CONSTRUCTOR) {
						// m.getMemberView().equals(member)) {
						m.forcePublic();
						//return true;
					}
				}
				return true;
				//throw new BCException("no match for " + member + " in " + gen);
			} else if (member.getKind() == Member.STATIC_INITIALIZATION) {
				gen.forcePublic();
				return true;
			} else {
				throw new RuntimeException("unimplemented");
			}
		}
		return false;
	}

	private void addFieldGetter(
		LazyClassGen gen,
		ResolvedMember field,
		ResolvedMember accessMethod)
	{
		LazyMethodGen mg = makeMethodGen(gen, accessMethod);
		InstructionList il = new InstructionList();
		InstructionFactory fact = gen.getFactory();
		if (field.isStatic()) {
			il.append(fact.createFieldAccess(
				gen.getClassName(), 
				field.getName(),
				BcelWorld.makeBcelType(field.getType()), Constants.GETSTATIC));
		} else {
			il.append(InstructionConstants.ALOAD_0);
			il.append(fact.createFieldAccess(
				gen.getClassName(), 
				field.getName(),
				BcelWorld.makeBcelType(field.getType()), Constants.GETFIELD));
		}
		il.append(InstructionFactory.createReturn(BcelWorld.makeBcelType(field.getType())));
		mg.getBody().insert(il);
				
		gen.addMethodGen(mg,getSignature().getSourceLocation());
	}
	
	private void addFieldSetter(
		LazyClassGen gen,
		ResolvedMember field,
		ResolvedMember accessMethod)
	{
		LazyMethodGen mg = makeMethodGen(gen, accessMethod);
		InstructionList il = new InstructionList();
		InstructionFactory fact = gen.getFactory();
		Type fieldType = BcelWorld.makeBcelType(field.getType());
		
		if (field.isStatic()) {
			il.append(InstructionFactory.createLoad(fieldType, 0));
			il.append(fact.createFieldAccess(
				gen.getClassName(), 
				field.getName(),
				fieldType, Constants.PUTSTATIC));
		} else {
			il.append(InstructionConstants.ALOAD_0);
			il.append(InstructionFactory.createLoad(fieldType, 1));
			il.append(fact.createFieldAccess(
				gen.getClassName(), 
				field.getName(),
				fieldType, Constants.PUTFIELD));
		}
		il.append(InstructionFactory.createReturn(Type.VOID));
		mg.getBody().insert(il);
				
		gen.addMethodGen(mg,getSignature().getSourceLocation());
	}
	
	private void addMethodDispatch(
		LazyClassGen gen,
		ResolvedMember method,
		ResolvedMember accessMethod)
	{
		LazyMethodGen mg = makeMethodGen(gen, accessMethod);
		InstructionList il = new InstructionList();
		InstructionFactory fact = gen.getFactory();
		//Type fieldType = BcelWorld.makeBcelType(field.getType());
		Type[] paramTypes = BcelWorld.makeBcelTypes(method.getParameterTypes());
		
		int pos = 0;
	
		if (!method.isStatic()) {
			il.append(InstructionConstants.ALOAD_0);
			pos++;
		}
		for (int i = 0, len = paramTypes.length; i < len; i++) {
			Type paramType = paramTypes[i];
			il.append(InstructionFactory.createLoad(paramType, pos));
			pos+=paramType.getSize();
		}
		il.append(Utility.createInvoke(fact, (BcelWorld)aspectType.getWorld(), 
				method));
		il.append(InstructionFactory.createReturn(BcelWorld.makeBcelType(method.getReturnType())));

		mg.getBody().insert(il);
				
		gen.addMethodGen(mg);
	}
	
	
	
	private LazyMethodGen makeMethodGen(LazyClassGen gen, ResolvedMember member) {
		LazyMethodGen ret = new LazyMethodGen(
			member.getModifiers(),
			BcelWorld.makeBcelType(member.getReturnType()),
			member.getName(),
			BcelWorld.makeBcelTypes(member.getParameterTypes()),
			TypeX.getNames(member.getExceptions()),
			gen);
        
        // 43972 : Static crosscutting makes interfaces unusable for javac
        // ret.makeSynthetic();    
		return ret;
	}


	private FieldGen makeFieldGen(LazyClassGen gen, ResolvedMember member) {
		return new FieldGen(
			member.getModifiers(),
			BcelWorld.makeBcelType(member.getReturnType()),
			member.getName(),
			gen.getConstantPoolGen());
	}


	

	private boolean mungePerObjectInterface(
		BcelClassWeaver weaver,
		PerObjectInterfaceTypeMunger munger)
	{
		LazyClassGen gen = weaver.getLazyClassGen();
		
		if (couldMatch(gen.getBcelObjectType(), munger.getTestPointcut())) {
			FieldGen fg = makeFieldGen(gen, 
				AjcMemberMaker.perObjectField(gen.getType(), aspectType));

	    	gen.addField(fg.getField(),getSourceLocation());
	    	
	    	
	    	Type fieldType = BcelWorld.makeBcelType(aspectType);
			LazyMethodGen mg = new LazyMethodGen(
				Modifier.PUBLIC,
				fieldType,
    			NameMangler.perObjectInterfaceGet(aspectType),
				new Type[0], new String[0],
				gen);
			InstructionList il = new InstructionList();
			InstructionFactory fact = gen.getFactory();
			il.append(InstructionConstants.ALOAD_0);
			il.append(fact.createFieldAccess(
				gen.getClassName(), 
				fg.getName(),
				fieldType, Constants.GETFIELD));
			il.append(InstructionFactory.createReturn(fieldType));
			mg.getBody().insert(il);
				
			gen.addMethodGen(mg);
			
			LazyMethodGen mg1 = new LazyMethodGen(
				Modifier.PUBLIC,
				Type.VOID,
				NameMangler.perObjectInterfaceSet(aspectType),
				
				new Type[]{fieldType,}, new String[0],
				gen);
			InstructionList il1 = new InstructionList();
			il1.append(InstructionConstants.ALOAD_0);
			il1.append(InstructionFactory.createLoad(fieldType, 1));
			il1.append(fact.createFieldAccess(
				gen.getClassName(), 
				fg.getName(), 
				fieldType, Constants.PUTFIELD));
			il1.append(InstructionFactory.createReturn(Type.VOID));
			mg1.getBody().insert(il1);
				
			gen.addMethodGen(mg1);
			
			gen.addInterface(munger.getInterfaceType(),getSourceLocation());

			return true;
		} else {
			return false;
		}
	}
	
	// PTWIMPL Add field to hold aspect instance and an accessor
	private boolean mungePerTypeWithinTransformer(BcelClassWeaver weaver) {
		LazyClassGen gen = weaver.getLazyClassGen();
			
		// if (couldMatch(gen.getBcelObjectType(), munger.getTestPointcut())) {
			
			// Add (to the target type) the field that will hold the aspect instance
			// e.g ajc$com_blah_SecurityAspect$ptwAspectInstance
			FieldGen fg = makeFieldGen(gen, AjcMemberMaker.perTypeWithinField(gen.getType(), aspectType));
		    gen.addField(fg.getField(),getSourceLocation());
		    	
		    // Add an accessor for this new field, the ajc$<aspectname>$localAspectOf() method
		    // e.g. "public com_blah_SecurityAspect ajc$com_blah_SecurityAspect$localAspectOf()"
		    Type fieldType = BcelWorld.makeBcelType(aspectType);
			LazyMethodGen mg = new LazyMethodGen(
					Modifier.PUBLIC | Modifier.STATIC,fieldType,
	    			NameMangler.perTypeWithinLocalAspectOf(aspectType),
					new Type[0], new String[0],gen);
			InstructionList il = new InstructionList();
			//PTWIMPL ?? Should check if it is null and throw NoAspectBoundException
			InstructionFactory fact = gen.getFactory();
			il.append(fact.createFieldAccess(
				gen.getClassName(), 
				fg.getName(),
				fieldType, Constants.GETSTATIC));
			il.append(InstructionFactory.createReturn(fieldType));
			mg.getBody().insert(il);		
			gen.addMethodGen(mg);
			return true;
//		} else {
//			return false;
//		}
	}

	// ??? Why do we have this method? I thought by now we would know if it matched or not
	private boolean couldMatch(
		BcelObjectType bcelObjectType,
		Pointcut pointcut) {
		return !bcelObjectType.isInterface();
	}
	
	private boolean mungeNewMethod(BcelClassWeaver weaver, NewMethodTypeMunger munger) {
		ResolvedMember signature = munger.getSignature();
		ResolvedMember dispatchMethod = munger.getDispatchMethod(aspectType);
		
		LazyClassGen gen = weaver.getLazyClassGen();
		
		ResolvedTypeX onType = weaver.getWorld().resolve(signature.getDeclaringType(),munger.getSourceLocation());
		boolean onInterface = onType.isInterface();
		
		if (onType.isAnnotation(weaver.getWorld())) {
			signalError(WeaverMessages.ITDM_ON_ANNOTATION_NOT_ALLOWED,weaver,onType);
			return false;		
		}
		
		if (onType.isEnum(weaver.getWorld())) {
			signalError(WeaverMessages.ITDM_ON_ENUM_NOT_ALLOWED,weaver,onType);
			return false;
		}
		
		if (onType.equals(gen.getType())) {
			ResolvedMember introMethod = 
					AjcMemberMaker.interMethod(signature, aspectType, onInterface);
            
			LazyMethodGen mg = makeMethodGen(gen, introMethod);

			if (!onInterface && !Modifier.isAbstract(introMethod.getModifiers())) {
				InstructionList body = mg.getBody();
				InstructionFactory fact = gen.getFactory();
				int pos = 0;
	
				if (!signature.isStatic()) {
					body.append(InstructionFactory.createThis());
					pos++;
				}
				Type[] paramTypes = BcelWorld.makeBcelTypes(introMethod.getParameterTypes());
				for (int i = 0, len = paramTypes.length; i < len; i++) {
					Type paramType = paramTypes[i];
					body.append(InstructionFactory.createLoad(paramType, pos));
					pos+=paramType.getSize();
				}
				body.append(Utility.createInvoke(fact, weaver.getWorld(), dispatchMethod));
				body.append(
					InstructionFactory.createReturn(
						BcelWorld.makeBcelType(introMethod.getReturnType())));
			} else {
				//??? this is okay
				//if (!(mg.getBody() == null)) throw new RuntimeException("bas");
			}
			

			// XXX make sure to check that we set exceptions properly on this guy.
			weaver.addLazyMethodGen(mg);
			weaver.getLazyClassGen().warnOnAddedMethod(mg.getMethod(),getSignature().getSourceLocation());
			
			addNeededSuperCallMethods(weaver, onType, munger.getSuperMethodsCalled());
			
    		return true;
    		
		} else if (onInterface && !Modifier.isAbstract(signature.getModifiers())) {
			
			// This means the 'gen' should be the top most implementor
			// - if it is *not* then something went wrong after we worked
			// out that it was the top most implementor (see pr49657)
    		if (!gen.getType().isTopmostImplementor(onType)) {
    			ResolvedTypeX rtx = gen.getType().getTopmostImplementor(onType);
    			if (!rtx.isExposedToWeaver()) {
    				ISourceLocation sLoc = munger.getSourceLocation();
    			    weaver.getWorld().getMessageHandler().handleMessage(MessageUtil.error(
    			    		WeaverMessages.format(WeaverMessages.ITD_NON_EXPOSED_IMPLEMENTOR,rtx,getAspectType().getName()),
							(sLoc==null?getAspectType().getSourceLocation():sLoc)));
    			} else {
    				// XXX what does this state mean?
    				// We have incorrectly identified what is the top most implementor and its not because
    				// a type wasn't exposed to the weaver
    			}
				return false;
    		} else {
		
			  ResolvedMember introMethod = 
					AjcMemberMaker.interMethod(signature, aspectType, false);
			
			  LazyMethodGen mg = makeMethodGen(gen, introMethod);
						
			  Type[] paramTypes = BcelWorld.makeBcelTypes(introMethod.getParameterTypes());
			  Type returnType = BcelWorld.makeBcelType(introMethod.getReturnType());
			
			  InstructionList body = mg.getBody();
			  InstructionFactory fact = gen.getFactory();
			  int pos = 0;

			  if (!introMethod.isStatic()) {
				body.append(InstructionFactory.createThis());
				pos++;
			  }
			  for (int i = 0, len = paramTypes.length; i < len; i++) {
				Type paramType = paramTypes[i];
				body.append(InstructionFactory.createLoad(paramType, pos));
				pos+=paramType.getSize();
			  }
			  body.append(Utility.createInvoke(fact, weaver.getWorld(), dispatchMethod));
			  body.append(InstructionFactory.createReturn(returnType));
			  mg.definingType = onType;
			
			  weaver.addOrReplaceLazyMethodGen(mg);
			
			  addNeededSuperCallMethods(weaver, onType, munger.getSuperMethodsCalled());
			
			  return true;
    		}
		} else {
			return false;
		}
	}

	private void addNeededSuperCallMethods(
		BcelClassWeaver weaver,
		ResolvedTypeX onType,
		Set neededSuperCalls)
	{
		LazyClassGen gen = weaver.getLazyClassGen();
		
		for (Iterator iter = neededSuperCalls.iterator(); iter.hasNext();) {
			ResolvedMember superMethod = (ResolvedMember) iter.next();
			if (weaver.addDispatchTarget(superMethod)) {
				//System.err.println("super type: " + superMethod.getDeclaringType() + ", " + gen.getType());
				boolean isSuper = !superMethod.getDeclaringType().equals(gen.getType());
				String dispatchName;
				if (isSuper)
					dispatchName =
						NameMangler.superDispatchMethod(onType, superMethod.getName());
				else
					dispatchName =
						NameMangler.protectedDispatchMethod(
							onType,
							superMethod.getName());
				LazyMethodGen dispatcher =
					makeDispatcher(
						gen,
						dispatchName,
						superMethod,
						weaver.getWorld(),
						isSuper);

				weaver.addLazyMethodGen(dispatcher);
			}
		}
	}

	private void signalError(String msgid,BcelClassWeaver weaver,TypeX onType) {
		IMessage msg = MessageUtil.error(
				WeaverMessages.format(msgid,onType.getName()),getSourceLocation());
		weaver.getWorld().getMessageHandler().handleMessage(msg);
	}

	private boolean mungeNewConstructor(
		BcelClassWeaver weaver,
		NewConstructorTypeMunger newConstructorTypeMunger) 
	{
		final LazyClassGen currentClass = weaver.getLazyClassGen();
		final InstructionFactory fact = currentClass.getFactory();

		ResolvedMember newConstructorMember = newConstructorTypeMunger.getSyntheticConstructor();
		TypeX          onType = newConstructorMember.getDeclaringType();
		
		if (onType.isAnnotation(weaver.getWorld())) {
			signalError(WeaverMessages.ITDC_ON_ANNOTATION_NOT_ALLOWED,weaver,onType);
			return false;
		}
		
		if (onType.isEnum(weaver.getWorld())) {
			signalError(WeaverMessages.ITDC_ON_ENUM_NOT_ALLOWED,weaver,onType);
			return false;
		}
		
		if (! onType.equals(currentClass.getType())) return false;

		ResolvedMember explicitConstructor = newConstructorTypeMunger.getExplicitConstructor();
		//int declaredParameterCount = newConstructorTypeMunger.getDeclaredParameterCount();
		LazyMethodGen freshConstructor = 
			makeMethodGen(currentClass, newConstructorMember);
		currentClass.addMethodGen(freshConstructor);
		//weaver.addLazyMethodGen(freshConstructor);
		
		InstructionList body = freshConstructor.getBody();
		
		// add to body:  push arts for call to pre, from actual args starting at 1 (skipping this), going to 
		//               declared argcount + 1
		TypeX[] declaredParams = newConstructorTypeMunger.getSignature().getParameterTypes();
		Type[] paramTypes = freshConstructor.getArgumentTypes();
		int frameIndex = 1;
		for (int i = 0, len = declaredParams.length; i < len; i++) {
			body.append(InstructionFactory.createLoad(paramTypes[i], frameIndex));
			frameIndex += paramTypes[i].getSize();
		}
		// do call to pre
		Member preMethod =
			AjcMemberMaker.preIntroducedConstructor(aspectType, onType, declaredParams);
		body.append(Utility.createInvoke(fact, null, preMethod));
		
		// create a local, and store return pre stuff into it.
		int arraySlot = freshConstructor.allocateLocal(1);
		body.append(InstructionFactory.createStore(Type.OBJECT, arraySlot));
		
		// put this on the stack
		body.append(InstructionConstants.ALOAD_0);
		
		// unpack pre args onto stack
		TypeX[] superParamTypes = explicitConstructor.getParameterTypes();
		
		for (int i = 0, len = superParamTypes.length; i < len; i++) {
			body.append(InstructionFactory.createLoad(Type.OBJECT, arraySlot));
			body.append(Utility.createConstant(fact, i));
			body.append(InstructionFactory.createArrayLoad(Type.OBJECT));
			body.append(
				Utility.createConversion(
					fact,
					Type.OBJECT,
					BcelWorld.makeBcelType(superParamTypes[i])));
		}

		// call super/this
		
		body.append(Utility.createInvoke(fact, null, explicitConstructor));
		
		// put this back on the stack

		body.append(InstructionConstants.ALOAD_0);
		
		// unpack params onto stack
		Member postMethod =
			AjcMemberMaker.postIntroducedConstructor(aspectType, onType, declaredParams);
		TypeX[] postParamTypes = postMethod.getParameterTypes();
		
		for (int i = 1, len = postParamTypes.length; i < len; i++) {
			body.append(InstructionFactory.createLoad(Type.OBJECT, arraySlot));
			body.append(Utility.createConstant(fact, superParamTypes.length + i-1));
			body.append(InstructionFactory.createArrayLoad(Type.OBJECT));
			body.append(
				Utility.createConversion(
					fact,
					Type.OBJECT,
					BcelWorld.makeBcelType(postParamTypes[i])));
		}		
		
		// call post
		body.append(Utility.createInvoke(fact, null, postMethod));
		
		// don't forget to return!!
		body.append(InstructionConstants.RETURN);
		
		return true;		
	}


	private static LazyMethodGen makeDispatcher(
		LazyClassGen onGen,
		String dispatchName,
		ResolvedMember superMethod,
		BcelWorld world,
		boolean isSuper) 
	{
		Type[] paramTypes = BcelWorld.makeBcelTypes(superMethod.getParameterTypes());
		Type returnType = BcelWorld.makeBcelType(superMethod.getReturnType());
				
		int modifiers = Modifier.PUBLIC;
		if (onGen.isInterface()) modifiers |= Modifier.ABSTRACT;
				
		LazyMethodGen mg = 
				new LazyMethodGen(
					modifiers,
					returnType,
					dispatchName,
					paramTypes,
					TypeX.getNames(superMethod.getExceptions()),
					onGen);
		InstructionList body = mg.getBody();
		
		if (onGen.isInterface()) return mg;
		
		// assert (!superMethod.isStatic())
		InstructionFactory fact = onGen.getFactory();
		int pos = 0;
		
		body.append(InstructionFactory.createThis());
		pos++;
		for (int i = 0, len = paramTypes.length; i < len; i++) {
			Type paramType = paramTypes[i];
			body.append(InstructionFactory.createLoad(paramType, pos));
			pos+=paramType.getSize();
		}
		if (isSuper) {
			body.append(Utility.createSuperInvoke(fact, world, superMethod));
		} else {
			body.append(Utility.createInvoke(fact, world, superMethod));
		}
		body.append(InstructionFactory.createReturn(returnType));

		return mg;
	}	
	
	private boolean mungeNewField(BcelClassWeaver weaver, NewFieldTypeMunger munger) {
		/*ResolvedMember initMethod = */munger.getInitMethod(aspectType);
		
		LazyClassGen gen = weaver.getLazyClassGen();
		ResolvedMember field = munger.getSignature();
		
		
		ResolvedTypeX onType = weaver.getWorld().resolve(field.getDeclaringType(),munger.getSourceLocation());
		boolean onInterface = onType.isInterface();
		
		if (onType.isAnnotation(weaver.getWorld())) {
			signalError(WeaverMessages.ITDF_ON_ANNOTATION_NOT_ALLOWED,weaver,onType);
			return false;
		}
		
		if (onType.isEnum(weaver.getWorld())) {
			signalError(WeaverMessages.ITDF_ON_ENUM_NOT_ALLOWED,weaver,onType);
			return false;
		}
		
		if (onType.equals(gen.getType())) {
			if (onInterface) {
				LazyMethodGen mg = makeMethodGen(gen, 
					AjcMemberMaker.interFieldInterfaceGetter(field, onType, aspectType));
				gen.addMethodGen(mg);
				
				LazyMethodGen mg1 = makeMethodGen(gen, 
					AjcMemberMaker.interFieldInterfaceSetter(field, onType, aspectType));
				gen.addMethodGen(mg1);
			} else {
				weaver.addInitializer(this);
				FieldGen fg = makeFieldGen(gen,
					AjcMemberMaker.interFieldClassField(field, aspectType));
	    		gen.addField(fg.getField(),getSourceLocation());
			}
    		return true;
		} else if (onInterface && gen.getType().isTopmostImplementor(onType)) {
			// wew know that we can't be static since we don't allow statics on interfaces
			if (field.isStatic()) throw new RuntimeException("unimplemented");
			weaver.addInitializer(this);
			//System.err.println("impl body on " + gen.getType() + " for " + munger);
			Type fieldType = 	BcelWorld.makeBcelType(field.getType());
			
			FieldGen fg = makeFieldGen(gen,
					AjcMemberMaker.interFieldInterfaceField(field, onType, aspectType));
	    	gen.addField(fg.getField(),getSourceLocation());
			
	    	//this uses a shadow munger to add init method to constructors
	    	//weaver.getShadowMungers().add(makeInitCallShadowMunger(initMethod));
	    	
			LazyMethodGen mg = makeMethodGen(gen, 
					AjcMemberMaker.interFieldInterfaceGetter(field, gen.getType(), aspectType));
			InstructionList il = new InstructionList();
			InstructionFactory fact = gen.getFactory();
			if (field.isStatic()) {
				il.append(fact.createFieldAccess(
					gen.getClassName(), 
					fg.getName(),
					fieldType, Constants.GETSTATIC));
			} else {
				il.append(InstructionConstants.ALOAD_0);
				il.append(fact.createFieldAccess(
					gen.getClassName(), 
					fg.getName(),
					fieldType, Constants.GETFIELD));
			}
			il.append(InstructionFactory.createReturn(fieldType));
			mg.getBody().insert(il);
				
			gen.addMethodGen(mg);
			
			LazyMethodGen mg1 = makeMethodGen(gen, 
					AjcMemberMaker.interFieldInterfaceSetter(field, gen.getType(), aspectType));
			InstructionList il1 = new InstructionList();
			if (field.isStatic()) {
				il1.append(InstructionFactory.createLoad(fieldType, 0));
				il1.append(fact.createFieldAccess(
					gen.getClassName(), 
					fg.getName(),
					fieldType, Constants.PUTSTATIC));
			} else {
				il1.append(InstructionConstants.ALOAD_0);
				il1.append(InstructionFactory.createLoad(fieldType, 1));
				il1.append(fact.createFieldAccess(
					gen.getClassName(), 
					fg.getName(), 
					fieldType, Constants.PUTFIELD));
			}
			il1.append(InstructionFactory.createReturn(Type.VOID));
			mg1.getBody().insert(il1);
				
			gen.addMethodGen(mg1);

			return true;
		} else {
			return false;
		}
	}
}
		