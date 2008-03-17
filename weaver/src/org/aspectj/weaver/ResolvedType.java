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


package org.aspectj.weaver;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.PerClause;

public abstract class ResolvedType extends UnresolvedType implements AnnotatedElement {

	public static final ResolvedType[] EMPTY_RESOLVED_TYPE_ARRAY  = new ResolvedType[0];
	public static final String PARAMETERIZED_TYPE_IDENTIFIER = "P";
	
	// Set during a type pattern match call - this currently used to hold the annotations
	// that may be attached to a type when it used as a parameter
	public ResolvedType[] temporaryAnnotationTypes;
	private ResolvedType[] resolvedTypeParams;
	private String binaryPath;
	
    protected World world;
	    
    protected ResolvedType(String signature, World world) {
        super(signature);
        this.world = world;
    }
        
    protected ResolvedType(String signature, String signatureErasure, World world) {
        super(signature,signatureErasure);
        this.world = world;
    }

    // ---- things that don't require a world
    
    /**
     * Returns an iterator through ResolvedType objects representing all the direct
     * supertypes of this type.  That is, through the superclass, if any, and
     * all declared interfaces.
     */
    public final Iterator getDirectSupertypes() {
        Iterator ifacesIterator = Iterators.array(getDeclaredInterfaces());
        ResolvedType superclass = getSuperclass();
        if (superclass == null) {
            return ifacesIterator;
        } else {
            return Iterators.snoc(ifacesIterator, superclass);
        }
    }

    public abstract ResolvedMember[] getDeclaredFields();
    public abstract ResolvedMember[] getDeclaredMethods();
    public abstract ResolvedType[] getDeclaredInterfaces();
    public abstract ResolvedMember[] getDeclaredPointcuts();
    /**
     * Returns a ResolvedType object representing the superclass of this type, or null.
     * If this represents a java.lang.Object, a primitive type, or void, this
     * method returns null.  
     */
    public abstract ResolvedType getSuperclass();

    /**
     * Returns the modifiers for this type.  
     * <p/>
     * See {@link Class#getModifiers()} for a description
     * of the weirdness of this methods on primitives and arrays.
     *
     * @param world the {@link World} in which the lookup is made.
     * @return an int representing the modifiers for this type
     * @see     java.lang.reflect.Modifier
     */
    public abstract int getModifiers();

    // return true if this resolved type couldn't be found (but we know it's name maybe)
    public boolean isMissing() {
        return false;
    }
    
    // FIXME asc I wonder if in some circumstances MissingWithKnownSignature should not be considered 
    // 'really' missing as some code can continue based solely on the signature
    public static boolean isMissing (UnresolvedType unresolved) {
    	if (unresolved instanceof ResolvedType) {
    		ResolvedType resolved = (ResolvedType)unresolved;
    		return resolved.isMissing();
    	}
    	else return (unresolved == MISSING);
    }
    
    public ResolvedType[] getAnnotationTypes() {
    	return EMPTY_RESOLVED_TYPE_ARRAY;
    }
    
    public AnnotationX getAnnotationOfType(UnresolvedType ofType) { 
    	return null;
    }
    
    public final UnresolvedType getSuperclass(World world) {
        return getSuperclass();
    }

    
    // This set contains pairs of types whose signatures are concatenated
    // together, this means with a fast lookup we can tell if two types
    // are equivalent.
    static Set validBoxing = new HashSet();
    
    static {
      validBoxing.add("Ljava/lang/Byte;B");
      validBoxing.add("Ljava/lang/Character;C");
      validBoxing.add("Ljava/lang/Double;D");
      validBoxing.add("Ljava/lang/Float;F");
      validBoxing.add("Ljava/lang/Integer;I");
      validBoxing.add("Ljava/lang/Long;J");
      validBoxing.add("Ljava/lang/Short;S");
      validBoxing.add("Ljava/lang/Boolean;Z");
      validBoxing.add("BLjava/lang/Byte;");
      validBoxing.add("CLjava/lang/Character;");
      validBoxing.add("DLjava/lang/Double;");
      validBoxing.add("FLjava/lang/Float;");
      validBoxing.add("ILjava/lang/Integer;");
      validBoxing.add("JLjava/lang/Long;");
      validBoxing.add("SLjava/lang/Short;");
      validBoxing.add("ZLjava/lang/Boolean;");
    }
    


    // utilities                
    public ResolvedType getResolvedComponentType() {
    	return null;
    }

	public World getWorld() {
		return world;
	}

    // ---- things from object

    public final boolean equals(Object other) {
        if (other instanceof ResolvedType) {
            return this == other;
        } else {
            return super.equals(other);
        }
    }
 
    // ---- difficult things
    
    /**
     * returns an iterator through all of the fields of this type, in order
     * for checking from JVM spec 2ed 5.4.3.2.  This means that the order is
     * <p/>
     * <ul><li> fields from current class </li>
     *     <li> recur into direct superinterfaces </li>
     *     <li> recur into superclass </li>
     * </ul>
     * <p/>
     * We keep a hashSet of interfaces that we've visited so we don't spiral
     * out into 2^n land.
     */
    public Iterator getFields() {
        final Iterators.Filter dupFilter = Iterators.dupFilter();
        Iterators.Getter typeGetter = new Iterators.Getter() {
            public Iterator get(Object o) {
                return 
                    dupFilter.filter(
                        ((ResolvedType)o).getDirectSupertypes());
            }
        };
        Iterators.Getter fieldGetter = new Iterators.Getter() {
            public Iterator get(Object o) {
                return Iterators.array(((ResolvedType)o).getDeclaredFields());
            }
        };
        return 
            Iterators.mapOver(
                Iterators.recur(this, typeGetter),
                fieldGetter);
    }

    /**
     * returns an iterator through all of the methods of this type, in order
     * for checking from JVM spec 2ed 5.4.3.3.  This means that the order is
     * <p/>
     * <ul><li> methods from current class </li>
     *     <li> recur into superclass, all the way up, not touching interfaces </li>
     *     <li> recur into all superinterfaces, in some unspecified order </li>
     * </ul>
     * <p/>
     * We keep a hashSet of interfaces that we've visited so we don't spiral
     * out into 2^n land.
     * NOTE: Take a look at the javadoc on getMethodsWithoutIterator() to see if
     * you are sensitive to a quirk in getMethods()
     */
    public Iterator getMethods() {
        final Iterators.Filter dupFilter = Iterators.dupFilter();
        Iterators.Getter ifaceGetter = new Iterators.Getter() {
            public Iterator get(Object o) {
                return 
                    dupFilter.filter(
                        Iterators.array(((ResolvedType)o).getDeclaredInterfaces())
                        );                       
            }
        };
        Iterators.Getter methodGetter = new Iterators.Getter() {
            public Iterator get(Object o) {
                return Iterators.array(((ResolvedType)o).getDeclaredMethods());
            }
        };
        return 
            Iterators.mapOver(
                Iterators.append(
                    new Iterator() {
                        ResolvedType curr = ResolvedType.this;
                        public boolean hasNext() {
                            return curr != null;
                        }
                        public Object next() {
                            ResolvedType ret = curr;
                            curr = curr.getSuperclass();
                            return ret;
                        }
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    },
                    Iterators.recur(this, ifaceGetter)),
                methodGetter);
    }
    
    /**
     * Return a list of methods, first those declared on this class, then those declared on the superclass (recurse) and then those declared
     * on the superinterfaces.  The getMethods() call above doesn't quite work the same as it will (through the iterator) return methods
     * declared on *this* class twice, once at the start and once at the end - I couldn't debug that problem, so created this alternative.
     */
    public List getMethodsWithoutIterator(boolean includeITDs, boolean allowMissing) {
        List methods = new ArrayList();
        Set knowninterfaces = new HashSet();
        addAndRecurse(knowninterfaces,methods,this,includeITDs,allowMissing);
        return methods;
    }
    
    private void addAndRecurse(Set knowninterfaces,List collector, ResolvedType rtx, boolean includeITDs, boolean allowMissing) {
      collector.addAll(Arrays.asList(rtx.getDeclaredMethods())); // Add the methods declared on this type
      // now add all the inter-typed members too
      if (includeITDs && rtx.interTypeMungers != null) {
    	  for (Iterator i = interTypeMungers.iterator(); i.hasNext();) {
				ConcreteTypeMunger tm = (ConcreteTypeMunger) i.next();	
				ResolvedMember rm = tm.getSignature();
				if (rm != null) {  // new parent type munger can have null signature...
					collector.add(tm.getSignature());
				} 
			}
      }
      if (!rtx.equals(ResolvedType.OBJECT)) {
    	  ResolvedType superType = rtx.getSuperclass();
    	  if (superType != null && !superType.isMissing()) {
    		  addAndRecurse(knowninterfaces,collector,superType,includeITDs,allowMissing); // Recurse if we aren't at the top
    	  }
      }
      ResolvedType[] interfaces = rtx.getDeclaredInterfaces(); // Go through the interfaces on the way back down
      for (int i = 0; i < interfaces.length; i++) {
		ResolvedType iface = interfaces[i];

            // we need to know if it is an interface from Parent kind munger
            // as those are used for @AJ ITD and we precisely want to skip those
            boolean shouldSkip = false;
            for (int j = 0; j < rtx.interTypeMungers.size(); j++) {
                ConcreteTypeMunger munger = (ConcreteTypeMunger) rtx.interTypeMungers.get(j);
                if (munger.getMunger()!=null && munger.getMunger().getKind() == ResolvedTypeMunger.Parent 
                		&& ((NewParentTypeMunger)munger.getMunger()).getNewParent().equals(iface) // pr171953
                		) {
                    shouldSkip = true;
                    break;
                }
            }

            if (!shouldSkip && !knowninterfaces.contains(iface)) { // Dont do interfaces more than once
          knowninterfaces.add(iface); 
          if (allowMissing && iface.isMissing()) {
        	if (iface instanceof MissingResolvedTypeWithKnownSignature) {
        		((MissingResolvedTypeWithKnownSignature)iface).raiseWarningOnMissingInterfaceWhilstFindingMethods();
        	}
          } else {
        	  addAndRecurse(knowninterfaces,collector,iface,includeITDs,allowMissing);
          }
        }
	  }
    }

