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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.asm.StructureModel;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.weaver.ResolvedTypeX.Name;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.Pointcut;

public abstract class World {
	protected IMessageHandler messageHandler = IMessageHandler.SYSTEM_ERR;

    protected Map typeMap = new HashMap(); // Signature to ResolvedType
    
    protected CrosscuttingMembersSet crosscuttingMembersSet = new CrosscuttingMembersSet(this);
    
    protected StructureModel model = null;
    
    protected Lint lint = new Lint(this);
    
    protected boolean XnoInline;
	
    protected World() {
        super();
        typeMap.put("B", ResolvedTypeX.BYTE);
        typeMap.put("S", ResolvedTypeX.SHORT);
        typeMap.put("I", ResolvedTypeX.INT);
        typeMap.put("J", ResolvedTypeX.LONG);
        typeMap.put("F", ResolvedTypeX.FLOAT);
        typeMap.put("D", ResolvedTypeX.DOUBLE);
        typeMap.put("C", ResolvedTypeX.CHAR);
        typeMap.put("Z", ResolvedTypeX.BOOLEAN);
        typeMap.put("V", ResolvedTypeX.VOID);
    }

    public ResolvedTypeX[] resolve(TypeX[] types) {
        int len = types.length;
        ResolvedTypeX[] ret = new ResolvedTypeX[len];
        for (int i=0; i<len; i++) {
            ret[i] = resolve(types[i]);
        }
        return ret;
    }
    
    public ResolvedTypeX resolve(TypeX ty) {
    	return resolve(ty, false);
    }

    public ResolvedTypeX resolve(TypeX ty, boolean allowMissing) {
    	//System.out.println("resolve: " + ty + " world " + typeMap.keySet());
        String signature = ty.getSignature();
        ResolvedTypeX ret = (ResolvedTypeX)typeMap.get(signature);
        if (ret != null) return ret;
        
        if (ty.isArray()) {
            ret = new ResolvedTypeX.Array(signature, this, resolve(ty.getComponentType(), allowMissing));
        } else {
            ret = resolveObjectType(ty);
            if (!allowMissing && ret == ResolvedTypeX.MISSING) {
            	//Thread.currentThread().dumpStack();
                MessageUtil.error(messageHandler, "can't find type " + ty.getName());
                // + " on classpath " + classPath);
            }
        }
        //System.out.println("ret: " + ret);
        typeMap.put(signature, ret);
        return ret;
    }
    
    //XXX helper method might be bad
    public ResolvedTypeX resolve(String name) {
    	return resolve(TypeX.forName(name));
    }
    protected final ResolvedTypeX resolveObjectType(TypeX ty) {
    	ResolvedTypeX.Name name = new ResolvedTypeX.Name(ty.getSignature(), this);
    	ResolvedTypeX.ConcreteName concreteName = resolveObjectType(name);
    	if (concreteName == null) return ResolvedTypeX.MISSING;
    	name.setDelegate(concreteName);
    	return name;
    }
    
    protected abstract ResolvedTypeX.ConcreteName resolveObjectType(ResolvedTypeX.Name ty);
    

    protected final boolean isCoerceableFrom(TypeX type, TypeX other) {
        return resolve(type).isCoerceableFrom(other);
    }

    protected final boolean isAssignableFrom(TypeX type, TypeX other) {
        return resolve(type).isAssignableFrom(other);
    }

    public boolean needsNoConversionFrom(TypeX type, TypeX other) {
        return resolve(type).needsNoConversionFrom(other);
    }
    
    protected final boolean isInterface(TypeX type) {
        return resolve(type).isInterface();
    }

    protected final ResolvedTypeX getSuperclass(TypeX type) {
        return resolve(type).getSuperclass();
    }

    protected final TypeX[] getDeclaredInterfaces(TypeX type) {
        return resolve(type).getDeclaredInterfaces();
    }

    protected final int getModifiers(TypeX type) {
        return resolve(type).getModifiers();
    }

    protected final ResolvedMember[] getDeclaredFields(TypeX type) {
        return resolve(type).getDeclaredFields();
    }

    protected final ResolvedMember[] getDeclaredMethods(TypeX type) {
        return resolve(type).getDeclaredMethods();
    }

    protected final ResolvedMember[] getDeclaredPointcuts(TypeX type) {
        return resolve(type).getDeclaredPointcuts();
    }

    // ---- members


    // XXX should we worry about dealing with context and looking up access?
    public ResolvedMember resolve(Member member) {
        ResolvedTypeX declaring = member.getDeclaringType().resolve(this);
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

    protected TypeX[] getExceptions(Member member) {
        return resolve(member).getExceptions();
    }

    // ---- pointcuts

    public ResolvedPointcutDefinition findPointcut(TypeX typeX, String name) {
        throw new RuntimeException("not implemented yet");
    }
    
    /**
     * Get the shadow mungers of this world.
     * 
     * @return a list of {@link IShadowMunger}s appropriate for this world.
     */      
    //public abstract List getShadowMungers();
    
    // ---- empty world
    
    public static final World EMPTY = new World() {
        public List getShadowMungers() { return Collections.EMPTY_LIST; }
        public ResolvedTypeX.ConcreteName resolveObjectType(ResolvedTypeX.Name ty) {
            return null;
        }
        public Advice concreteAdvice(AjAttribute.AdviceAttribute attribute, Pointcut p, Member m) {
            throw new RuntimeException("unimplemented");
        }
        public ConcreteTypeMunger concreteTypeMunger(ResolvedTypeMunger munger, ResolvedTypeX aspectType) {
            throw new RuntimeException("unimplemented");
        }        
    };
    
    
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

    public abstract ConcreteTypeMunger concreteTypeMunger(ResolvedTypeMunger munger, ResolvedTypeX aspectType);

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


	

//	public void addDeclare(ResolvedTypeX onType, Declare declare, boolean forWeaving) {
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
	public int compareByDominates(ResolvedTypeX aspect1, ResolvedTypeX aspect2) {
		//System.out.println("dom compare: " + aspect1 + " with " + aspect2);
		//System.out.println(crosscuttingMembersSet.getDeclareDominates());
		
		//??? We probably want to cache this result.  This is order N where N is the
		//??? number of dominates declares in the whole system.
		//??? This method can be called a large number of times.
		int order = 0;
		for (Iterator i = crosscuttingMembersSet.getDeclareDominates().iterator(); i.hasNext(); ) {
			DeclarePrecedence d = (DeclarePrecedence)i.next();
			int thisOrder = d.compare(aspect1, aspect2);
			//System.out.println("comparing: " + thisOrder + ": " + d);
			if (thisOrder != 0) {
				if (order != 0 && order != thisOrder) {
					throw new BCException("conflicting dominates orders");
				} else {
					order = thisOrder;
				}
			}
		}
		
		
		return order; 
	}
	
	
	public int comparePrecedence(ResolvedTypeX aspect1, ResolvedTypeX aspect2) {
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

	public List getDeclareSoft() {
		return crosscuttingMembersSet.getDeclareSofts();
	}

	public CrosscuttingMembersSet getCrosscuttingMembersSet() {
		return crosscuttingMembersSet;
	}

	public StructureModel getModel() {
		return model;
	}

	public void setModel(StructureModel model) {
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

	public ResolvedTypeX.Name lookupOrCreateName(TypeX ty) {
		String signature = ty.getSignature();
        ResolvedTypeX.Name ret = (ResolvedTypeX.Name)typeMap.get(signature);
        if (ret == null) {
        	ret = new ResolvedTypeX.Name(signature, this);
        	typeMap.put(signature, ret);
        }
        
		return ret;
	}

}
