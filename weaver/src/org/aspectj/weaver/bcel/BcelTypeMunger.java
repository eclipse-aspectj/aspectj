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
 *     Alexandre Vasseur    @AspectJ ITDs
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.apache.bcel.generic.BranchInstruction;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.generic.FieldGen;
import org.aspectj.apache.bcel.generic.INVOKESPECIAL;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.generic.annotation.AnnotationGen;
import org.aspectj.asm.AsmManager;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.WeaveMessage;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.bridge.context.ContextToken;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.AnnotationOnTypeMunger;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.AsmRelationshipProvider;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.MethodDelegateTypeMunger;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.NewConstructorTypeMunger;
import org.aspectj.weaver.NewFieldTypeMunger;
import org.aspectj.weaver.NewMethodTypeMunger;
import org.aspectj.weaver.NewParentTypeMunger;
import org.aspectj.weaver.PerObjectInterfaceTypeMunger;
import org.aspectj.weaver.PrivilegedAccessMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.TypeVariableReference;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.Pointcut;

//XXX addLazyMethodGen is probably bad everywhere
public class BcelTypeMunger extends ConcreteTypeMunger {

	public BcelTypeMunger(ResolvedTypeMunger munger, ResolvedType aspectType) {
		super(munger, aspectType);
	}

	public String toString() {
		return "(BcelTypeMunger " + getMunger() + ")";
	}

	public boolean munge(BcelClassWeaver weaver) {
		ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.MUNGING_WITH, this);
		boolean changed = false;
		boolean worthReporting = true;
		