    public ResolvedType[] getResolvedTypeParameters() {
    	if (resolvedTypeParams == null) {
    		resolvedTypeParams = world.resolve(typeParameters);
    	}
    	return resolvedTypeParams;
    }
    
    /** 
     * described in JVM spec 2ed 5.4.3.2
     */
    public ResolvedMember lookupField(Member m) {
        return lookupMember(m, getFields());
    }

    /**
     * described in JVM spec 2ed 5.4.3.3.
     * Doesnt check ITDs.
     */
    public ResolvedMember lookupMethod(Member m) {
        return lookupMember(m, getMethods());
    }
    
    public ResolvedMember lookupMethodInITDs(Member m) {
    	if (interTypeMungers != null) {
			for (Iterator i = interTypeMungers.iterator(); i.hasNext();) {
				ConcreteTypeMunger tm = (ConcreteTypeMunger) i.next();
				if (matches(tm.getSignature(), m)) {
					return tm.getSignature();
				}
			}
		}
    	return null;
    }
    
    /**
     * return null if not found
     */
    private ResolvedMember lookupMember(Member m, Iterator i) {
        while (i.hasNext()) {
            ResolvedMember f = (ResolvedMember) i.next();
            if (matches(f, m)) return f;
            if (f.hasBackingGenericMember() && m.getName().equals(f.getName())) { // might be worth checking the method behind the parameterized method (see pr137496)
            	  if (matches(f.getBackingGenericMember(),m)) return f;
            }
        }
        return null; //ResolvedMember.Missing;
        //throw new BCException("can't find " + m);
    }  
    
    /**
     * return null if not found
     */
    private ResolvedMember lookupMember(Member m, ResolvedMember[] a) {
		for (int i = 0; i < a.length; i++) {
			ResolvedMember f = a[i];
            if (matches(f, m)) return f;	
		}
		return null;
    }      
    
    
    /**
     * Looks for the first member in the hierarchy matching aMember. This method
     * differs from lookupMember(Member) in that it takes into account parameters
     * which are type variables - which clearly an unresolved Member cannot do since
     * it does not know anything about type variables. 
     */
     public ResolvedMember lookupResolvedMember(ResolvedMember aMember,boolean allowMissing) {
    	Iterator toSearch = null;
    	ResolvedMember found = null;
    	if ((aMember.getKind() == Member.METHOD) || (aMember.getKind() == Member.CONSTRUCTOR)) {
    		toSearch = getMethodsWithoutIterator(true,allowMissing).iterator();
    	} else {
    		if (aMember.getKind() != Member.FIELD) 
    			throw new IllegalStateException("I didn't know you would look for members of kind " + aMember.getKind());
    		toSearch = getFields();
    	}
    	while(toSearch.hasNext()) {
			ResolvedMemberImpl candidate = (ResolvedMemberImpl) toSearch.next();			
			if (candidate.matches(aMember)) {
				found = candidate;
				break;
			} 
		}
    	
    	return found;
    }
    
    public static boolean matches(Member m1, Member m2) {
        if (m1 == null) return m2 == null;
        if (m2 == null) return false;
        
        // Check the names
        boolean equalNames = m1.getName().equals(m2.getName());
        if (!equalNames) return false;
        
        // Check the signatures
        boolean equalSignatures = m1.getSignature().equals(m2.getSignature());
        if (equalSignatures) return true;

        // If they aren't the same, we need to allow for covariance ... where one sig might be ()LCar; and 
        // the subsig might be ()LFastCar; - where FastCar is a subclass of Car
        boolean equalCovariantSignatures = m1.getParameterSignature().equals(m2.getParameterSignature());
        if (equalCovariantSignatures) return true;
        
        return false;
    }
    
    public static boolean conflictingSignature(Member m1, Member m2) {
    	if (m1 == null || m2 == null) return false;
    	
        if (!m1.getName().equals(m2.getName())) {
            return false;
        }
        if (m1.getKind() != m2.getKind()) {
            return false;
        }
    	
    	if (m1.getKind() == Member.FIELD) {
    		return m1.getDeclaringType().equals(m2.getDeclaringType());
    	} else if (m1.getKind() == Member.POINTCUT) {
    		return true;
    	}
    	
    	
    	UnresolvedType[] p1 = m1.getGenericParameterTypes();
    	UnresolvedType[] p2 = m2.getGenericParameterTypes();
    	if (p1==null) p1 = m1.getParameterTypes();
    	if (p2==null) p2 = m2.getParameterTypes();
    	int n = p1.length;
    	if (n != p2.length) return false;
    	
    	for (int i=0; i < n; i++) {
    		if (!p1[i].equals(p2[i])) return false;
    	}
    	return true;
    }
    
    
    /**
     * returns an iterator through all of the pointcuts of this type, in order
     * for checking from JVM spec 2ed 5.4.3.2 (as for fields).  This means that the order is
     * <p/>
     * <ul><li> pointcuts from current class </li>
     *     <li> recur into direct superinterfaces </li>
     *     <li> recur into superclass </li>
     * </ul>
     * <p/>
     * We keep a hashSet of interfaces that we've visited so we don't spiral
     * out into 2^n land.
     */
    public Iterator getPointcuts() {
        final Iterators.Filter dupFilter = Iterators.dupFilter();
        // same order as fields
        Iterators.Getter typeGetter = new Iterators.Getter() {
            public Iterator get(Object o) {
                return 
                    dupFilter.filter(
                        ((ResolvedType)o).getDirectSupertypes());
            }
        };
        Iterators.Getter pointcutGetter = new Iterators.Getter() {
            public Iterator get(Object o) {
                //System.err.println("getting for " + o);
                return Iterators.array(((ResolvedType)o).getDeclaredPointcuts());
            }
        };
        return 
            Iterators.mapOver(
                Iterators.recur(this, typeGetter),
                pointcutGetter);
    }
    
    public ResolvedPointcutDefinition findPointcut(String name) {
        //System.err.println("looking for pointcuts " + this);
        for (Iterator i = getPointcuts(); i.hasNext(); ) {
            ResolvedPointcutDefinition f = (ResolvedPointcutDefinition) i.next();
            //System.err.println(f);
            if (name.equals(f.getName())) {
                return f;
            }
        }
        // pr120521
        if (!getOutermostType().equals(this)) {
        	ResolvedType outerType = getOutermostType().resolve(world);
        	ResolvedPointcutDefinition rpd = outerType.findPointcut(name);
        	return rpd;
        }
        return null; // should we throw an exception here?
    }
    
    
    // all about collecting CrosscuttingMembers

	//??? collecting data-structure, shouldn't really be a field
    public CrosscuttingMembers crosscuttingMembers;

	public CrosscuttingMembers collectCrosscuttingMembers(boolean shouldConcretizeIfNeeded) {
		crosscuttingMembers = new CrosscuttingMembers(this,shouldConcretizeIfNeeded);
		crosscuttingMembers.setPerClause(getPerClause());
		crosscuttingMembers.addShadowMungers(collectShadowMungers());
		// GENERICITDFIX
//		crosscuttingMembers.addTypeMungers(collectTypeMungers());
        crosscuttingMembers.addTypeMungers(getTypeMungers());
        //FIXME AV - skip but needed ?? or  ?? crosscuttingMembers.addLateTypeMungers(getLateTypeMungers());
		crosscuttingMembers.addDeclares(collectDeclares(!this.doesNotExposeShadowMungers()));
		crosscuttingMembers.addPrivilegedAccesses(getPrivilegedAccesses());
		
		
		//System.err.println("collected cc members: " + this + ", " + collectDeclares());
		return crosscuttingMembers;
	}
	
	public final Collection collectTypeMungers() {
		if (! this.isAspect() ) return Collections.EMPTY_LIST;
		
		ArrayList ret = new ArrayList();
		//if (this.isAbstract()) {
//		for (Iterator i = getDeclares().iterator(); i.hasNext();) {
//			Declare dec = (Declare) i.next();
//			if (!dec.isAdviceLike()) ret.add(dec);
//		}
//        
//        if (!includeAdviceLike) return ret;
        
		if (!this.isAbstract()) {
			final Iterators.Filter dupFilter = Iterators.dupFilter();
	        Iterators.Getter typeGetter = new Iterators.Getter() {
	            public Iterator get(Object o) {
	                return 
	                    dupFilter.filter(
	                        ((ResolvedType)o).getDirectSupertypes());
	            }
	        };
	        Iterator typeIterator = Iterators.recur(this, typeGetter);
	
	        while (typeIterator.hasNext()) {
	        	ResolvedType ty = (ResolvedType) typeIterator.next();
	        	for (Iterator i = ty.getTypeMungers().iterator(); i.hasNext();) {
	        		ConcreteTypeMunger dec = (ConcreteTypeMunger) i.next();
					ret.add(dec);
				}
	        }
		}
		
		return ret;
    }
	
	public final Collection collectDeclares(boolean includeAdviceLike) {
		if (! this.isAspect() ) return Collections.EMPTY_LIST;
		
		ArrayList ret = new ArrayList();
		//if (this.isAbstract()) {
//		for (Iterator i = getDeclares().iterator(); i.hasNext();) {
//			Declare dec = (Declare) i.next();
//			if (!dec.isAdviceLike()) ret.add(dec);
//		}
//        
//        if (!includeAdviceLike) return ret;
        
		if (!this.isAbstract()) {
			//ret.addAll(getDeclares());
			final Iterators.Filter dupFilter = Iterators.dupFilter();
	        Iterators.Getter typeGetter = new Iterators.Getter() {
	            public Iterator get(Object o) {
	                return 
	                    dupFilter.filter(
	                        ((ResolvedType)o).getDirectSupertypes());
	            }
	        };
	        Iterator typeIterator = Iterators.recur(this, typeGetter);
	
	        while (typeIterator.hasNext()) {
	        	ResolvedType ty = (ResolvedType) typeIterator.next();
	        	//System.out.println("super: " + ty + ", " + );
	        	for (Iterator i = ty.getDeclares().iterator(); i.hasNext();) {
					Declare dec = (Declare) i.next();
					if (dec.isAdviceLike()) {
						if (includeAdviceLike) ret.add(dec);
					} else {
						ret.add(dec);
					}
				}
	        }
		}
		
		return ret;
    }
    
	
	
	
    private final Collection collectShadowMungers() {
        if (! this.isAspect() || this.isAbstract() || this.doesNotExposeShadowMungers()) return Collections.EMPTY_LIST;

		ArrayList acc = new ArrayList();
        final Iterators.Filter dupFilter = Iterators.dupFilter();
        Iterators.Getter typeGetter = new Iterators.Getter() {
            public Iterator get(Object o) {
                return 
                    dupFilter.filter(
                        ((ResolvedType)o).getDirectSupertypes());
            }
        };
        Iterator typeIterator = Iterators.recur(this, typeGetter);

        while (typeIterator.hasNext()) {
            ResolvedType ty = (ResolvedType) typeIterator.next();
            acc.addAll(ty.getDeclaredShadowMungers());     
        }
        
        return acc;
    }
    
	protected boolean doesNotExposeShadowMungers() {
		return false;
	}

    public PerClause getPerClause() {
        return null;
    }

	protected Collection getDeclares() {
		return Collections.EMPTY_LIST; 
	}
	
    protected Collection getTypeMungers() {
        return Collections.EMPTY_LIST;
    }
	
    protected Collection getPrivilegedAccesses() {
        return Collections.EMPTY_LIST;
    }
	

    // ---- useful things
    
    public final boolean isInterface() {       
        return Modifier.isInterface(getModifiers());
    }
    
    public final boolean isAbstract() {       
        return Modifier.isAbstract(getModifiers());
    }
    
    public boolean isClass() {
    	return false;
    }
    
    public boolean isAspect() {
    	return false;
    }

    public boolean isAnnotationStyleAspect() {
    	return false;
    }

    /**
     * Note: Only overridden by Name subtype.
     */
    public boolean isEnum() {
    	return false;
    }
    
    /**
     * Note: Only overridden by Name subtype.
     */
    public boolean isAnnotation() {
    	return false;
    }
    
    public boolean isAnonymous() {
    		return false;
    }
    
    public boolean isNested() {
    		return false;
    }
    
    /**
     * Note: Only overridden by Name subtype
     */
	public void addAnnotation(AnnotationX annotationX) {
		throw new RuntimeException("ResolvedType.addAnnotation() should never be called");
	}
	
	/**
	 * Note: Only overridden by Name subtype
	 */
	public AnnotationX[] getAnnotations() {
		throw new RuntimeException("ResolvedType.getAnnotations() should never be called");
	}
    
	/**
	 * Note: Only overridden by ReferenceType subtype
	 */
	public boolean canAnnotationTargetType() {
		return false;
	}
	
	/**
	 * Note: Only overridden by ReferenceType subtype
	 */	
	public AnnotationTargetKind[] getAnnotationTargetKinds() {
		return null;
	}
	
    /**
     * Note: Only overridden by Name subtype.
     */
    public boolean isAnnotationWithRuntimeRetention() {
        return false;
    }
    
    public boolean isSynthetic() {
    	return signature.indexOf("$ajc") != -1;
    }
    
    public final boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

	protected Map /*Type variable name -> UnresolvedType*/ getMemberParameterizationMap() {
		if (!isParameterizedType()) return Collections.EMPTY_MAP;
		TypeVariable[] tvs = getGenericType().getTypeVariables();
		Map parameterizationMap = new HashMap();
		for (int i = 0; i < tvs.length; i++) {
			parameterizationMap.put(tvs[i].getName(), typeParameters[i]);
		}
		return parameterizationMap;
	}

	
	public Collection getDeclaredAdvice() {
		List l = new ArrayList();
		ResolvedMember[] methods = getDeclaredMethods();
		if (isParameterizedType()) methods = getGenericType().getDeclaredMethods();
		Map typeVariableMap = getAjMemberParameterizationMap();
		for (int i=0, len = methods.length; i < len; i++) {
			ShadowMunger munger = methods[i].getAssociatedShadowMunger();
			if (munger != null) {
				if (ajMembersNeedParameterization()) {
					//munger.setPointcut(munger.getPointcut().parameterizeWith(typeVariableMap));
					munger = munger.parameterizeWith(this,typeVariableMap);
					if (munger instanceof Advice) {
						Advice advice = (Advice) munger;
						// update to use the parameterized signature...
						UnresolvedType[] ptypes = methods[i].getGenericParameterTypes()	;
						UnresolvedType[] newPTypes = new UnresolvedType[ptypes.length];
						for (int j = 0; j < ptypes.length; j++) {
							if (ptypes[j] instanceof TypeVariableReferenceType) {
								TypeVariableReferenceType tvrt = (TypeVariableReferenceType) ptypes[j];
								if (typeVariableMap.containsKey(tvrt.getTypeVariable().getName())) {
									newPTypes[j] = (UnresolvedType) typeVariableMap.get(tvrt.getTypeVariable().getName());
								} else {
									newPTypes[j] = ptypes[j];
								}
							} else {
								newPTypes[j] = ptypes[j];
							}
						}
						advice.setBindingParameterTypes(newPTypes);
					}
				}
				munger.setDeclaringType(this);
				l.add(munger);
			}
		}
		return l;
	}
	
	public Collection getDeclaredShadowMungers() {
		Collection c = getDeclaredAdvice();
		return c;
	}
	
	// ---- only for testing!


	public ResolvedMember[] getDeclaredJavaFields() {
		return filterInJavaVisible(getDeclaredFields());
	}
	public ResolvedMember[] getDeclaredJavaMethods() {
		return filterInJavaVisible(getDeclaredMethods());
	}
	public ShadowMunger[] getDeclaredShadowMungersArray() {
		List l = (List) getDeclaredShadowMungers();
		return (ShadowMunger[]) l.toArray(new ShadowMunger[l.size()]);
	}
	private ResolvedMember[] filterInJavaVisible(ResolvedMember[] ms) {
		List l = new ArrayList();
		for (int i=0, len = ms.length; i < len; i++) {
			if (! ms[i].isAjSynthetic() && ms[i].getAssociatedShadowMunger() == null) {
				l.add(ms[i]);
			}
		}
		return (ResolvedMember[]) l.toArray(new ResolvedMember[l.size()]);
	}
	
	public abstract ISourceContext getSourceContext();


    // ---- fields
    
    public static final ResolvedType[] NONE = new ResolvedType[0];

    public static final Primitive BYTE    = new Primitive("B", 1, 0);
    public static final Primitive CHAR    = new Primitive("C", 1, 1);
    public static final Primitive DOUBLE  = new Primitive("D", 2, 2);
    public static final Primitive FLOAT   = new Primitive("F", 1, 3);
    public static final Primitive INT     = new Primitive("I", 1, 4);
    public static final Primitive LONG    = new Primitive("J", 2, 5);
    public static final Primitive SHORT   = new Primitive("S", 1, 6);
    public static final Primitive VOID    = new Primitive("V", 0, 8);
    public static final Primitive BOOLEAN = new Primitive("Z", 1, 7);
    public static final Missing   MISSING = new Missing();
    
    /** Reset the static state in the primitive types */
    public static void resetPrimitives() {
    	BYTE.world=null;
    	CHAR.world=null;
    	DOUBLE.world=null;
    	FLOAT.world=null;
    	INT.world=null;
    	LONG.world=null;
    	SHORT.world=null;
    	VOID.world=null;
    	BOOLEAN.world=null;
    }
    
    
    // ---- types
    public static ResolvedType makeArray(ResolvedType type, int dim) {
    	if (dim == 0) return type;
    	ResolvedType array = new ArrayReferenceType("[" + type.getSignature(),"["+type.getErasureSignature(),type.getWorld(),type);
    	return makeArray(array,dim-1);
    }
    
    static class Array extends ResolvedType {
        ResolvedType componentType;
       
        
        // Sometimes the erasure is different, eg.  [TT;  and [Ljava/lang/Object; 
        Array(String sig, String erasureSig,World world, ResolvedType componentType) {
            super(sig,erasureSig, world);
            this.componentType = componentType;
        }
        public final ResolvedMember[] getDeclaredFields() {
            return ResolvedMember.NONE;
        }
        public final ResolvedMember[] getDeclaredMethods() {
            // ??? should this return clone?  Probably not...
            // If it ever does, here is the code:
            //  ResolvedMember cloneMethod =
            //    new ResolvedMember(Member.METHOD,this,Modifier.PUBLIC,UnresolvedType.OBJECT,"clone",new UnresolvedType[]{});
            //  return new ResolvedMember[]{cloneMethod};
        	return ResolvedMember.NONE;
        }
        public final ResolvedType[] getDeclaredInterfaces() {
            return
                new ResolvedType[] {
                    world.getCoreType(CLONEABLE), 
                    world.getCoreType(SERIALIZABLE)
                };
        }
        public final ResolvedMember[] getDeclaredPointcuts() {
            return ResolvedMember.NONE;
        }
        
        public boolean hasAnnotation(UnresolvedType ofType) {
        	return false;
        }
        
        public final ResolvedType getSuperclass() {
            return world.getCoreType(OBJECT);
        }
        public final boolean isAssignableFrom(ResolvedType o) {
            if (! o.isArray()) return false;
            if (o.getComponentType().isPrimitiveType()) {
                return o.equals(this);
            } else {
                return getComponentType().resolve(world).isAssignableFrom(o.getComponentType().resolve(world));
            }
        }
        
        public boolean isAssignableFrom(ResolvedType o, boolean allowMissing) {
        	return isAssignableFrom(o);
        }
        
        public final boolean isCoerceableFrom(ResolvedType o) {
            if (o.equals(UnresolvedType.OBJECT) || 
                    o.equals(UnresolvedType.SERIALIZABLE) ||
                    o.equals(UnresolvedType.CLONEABLE)) {
                return true;
            }
            if (! o.isArray()) return false;
            if (o.getComponentType().isPrimitiveType()) {
                return o.equals(this);
            } else {
                return getComponentType().resolve(world).isCoerceableFrom(o.getComponentType().resolve(world));
            }
        }
        public final int getModifiers() {
            int mask = Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED;
            return (componentType.getModifiers() & mask) | Modifier.FINAL;
        }
        public UnresolvedType getComponentType() {
            return componentType;
        }
        public ResolvedType getResolvedComponentType() {
            return componentType;
        }
        public ISourceContext getSourceContext() {
        	return getResolvedComponentType().getSourceContext();
        }
    }
    