		if (munger.getKind() == ResolvedTypeMunger.Field) {
			changed = mungeNewField(weaver, (NewFieldTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.Method) {
			changed = mungeNewMethod(weaver, (NewMethodTypeMunger)munger);
        } else if (munger.getKind() == ResolvedTypeMunger.MethodDelegate) {
            changed = mungeMethodDelegate(weaver, (MethodDelegateTypeMunger)munger);
        } else if (munger.getKind() == ResolvedTypeMunger.FieldHost) {
            changed = mungeFieldHost(weaver, (MethodDelegateTypeMunger.FieldHostTypeMunger)munger);
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
		} else if (munger.getKind() == ResolvedTypeMunger.AnnotationOnType) {
			changed = mungeNewAnnotationOnType(weaver,(AnnotationOnTypeMunger)munger);
			worthReporting=false;
        } else {
			throw new RuntimeException("unimplemented");
		}
		
		if (changed && munger.changesPublicSignature()) {
			WeaverStateInfo info = 
				weaver.getLazyClassGen().getOrCreateWeaverStateInfo(BcelClassWeaver.getReweavableMode());
			info.addConcreteMunger(this);
		}

		if (changed && worthReporting) {
			if (munger.getKind().equals(ResolvedTypeMunger.Parent)) {
		  	  AsmRelationshipProvider.getDefault().addRelationship(weaver.getLazyClassGen().getType(), munger,getAspectType());
			} else {
		  	  AsmRelationshipProvider.getDefault().addRelationship(weaver.getLazyClassGen().getType(), munger,getAspectType());	
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
            } else if (munger.getKind().equals(ResolvedTypeMunger.FieldHost)) {
                ;//hidden
            } else {
        		ResolvedMember declaredSig = munger.getSignature();
//        		if (declaredSig==null) declaredSig= munger.getSignature();
        		weaver.getWorld().getMessageHandler().handleMessage(WeaveMessage.constructWeavingMessage(WeaveMessage.WEAVEMESSAGE_ITD,
        		new String[]{weaver.getLazyClassGen().getType().getName(),
        			         tName,munger.getKind().toString().toLowerCase(),
        			         getAspectType().getName(),
        					 fName+":'"+declaredSig+"'"},
							 weaver.getLazyClassGen().getClassName(), getAspectType().getName()));
        	}	
		}
		
		CompilationAndWeavingContext.leavingPhase(tok);
		return changed;
	}

	private String getShortname(String path)  {
		int takefrom = path.lastIndexOf('/');
		if (takefrom == -1) {
			takefrom = path.lastIndexOf('\\');
		}
		return path.substring(takefrom+1);
	}
	
	private boolean mungeNewAnnotationOnType(BcelClassWeaver weaver,AnnotationOnTypeMunger munger) {
		// FIXME asc this has already been done up front, need to do it here too?
		weaver.getLazyClassGen().addAnnotation(munger.getNewAnnotation().getBcelAnnotation());
		return true;
	}

	/** 
     * For a long time, AspectJ did not allow binary weaving of declare parents.  This restriction is now lifted
     * but could do with more testing!
	 */
	private boolean mungeNewParent(BcelClassWeaver weaver, NewParentTypeMunger munger) {
		LazyClassGen  newParentTarget = weaver.getLazyClassGen();
		ResolvedType newParent       = munger.getNewParent();
        
        boolean cont = true; // Set to false when we error, so we don't actually *do* the munge           
        cont = enforceDecpRule1_abstractMethodsImplemented(weaver, munger.getSourceLocation(),newParentTarget, newParent);
        cont = enforceDecpRule2_cantExtendFinalClass(weaver,munger.getSourceLocation(),newParentTarget,newParent) && cont;
                
        List methods = newParent.getMethodsWithoutIterator(false,true);
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
		  ResolvedMember    superMethod = (ResolvedMember) iter.next();
          if (!superMethod.getName().equals("<init>")) {
		    LazyMethodGen   subMethod = findMatchingMethod(newParentTarget, superMethod);
            if (subMethod!=null && !subMethod.isBridgeMethod()) { // FIXME asc is this safe for all bridge methods?
              if (!(subMethod.isSynthetic() && superMethod.isSynthetic())) {
            	if (!(subMethod.isStatic() && subMethod.getName().startsWith("access$"))) { // ignore generated accessors
                  cont = enforceDecpRule3_visibilityChanges(weaver, newParent, superMethod, subMethod) && cont;
	              cont = enforceDecpRule4_compatibleReturnTypes(weaver, superMethod, subMethod)        && cont;
	              cont = enforceDecpRule5_cantChangeFromStaticToNonstatic(weaver,munger.getSourceLocation(),superMethod,subMethod) && cont;
                }
              }
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
    private boolean enforceDecpRule1_abstractMethodsImplemented(BcelClassWeaver weaver, ISourceLocation mungerLoc,LazyClassGen newParentTarget, ResolvedType newParent) {
        boolean ruleCheckingSucceeded = true;
        if (!(newParentTarget.isAbstract() || newParentTarget.isInterface())) { // Ignore abstract classes or interfaces
            List methods = newParent.getMethodsWithoutIterator(false,true);
            for (Iterator i = methods.iterator(); i.hasNext();) {
                ResolvedMember o = (ResolvedMember)i.next();
                if (o.isAbstract() && !o.getName().startsWith("ajc$interField")) { // Ignore abstract methods of ajc$interField prefixed methods
                    ResolvedMember discoveredImpl = null;
                    List newParentTargetMethods = newParentTarget.getType().getMethodsWithoutIterator(false,true);
                    for (Iterator ii = newParentTargetMethods.iterator(); ii.hasNext() && discoveredImpl==null;) {
                        ResolvedMember gen2 = (ResolvedMember) ii.next();
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
                            if (m.getMunger()!=null && m.getMunger().getKind() == ResolvedTypeMunger.Method) {
                                ResolvedMember sig = m.getSignature();
                                if (!Modifier.isAbstract(sig.getModifiers())) {
                                	
                                	// If the ITD shares a type variable with some target type, we need to tailor it for that
                                	// type
                                	if (m.isTargetTypeParameterized()) {
                            	        ResolvedType genericOnType = getWorld().resolve(sig.getDeclaringType()).getGenericType();
                                		m = m.parameterizedFor(newParent.discoverActualOccurrenceOfTypeInHierarchy(genericOnType));
                                    	sig = m.getSignature(); // possible sig change when type parameters filled in
                                	}
                                    if (ResolvedType
                                        .matches(
                                            AjcMemberMaker.interMethod(
                                                sig,m.getAspectType(),sig.getDeclaringType().resolve(weaver.getWorld()).isInterface()),o)) {
                                        satisfiedByITD = true;
                                    }
                                }
                            } else if (m.getMunger()!=null && m.getMunger().getKind() == ResolvedTypeMunger.MethodDelegate) {
                                satisfiedByITD = true;//AV - that should be enough, no need to check more
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
            LazyClassGen newParentTarget, ResolvedType newParent) {
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
	private boolean enforceDecpRule3_visibilityChanges(BcelClassWeaver weaver, ResolvedType newParent, ResolvedMember superMethod, LazyMethodGen subMethod) {
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
    private boolean enforceDecpRule4_compatibleReturnTypes(BcelClassWeaver weaver, ResolvedMember superMethod, LazyMethodGen subMethod) {
        boolean cont = true;
        String superReturnTypeSig = superMethod.getReturnType().getSignature();
          String subReturnTypeSig   = subMethod.getReturnType().getSignature();
          superReturnTypeSig = superReturnTypeSig.replace('.','/');
          subReturnTypeSig = subReturnTypeSig.replace('.','/');
          if (!superReturnTypeSig.equals(subReturnTypeSig)) {
            // Allow for covariance - wish I could test this (need Java5...)
            ResolvedType subType   = weaver.getWorld().resolve(subMethod.getReturnType());
            ResolvedType superType = weaver.getWorld().resolve(superMethod.getReturnType());
            if (!superType.isAssignableFrom(subType)) {
                ISourceLocation sloc = subMethod.getSourceLocation();
                weaver.getWorld().getMessageHandler().handleMessage(MessageUtil.error(
                        "The return type is incompatible with "+superMethod.getDeclaringType()+"."+superMethod.getName()+superMethod.getParameterSignature(),
                        subMethod.getSourceLocation()));
// this just might be a better error message...                      
//                        "The return type '"+subReturnTypeSig+"' is incompatible with the overridden method "+superMethod.getDeclaringType()+"."+
//                        superMethod.getName()+superMethod.getParameterSignature()+" which returns '"+superReturnTypeSig+"'",
                 cont=false;
            }
          }
        return cont;
    }
    
    /**
     * Rule5. Method overrides can't change the staticality (word?) - you can't override and make an instance
     *        method static or override and make a static method an instance method.
     */
    private boolean enforceDecpRule5_cantChangeFromStaticToNonstatic(BcelClassWeaver weaver,ISourceLocation mungerLoc,ResolvedMember superMethod, LazyMethodGen subMethod ) {
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
   

	private LazyMethodGen findMatchingMethod(LazyClassGen newParentTarget, ResolvedMember m) {
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
	public boolean attemptToModifySuperCalls(BcelClassWeaver weaver,LazyClassGen newParentTarget, ResolvedType newParent) {
        String currentParent = newParentTarget.getSuperClassname();
        if (newParent.getGenericType()!=null) newParent = newParent.getGenericType(); // target new super calls at the generic type if its raw or parameterized
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
                     
        					int idx = cpg.addMethodref(newParent.getName(), invokeSpecial.getMethodName(cpg), invokeSpecial.getSignature(cpg));
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
	private String createReadableCtorSig(ResolvedType newParent, ConstantPoolGen cpg, INVOKESPECIAL invokeSpecial) {
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

	private ResolvedMember getConstructorWithSignature(ResolvedType tx,String signature) {
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
		
		ResolvedType onType = weaver.getWorld().resolve(member.getDeclaringType(),munger.getSourceLocation());
		if (onType.isRawType()) onType = onType.getGenericType();

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
				return gen.forcePublic();
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
	
	
	protected LazyMethodGen makeMethodGen(LazyClassGen gen, ResolvedMember member) {
		LazyMethodGen ret = new LazyMethodGen(
			member.getModifiers(),
			BcelWorld.makeBcelType(member.getReturnType()),
			member.getName(),
			BcelWorld.makeBcelTypes(member.getParameterTypes()),
			UnresolvedType.getNames(member.getExceptions()),
			gen);
        
        // 43972 : Static crosscutting makes interfaces unusable for javac
        // ret.makeSynthetic();    
		return ret;
	}


	protected FieldGen makeFieldGen(LazyClassGen gen, ResolvedMember member) {
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
		//System.err.println("Munging perobject ["+munger+"] onto "+weaver.getLazyClassGen().getClassName());
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
		World w = weaver.getWorld();
		// Resolving it will sort out the tvars
		ResolvedMember unMangledInterMethod = munger.getSignature().resolve(w);
		// do matching on the unMangled one, but actually add them to the mangled method
		ResolvedMember interMethodBody = munger.getDeclaredInterMethodBody(aspectType,w);
		ResolvedMember interMethodDispatcher = munger.getDeclaredInterMethodDispatcher(aspectType,w);
		ResolvedMember memberHoldingAnyAnnotations = interMethodDispatcher;
		ResolvedType onType = weaver.getWorld().resolve(unMangledInterMethod.getDeclaringType(),munger.getSourceLocation());
		LazyClassGen gen = weaver.getLazyClassGen();
		boolean mungingInterface = gen.isInterface();
		
		if (onType.isRawType()) onType = onType.getGenericType();

		boolean onInterface = onType.isInterface();
		
		
		// Simple checks, can't ITD on annotations or enums
		if (onType.isAnnotation()) {
			signalError(WeaverMessages.ITDM_ON_ANNOTATION_NOT_ALLOWED,weaver,onType);
			return false;		
		}
		
		if (onType.isEnum()) {
			signalError(WeaverMessages.ITDM_ON_ENUM_NOT_ALLOWED,weaver,onType);
			return false;
		}
		
		if (onInterface && gen.getLazyMethodGen(unMangledInterMethod.getName(), unMangledInterMethod.getSignature(),true) != null) {
				// this is ok, we could be providing the default implementation of a method
				// that the target has already declared
				return false;
		}
		
		// If we are processing the intended ITD target type (might be an interface)
		if (onType.equals(gen.getType())) {
			ResolvedMember mangledInterMethod =
					AjcMemberMaker.interMethod(unMangledInterMethod, aspectType, onInterface);
            
			
			LazyMethodGen newMethod = makeMethodGen(gen, mangledInterMethod);
			if (mungingInterface) {
				// we want the modifiers of the ITD to be used for all *implementors* of the
				// interface, but the method itself we add to the interface must be public abstract
				newMethod.setAccessFlags(Modifier.PUBLIC | Modifier.ABSTRACT);
			}
			
			// pr98901
		    // For copying the annotations across, we have to discover the real member in the aspect
		    // which is holding them.
			if (weaver.getWorld().isInJava5Mode()){
				AnnotationX annotationsOnRealMember[] = null;
				ResolvedType toLookOn = aspectType;
				if (aspectType.isRawType()) toLookOn = aspectType.getGenericType();
				ResolvedMember realMember = getRealMemberForITDFromAspect(toLookOn,memberHoldingAnyAnnotations,false);
				if (realMember==null) throw new BCException("Couldn't find ITD holder member '"+
						memberHoldingAnyAnnotations+"' on aspect "+aspectType);
				annotationsOnRealMember = realMember.getAnnotations();
				
				if (annotationsOnRealMember!=null) {
					for (int i = 0; i < annotationsOnRealMember.length; i++) {
						AnnotationX annotationX = annotationsOnRealMember[i];
						Annotation a = annotationX.getBcelAnnotation();
						AnnotationGen ag = new AnnotationGen(a,weaver.getLazyClassGen().getConstantPoolGen(),true);
						newMethod.addAnnotation(new AnnotationX(ag.getAnnotation(),weaver.getWorld()));
					}
				}
				// the below loop fixes the very special (and very stupid)
				// case where an aspect declares an annotation
				// on an ITD it declared on itself.
				List allDecams = weaver.getWorld().getDeclareAnnotationOnMethods();
				for (Iterator i = allDecams.iterator(); i.hasNext();){
					DeclareAnnotation decaMC = (DeclareAnnotation) i.next();	
					if (decaMC.matches(unMangledInterMethod,weaver.getWorld())
							&& newMethod.getEnclosingClass().getType() == aspectType) {
						newMethod.addAnnotation(decaMC.getAnnotationX());
					}
				}
			}

			// If it doesn't target an interface and there is a body (i.e. it isnt abstract)
			if (!onInterface && !Modifier.isAbstract(mangledInterMethod.getModifiers())) {
				InstructionList body = newMethod.getBody();
				InstructionFactory fact = gen.getFactory();
				int pos = 0;
	
				if (!unMangledInterMethod.isStatic()) {
					body.append(InstructionFactory.createThis());
					pos++;
				}
				Type[] paramTypes = BcelWorld.makeBcelTypes(mangledInterMethod.getParameterTypes());
				for (int i = 0, len = paramTypes.length; i < len; i++) {
					Type paramType = paramTypes[i];
					body.append(InstructionFactory.createLoad(paramType, pos));
					pos+=paramType.getSize();
				}
				body.append(Utility.createInvoke(fact, weaver.getWorld(), interMethodBody));
				body.append(
					InstructionFactory.createReturn(
						BcelWorld.makeBcelType(mangledInterMethod.getReturnType())));
				
				if (weaver.getWorld().isInJava5Mode()) { // Don't need bridge methods if not in 1.5 mode.
					createAnyBridgeMethodsForCovariance(weaver, munger, unMangledInterMethod, onType, gen, paramTypes);
				}
				
			} else {
				//??? this is okay
				//if (!(mg.getBody() == null)) throw new RuntimeException("bas");
			}
			

			// XXX make sure to check that we set exceptions properly on this guy.
			weaver.addLazyMethodGen(newMethod);
			weaver.getLazyClassGen().warnOnAddedMethod(newMethod.getMethod(),getSignature().getSourceLocation());
			
			addNeededSuperCallMethods(weaver, onType, munger.getSuperMethodsCalled());
			
    		return true;
    		
		} else if (onInterface && !Modifier.isAbstract(unMangledInterMethod.getModifiers())) {
			
			// This means the 'gen' should be the top most implementor
			// - if it is *not* then something went wrong after we worked
			// out that it was the top most implementor (see pr49657)
    		if (!gen.getType().isTopmostImplementor(onType)) {
    			ResolvedType rtx = gen.getType().getTopmostImplementor(onType);
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
		
			  ResolvedMember mangledInterMethod =
					AjcMemberMaker.interMethod(unMangledInterMethod, aspectType, false);
			  
			  LazyMethodGen mg = makeMethodGen(gen, mangledInterMethod);
			  
			  // From 98901#29 - need to copy annotations across
			  if (weaver.getWorld().isInJava5Mode()){
					AnnotationX annotationsOnRealMember[] = null;
					ResolvedType toLookOn = aspectType;
					if (aspectType.isRawType()) toLookOn = aspectType.getGenericType();
					ResolvedMember realMember = getRealMemberForITDFromAspect(toLookOn,memberHoldingAnyAnnotations,false);
					if (realMember==null) throw new BCException("Couldn't find ITD holder member '"+
							memberHoldingAnyAnnotations+"' on aspect "+aspectType);
					annotationsOnRealMember = realMember.getAnnotations();
					
					if (annotationsOnRealMember!=null) {
						for (int i = 0; i < annotationsOnRealMember.length; i++) {
							AnnotationX annotationX = annotationsOnRealMember[i];
							Annotation a = annotationX.getBcelAnnotation();
							AnnotationGen ag = new AnnotationGen(a,weaver.getLazyClassGen().getConstantPoolGen(),true);
							mg.addAnnotation(new AnnotationX(ag.getAnnotation(),weaver.getWorld()));
						}
					}
			  }

			  if (mungingInterface) {
				// we want the modifiers of the ITD to be used for all *implementors* of the
				// interface, but the method itself we add to the interface must be public abstract
				mg.setAccessFlags(Modifier.PUBLIC | Modifier.ABSTRACT);
			  }
						
			  Type[] paramTypes = BcelWorld.makeBcelTypes(mangledInterMethod.getParameterTypes());
			  Type returnType = BcelWorld.makeBcelType(mangledInterMethod.getReturnType());
			
			  InstructionList body = mg.getBody();
			  InstructionFactory fact = gen.getFactory();
			  int pos = 0;

			  if (!mangledInterMethod.isStatic()) {
				body.append(InstructionFactory.createThis());
				pos++;
			  }
			  for (int i = 0, len = paramTypes.length; i < len; i++) {
				Type paramType = paramTypes[i];
				body.append(InstructionFactory.createLoad(paramType, pos));
				pos+=paramType.getSize();
			  }
			 
			  body.append(Utility.createInvoke(fact, weaver.getWorld(), interMethodBody));
			  Type t= BcelWorld.makeBcelType(interMethodBody.getReturnType());
			  if (!t.equals(returnType)) {
				  body.append(fact.createCast(t,returnType));
			  }
			  body.append(InstructionFactory.createReturn(returnType));
			  mg.definingType = onType;
			
			  weaver.addOrReplaceLazyMethodGen(mg);
			
			  addNeededSuperCallMethods(weaver, onType, munger.getSuperMethodsCalled());

			  // Work out if we need a bridge method for the new method added to the topmostimplementor.
			  if (munger.getDeclaredSignature()!=null) { // Check if the munger being processed is a parameterized form of some original munger.
				boolean needsbridging = false;
			    ResolvedMember toBridgeTo = munger.getDeclaredSignature().parameterizedWith(null,munger.getSignature().getDeclaringType().resolve(getWorld()),false,munger.getTypeVariableAliases());
				if (!toBridgeTo.getReturnType().getErasureSignature().equals(munger.getSignature().getReturnType().getErasureSignature())) needsbridging = true;
				UnresolvedType[] originalParams = toBridgeTo.getParameterTypes();
				UnresolvedType[] newParams = munger.getSignature().getParameterTypes();
				for (int ii = 0;ii<originalParams.length;ii++) {
					if (!originalParams[ii].getErasureSignature().equals(newParams[ii].getErasureSignature())) needsbridging=true;
				}
				if (toBridgeTo!=null && needsbridging) {
					ResolvedMember bridgerMethod = AjcMemberMaker.bridgerToInterMethod(unMangledInterMethod,gen.getType());
					ResolvedMember bridgingSetter = AjcMemberMaker.interMethod(toBridgeTo, aspectType, false);
					
					// FIXME asc ----------------8<---------------- extract method
					LazyMethodGen bridgeMethod = makeMethodGen(gen,bridgingSetter);	
					paramTypes = BcelWorld.makeBcelTypes(bridgingSetter.getParameterTypes());
					Type[] bridgingToParms = BcelWorld.makeBcelTypes(unMangledInterMethod.getParameterTypes());
					returnType   = BcelWorld.makeBcelType(bridgingSetter.getReturnType());
					body = bridgeMethod.getBody();
					fact = gen.getFactory();
					pos = 0;

				    if (!bridgingSetter.isStatic()) {
					  body.append(InstructionFactory.createThis());
					  pos++;
				    }
				    for (int i = 0, len = paramTypes.length; i < len; i++) {
				 	  Type paramType = paramTypes[i];
					  body.append(InstructionFactory.createLoad(paramType, pos));
					  if (!bridgingSetter.getParameterTypes()[i].getErasureSignature().equals(unMangledInterMethod.getParameterTypes()[i].getErasureSignature()) ) {
//						System.err.println("Putting in cast from "+paramType+" to "+bridgingToParms[i]);
						body.append(fact.createCast(paramType,bridgingToParms[i]));
					  }
					  pos+=paramType.getSize();
				    }
				 
				    body.append(Utility.createInvoke(fact, weaver.getWorld(), bridgerMethod));
				    body.append(InstructionFactory.createReturn(returnType));
				    gen.addMethodGen(bridgeMethod);
//			        mg.definingType = onType;
				    // FIXME asc (see above) ---------------------8<--------------- extract method
				}
			  
				}			  
			  
			  
			  return true;
    		}
		} else {
			return false;
		}
	}
	
	/**
	 * Create any bridge method required because of covariant returns being used.  This method is used in the case
	 * where an ITD is applied to some type and it may be in an override relationship with a method from the supertype - but
	 * due to covariance there is a mismatch in return values.
	 * Example of when required:
		 Super defines:   Object m(String s)
		   Sub defines:   String m(String s) 
		   then we need a bridge method in Sub called 'Object m(String s)' that forwards to 'String m(String s)'
	 */
	private void createAnyBridgeMethodsForCovariance(BcelClassWeaver weaver, NewMethodTypeMunger munger, ResolvedMember unMangledInterMethod, ResolvedType onType, LazyClassGen gen, Type[] paramTypes) {
		// PERFORMANCE BOTTLENECK? Might need investigating, method analysis between types in a hierarchy just seems expensive...
		// COVARIANCE BRIDGING
		// Algorithm:  Step1. Check in this type - has someone already created the bridge method?
		//             Step2. Look above us - do we 'override' a method and yet differ in return type (i.e. covariance)
		//             Step3. Create a forwarding bridge method
		ResolvedType superclass = onType.getSuperclass();
		boolean quitRightNow = false;
		
		String localMethodName    = unMangledInterMethod.getName();
		String localParameterSig  = unMangledInterMethod.getParameterSignature();
		String localReturnTypeESig = unMangledInterMethod.getReturnType().getErasureSignature(); 

		// Step1
		boolean alreadyDone = false; // Compiler might have done it
		ResolvedMember[] localMethods = onType.getDeclaredMethods();
		for (int i = 0; i < localMethods.length; i++) {
			ResolvedMember member = localMethods[i];
			if (member.getName().equals(localMethodName)) {
				// Check the params
				if (member.getParameterSignature().equals(localParameterSig)) alreadyDone = true;
			}
		}
		
		// Step2
		if (!alreadyDone) {
			// Use the iterator form of 'getMethods()' so we do as little work as necessary
			for (Iterator iter = onType.getSuperclass().getMethods();iter.hasNext() && !quitRightNow;) {
				ResolvedMember aMethod = (ResolvedMember) iter.next();
				if (aMethod.getName().equals(localMethodName) && aMethod.getParameterSignature().equals(localParameterSig)) {
					// check the return types, if they are different we need a bridging method.
					if (!aMethod.getReturnType().getErasureSignature().equals(localReturnTypeESig) && !Modifier.isPrivate(aMethod.getModifiers())) {
						// Step3
						createBridgeMethod(weaver.getWorld(), munger, unMangledInterMethod, gen, paramTypes, aMethod);
						quitRightNow = true;
					}
				}
			}
		}
	}

	/**
	 * Create a bridge method for a particular munger.  
	 * @param world
	 * @param munger
	 * @param unMangledInterMethod the method to bridge 'to' that we have already created in the 'subtype'
	 * @param clazz the class in which to put the bridge method
	 * @param paramTypes Parameter types for the bridge method, passed in as an optimization since the caller is likely to have already created them.
	 * @param theBridgeMethod 
	 */
	private void createBridgeMethod(BcelWorld world, NewMethodTypeMunger munger, 
			ResolvedMember unMangledInterMethod, LazyClassGen clazz, Type[] paramTypes, ResolvedMember theBridgeMethod) {
		InstructionList body;
		InstructionFactory fact;
		int pos = 0;
		
		LazyMethodGen bridgeMethod = makeMethodGen(clazz,theBridgeMethod); // The bridge method in this type will have the same signature as the one in the supertype
		bridgeMethod.setAccessFlags(bridgeMethod.getAccessFlags() | 0x00000040 /*BRIDGE    = 0x00000040*/ );
		UnresolvedType[] newParams = munger.getSignature().getParameterTypes();
		Type returnType   = BcelWorld.makeBcelType(theBridgeMethod.getReturnType());
		body = bridgeMethod.getBody();
		fact = clazz.getFactory();

		if (!unMangledInterMethod.isStatic()) {
		   body.append(InstructionFactory.createThis());
		   pos++;
		}
		for (int i = 0, len = paramTypes.length; i < len; i++) {
		  Type paramType = paramTypes[i];
		  body.append(InstructionFactory.createLoad(paramType, pos));
//          if (!bridgingSetter.getParameterTypes()[i].getErasureSignature().equals(unMangledInterMethod.getParameterTypes()[i].getErasureSignature())) {
//            System.err.println("Putting in cast from "+paramType+" to "+bridgingToParms[i]);
//            body.append(fact.createCast(paramType,bridgingToParms[i]));
//          }
		  pos+=paramType.getSize();
		}

		body.append(Utility.createInvoke(fact, world,unMangledInterMethod));
		body.append(InstructionFactory.createReturn(returnType));
		clazz.addMethodGen(bridgeMethod);
	}
	
	// Unlike toString() on a member, this does not include the declaring type
	private String stringifyMember(ResolvedMember member) {
		StringBuffer buf = new StringBuffer();
    	buf.append(member.getReturnType().getName());
    	buf.append(' ');
   		buf.append(member.getName());
    	if (member.getKind() != Member.FIELD) {
    		buf.append("(");
    		UnresolvedType[] params = member.getParameterTypes();
            if (params.length != 0) {
                buf.append(params[0]);
        		for (int i=1, len = params.length; i < len; i++) {
                    buf.append(", ");
        		    buf.append(params[i].getName());
        		}
            }
    		buf.append(")");
    	}
    	return buf.toString();
	}
	
    private boolean mungeMethodDelegate(BcelClassWeaver weaver, MethodDelegateTypeMunger munger) {
        ResolvedMember introduced = munger.getSignature();

        LazyClassGen gen = weaver.getLazyClassGen();

        ResolvedType fromType = weaver.getWorld().resolve(introduced.getDeclaringType(),munger.getSourceLocation());
        if (fromType.isRawType()) fromType = fromType.getGenericType();

        if (gen.getType().isAnnotation() || gen.getType().isEnum()) {
            // don't signal error as it could be a consequence of a wild type pattern
            return false;
        }

        boolean shouldApply = munger.matches(weaver.getLazyClassGen().getType(), aspectType);
        if (shouldApply) {
        	
        	// If no implementation class was specified, the intention was that the types matching the pattern
        	// already implemented the interface, let's check that now!
        	if (munger.getImplClassName()==null) {
        		boolean isOK = false;
        		List/*LazyMethodGen*/ existingMethods = gen.getMethodGens();
        		for (Iterator i = existingMethods.iterator(); i.hasNext() && !isOK;) {
        		    LazyMethodGen m = (LazyMethodGen) i.next();
        		    if (m.getName().equals(introduced.getName()) &&  
        		        m.getParameterSignature().equals(introduced.getParameterSignature()) &&
        		        m.getReturnType().equals(introduced.getReturnType())) {
        		    	isOK = true;
        		    }
        		}        
        		if (!isOK) {
        			// the class does not implement this method, they needed to supply a default impl class
        			IMessage msg = new Message("@DeclareParents: No defaultImpl was specified but the type '"+gen.getName()+
        					"' does not implement the method '"+stringifyMember(introduced)+"' defined on the interface '"+introduced.getDeclaringType()+"'",
        					weaver.getLazyClassGen().getType().getSourceLocation(),true,new ISourceLocation[]{munger.getSourceLocation()});
        			weaver.getWorld().getMessageHandler().handleMessage(msg);
        			return false;
        		}
        		
        		return true;
        	}
        	
        	
        	
            LazyMethodGen mg = new LazyMethodGen(
                    introduced.getModifiers() - Modifier.ABSTRACT,
                    BcelWorld.makeBcelType(introduced.getReturnType()),
                    introduced.getName(),
                    BcelWorld.makeBcelTypes(introduced.getParameterTypes()),
                    BcelWorld.makeBcelTypesAsClassNames(introduced.getExceptions()),
                    gen
            );

            //annotation copy from annotation on ITD interface
            if (weaver.getWorld().isInJava5Mode()){
                AnnotationX annotationsOnRealMember[] = null;
                ResolvedType toLookOn = weaver.getWorld().lookupOrCreateName(introduced.getDeclaringType());
                if (fromType.isRawType()) toLookOn = fromType.getGenericType();
                // lookup the method
                ResolvedMember[] ms = toLookOn.getDeclaredJavaMethods();
                for (int i = 0; i < ms.length; i++) {
                    ResolvedMember m = ms[i];
                    if (introduced.getName().equals(m.getName()) && introduced.getSignature().equals(m.getSignature())) {
                        annotationsOnRealMember = m.getAnnotations();
                    }
                }
                if (annotationsOnRealMember!=null) {
                    for (int i = 0; i < annotationsOnRealMember.length; i++) {
                        AnnotationX annotationX = annotationsOnRealMember[i];
                        Annotation a = annotationX.getBcelAnnotation();
                        AnnotationGen ag = new AnnotationGen(a,weaver.getLazyClassGen().getConstantPoolGen(),true);
                        mg.addAnnotation(new AnnotationX(ag.getAnnotation(),weaver.getWorld()));
                    }
                }
            }

            InstructionList body = new InstructionList();
            InstructionFactory fact = gen.getFactory();

            // getfield
            body.append(InstructionConstants.ALOAD_0);
            body.append(Utility.createGet(fact, munger.getDelegate(weaver.getLazyClassGen().getType())));
            BranchInstruction ifNonNull = InstructionFactory.createBranchInstruction(Constants.IFNONNULL, null);
            body.append(ifNonNull);
            
            // Create and store a new instance
           	body.append(InstructionConstants.ALOAD_0);
            body.append(fact.createNew(munger.getImplClassName()));
            body.append(InstructionConstants.DUP);
            body.append(fact.createInvoke(munger.getImplClassName(), "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
            body.append(Utility.createSet(fact, munger.getDelegate(weaver.getLazyClassGen().getType())));
            
            // if not null use the instance we've got
            InstructionHandle ifNonNullElse =  body.append(InstructionConstants.ALOAD_0);
            ifNonNull.setTarget(ifNonNullElse);
            body.append(Utility.createGet(fact, munger.getDelegate(weaver.getLazyClassGen().getType())));

            //args
            int pos = 0;
    		if (!introduced.isStatic()) { // skip 'this' (?? can this really happen)
    		  //body.append(InstructionFactory.createThis());
    		  pos++;
    		}
            Type[] paramTypes = BcelWorld.makeBcelTypes(introduced.getParameterTypes());
            for (int i = 0, len = paramTypes.length; i < len; i++) {
                Type paramType = paramTypes[i];
                body.append(InstructionFactory.createLoad(paramType, pos));
                pos+=paramType.getSize();
            }
            body.append(Utility.createInvoke(fact, Constants.INVOKEINTERFACE, introduced));
            body.append(
                InstructionFactory.createReturn(
                    BcelWorld.makeBcelType(introduced.getReturnType())
                )
            );

            mg.getBody().append(body);
            weaver.addLazyMethodGen(mg);
            weaver.getLazyClassGen().warnOnAddedMethod(mg.getMethod(),getSignature().getSourceLocation());
            return true;
        }
        return false;
    }

    private boolean mungeFieldHost(BcelClassWeaver weaver, MethodDelegateTypeMunger.FieldHostTypeMunger munger) {
        LazyClassGen gen = weaver.getLazyClassGen();
        if (gen.getType().isAnnotation() || gen.getType().isEnum()) {
            // don't signal error as it could be a consequence of a wild type pattern
            return false;
        }
        boolean shouldApply = munger.matches(weaver.getLazyClassGen().getType(), aspectType);
        ResolvedMember host = AjcMemberMaker.itdAtDeclareParentsField(
                weaver.getLazyClassGen().getType(),
                munger.getSignature().getType(),
                aspectType);
        weaver.getLazyClassGen().addField(makeFieldGen(
                weaver.getLazyClassGen(),
                host).getField(), null);
        return true;
    }


    private ResolvedMember getRealMemberForITDFromAspect(ResolvedType aspectType,ResolvedMember lookingFor,boolean isCtorRelated) {
		World world = aspectType.getWorld();
		boolean debug = false;
		if (debug) {
			System.err.println("Searching for a member on type: "+aspectType);
			System.err.println("Member we are looking for: "+lookingFor);
		}

		ResolvedMember aspectMethods[] = aspectType.getDeclaredMethods();
		UnresolvedType [] lookingForParams = lookingFor.getParameterTypes();
		
		ResolvedMember realMember = null;
		for (int i = 0; realMember==null && i < aspectMethods.length; i++) {
			ResolvedMember member = aspectMethods[i];
			if (member.getName().equals(lookingFor.getName())){
				UnresolvedType [] memberParams = member.getGenericParameterTypes();
				if (memberParams.length == lookingForParams.length){
					if (debug) System.err.println("Reviewing potential candidates: "+member);
					boolean matchOK = true;
					// If not related to a ctor ITD then the name is enough to confirm we have the
					// right one.  If it is ctor related we need to check the params all match, although
					// only the erasure.
					if (isCtorRelated) {
					  for (int j = 0; j < memberParams.length && matchOK; j++){
						ResolvedType pMember = memberParams[j].resolve(world);
						ResolvedType pLookingFor = lookingForParams[j].resolve(world); 
						
						if (pMember.isTypeVariableReference()) 
							pMember = ((TypeVariableReference)pMember).getTypeVariable().getFirstBound().resolve(world);
						if (pMember.isParameterizedType() || pMember.isGenericType()) 
							pMember = pMember.getRawType().resolve(aspectType.getWorld());
						
						if (pLookingFor.isTypeVariableReference()) 
							pLookingFor = ((TypeVariableReference)pLookingFor).getTypeVariable().getFirstBound().resolve(world);
						if (pLookingFor.isParameterizedType() || pLookingFor.isGenericType()) 
							pLookingFor = pLookingFor.getRawType().resolve(world);
						
						if (debug) System.err.println("Comparing parameter "+j+"   member="+pMember+"   lookingFor="+pLookingFor);
						if (!pMember.equals(pLookingFor)){
							matchOK=false;
						}
					  }
					}
					if (matchOK) realMember = member;
				}
			}
		}
		if (debug && realMember==null) System.err.println("Didn't find a match");
		return realMember;
	}

	private void addNeededSuperCallMethods(
		BcelClassWeaver weaver,
		ResolvedType onType,
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

	private void signalError(String msgid,BcelClassWeaver weaver,UnresolvedType onType) {
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
		ResolvedType   onType = newConstructorMember.getDeclaringType().resolve(weaver.getWorld());
		if (onType.isRawType()) onType = onType.getGenericType();
		
		if (onType.isAnnotation()) {
			signalError(WeaverMessages.ITDC_ON_ANNOTATION_NOT_ALLOWED,weaver,onType);
			return false;
		}
		
		if (onType.isEnum()) {
			signalError(WeaverMessages.ITDC_ON_ENUM_NOT_ALLOWED,weaver,onType);
			return false;
		}
		
		if (! onType.equals(currentClass.getType())) return false;

		ResolvedMember explicitConstructor = newConstructorTypeMunger.getExplicitConstructor();
		//int declaredParameterCount = newConstructorTypeMunger.getDeclaredParameterCount();
		LazyMethodGen mg = 
			makeMethodGen(currentClass, newConstructorMember);
		mg.setEffectiveSignature(newConstructorTypeMunger.getSignature(),Shadow.ConstructorExecution,true);
		
//		 pr98901
	    // For copying the annotations across, we have to discover the real member in the aspect
	    // which is holding them.
		if (weaver.getWorld().isInJava5Mode()){
			
			ResolvedMember interMethodDispatcher =AjcMemberMaker.postIntroducedConstructor(aspectType,onType,newConstructorTypeMunger.getSignature().getParameterTypes());
			AnnotationX annotationsOnRealMember[] = null;
			ResolvedMember realMember = getRealMemberForITDFromAspect(aspectType,interMethodDispatcher,true);
			if (realMember==null) throw new BCException("Couldn't find ITD holder member '"+
					interMethodDispatcher+"' on aspect "+aspectType);
			annotationsOnRealMember = realMember.getAnnotations();
			if (annotationsOnRealMember!=null) {
				for (int i = 0; i < annotationsOnRealMember.length; i++) {
					AnnotationX annotationX = annotationsOnRealMember[i];
					Annotation a = annotationX.getBcelAnnotation();
					AnnotationGen ag = new AnnotationGen(a,weaver.getLazyClassGen().getConstantPoolGen(),true);
					mg.addAnnotation(new AnnotationX(ag.getAnnotation(),weaver.getWorld()));
				}
			}
			// the below loop fixes the very special (and very stupid)
			// case where an aspect declares an annotation
			// on an ITD it declared on itself.
			List allDecams = weaver.getWorld().getDeclareAnnotationOnMethods();
			for (Iterator i = allDecams.iterator(); i.hasNext();){
				DeclareAnnotation decaMC = (DeclareAnnotation) i.next();	
				if (decaMC.matches(explicitConstructor,weaver.getWorld())
						&& mg.getEnclosingClass().getType() == aspectType) {
					mg.addAnnotation(decaMC.getAnnotationX());
				}
			}
		}
		
		currentClass.addMethodGen(mg);
		//weaver.addLazyMethodGen(freshConstructor);
		
		InstructionList body = mg.getBody();
		
		// add to body:  push arts for call to pre, from actual args starting at 1 (skipping this), going to 
		//               declared argcount + 1
		UnresolvedType[] declaredParams = newConstructorTypeMunger.getSignature().getParameterTypes();
		Type[] paramTypes = mg.getArgumentTypes();
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
		int arraySlot = mg.allocateLocal(1);
		body.append(InstructionFactory.createStore(Type.OBJECT, arraySlot));
		
		// put this on the stack
		body.append(InstructionConstants.ALOAD_0);
		
		// unpack pre args onto stack
		UnresolvedType[] superParamTypes = explicitConstructor.getParameterTypes();
		
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
		UnresolvedType[] postParamTypes = postMethod.getParameterTypes();
		
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
					UnresolvedType.getNames(superMethod.getExceptions()),
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
		
		ResolvedType onType = weaver.getWorld().resolve(field.getDeclaringType(),munger.getSourceLocation());
		if (onType.isRawType()) onType = onType.getGenericType();

		boolean onInterface = onType.isInterface();
		
		if (onType.isAnnotation()) {
			signalError(WeaverMessages.ITDF_ON_ANNOTATION_NOT_ALLOWED,weaver,onType);
			return false;
		}
		
		if (onType.isEnum()) {
			signalError(WeaverMessages.ITDF_ON_ENUM_NOT_ALLOWED,weaver,onType);
			return false;
		}
		
		ResolvedMember interMethodBody = munger.getInitMethod(aspectType);
		
		AnnotationX annotationsOnRealMember[] = null;
		// pr98901
	    // For copying the annotations across, we have to discover the real member in the aspect
	    // which is holding them.
		if (weaver.getWorld().isInJava5Mode()){
				// the below line just gets the method with the same name in aspectType.getDeclaredMethods();
				ResolvedType toLookOn = aspectType;
				if (aspectType.isRawType()) toLookOn = aspectType.getGenericType();
				ResolvedMember realMember = getRealMemberForITDFromAspect(toLookOn,interMethodBody,false);
				if (realMember==null) throw new BCException("Couldn't find ITD init member '"+
						interMethodBody+"' on aspect "+aspectType);
				annotationsOnRealMember = realMember.getAnnotations();
		}
		
		if (onType.equals(gen.getType())) {
			if (onInterface) {
				ResolvedMember itdfieldGetter = AjcMemberMaker.interFieldInterfaceGetter(field, onType, aspectType);
				LazyMethodGen mg = makeMethodGen(gen, itdfieldGetter);
				gen.addMethodGen(mg);
				
				LazyMethodGen mg1 = makeMethodGen(gen, 
					AjcMemberMaker.interFieldInterfaceSetter(field, onType, aspectType));
				gen.addMethodGen(mg1);
			} else {
				weaver.addInitializer(this);
				FieldGen fg = makeFieldGen(gen,
					AjcMemberMaker.interFieldClassField(field, aspectType));
				
				if (annotationsOnRealMember!=null) {
					for (int i = 0; i < annotationsOnRealMember.length; i++) {
						AnnotationX annotationX = annotationsOnRealMember[i];
						Annotation a = annotationX.getBcelAnnotation();
						AnnotationGen ag = new AnnotationGen(a,weaver.getLazyClassGen().getConstantPoolGen(),true);	
						fg.addAnnotation(ag);
					}
				}
				
				gen.addField(fg.getField(),getSourceLocation());
	    		
			}
    		return true;
		} else if (onInterface && gen.getType().isTopmostImplementor(onType)) {
			// wew know that we can't be static since we don't allow statics on interfaces
			if (field.isStatic()) throw new RuntimeException("unimplemented");
			weaver.addInitializer(this);
			//System.err.println("impl body on " + gen.getType() + " for " + munger);
			
			
			Type fieldType = 	BcelWorld.makeBcelType(field.getType());
				
			FieldGen fg = makeFieldGen(gen,AjcMemberMaker.interFieldInterfaceField(field, onType, aspectType));
		    	
				if (annotationsOnRealMember!=null) {
					for (int i = 0; i < annotationsOnRealMember.length; i++) {
						AnnotationX annotationX = annotationsOnRealMember[i];
						Annotation a = annotationX.getBcelAnnotation();
						AnnotationGen ag = new AnnotationGen(a,weaver.getLazyClassGen().getConstantPoolGen(),true);	
						fg.addAnnotation(ag);
					}
				}
				
		    	gen.addField(fg.getField(),getSourceLocation());
	    	//this uses a shadow munger to add init method to constructors
	    	//weaver.getShadowMungers().add(makeInitCallShadowMunger(initMethod));
	    	
	    	ResolvedMember itdfieldGetter = AjcMemberMaker.interFieldInterfaceGetter(field, gen.getType()/*onType*/, aspectType);
			LazyMethodGen mg = makeMethodGen(gen, itdfieldGetter);
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
						
			// Check if we need bridge methods for the field getter and setter
			if (munger.getDeclaredSignature()!=null) { // is this munger a parameterized form of some original munger?
				ResolvedMember toBridgeTo = munger.getDeclaredSignature().parameterizedWith(null,munger.getSignature().getDeclaringType().resolve(getWorld()),false,munger.getTypeVariableAliases());
				boolean needsbridging = false;
				if (!toBridgeTo.getReturnType().getErasureSignature().equals(munger.getSignature().getReturnType().getErasureSignature())) needsbridging = true;
				if (toBridgeTo!=null && needsbridging) {
				  ResolvedMember bridgingGetter = AjcMemberMaker.interFieldInterfaceGetter(toBridgeTo, gen.getType(), aspectType);				  
				  createBridgeMethodForITDF(weaver,gen,itdfieldGetter,bridgingGetter);
			  }
			}		
			
			ResolvedMember itdfieldSetter = AjcMemberMaker.interFieldInterfaceSetter(field, gen.getType(), aspectType);
			LazyMethodGen mg1 = makeMethodGen(gen, itdfieldSetter);
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
			
			if (munger.getDeclaredSignature()!=null) {
			  ResolvedMember toBridgeTo = munger.getDeclaredSignature().parameterizedWith(null,munger.getSignature().getDeclaringType().resolve(getWorld()),false,munger.getTypeVariableAliases());
			  boolean needsbridging = false;
			  if (!toBridgeTo.getReturnType().getErasureSignature().equals(munger.getSignature().getReturnType().getErasureSignature())) needsbridging = true;
			  if (toBridgeTo!=null && needsbridging) {
				ResolvedMember bridgingSetter = AjcMemberMaker.interFieldInterfaceSetter(toBridgeTo, gen.getType(), aspectType);
				createBridgeMethodForITDF(weaver, gen, itdfieldSetter, bridgingSetter);
			  }
		    }
			
		    return true;
		} else {
			return false;
		}
	}

	// FIXME asc combine with other createBridge.. method in this class, avoid the duplication...
	private void createBridgeMethodForITDF(BcelClassWeaver weaver, LazyClassGen gen, ResolvedMember itdfieldSetter, ResolvedMember bridgingSetter) {
		InstructionFactory fact;
		LazyMethodGen bridgeMethod = makeMethodGen(gen,bridgingSetter);	
		Type[] paramTypes = BcelWorld.makeBcelTypes(bridgingSetter.getParameterTypes());
		  Type[] bridgingToParms = BcelWorld.makeBcelTypes(itdfieldSetter.getParameterTypes());
		  Type returnType   = BcelWorld.makeBcelType(bridgingSetter.getReturnType());
		  InstructionList body = bridgeMethod.getBody();
		  fact = gen.getFactory();
		  int pos = 0;

		  if (!bridgingSetter.isStatic()) {
			body.append(InstructionFactory.createThis());
			pos++;
		  }
		  for (int i = 0, len = paramTypes.length; i < len; i++) {
			Type paramType = paramTypes[i];
			body.append(InstructionFactory.createLoad(paramType, pos));
			if (!bridgingSetter.getParameterTypes()[i].getErasureSignature().equals(itdfieldSetter.getParameterTypes()[i].getErasureSignature()) ) {
				// une cast est required
				System.err.println("Putting in cast from "+paramType+" to "+bridgingToParms[i]);
				body.append(fact.createCast(paramType,bridgingToParms[i]));
			}
			pos+=paramType.getSize();
		  }
		 
		  body.append(Utility.createInvoke(fact, weaver.getWorld(), itdfieldSetter));
		  body.append(InstructionFactory.createReturn(returnType));
		  gen.addMethodGen(bridgeMethod);
	}
	
	public ConcreteTypeMunger parameterizedFor(ResolvedType target) {
		return new BcelTypeMunger(munger.parameterizedFor(target),aspectType);
	}

	public ConcreteTypeMunger parameterizeWith(Map m, World w) {
		return new BcelTypeMunger(munger.parameterizeWith(m,w),aspectType);
	}
	
	/**
	 * Returns a list of type variable aliases used in this munger.  For example, if the
	 * ITD is 'int I<A,B>.m(List<A> las,List<B> lbs) {}' then this returns a list containing
	 * the strings "A" and "B".
	 */
	public List /*String*/ getTypeVariableAliases() {
		return munger.getTypeVariableAliases();
	}
	
	public boolean equals(Object other) {
        if (! (other instanceof BcelTypeMunger))  return false;
        BcelTypeMunger o = (BcelTypeMunger) other;
        return ((o.getMunger() == null) ? (getMunger() == null) : o.getMunger().equals(getMunger()))
               && ((o.getAspectType() == null) ? (getAspectType() == null) : o.getAspectType().equals(getAspectType()))
               && (AsmManager.getDefault().getHandleProvider().dependsOnLocation()
            		   ?((o.getSourceLocation()==null)? (getSourceLocation()==null): o.getSourceLocation().equals(getSourceLocation())):true); // pr134471 - remove when handles are improved to be independent of location
               
    }
	   
    private volatile int hashCode = 0;
    public int hashCode() {
       if (hashCode == 0) {
            int result = 17;
	        result = 37*result + ((getMunger() == null) ? 0 : getMunger().hashCode());
	        result = 37*result + ((getAspectType() == null) ? 0 : getAspectType().hashCode());
            hashCode = result;
       }
       return hashCode;
    }
	
	
	/**
	 * Some type mungers are created purely to help with the implementation of shadow mungers.  
	 * For example to support the cflow() pointcut we create a new cflow field in the aspect, and
	 * that is added via a BcelCflowCounterFieldAdder.
	 * 
	 * During compilation we need to compare sets of type mungers, and if some only come into
	 * existence after the 'shadowy' type things have been processed, we need to ignore
	 * them during the comparison.
	 * 
	 * Returning true from this method indicates the type munger exists to support 'shadowy' stuff -
	 * and so can be ignored in some comparison.
	 */
	public boolean existsToSupportShadowMunging() {
		if (munger != null) {
			return munger.existsToSupportShadowMunging();
		}
		return false; 
	}
}
		