    static class Primitive extends ResolvedType {
        private int size;
        private int index;
        Primitive(String signature, int size, int index) {
            super(signature, null);
            this.size = size;
            this.index = index;
            this.typeKind=TypeKind.PRIMITIVE;
        }
        public final int getSize() {
            return size;
        }
        public final int getModifiers() {
            return Modifier.PUBLIC | Modifier.FINAL;
        }
        public final boolean isPrimitiveType() {
            return true;
        }
        public boolean hasAnnotation(UnresolvedType ofType) {
        	return false;
        }
        public final boolean isAssignableFrom(ResolvedType other) {
            if (!other.isPrimitiveType()) {
            	if (!world.isInJava5Mode()) return false;
            	return validBoxing.contains(this.getSignature()+other.getSignature());
            }
            return assignTable[((Primitive)other).index][index];
        }
        public final boolean isAssignableFrom(ResolvedType other, boolean allowMissing) {
        	return isAssignableFrom(other);
        }        
        public final boolean isCoerceableFrom(ResolvedType other) {
            if (this == other) return true;
            if (! other.isPrimitiveType()) return false;
            if (index > 6 || ((Primitive)other).index > 6) return false;
            return true;
        }
        public ResolvedType resolve(World world) {
            this.world = world;
            return super.resolve(world);
        }
        public final boolean needsNoConversionFrom(ResolvedType other) {
            if (! other.isPrimitiveType()) return false;
            return noConvertTable[((Primitive)other).index][index];
        }           
        private static final boolean[][] assignTable = 
            {// to: B     C      D       F      I       J       S      V       Z        from
                { true , true , true , true , true , true , true , false, false }, // B
                { false, true , true , true , true , true , false, false, false }, // C
                { false, false, true , false, false, false, false, false, false }, // D
                { false, false, true , true , false, false, false, false, false }, // F
                { false, false, true , true , true , true , false, false, false }, // I
                { false, false, true , true , false, true , false, false, false }, // J
                { false, false, true , true , true , true , true , false, false }, // S
                { false, false, false, false, false, false, false, true , false }, // V
                { false, false, false, false, false, false, false, false, true  }, // Z
            };             
        private static final boolean[][] noConvertTable = 
            {// to: B     C      D       F      I       J       S      V       Z        from
                { true , true , false, false, true , false, true , false, false }, // B
                { false, true , false, false, true , false, false, false, false }, // C
                { false, false, true , false, false, false, false, false, false }, // D
                { false, false, false, true , false, false, false, false, false }, // F
                { false, false, false, false, true , false, false, false, false }, // I
                { false, false, false, false, false, true , false, false, false }, // J
                { false, false, false, false, true , false, true , false, false }, // S
                { false, false, false, false, false, false, false, true , false }, // V
                { false, false, false, false, false, false, false, false, true  }, // Z
            };             
                
        // ----
 
        public final ResolvedMember[] getDeclaredFields() {
            return ResolvedMember.NONE;
        }
        public final ResolvedMember[] getDeclaredMethods() {
            return ResolvedMember.NONE;
        }
        public final ResolvedType[] getDeclaredInterfaces() {
            return ResolvedType.NONE;
        }
        public final ResolvedMember[] getDeclaredPointcuts() {
            return ResolvedMember.NONE;
        }

        public final ResolvedType getSuperclass() {
            return null;
        }
        
        public ISourceContext getSourceContext() {
        	return null;
        }
   
    }

    static class Missing extends ResolvedType {
        Missing() {
            super(MISSING_NAME, null);
        }       
//        public final String toString() {
//            return "<missing>";
//        }      
        public final String getName() {
        	return MISSING_NAME;
        }
        
        public final boolean isMissing() {
            return true;
        }
        
        public boolean hasAnnotation(UnresolvedType ofType) {
        	return false;
        }
        public final ResolvedMember[] getDeclaredFields() {
            return ResolvedMember.NONE;
        }
        public final ResolvedMember[] getDeclaredMethods() {
            return ResolvedMember.NONE;
        }
        public final ResolvedType[] getDeclaredInterfaces() {
            return ResolvedType.NONE;
        }

        public final ResolvedMember[] getDeclaredPointcuts() {
            return ResolvedMember.NONE;
        }
        public final ResolvedType getSuperclass() {
            return null;
        }
        public final int getModifiers() {
            return 0;
        }
        public final boolean isAssignableFrom(ResolvedType other) {
            return false;
        }   
        public final boolean isAssignableFrom(ResolvedType other, boolean allowMissing) {
            return false;
        }   
        public final boolean isCoerceableFrom(ResolvedType other) {
            return false;
        }        
        public boolean needsNoConversionFrom(ResolvedType other) {
            return false;
        }
        public ISourceContext getSourceContext() {
        	return null;
        }

    }

    /** 
     * Look up a member, takes into account any ITDs on this type.
     * return null if not found
     */
	public ResolvedMember lookupMemberNoSupers(Member member) {
		ResolvedMember ret = lookupDirectlyDeclaredMemberNoSupers(member);
		if (ret == null && interTypeMungers != null) {
			for (Iterator i = interTypeMungers.iterator(); i.hasNext();) {
				ConcreteTypeMunger tm = (ConcreteTypeMunger) i.next();
				if (matches(tm.getSignature(), member)) {
					return tm.getSignature();
				}
			}
		}
		return ret;
	}
	
	public ResolvedMember lookupMemberWithSupersAndITDs(Member member) {
		ResolvedMember ret = lookupMemberNoSupers(member);
		if (ret != null) return ret;
		
		ResolvedType supert = getSuperclass();
		while (ret==null && supert!=null) {
			ret = supert.lookupMemberNoSupers(member);
			if (ret==null) supert = supert.getSuperclass();
		}
		
		return ret;
	}
	
	/**
	 * as lookupMemberNoSupers, but does not include ITDs
     *
	 * @param member
	 * @return
	 */
	public ResolvedMember lookupDirectlyDeclaredMemberNoSupers(Member member) {
		ResolvedMember ret;
		if (member.getKind() == Member.FIELD) {
			ret = lookupMember(member, getDeclaredFields());
		} else {
			// assert member.getKind() == Member.METHOD || member.getKind() == Member.CONSTRUCTOR
			ret = lookupMember(member, getDeclaredMethods());
		}
		return ret;
	}
	
	/**
	 * This lookup has specialized behaviour - a null result tells the
	 * EclipseTypeMunger that it should make a default implementation of a
	 * method on this type.
     *
	 * @param member
	 * @return
	 */
	public ResolvedMember lookupMemberIncludingITDsOnInterfaces(Member member) {
		return lookupMemberIncludingITDsOnInterfaces(member, this);
	}
	
	private ResolvedMember lookupMemberIncludingITDsOnInterfaces(Member member, ResolvedType onType) {
		ResolvedMember ret = onType.lookupMemberNoSupers(member);
		if (ret != null) {
			return ret;
		} else {
			ResolvedType superType = onType.getSuperclass();
			if (superType != null) {
				ret = lookupMemberIncludingITDsOnInterfaces(member,superType);
			}
			if (ret == null) {
				// try interfaces then, but only ITDs now...
				ResolvedType[] superInterfaces = onType.getDeclaredInterfaces();
				for (int i = 0; i < superInterfaces.length; i++) {
					ret = superInterfaces[i].lookupMethodInITDs(member);
					if (ret != null) return ret;
				}
			}
		}
		return ret;
	}
	
	protected List interTypeMungers = new ArrayList(0);
	
	public List getInterTypeMungers() {
		return interTypeMungers;
	}
    
    public List getInterTypeParentMungers() {
      List l = new ArrayList();
      for (Iterator iter = interTypeMungers.iterator(); iter.hasNext();) {
		ConcreteTypeMunger element = (ConcreteTypeMunger) iter.next();
		if (element.getMunger() instanceof NewParentTypeMunger) l.add(element);
	}
      return l;
    }
    
	/**
	 * ??? This method is O(N*M) where N = number of methods and M is number of
	 * inter-type declarations in my super
	 */
     public List getInterTypeMungersIncludingSupers() {
        ArrayList ret = new ArrayList();
        collectInterTypeMungers(ret);
        return ret;
    }

     
    public List getInterTypeParentMungersIncludingSupers() {
      ArrayList ret = new ArrayList();
      collectInterTypeParentMungers(ret);
      return ret;
    }
    
    private void collectInterTypeParentMungers(List collector) {
        for (Iterator iter = getDirectSupertypes(); iter.hasNext();) {
            ResolvedType superType = (ResolvedType) iter.next();
            superType.collectInterTypeParentMungers(collector);
        }
        collector.addAll(getInterTypeParentMungers());
    }
        
        
    protected void collectInterTypeMungers(List collector) {
        for (Iterator iter = getDirectSupertypes(); iter.hasNext();) {
			ResolvedType superType = (ResolvedType) iter.next();
            superType.collectInterTypeMungers(collector);
		}
        
        outer:
        for (Iterator iter1 = collector.iterator(); iter1.hasNext();) {
            ConcreteTypeMunger superMunger = (ConcreteTypeMunger) iter1.next();
            if ( superMunger.getSignature() == null) continue;
            
            if ( !superMunger.getSignature().isAbstract()) continue;
            
            for (Iterator iter = getInterTypeMungers().iterator(); iter.hasNext();) {
                ConcreteTypeMunger  myMunger = (ConcreteTypeMunger) iter.next();
                if (conflictingSignature(myMunger.getSignature(), superMunger.getSignature())) {
                    iter1.remove();
                    continue outer;
                }
            }
            
            if (!superMunger.getSignature().isPublic()) continue;
            
            for (Iterator iter = getMethods(); iter.hasNext(); ) {
                ResolvedMember method = (ResolvedMember)iter.next();
                if (conflictingSignature(method, superMunger.getSignature())) {
                    iter1.remove();
                    continue outer;
                }
            }
        }
        
        collector.addAll(getInterTypeMungers());
    }
    
 
    
    /**
     * Check:
     * 1) That we don't have any abstract type mungers unless this type is abstract.
     * 2) That an abstract ITDM on an interface is declared public. (Compiler limitation) (PR70794)
     */
    public void checkInterTypeMungers() {
        if (isAbstract()) return;
        
        boolean itdProblem = false;
        
        for (Iterator iter = getInterTypeMungersIncludingSupers().iterator(); iter.hasNext();) {
			ConcreteTypeMunger munger = (ConcreteTypeMunger) iter.next();
			itdProblem = checkAbstractDeclaration(munger) || itdProblem; // Rule 2

        }
        
        if (itdProblem) return; // If the rules above are broken, return right now
        
		for (Iterator iter = getInterTypeMungersIncludingSupers().iterator(); iter.hasNext();) {
			ConcreteTypeMunger munger = (ConcreteTypeMunger) iter.next();
            if (munger.getSignature() != null && munger.getSignature().isAbstract()) { // Rule 1
                if (munger.getMunger().getKind() == ResolvedTypeMunger.MethodDelegate) {
                    ;//ignore for @AJ ITD as munger.getSignature() is the interface method hence abstract
                } else {
                world.getMessageHandler().handleMessage(
                    new Message("must implement abstract inter-type declaration: " + munger.getSignature(),
                        "", IMessage.ERROR, getSourceLocation(), null, 
                        new ISourceLocation[] { getMungerLocation(munger) }));
            }
		}
    }
    }
    
    /**
     * See PR70794.  This method checks that if an abstract inter-type method declaration is made on
     * an interface then it must also be public.
     * This is a compiler limitation that could be made to work in the future (if someone
     * provides a worthwhile usecase)
     * 
     * @return indicates if the munger failed the check
     */
    private boolean checkAbstractDeclaration(ConcreteTypeMunger munger) {
		if (munger.getMunger()!=null && (munger.getMunger() instanceof NewMethodTypeMunger)) {
			ResolvedMember itdMember = munger.getSignature();
			ResolvedType onType = itdMember.getDeclaringType().resolve(world);
			if (onType.isInterface() && itdMember.isAbstract() && !itdMember.isPublic()) {
					world.getMessageHandler().handleMessage(
							new Message(WeaverMessages.format(WeaverMessages.ITD_ABSTRACT_MUST_BE_PUBLIC_ON_INTERFACE,munger.getSignature(),onType),"",
									Message.ERROR,getSourceLocation(),null,
									new ISourceLocation[]{getMungerLocation(munger)})	
						);
					return true;
				}			
		}
		return false;
    }
    
    /**
     * Get a source location for the munger.
     * Until intertype mungers remember where they came from, the source location
     * for the munger itself is null.  In these cases use the
     * source location for the aspect containing the ITD.
     */
    private ISourceLocation getMungerLocation(ConcreteTypeMunger munger) {
    	ISourceLocation sloc = munger.getSourceLocation();
    	if (sloc == null) {
    		sloc = munger.getAspectType().getSourceLocation();
    	}
    	return sloc;
    }
	
    /**
     * Returns a ResolvedType object representing the declaring type of this type, or
     * null if this type does not represent a non-package-level-type.
     * <p/>
     * <strong>Warning</strong>:  This is guaranteed to work for all member types.
     * For anonymous/local types, the only guarantee is given in JLS 13.1, where
     * it guarantees that if you call getDeclaringType() repeatedly, you will eventually
     * get the top-level class, but it does not say anything about classes in between.
     *
     * @return the declaring UnresolvedType object, or null.
     */
    public ResolvedType getDeclaringType() {
    	if (isArray()) return null;
		String name = getName();
		int lastDollar = name.lastIndexOf('$');
		while (lastDollar >0) { // allow for classes starting '$' (pr120474)
			ResolvedType ret = world.resolve(UnresolvedType.forName(name.substring(0, lastDollar)), true);
			if (!ResolvedType.isMissing(ret)) return ret;
			lastDollar = name.lastIndexOf('$', lastDollar-1);
		}
		return null;
    }
	
	
	public static boolean isVisible(int modifiers, ResolvedType targetType, ResolvedType fromType) {
		//System.err.println("mod: " + modifiers + ", " + targetType + " and " + fromType);
		
		if (Modifier.isPublic(modifiers)) {
			return true;
		} else if (Modifier.isPrivate(modifiers)) {
			return targetType.getOutermostType().equals(fromType.getOutermostType());
		} else if (Modifier.isProtected(modifiers)) {
			return samePackage(targetType, fromType) || targetType.isAssignableFrom(fromType);
		} else { // package-visible
			return samePackage(targetType, fromType);
		}	
	}
	
	public static boolean hasBridgeModifier(int modifiers) {
		return (modifiers & Constants.ACC_BRIDGE)!=0;
	}

	private static boolean samePackage(
		ResolvedType targetType,
            ResolvedType fromType) {
		String p1 = targetType.getPackageName();
		String p2 = fromType.getPackageName();
		if (p1 == null) return p2 == null;
		if (p2 == null) return false;
		return p1.equals(p2);
	}

	/**
	 * Checks if the generic type for 'this' and the generic type for 'other' are the same -
	 * it can be passed raw or parameterized versions and will just compare the underlying
	 * generic type.
	 */
    private boolean genericTypeEquals(ResolvedType other) {
        ResolvedType rt = other;
        if (rt.isParameterizedType() || rt.isRawType()) rt.getGenericType();
        if (( (isParameterizedType() || isRawType()) && getGenericType().equals(rt)) ||
                (this.equals(other))) return true;
        return false;
    }
	
	 /**
     * Look up the actual occurence of a particular type in the hierarchy for
     * 'this' type.  The input is going to be a generic type, and the caller
     * wants to know if it was used in its RAW or a PARAMETERIZED form in this
     * hierarchy.
     *
     * returns null if it can't be found.
     */
    public ResolvedType discoverActualOccurrenceOfTypeInHierarchy(ResolvedType lookingFor) {
            if (!lookingFor.isGenericType())
                    throw new BCException("assertion failed: method should only be called with generic type, but "+lookingFor+" is "+lookingFor.typeKind);

            if (this.equals(ResolvedType.OBJECT)) return null;

            if (genericTypeEquals(lookingFor)) return this;

            ResolvedType superT = getSuperclass();
            if (superT.genericTypeEquals(lookingFor)) return superT;

            ResolvedType[] superIs = getDeclaredInterfaces();
            for (int i = 0; i < superIs.length; i++) {
                    ResolvedType superI = superIs[i];
                    if (superI.genericTypeEquals(lookingFor)) return superI;
                    ResolvedType checkTheSuperI = superI.discoverActualOccurrenceOfTypeInHierarchy(lookingFor);
                    if (checkTheSuperI!=null) return checkTheSuperI;
            }
            return superT.discoverActualOccurrenceOfTypeInHierarchy(lookingFor);
    }

    /**
     * Called for all type mungers but only does something if they share type variables
     * with a generic type which they target.  When this happens this routine will check
     * for the target type in the target hierarchy and 'bind' any type parameters as
     * appropriate.  For example, for the ITD "List<T> I<T>.x" against a type like this:
     * "class A implements I<String>" this routine will return a parameterized form of
     * the ITD "List<String> I.x"
     */
    public ConcreteTypeMunger fillInAnyTypeParameters(ConcreteTypeMunger munger) {
    	boolean debug = false;
		ResolvedMember member = munger.getSignature();
		if (munger.isTargetTypeParameterized()) {
			if (debug) System.err.println("Processing attempted parameterization of "+munger+" targetting type "+this);
			if (debug) System.err.println("  This type is "+this+"  ("+typeKind+")");
	        // need to tailor this munger instance for the particular target...
	        if (debug) System.err.println("  Signature that needs parameterizing: "+member);
	        // Retrieve the generic type
	        ResolvedType onType = world.resolve(member.getDeclaringType()).getGenericType();
	        member.resolve(world); // Ensure all parts of the member are resolved
	        if (debug) System.err.println("  Actual target ontype: "+onType+"  ("+onType.typeKind+")");
	        // quickly find the targettype in the type hierarchy for this type (it will be either RAW or PARAMETERIZED)
	        ResolvedType actualTarget = discoverActualOccurrenceOfTypeInHierarchy(onType);
	        if (actualTarget==null)
	                throw new BCException("assertion failed: asked "+this+" for occurrence of "+onType+" in its hierarchy??");
	
	        // only bind the tvars if its a parameterized type or the raw type (in which case they collapse to bounds) - don't do it for generic types ;)
	        if (!actualTarget.isGenericType()) {
	            if (debug) System.err.println("Occurrence in "+this+" is actually "+actualTarget+"  ("+actualTarget.typeKind+")");
	            // parameterize the signature
	            // ResolvedMember newOne = member.parameterizedWith(actualTarget.getTypeParameters(),onType,actualTarget.isParameterizedType());
	        }
	       //if (!actualTarget.isRawType()) 
	    	munger = munger.parameterizedFor(actualTarget);
	        if (debug) System.err.println("New sig: "+munger.getSignature());
	            
	        
	        if (debug) System.err.println("=====================================");
		}
		return munger;
    }

    
    
	public void addInterTypeMunger(ConcreteTypeMunger munger) {
		ResolvedMember sig = munger.getSignature();
		if (sig == null || munger.getMunger() == null || 
				munger.getMunger().getKind() == ResolvedTypeMunger.PrivilegedAccess)
		{
			interTypeMungers.add(munger);
			return;
		}
		
		ConcreteTypeMunger originalMunger = munger;
		// we will use the 'parameterized' ITD for all the comparisons but we say the original
        // one passed in actually matched as it will be added to the intertype member finder
		// for the target type.  It is possible we only want to do this if a generic type 
		// is discovered and the tvar is collapsed to a bound?
		munger = fillInAnyTypeParameters(munger);
		sig = munger.getSignature(); // possibly changed when type parms filled in

		
		//System.err.println("add: " + munger + " to " + this.getClassName() + " with " + interTypeMungers);
		if (sig.getKind() == Member.METHOD) {
			if (!compareToExistingMembers(munger, getMethodsWithoutIterator(false,true) /*getMethods()*/)) return;
			if (this.isInterface()) {
				if (!compareToExistingMembers(munger, 
						Arrays.asList(world.getCoreType(OBJECT).getDeclaredMethods()).iterator())) return;
			}
		} else if (sig.getKind() == Member.FIELD) {
			if (!compareToExistingMembers(munger, Arrays.asList(getDeclaredFields()).iterator())) return;
		} else {
			if (!compareToExistingMembers(munger, Arrays.asList(getDeclaredMethods()).iterator())) return;
		}

		
		// now compare to existingMungers
		for (Iterator i = interTypeMungers.iterator(); i.hasNext(); ) {
			ConcreteTypeMunger existingMunger = (ConcreteTypeMunger)i.next();
			if (conflictingSignature(existingMunger.getSignature(), munger.getSignature())) {
				//System.err.println("match " + munger + " with " + existingMunger);
				if (isVisible(munger.getSignature().getModifiers(),
							munger.getAspectType(), existingMunger.getAspectType()))
				{
					//System.err.println("    is visible");
					int c = compareMemberPrecedence(sig, existingMunger.getSignature());
					if (c == 0) {
						c = getWorld().compareByPrecedenceAndHierarchy(munger.getAspectType(), existingMunger.getAspectType());
					}
					//System.err.println("       compare: " + c);
					if (c < 0) {
						// the existing munger dominates the new munger
						checkLegalOverride(munger.getSignature(), existingMunger.getSignature());
						return;
					} else if (c > 0) {
						// the new munger dominates the existing one
						checkLegalOverride(existingMunger.getSignature(), munger.getSignature());
						i.remove();
						break;
					} else {
						interTypeConflictError(munger, existingMunger);
						interTypeConflictError(existingMunger, munger);
						return;
					}					
				}
			}
		}
		//System.err.println("adding: " + munger + " to " + this);
		// we are adding the parameterized form of the ITD to the list of
		// mungers.  Within it, the munger knows the original declared
		// signature for the ITD so it can be retrieved.
		interTypeMungers.add(munger);
	}
	
	private boolean compareToExistingMembers(ConcreteTypeMunger munger, List existingMembersList) {
		return compareToExistingMembers(munger,existingMembersList.iterator());
	}
	
	//??? returning too soon
	private boolean compareToExistingMembers(ConcreteTypeMunger munger, Iterator existingMembers) {
		ResolvedMember sig = munger.getSignature();
		
		ResolvedType declaringAspectType = munger.getAspectType();
//		if (declaringAspectType.isRawType()) declaringAspectType = declaringAspectType.getGenericType();
//		if (declaringAspectType.isGenericType()) {
//
//		       ResolvedType genericOnType =		getWorld().resolve(sig.getDeclaringType()).getGenericType();
//		       ConcreteTypeMunger ctm =		munger.parameterizedFor(discoverActualOccurrenceOfTypeInHierarchy(genericOnType));
//		       sig = ctm.getSignature(); // possible sig change when type
//		}
//		   if (munger.getMunger().hasTypeVariableAliases()) {
//		       ResolvedType genericOnType =
//		getWorld().resolve(sig.getDeclaringType()).getGenericType();
//		       ConcreteTypeMunger ctm =
//		munger.parameterizedFor(discoverActualOccurrenceOfTypeInHierarchy(genericOnType));
//		       sig = ctm.getSignature(); // possible sig change when type parameters filled in
//		       }
		while (existingMembers.hasNext()) {
			
			ResolvedMember existingMember = (ResolvedMember)existingMembers.next();
			// don't worry about clashing with bridge methods
			if (existingMember.isBridgeMethod()) continue;
			//System.err.println("Comparing munger: "+sig+" with member "+existingMember);
			if (conflictingSignature(existingMember, munger.getSignature())) {
				//System.err.println("conflict: existingMember=" + existingMember + "   typeMunger=" + munger);
				//System.err.println(munger.getSourceLocation() + ", " + munger.getSignature() + ", " + munger.getSignature().getSourceLocation());
				
				if (isVisible(existingMember.getModifiers(), this, munger.getAspectType())) {
					int c = compareMemberPrecedence(sig, existingMember);
					//System.err.println("   c: " + c);
					if (c < 0) {
						// existingMember dominates munger
						checkLegalOverride(munger.getSignature(), existingMember);
						return false;
					} else if (c > 0) {
						// munger dominates existingMember
						checkLegalOverride(existingMember, munger.getSignature());
						//interTypeMungers.add(munger);  
						//??? might need list of these overridden abstracts
						continue;
					} else {
					  // bridge methods can differ solely in return type.
					  // FIXME this whole method seems very hokey - unaware of covariance/varargs/bridging - it
					  // could do with a rewrite !
					  boolean sameReturnTypes = (existingMember.getReturnType().equals(sig.getReturnType()));
					  if (sameReturnTypes) {
						  // pr206732 - if the existingMember is due to a previous application of this same ITD (which can
						  // happen if this is a binary type being brought in from the aspectpath).  The 'better' fix is
						  // to recognize it is from the aspectpath at a higher level and dont do this, but that is rather
						  // more work.
						  boolean isDuplicateOfPreviousITD = false;
						  ResolvedType declaringRt = existingMember.getDeclaringType().resolve(world);
						  WeaverStateInfo wsi = declaringRt.getWeaverState();
						  if (wsi!=null) {
							  List mungersAffectingThisType =  wsi.getTypeMungers(declaringRt);
							  if (mungersAffectingThisType!=null) {
								  for (Iterator iterator = mungersAffectingThisType.iterator(); iterator.hasNext() && !isDuplicateOfPreviousITD;) {
									ConcreteTypeMunger ctMunger = (ConcreteTypeMunger) iterator.next();
									// relatively crude check - is the ITD for the same as the existingmember and does it come from the same aspect
									if (ctMunger.getSignature().equals(existingMember) &&  ctMunger.aspectType.equals(munger.getAspectType())) {
										isDuplicateOfPreviousITD=true;
									}
								  }
							  }
						  }
						  if (!isDuplicateOfPreviousITD) {
							  getWorld().getMessageHandler().handleMessage(
								MessageUtil.error(WeaverMessages.format(WeaverMessages.ITD_MEMBER_CONFLICT,munger.getAspectType().getName(),
										existingMember),
								munger.getSourceLocation())
							  );
						  }
					  }
					}
				} else if (isDuplicateMemberWithinTargetType(existingMember,this,sig)) {
				    	getWorld().getMessageHandler().handleMessage(
							MessageUtil.error(WeaverMessages.format(WeaverMessages.ITD_MEMBER_CONFLICT,munger.getAspectType().getName(),
									existingMember),
							munger.getSourceLocation())
                    );
                    ;
				}
				//return;
			}
		}
		return true;
	}
	
	// we know that the member signature matches, but that the member in the target type is not visible to the aspect.
	// this may still be disallowed if it would result in two members within the same declaring type with the same
	// signature AND more than one of them is concrete AND they are both visible within the target type.
	private boolean isDuplicateMemberWithinTargetType(ResolvedMember existingMember, ResolvedType targetType,ResolvedMember itdMember) {
	    if ( (existingMember.isAbstract() || itdMember.isAbstract())) return false;
	    UnresolvedType declaringType = existingMember.getDeclaringType();
	    if (!targetType.equals(declaringType)) return false;
	    // now have to test that itdMember is visible from targetType
	    if (itdMember.isPrivate()) return false;
	    if (itdMember.isPublic()) return true;
	    // must be in same package to be visible then...
	    if (!targetType.getPackageName().equals(itdMember.getDeclaringType().getPackageName())) return false;
	    
	    // trying to put two members with the same signature into the exact same type..., and both visible in that type.
	    return true;
	}
	
	/**
	 * @return true if the override is legal
	 * note: calling showMessage with two locations issues TWO messages, not ONE message
	 * with an additional source location.
	 */
	public boolean checkLegalOverride(ResolvedMember parent, ResolvedMember child) {
		//System.err.println("check: " + child.getDeclaringType() + " overrides " + parent.getDeclaringType());
		if (Modifier.isFinal(parent.getModifiers())) {
			world.showMessage(Message.ERROR,
					WeaverMessages.format(WeaverMessages.CANT_OVERRIDE_FINAL_MEMBER,parent),
					child.getSourceLocation(),null);
			return false;
		}
		
		boolean incompatibleReturnTypes = false;
		// In 1.5 mode, allow for covariance on return type
		if (world.isInJava5Mode() && parent.getKind()==Member.METHOD) {
			
		  // Look at the generic types when doing this comparison
	      ResolvedType rtParentReturnType = parent.getGenericReturnType().resolve(world);
		  ResolvedType rtChildReturnType  = child.getGenericReturnType().resolve(world);
		  incompatibleReturnTypes = !rtParentReturnType.isAssignableFrom(rtChildReturnType);
		  if (incompatibleReturnTypes) {
			  incompatibleReturnTypes = !rtParentReturnType.isAssignableFrom(rtChildReturnType);
		  }
		} else {
		  incompatibleReturnTypes =!parent.getReturnType().equals(child.getReturnType());
		}
		
		if (incompatibleReturnTypes) {
			world.showMessage(IMessage.ERROR,
					WeaverMessages.format(WeaverMessages.ITD_RETURN_TYPE_MISMATCH,parent,child),
					child.getSourceLocation(), parent.getSourceLocation());
			return false;
		}		
		if (parent.getKind() == Member.POINTCUT) {
			UnresolvedType[] pTypes = parent.getParameterTypes();
			UnresolvedType[] cTypes = child.getParameterTypes();
			if (!Arrays.equals(pTypes, cTypes)) {
				world.showMessage(IMessage.ERROR,
						WeaverMessages.format(WeaverMessages.ITD_PARAM_TYPE_MISMATCH,parent,child),
						child.getSourceLocation(), parent.getSourceLocation());
				return false;
			}
		}		
		//System.err.println("check: " + child.getModifiers() + " more visible " + parent.getModifiers());
		if (isMoreVisible(parent.getModifiers(), child.getModifiers())) {
			world.showMessage(IMessage.ERROR,
					WeaverMessages.format(WeaverMessages.ITD_VISIBILITY_REDUCTION,parent,child),
					child.getSourceLocation(), parent.getSourceLocation());
			return false;
		}
		
		// check declared exceptions
		ResolvedType[] childExceptions = world.resolve(child.getExceptions());
		ResolvedType[] parentExceptions = world.resolve(parent.getExceptions());
		ResolvedType runtimeException = world.resolve("java.lang.RuntimeException");
		ResolvedType error = world.resolve("java.lang.Error");
		
        outer:
        for (int i = 0, leni = childExceptions.length; i < leni; i++) {
			//System.err.println("checking: " + childExceptions[i]);
			if (runtimeException.isAssignableFrom(childExceptions[i])) continue;
			if (error.isAssignableFrom(childExceptions[i])) continue;
			
			for (int j = 0, lenj = parentExceptions.length; j < lenj; j++) {
				if (parentExceptions[j].isAssignableFrom(childExceptions[i])) continue outer;
			}
			
			// this message is now better handled my MethodVerifier in JDT core.
//			world.showMessage(IMessage.ERROR,
//					WeaverMessages.format(WeaverMessages.ITD_DOESNT_THROW,childExceptions[i].getName()),
//					child.getSourceLocation(), null);
						
			return false;
		}
		if (parent.isStatic() && !child.isStatic()) {
			world.showMessage(IMessage.ERROR,
					WeaverMessages.format(WeaverMessages.ITD_OVERRIDDEN_STATIC,child,parent),
					child.getSourceLocation(),null);
			return false;
		} else if (child.isStatic() && !parent.isStatic()) {
			world.showMessage(IMessage.ERROR,
					WeaverMessages.format(WeaverMessages.ITD_OVERIDDING_STATIC,child,parent),
					child.getSourceLocation(),null);
			return false;
		}
		return true;
		
	}
	
	private int compareMemberPrecedence(ResolvedMember m1, ResolvedMember m2) {
		//if (!m1.getReturnType().equals(m2.getReturnType())) return 0;
		
		// need to allow for the special case of 'clone' - which is like abstract but is
		// not marked abstract.  The code below this next line seems to make assumptions
		// about what will have gotten through the compiler based on the normal
		// java rules.  clone goes against these...
		if (m2.isProtected() && m2.getName().charAt(0)=='c') {
			UnresolvedType declaring = m2.getDeclaringType();
			if (declaring!=null) {
				if (declaring.getName().equals("java.lang.Object") && m2.getName().equals("clone")) return +1;
			}
		}

		if (Modifier.isAbstract(m1.getModifiers())) return -1;
		if (Modifier.isAbstract(m2.getModifiers())) return +1;
	
		if (m1.getDeclaringType().equals(m2.getDeclaringType())) return 0;
		
		ResolvedType t1 = m1.getDeclaringType().resolve(world);
		ResolvedType t2 = m2.getDeclaringType().resolve(world);
		if (t1.isAssignableFrom(t2)) {
			return -1;
		}
		if (t2.isAssignableFrom(t1)) {
			return +1;
		}
		return 0;
	}
	

	public static boolean isMoreVisible(int m1, int m2) {
		if (Modifier.isPrivate(m1)) return false;
		if (isPackage(m1)) return Modifier.isPrivate(m2);
		if (Modifier.isProtected(m1)) return /* private package */ (Modifier.isPrivate(m2) || isPackage(m2));
		if (Modifier.isPublic(m1)) return /* private package protected */ ! Modifier.isPublic(m2);
		throw new RuntimeException("bad modifier: " + m1);
	}

	private static boolean isPackage(int i) {
		return (0 == (i & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED)));
	}

	private void interTypeConflictError(
		ConcreteTypeMunger m1,
            ConcreteTypeMunger m2) {
		//XXX this works only if we ignore separate compilation issues
		//XXX dual errors possible if (this instanceof BcelObjectType) return;
		
		//System.err.println("conflict at " + m2.getSourceLocation());
		getWorld().showMessage(IMessage.ERROR,
				WeaverMessages.format(WeaverMessages.ITD_CONFLICT,m1.getAspectType().getName(),
									m2.getSignature(),m2.getAspectType().getName()),
						m2.getSourceLocation(), getSourceLocation());
	}
	
	
	public ResolvedMember lookupSyntheticMember(Member member) {
		//??? horribly inefficient
		//for (Iterator i = 
		//System.err.println("lookup " + member + " in " + interTypeMungers);
		for (Iterator i = interTypeMungers.iterator(); i.hasNext(); ) {
			ConcreteTypeMunger m = (ConcreteTypeMunger)i.next();
			ResolvedMember ret = m.getMatchingSyntheticMember(member);
			if (ret != null) {
				//System.err.println("   found: " + ret);
				return ret;
			}
		}
		
		// Handling members for the new array join point
		if (world.isJoinpointArrayConstructionEnabled() && this.isArray()) {
			if (member.getKind()==Member.CONSTRUCTOR) {
				ResolvedMemberImpl ret =  
					new ResolvedMemberImpl(Member.CONSTRUCTOR,this,Modifier.PUBLIC,
						ResolvedType.VOID,"<init>",world.resolve(member.getParameterTypes()));
				return ret;
			}
		}
		
//		if (this.getSuperclass() != ResolvedType.OBJECT && this.getSuperclass() != null) {
//			return getSuperclass().lookupSyntheticMember(member);
//		}
		
		return null;
	}

	public void clearInterTypeMungers() {
		if (isRawType()) getGenericType().clearInterTypeMungers();
		interTypeMungers = new ArrayList();
	}


	public boolean isTopmostImplementor(ResolvedType interfaceType) {
		if (isInterface()) return false;
		if (!interfaceType.isAssignableFrom(this,true)) return false;
		// check that I'm truly the topmost implementor
		if (this.getSuperclass().isMissing()) return true; // we don't know anything about supertype, and it can't be exposed to weaver
		if (interfaceType.isAssignableFrom(this.getSuperclass(),true)) {
			return false;
		}
		return true;
	}
	
	public ResolvedType getTopmostImplementor(ResolvedType interfaceType) {
		if (isInterface()) return null;
		if (!interfaceType.isAssignableFrom(this)) return null;
		// Check if my super class is an implementor?
		ResolvedType higherType  = this.getSuperclass().getTopmostImplementor(interfaceType);
		if (higherType!=null) return higherType;
		return this;
	}
	
	private ResolvedType findHigher(ResolvedType other) {
	 if (this == other) return this;
     for(Iterator i = other.getDirectSupertypes(); i.hasNext(); ) {
     	ResolvedType rtx = (ResolvedType)i.next();
     	boolean b = this.isAssignableFrom(rtx);
     	if (b) return rtx;
     }       
     return null;
	}
	
	public List getExposedPointcuts() {
		List ret = new ArrayList();
		if (getSuperclass() != null) ret.addAll(getSuperclass().getExposedPointcuts());
		
		for (Iterator i = Arrays.asList(getDeclaredInterfaces()).iterator(); i.hasNext(); ) {
			ResolvedType t = (ResolvedType)i.next();
			addPointcutsResolvingConflicts(ret, Arrays.asList(t.getDeclaredPointcuts()), false);
		}
		addPointcutsResolvingConflicts(ret, Arrays.asList(getDeclaredPointcuts()), true);
		for (Iterator i = ret.iterator(); i.hasNext(); ) {
			ResolvedPointcutDefinition inherited = (ResolvedPointcutDefinition)i.next();
//			System.err.println("looking at: " + inherited + " in " + this);
//			System.err.println("            " + inherited.isAbstract() + " in " + this.isAbstract());
			if (inherited.isAbstract()) {
				if (!this.isAbstract()) {
					getWorld().showMessage(IMessage.ERROR,
							WeaverMessages.format(WeaverMessages.POINCUT_NOT_CONCRETE,inherited,this.getName()),
							inherited.getSourceLocation(), this.getSourceLocation());
				}
			}
		}		
		
		
		return ret;
	}
	
	private void addPointcutsResolvingConflicts(List acc, List added, boolean isOverriding) {
		for (Iterator i = added.iterator(); i.hasNext();) {
			ResolvedPointcutDefinition toAdd =
				(ResolvedPointcutDefinition) i.next();
				//System.err.println("adding: " + toAdd);
			for (Iterator j = acc.iterator(); j.hasNext();) {
				ResolvedPointcutDefinition existing =
					(ResolvedPointcutDefinition) j.next();
				if (existing == toAdd) continue;
				if (!isVisible(existing.getModifiers(),
					existing.getDeclaringType().resolve(getWorld()),
					this)) {
					continue;
				}
				if (conflictingSignature(existing, toAdd)) {
					if (isOverriding) {
						checkLegalOverride(existing, toAdd);
						j.remove();
					} else {
						getWorld().showMessage(
							IMessage.ERROR,
							WeaverMessages.format(WeaverMessages.CONFLICTING_INHERITED_POINTCUTS,this.getName() + toAdd.getSignature()),
							existing.getSourceLocation(),
							toAdd.getSourceLocation());
						j.remove();
					}
				}
			}
			acc.add(toAdd);
		}
	}
	
    public ISourceLocation getSourceLocation() {
        return null;
    }

    public boolean isExposedToWeaver() {
        return false;
    }

	public WeaverStateInfo getWeaverState() {
		return null;
	}
	
	/**
	 * Overridden by ReferenceType to return a sensible answer for parameterized and raw types.
     *
	 * @return
	 */
	public ResolvedType getGenericType() {
		if (!(isParameterizedType() || isRawType()))
			throw new BCException("The type "+getBaseName()+" is not parameterized or raw - it has no generic type");
		return null;
	}
	
	/**
	 * overriden by ReferenceType to return the gsig for a generic type
	 * @return
	 */
	public String getGenericSignature() {
		return "";
	}
	
	
	public ResolvedType parameterizedWith(UnresolvedType[] typeParameters) {
		if (!(isGenericType() || isParameterizedType())) return this;
		return TypeFactory.createParameterizedType(this.getGenericType(), typeParameters, getWorld());
	}
	
	/**
	 * Iff I am a parameterized type, and any of my parameters are type variable
	 * references, return a version with those type parameters replaced in accordance
	 * with the passed bindings.
	 */
	public UnresolvedType parameterize(Map typeBindings) {
	  	if (!isParameterizedType()) return this;//throw new IllegalStateException("Can't parameterize a type that is not a parameterized type");
    	boolean workToDo = false;
    	for (int i = 0; i < typeParameters.length; i++) {
			if (typeParameters[i].isTypeVariableReference() || 
					(typeParameters[i] instanceof BoundedReferenceType)) {
				workToDo = true;
			}
		}
    	if (!workToDo) {
    		return this;
    	} else {
    		UnresolvedType[] newTypeParams = new UnresolvedType[typeParameters.length];
    		for (int i = 0; i < newTypeParams.length; i++) {
				newTypeParams[i] = typeParameters[i];
				if (newTypeParams[i].isTypeVariableReference()) {
					TypeVariableReferenceType tvrt = (TypeVariableReferenceType) newTypeParams[i];
					UnresolvedType binding = (UnresolvedType) typeBindings.get(tvrt.getTypeVariable().getName());
					if (binding != null) newTypeParams[i] = binding;
				} else if (newTypeParams[i] instanceof BoundedReferenceType) {
					BoundedReferenceType brType = (BoundedReferenceType)newTypeParams[i];
					newTypeParams[i] = brType.parameterize(typeBindings);
//					brType.parameterize(typeBindings)
				}
			}
    		return TypeFactory.createParameterizedType(getGenericType(), newTypeParams, getWorld());
    	}
    }
	
	public boolean hasParameterizedSuperType() {
		getParameterizedSuperTypes();
		return parameterizedSuperTypes.length > 0;
	}
	
	public boolean hasGenericSuperType() {
		ResolvedType[] superTypes = getDeclaredInterfaces();
		for (int i = 0; i < superTypes.length; i++) {
			if (superTypes[i].isGenericType()) return true;
		}
		return false;
	}
	
	private ResolvedType[] parameterizedSuperTypes = null;
	/**
	 * Similar to the above method, but accumulates the super types
     *
	 * @return
	 */
	public ResolvedType[] getParameterizedSuperTypes() {
		if (parameterizedSuperTypes != null) return parameterizedSuperTypes;
		List accumulatedTypes = new ArrayList();
		accumulateParameterizedSuperTypes(this,accumulatedTypes);
		ResolvedType[] ret = new ResolvedType[accumulatedTypes.size()];
		parameterizedSuperTypes = (ResolvedType[]) accumulatedTypes.toArray(ret);
		return parameterizedSuperTypes;
	}
	
	private void accumulateParameterizedSuperTypes(ResolvedType forType, List parameterizedTypeList) {
		if (forType.isParameterizedType()) {
			parameterizedTypeList.add(forType);
		}
		if (forType.getSuperclass() != null) {
			accumulateParameterizedSuperTypes(forType.getSuperclass(), parameterizedTypeList);
		}
		ResolvedType[] interfaces = forType.getDeclaredInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			accumulateParameterizedSuperTypes(interfaces[i], parameterizedTypeList);
		}
	}

	/**
	 * Types may have pointcuts just as they have methods and fields.
	 */
	public ResolvedPointcutDefinition findPointcut(String name, World world) {
	    throw new UnsupportedOperationException("Not yet implemenented");
	}
	
	/**
	 * @return true if assignable to java.lang.Exception
	 */
	public boolean isException() {
		return (world.getCoreType(UnresolvedType.JAVA_LANG_EXCEPTION).isAssignableFrom(this));
	}
	
	/**
	 * @return true if it is an exception and it is a checked one, false otherwise.
	 */
	public boolean isCheckedException() {
		if (!isException()) return false;
		if (world.getCoreType(UnresolvedType.RUNTIME_EXCEPTION).isAssignableFrom(this)) return false;
		return true;
	}

	/**
	 * Determines if variables of this type could be assigned values of another
	 * with lots of help.  
	 * java.lang.Object is convertable from all types.
	 * A primitive type is convertable from X iff it's assignable from X.
	 * A reference type is convertable from X iff it's coerceable from X.
	 * In other words, X isConvertableFrom Y iff the compiler thinks that _some_ value of Y
	 * could be assignable to a variable of type X without loss of precision. 
	 * 
	 * @param other the other type
	 * @param world the {@link World} in which the possible assignment should be checked.
	 * @return true iff variables of this type could be assigned values of other with possible conversion
	 */
     public final boolean isConvertableFrom(ResolvedType other) {

//    	 // version from TypeX
//    	 if (this.equals(OBJECT)) return true;
//    	 if (this.isPrimitiveType() || other.isPrimitiveType()) return this.isAssignableFrom(other);
//    	 return this.isCoerceableFrom(other);
//    	 
    	 
    	 // version from ResolvedTypeX
    	 if (this.equals(OBJECT)) return true;
    	 if (world.isInJava5Mode()) {
        	if (this.isPrimitiveType()^other.isPrimitiveType()) { // If one is primitive and the other isnt
        		if (validBoxing.contains(this.getSignature()+other.getSignature())) return true;
        	}
    	 }
    	 if (this.isPrimitiveType() || other.isPrimitiveType()) return this.isAssignableFrom(other);
    	 return this.isCoerceableFrom(other);
	 }

	/**
	 * Determines if the variables of this type could be assigned values
	 * of another type without casting.  This still allows for assignment conversion
	 * as per JLS 2ed 5.2.  For object types, this means supertypeOrEqual(THIS, OTHER).
	 * 
	 * @param other the other type
	 * @param world the {@link World} in which the possible assignment should be checked.
	 * @return true iff variables of this type could be assigned values of other without casting
     * @throws NullPointerException if other is null
	 */
	public abstract boolean isAssignableFrom(ResolvedType other);
	
	public abstract boolean isAssignableFrom(ResolvedType other, boolean allowMissing);

	/**
	 * Determines if values of another type could possibly be cast to
	 * this type.  The rules followed are from JLS 2ed 5.5, "Casting Conversion".
     * <p/>
	 * <p> This method should be commutative, i.e., for all UnresolvedType a, b and all World w:
     * <p/>
	 * <blockquote><pre>
	 *    a.isCoerceableFrom(b, w) == b.isCoerceableFrom(a, w)
	 * </pre></blockquote>
	 *
	 * @param other the other type
	 * @param world the {@link World} in which the possible coersion should be checked.
	 * @return true iff values of other could possibly be cast to this type. 
     * @throws NullPointerException if other is null.
	 */
	public abstract boolean isCoerceableFrom(ResolvedType other);
	
	public boolean needsNoConversionFrom(ResolvedType o) {
	    return isAssignableFrom(o);
	}
	
	/** 
     * Implemented by ReferenceTypes
     */
	public String getSignatureForAttribute() {
		throw new RuntimeException("Cannot ask this type "+this+" for a generic sig attribute");
	}
	
	private FuzzyBoolean parameterizedWithAMemberTypeVariable = FuzzyBoolean.MAYBE;
	
	/**
	 * return true if the parameterization of this type includes a member type variable.  Member
	 * type variables occur in generic methods/ctors.
	 */
	public boolean isParameterizedWithAMemberTypeVariable() {
		// MAYBE means we haven't worked it out yet...
		if (parameterizedWithAMemberTypeVariable==FuzzyBoolean.MAYBE) {
			
			// if there are no type parameters then we cant be...
			if (typeParameters==null || typeParameters.length==0) {
				parameterizedWithAMemberTypeVariable = FuzzyBoolean.NO;
				return false;
			}
			
			for (int i = 0; i < typeParameters.length; i++) {
				UnresolvedType aType = (ResolvedType)typeParameters[i];
				if (aType.isTypeVariableReference()  && 
				// assume the worst - if its definetly not a type declared one, it could be anything
						((TypeVariableReference)aType).getTypeVariable().getDeclaringElementKind()!=TypeVariable.TYPE) {
					parameterizedWithAMemberTypeVariable = FuzzyBoolean.YES;
					return true;
				}
				if (aType.isParameterizedType()) {
					boolean b = aType.isParameterizedWithAMemberTypeVariable();
					if (b) {
						parameterizedWithAMemberTypeVariable = FuzzyBoolean.YES;
						return true;
					}
				}
				if (aType.isGenericWildcard()) {
					if (aType.isExtends()) {
						boolean b = false;
						UnresolvedType upperBound = aType.getUpperBound();
						if (upperBound.isParameterizedType()) {
							b = upperBound.isParameterizedWithAMemberTypeVariable();
						} else if (upperBound.isTypeVariableReference() && ((TypeVariableReference)upperBound).getTypeVariable().getDeclaringElementKind()==TypeVariable.METHOD) {
							b = true;
						}
						if (b) {
							parameterizedWithAMemberTypeVariable = FuzzyBoolean.YES;
							return true;
						}
						// FIXME asc need to check additional interface bounds
					}
					if (aType.isSuper()) {
						boolean b = false;
						UnresolvedType lowerBound = aType.getLowerBound();
						if (lowerBound.isParameterizedType()) {
							b = lowerBound.isParameterizedWithAMemberTypeVariable();
						} else if (lowerBound.isTypeVariableReference() && ((TypeVariableReference)lowerBound).getTypeVariable().getDeclaringElementKind()==TypeVariable.METHOD) {
							b = true;
						}
						if (b) {
							parameterizedWithAMemberTypeVariable = FuzzyBoolean.YES;
							return true;
						}
					}
				}
			}
			parameterizedWithAMemberTypeVariable=FuzzyBoolean.NO;
		}
		return parameterizedWithAMemberTypeVariable.alwaysTrue();
	}

	protected boolean ajMembersNeedParameterization() {
		if (isParameterizedType()) return true;
		if (getSuperclass() != null) return getSuperclass().ajMembersNeedParameterization();
		return false;
	}

	protected Map getAjMemberParameterizationMap() {
		Map myMap = getMemberParameterizationMap();
		if (myMap.size() == 0) {
			// might extend a parameterized aspect that we also need to consider...
			if (getSuperclass() != null) return getSuperclass().getAjMemberParameterizationMap();
		}
		return myMap;
	}
	
	public void setBinaryPath(String binaryPath) {
		this.binaryPath = binaryPath;
	}

	/**
	 * Returns the path to the jar or class file from which this
	 * binary aspect came or null if not a binary aspect 
	 */
	public String getBinaryPath() {
		return binaryPath;
	}
	    
}
