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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.bcel.BcelObjectType;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.PerClause;

public abstract class ResolvedTypeX extends TypeX {

    protected World world;
	

    ResolvedTypeX(String signature, World world) {
        super(signature);
        this.world = world;
    }

    // ---- things that don't require a world

	/** returns Iterator&lt;ResolvedTypeX&gt;
	 */
    public final Iterator getDirectSupertypes() {
        Iterator ifacesIterator = Iterators.array(getDeclaredInterfaces());
        ResolvedTypeX superclass = getSuperclass();
        if (superclass == null) {
            return ifacesIterator;
        } else {
            return Iterators.snoc(ifacesIterator, superclass);
        }
    }

    public abstract ResolvedMember[] getDeclaredFields();
    public abstract ResolvedMember[] getDeclaredMethods();
    public abstract ResolvedTypeX[] getDeclaredInterfaces();
    public abstract ResolvedMember[] getDeclaredPointcuts();
    public abstract ResolvedTypeX getSuperclass();
    public abstract int getModifiers();


    public abstract boolean needsNoConversionFrom(TypeX other);
    public abstract boolean isCoerceableFrom(TypeX other);
    public abstract boolean isAssignableFrom(TypeX other);
    
    // ---- things that would require a world if I weren't resolved
    public final Iterator getDirectSupertypes(World world) {
        return getDirectSupertypes();
    }  
    
    public final ResolvedMember[] getDeclaredFields(World world) {
        return getDeclaredFields();
    }    
    public final ResolvedMember[] getDeclaredMethods(World world) {
        return getDeclaredMethods();
    }    
    public final TypeX[] getDeclaredInterfaces(World world) {
        return getDeclaredInterfaces();
    }
    public final ResolvedMember[] getDeclaredPointcuts(World world) {
    	return getDeclaredPointcuts();
    }
  
    public final int getModifiers(World world) {
        return getModifiers();
    }    
    public final TypeX getSuperclass(World world) {
        return getSuperclass();
    }

    // conversions
    public final boolean isAssignableFrom(TypeX other, World world) {
        return isAssignableFrom(other);
    }   
    public final boolean isCoerceableFrom(TypeX other, World world) {
        return isCoerceableFrom(other);
    }
    public boolean needsNoConversionFrom(TypeX other, World world) {
        return needsNoConversionFrom(other);
    }
    public final boolean isConvertableFrom(TypeX other) {
        if (this.equals(OBJECT) || other.equals(OBJECT)) return true;
        return this.isCoerceableFrom(other);
    }
    
    // utilities                
    public ResolvedTypeX getResolvedComponentType() {
    	return null;
    }
	public ResolvedTypeX resolve(World world) {
		return this;
	}
	public World getWorld() {
		return world;
	}

    // ---- things from object

    public final boolean equals(Object other) {
        if (other instanceof ResolvedTypeX) {
            return this == other;
        } else {
            return super.equals(other);
        }
    }
 
    // ---- difficult things
    
    /**
     * returns an iterator through all of the fields of this type, in order
     * for checking from JVM spec 2ed 5.4.3.2.  This means that the order is
     *
     * <ul><li> fields from current class </li>
     *     <li> recur into direct superinterfaces </li>
     *     <li> recur into superclass </li>
     * </ul>
     * 
     * We keep a hashSet of interfaces that we've visited so we don't spiral
     * out into 2^n land.
     */
    public Iterator getFields() {
        final Iterators.Filter dupFilter = Iterators.dupFilter();
        Iterators.Getter typeGetter = new Iterators.Getter() {
            public Iterator get(Object o) {
                return 
                    dupFilter.filter(
                        ((ResolvedTypeX)o).getDirectSupertypes());
            }
        };
        Iterators.Getter fieldGetter = new Iterators.Getter() {
            public Iterator get(Object o) {
                return Iterators.array(((ResolvedTypeX)o).getDeclaredFields());
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
     *
     * <ul><li> methods from current class </li>
     *     <li> recur into superclass, all the way up, not touching interfaces </li>
     *     <li> recur into all superinterfaces, in some unspecified order </li>
     * </ul>
     * 
     * We keep a hashSet of interfaces that we've visited so we don't spiral
     * out into 2^n land.
     */
    public Iterator getMethods() {
        final Iterators.Filter dupFilter = Iterators.dupFilter();
        Iterators.Getter ifaceGetter = new Iterators.Getter() {
            public Iterator get(Object o) {
                return 
                    dupFilter.filter(
                        Iterators.array(((ResolvedTypeX)o).getDeclaredInterfaces())
                        );                       
            }
        };
        Iterators.Getter methodGetter = new Iterators.Getter() {
            public Iterator get(Object o) {
                return Iterators.array(((ResolvedTypeX)o).getDeclaredMethods());
            }
        };
        return 
            Iterators.mapOver(
                Iterators.append(
                    new Iterator() {
                        ResolvedTypeX curr = ResolvedTypeX.this;
                        public boolean hasNext() {
                            return curr != null;
                        }
                        public Object next() {
                            ResolvedTypeX ret = curr;
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
     * described in JVM spec 2ed 5.4.3.2
     */
    public ResolvedMember lookupField(Member m) {
        return lookupMember(m, getFields());
    }

    /**
     * described in JVM spec 2ed 5.4.3.3
     */
    public ResolvedMember lookupMethod(Member m) {
        return lookupMember(m, getMethods());
    }
    
    /** return null if not found */
    private ResolvedMember lookupMember(Member m, Iterator i) {
        while (i.hasNext()) {
            ResolvedMember f = (ResolvedMember) i.next();
            if (matches(f, m)) return f;
        }
        return null; //ResolvedMember.Missing;
        //throw new BCException("can't find " + m);
    }  
    
    /** return null if not found */
    private ResolvedMember lookupMember(Member m, ResolvedMember[] a) {
		for (int i = 0; i < a.length; i++) {
			ResolvedMember f = a[i];
            if (matches(f, m)) return f;		
		}
		return null;
    }      
    
    
    public static boolean matches(Member m1, Member m2) {
    	return m1.getName().equals(m2.getName()) && m1.getSignature().equals(m2.getSignature());
    }
    
    
    public static boolean conflictingSignature(Member m1, Member m2) {
    	if (m1 == null || m2 == null) return false;
    	
    	if (!m1.getName().equals(m2.getName())) { return false; }
    	if (m1.getKind() != m2.getKind()) { return false; }    	
    	
    	if (m1.getKind() == Member.FIELD) {
    		return m1.getDeclaringType().equals(m2.getDeclaringType());
    	} else if (m1.getKind() == Member.POINTCUT) {
    		return true;
    	}
    	
    	TypeX[] p1 = m1.getParameterTypes();
    	TypeX[] p2 = m2.getParameterTypes();
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
     *
     * <ul><li> pointcuts from current class </li>
     *     <li> recur into direct superinterfaces </li>
     *     <li> recur into superclass </li>
     * </ul>
     * 
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
                        ((ResolvedTypeX)o).getDirectSupertypes());
            }
        };
        Iterators.Getter pointcutGetter = new Iterators.Getter() {
            public Iterator get(Object o) {
                //System.err.println("getting for " + o);
                return Iterators.array(((ResolvedTypeX)o).getDeclaredPointcuts());
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
        return null; // should we throw an exception here?
    }
    
    
    // all about collecting CrosscuttingMembers

	//??? collecting data-structure, shouldn't really be a field
    public CrosscuttingMembers crosscuttingMembers;

	public CrosscuttingMembers collectCrosscuttingMembers() {
		crosscuttingMembers = new CrosscuttingMembers(this);
		crosscuttingMembers.setPerClause(getPerClause());
		crosscuttingMembers.addShadowMungers(collectShadowMungers());
		crosscuttingMembers.addTypeMungers(getTypeMungers());
		crosscuttingMembers.addDeclares(collectDeclares());
		crosscuttingMembers.addPrivilegedAccesses(getPrivilegedAccesses());
		
		//System.err.println("collected cc members: " + this + ", " + collectDeclares());
		return crosscuttingMembers;
	}
	
	private final Collection collectDeclares() {
		if (! this.isAspect() ) return Collections.EMPTY_LIST;
		
		ArrayList ret = new ArrayList();
		//if (this.isAbstract()) {
		for (Iterator i = getDeclares().iterator(); i.hasNext();) {
			Declare dec = (Declare) i.next();
			if (!dec.isAdviceLike()) ret.add(dec);
		}
		if (!this.isAbstract()) {
			//ret.addAll(getDeclares());
			final Iterators.Filter dupFilter = Iterators.dupFilter();
	        Iterators.Getter typeGetter = new Iterators.Getter() {
	            public Iterator get(Object o) {
	                return 
	                    dupFilter.filter(
	                        ((ResolvedTypeX)o).getDirectSupertypes());
	            }
	        };
	        Iterator typeIterator = Iterators.recur(this, typeGetter);
	
	        while (typeIterator.hasNext()) {
	        	ResolvedTypeX ty = (ResolvedTypeX) typeIterator.next();
	        	//System.out.println("super: " + ty + ", " + );
	        	for (Iterator i = ty.getDeclares().iterator(); i.hasNext();) {
					Declare dec = (Declare) i.next();
					if (dec.isAdviceLike()) ret.add(dec);
				}
	        }
		}
		
		return ret;
    }
    
	
	
	
    private final Collection collectShadowMungers() {
        if (! this.isAspect() || this.isAbstract()) return Collections.EMPTY_LIST;

		ArrayList acc = new ArrayList();
        final Iterators.Filter dupFilter = Iterators.dupFilter();
        Iterators.Getter typeGetter = new Iterators.Getter() {
            public Iterator get(Object o) {
                return 
                    dupFilter.filter(
                        ((ResolvedTypeX)o).getDirectSupertypes());
            }
        };
        Iterator typeIterator = Iterators.recur(this, typeGetter);

        while (typeIterator.hasNext()) {
            ResolvedTypeX ty = (ResolvedTypeX) typeIterator.next();
            acc.addAll(ty.getDeclaredShadowMungers());     
        }
        
        return acc;
    }
    
	public PerClause getPerClause() { return null; }
	protected Collection getDeclares() {
		return Collections.EMPTY_LIST; 
	}
	protected Collection getTypeMungers() { return Collections.EMPTY_LIST; }
	
	protected Collection getPrivilegedAccesses() { return Collections.EMPTY_LIST; }
	
	

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
    
    
    public boolean isSynthetic() {
    	return signature.indexOf("$ajc") != -1;
    }
    
    public final boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

	public Collection getDeclaredAdvice() {
		List l = new ArrayList();
		ResolvedMember[] methods = getDeclaredMethods();
		for (int i=0, len = methods.length; i < len; i++) {
			ShadowMunger munger = methods[i].getAssociatedShadowMunger();
			if (munger != null) l.add(munger);
		}
		return l;
	}
	
	private List shadowMungers = new ArrayList(0);

	public Collection getDeclaredShadowMungers() {
		Collection c = getDeclaredAdvice();
		c.addAll(shadowMungers);
		return c;
	}
	
	
	public void addShadowMunger(ShadowMunger munger) {
		shadowMungers.add(munger);
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
    
    public static final ResolvedTypeX[] NONE = new ResolvedTypeX[0];

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
    
    // ---- types
    
    public static class Name extends ResolvedTypeX {
    	private ConcreteName delegate = null;
    	private ISourceContext sourceContext = null;
    	private int startPos = 0;
    	private int endPos = 0;

		//??? should set delegate before any use
        public Name(String signature, World world) {
            super(signature, world);
        }
	        
	    public final boolean isClass() {
	    	return delegate.isClass();
	    }
	    
	    public boolean isAspect() {
	    	return delegate.isAspect();
	    }
	     
        public final boolean needsNoConversionFrom(TypeX o) {
            return isAssignableFrom(o);
        }
	     
        public final boolean isAssignableFrom(TypeX o) {
            if (o.isPrimitive()) return false;
            ResolvedTypeX other = o.resolve(world);

            return isAssignableFrom(other);
        }
        
        public final boolean isCoerceableFrom(TypeX o) {
            ResolvedTypeX other = o.resolve(world);

            if (this.isAssignableFrom(other) || other.isAssignableFrom(this)) {
                return true;
            }          
            if (!this.isInterface() && !other.isInterface()) {
                return false;
            }
            if (this.isFinal() || other.isFinal()) {
                return false;
            }            
            // ??? needs to be Methods, not just declared methods? JLS 5.5 unclear
            ResolvedMember[] a = getDeclaredMethods();
            ResolvedMember[] b = ((Name)other).getDeclaredMethods();  //??? is this cast always safe
            for (int ai = 0, alen = a.length; ai < alen; ai++) {
                for (int bi = 0, blen = b.length; bi < blen; bi++) {
                    if (! b[bi].isCompatibleWith(a[ai])) return false;
                }
            } 
            return true;
        }
        
        private boolean isAssignableFrom(ResolvedTypeX other) {
            if (this == other) return true;
            for(Iterator i = other.getDirectSupertypes(); i.hasNext(); ) {
                if (this.isAssignableFrom((ResolvedTypeX) i.next())) return true;
            }       
            return false;
        }

		public ISourceContext getSourceContext() {
			return sourceContext;
		}
		
		public ISourceLocation getSourceLocation() {
			if (sourceContext == null) return null;
			return sourceContext.makeSourceLocation(new Position(startPos, endPos));
		}

		public boolean isExposedToWeaver() {
			return delegate.isExposedToWeaver();  //??? where does this belong
		}
		
		public boolean isWovenBy(ResolvedTypeX aspectType) {
			return delegate.isWovenBy(aspectType);
		}

		public ResolvedMember[] getDeclaredFields() {
			return delegate.getDeclaredFields();
		}

		public ResolvedTypeX[] getDeclaredInterfaces() {
			return delegate.getDeclaredInterfaces();
		}

		public ResolvedMember[] getDeclaredMethods() {
			return delegate.getDeclaredMethods();
		}

		public ResolvedMember[] getDeclaredPointcuts() {
			return delegate.getDeclaredPointcuts();
		}

		public PerClause getPerClause() { return delegate.getPerClause(); }
		protected Collection getDeclares() { return delegate.getDeclares(); }
		protected Collection getTypeMungers() { return delegate.getTypeMungers(); }
		
		protected Collection getPrivilegedAccesses() { return delegate.getPrivilegedAccesses(); }


		public int getModifiers() {
			return delegate.getModifiers();
		}

		public ResolvedTypeX getSuperclass() {
			return delegate.getSuperclass();
		}


		public ConcreteName getDelegate() {
			return delegate;
		}

		public void setDelegate(ConcreteName delegate) {
			this.delegate = delegate;
		}
		public int getEndPos() {
			return endPos;
		}

		public int getStartPos() {
			return startPos;
		}

		public void setEndPos(int endPos) {
			this.endPos = endPos;
		}

		public void setSourceContext(ISourceContext sourceContext) {
			this.sourceContext = sourceContext;
		}

		public void setStartPos(int startPos) {
			this.startPos = startPos;
		}

    }
    
    public static abstract class ConcreteName {
    	//protected ISourceContext sourceContext;
    	protected boolean exposedToWeaver;
    	ResolvedTypeX.Name resolvedTypeX;
	

        public ConcreteName(ResolvedTypeX.Name resolvedTypeX, boolean exposedToWeaver) {
            //???super(signature, world);
            this.resolvedTypeX = resolvedTypeX;
            this.exposedToWeaver = exposedToWeaver;
        }
	        
	    public final boolean isClass() {
	    	return !isAspect() && !isInterface();
	    }
	    
	    public abstract boolean isAspect();
	    public abstract boolean isInterface();

		public abstract ResolvedMember[] getDeclaredFields();

		public abstract ResolvedTypeX[] getDeclaredInterfaces();

		public abstract ResolvedMember[] getDeclaredMethods();

		public abstract ResolvedMember[] getDeclaredPointcuts();

		public abstract PerClause getPerClause();
		protected abstract Collection getDeclares() ;
		protected abstract Collection getTypeMungers();
		
		protected abstract Collection getPrivilegedAccesses();


		public abstract int getModifiers();

		public abstract ResolvedTypeX getSuperclass();

//		public abstract ISourceLocation getSourceLocation();

		public abstract boolean isWovenBy(ResolvedTypeX aspectType);

//		public ISourceContext getSourceContext() {
//			return sourceContext;
//		}

		public boolean isExposedToWeaver() {
			return exposedToWeaver;
		}

		public ResolvedTypeX.Name getResolvedTypeX() {
			return resolvedTypeX;
		}

	}
    
    static class Array extends ResolvedTypeX {
        ResolvedTypeX componentType;
        Array(String s, World world, ResolvedTypeX componentType) {
            super(s, world);
            this.componentType = componentType;
        }
        public final ResolvedMember[] getDeclaredFields() {
            return ResolvedMember.NONE;
        }
        public final ResolvedMember[] getDeclaredMethods() {
            // ??? should this return clone?  Probably not...
            return ResolvedMember.NONE;
        }
        public final ResolvedTypeX[] getDeclaredInterfaces() {
            return
                new ResolvedTypeX[] {
                    world.resolve(CLONEABLE), 
                    world.resolve(SERIALIZABLE)
                };
        }
        public final ResolvedMember[] getDeclaredPointcuts() {
            return ResolvedMember.NONE;
        }
        
        public final ResolvedTypeX getSuperclass() {
            return world.resolve(OBJECT);
        }
        public final boolean isAssignableFrom(TypeX o) {
            if (! o.isArray()) return false;
            if (o.getComponentType().isPrimitive()) {
                return o.equals(this);
            } else {
                return getComponentType().isAssignableFrom(o.getComponentType(), world);
            }
        }
        public final boolean isCoerceableFrom(TypeX o) {
            if (o.equals(TypeX.OBJECT) || 
                    o.equals(TypeX.SERIALIZABLE) ||
                    o.equals(TypeX.CLONEABLE)) {
                return true;
            }
            if (! o.isArray()) return false;
            if (o.getComponentType().isPrimitive()) {
                return o.equals(this);
            } else {
                return getComponentType().isCoerceableFrom(o.getComponentType(), world);
            }
        }
        public final boolean needsNoConversionFrom(TypeX o) {
            return isAssignableFrom(o);
        }
        public final int getModifiers() {
            int mask = Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED;
            return (componentType.getModifiers() & mask) | Modifier.FINAL;
        }
        public TypeX getComponentType() {
            return componentType;
        }
        public ResolvedTypeX getResolvedComponentType() {
            return componentType;
        }
        public ISourceContext getSourceContext() {
        	return getResolvedComponentType().getSourceContext();
        }
    }
    
    static class Primitive extends ResolvedTypeX {
        private int size;
        private int index;
        Primitive(String signature, int size, int index) {
            super(signature, null);
            this.size = size;
            this.index = index;
        }
        public final int getSize() {
            return size;
        }
        public final int getModifiers() {
            return Modifier.PUBLIC | Modifier.FINAL;
        }
        public final boolean isPrimitive() {
            return true;
        }
        public final boolean isAssignableFrom(TypeX other) {
            if (! other.isPrimitive()) return false;
            return assignTable[((Primitive)other).index][index];
        }
        public final boolean isCoerceableFrom(TypeX other) {
            if (this == other) return true;
            if (! other.isPrimitive()) return false;
            if (index > 6 || ((Primitive)other).index > 6) return false;
            return true;
        }
        public final boolean needsNoConversionFrom(TypeX other) {
            if (! other.isPrimitive()) return false;
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
        public final ResolvedTypeX[] getDeclaredInterfaces() {
            return ResolvedTypeX.NONE;
        }
        public final ResolvedMember[] getDeclaredPointcuts() {
            return ResolvedMember.NONE;
        }

        public final ResolvedTypeX getSuperclass() {
            return null;
        }
        
        public ISourceContext getSourceContext() {
        	return null;
        }
   
    }

    static class Missing extends ResolvedTypeX {
        Missing() {
            super(MISSING_NAME, null);
        }       
//        public final String toString() {
//            return "<missing>";
//        }      
        public final String getName() {
        	return MISSING_NAME;
        }
        public final ResolvedMember[] getDeclaredFields() {
            return ResolvedMember.NONE;
        }
        public final ResolvedMember[] getDeclaredMethods() {
            return ResolvedMember.NONE;
        }
        public final ResolvedTypeX[] getDeclaredInterfaces() {
            return ResolvedTypeX.NONE;
        }

        public final ResolvedMember[] getDeclaredPointcuts() {
            return ResolvedMember.NONE;
        }
        public final ResolvedTypeX getSuperclass() {
            return null;
        }
        public final int getModifiers() {
            return 0;
        }
        public final boolean isAssignableFrom(TypeX other) {
            return false;
        }   
        public final boolean isCoerceableFrom(TypeX other) {
            return false;
        }        
        public boolean needsNoConversionFrom(TypeX other) {
            return false;
        }
        public ISourceContext getSourceContext() {
        	return null;
        }

    }

    /** return null if not found */
	public ResolvedMember lookupMemberNoSupers(Member member) {
		if (member.getKind() == Member.FIELD) {
			return lookupMember(member, getDeclaredFields());
		} else {
			// assert member.getKind() == Member.METHOD || member.getKind() == Member.CONSTRUCTOR
			return lookupMember(member, getDeclaredMethods());
		}
	}
	
	protected List interTypeMungers = new ArrayList(0);
	
	public List getInterTypeMungers() {
		return interTypeMungers;
	}
	
    /**
     * Returns a ResolvedTypeX object representing the declaring type of this type, or
     * null if this type does not represent a non-package-level-type.
     * 
     * <strong>Warning</strong>:  This is guaranteed to work for all member types.
     * For anonymous/local types, the only guarantee is given in JLS 13.1, where
     * it guarantees that if you call getDeclaringType() repeatedly, you will eventually
     * get the top-level class, but it does not say anything about classes in between.
     *
     * @return the declaring TypeX object, or null.
     */
    public ResolvedTypeX getDeclaringType() {
    	if (isArray()) return null;
		String name = getName();
		int lastDollar = name.lastIndexOf('$');
		while (lastDollar != -1) {
			ResolvedTypeX ret = world.resolve(TypeX.forName(name.substring(0, lastDollar)), true);
			if (ret != ResolvedTypeX.MISSING) return ret;
			lastDollar = name.lastIndexOf('$', lastDollar-1);
		}
		return null;
    }
	
	
	public static boolean isVisible(int modifiers, ResolvedTypeX targetType, ResolvedTypeX fromType) {
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

	private static boolean samePackage(
		ResolvedTypeX targetType,
		ResolvedTypeX fromType)
	{
		String p1 = targetType.getPackageName();
		String p2 = fromType.getPackageName();
		if (p1 == null) return p2 == null;
		if (p2 == null) return false;
		return p1.equals(p2);
	}
	
	public void addInterTypeMunger(ConcreteTypeMunger munger) {
		ResolvedMember sig = munger.getSignature();
		if (sig == null || munger.getMunger() == null || 
				munger.getMunger().getKind() == ResolvedTypeMunger.PrivilegedAccess)
		{
			interTypeMungers.add(munger);
			return;
		}
		
		//System.err.println("add: " + munger + " to " + this.getClassName() + " with " + interTypeMungers);
		if (sig.getKind() == Member.METHOD) {
			if (!compareToExistingMembers(munger, getMethods())) return;
			if (this.isInterface()) {
				if (!compareToExistingMembers(munger, 
						Arrays.asList(world.resolve(OBJECT).getDeclaredMethods()).iterator())) return;
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
						c = getWorld().comparePrecedence(munger.getAspectType(), existingMunger.getAspectType());
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
		interTypeMungers.add(munger);
	}
	
	
	//??? returning too soon
	private boolean compareToExistingMembers(ConcreteTypeMunger munger, Iterator existingMembers) {
		ResolvedMember sig = munger.getSignature();
		while (existingMembers.hasNext()) {
			ResolvedMember existingMember = (ResolvedMember)existingMembers.next();
			
			if (conflictingSignature(existingMember, munger.getSignature())) {
				//System.err.println("conflict: " + existingMember + " with " + munger);
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
						//XXX dual errors possible if (this instanceof BcelObjectType) return false;  //XXX ignores separate comp
						getWorld().getMessageHandler().handleMessage(
							MessageUtil.error("inter-type declaration from " + munger.getAspectType().getName() +
											" conflicts with existing member: " + existingMember,
											munger.getSourceLocation())
						);
					}
				} else {
					//interTypeMungers.add(munger);
				}
				//return;
			}
		}
		return true;
	}
	
	public boolean checkLegalOverride(ResolvedMember parent, ResolvedMember child) {
		//System.err.println("check: " + child.getDeclaringType() + " overrides " + parent.getDeclaringType());
		if (!parent.getReturnType().equals(child.getReturnType())) {
			world.showMessage(IMessage.ERROR,
				"can't override " + parent +
				" with " + child + " return types don't match",
				child.getSourceLocation(), parent.getSourceLocation());
			return false;
		}		
		if (parent.getKind() == Member.POINTCUT) {
			TypeX[] pTypes = parent.getParameterTypes();
			TypeX[] cTypes = child.getParameterTypes();
			if (!Arrays.equals(pTypes, cTypes)) {
				world.showMessage(IMessage.ERROR,
					"can't override " + parent +
					" with " + child + " parameter types don't match",
					child.getSourceLocation(), parent.getSourceLocation());
				return false;
			}
		}		
		//System.err.println("check: " + child.getModifiers() + " more visible " + parent.getModifiers());
		if (isMoreVisible(parent.getModifiers(), child.getModifiers())) {
			world.showMessage(IMessage.ERROR,
				"can't override " + parent +
				" with " + child + " visibility is reduced",
				child.getSourceLocation(), parent.getSourceLocation());
			return false;
		}
		
		// check declared exceptions
		ResolvedTypeX[] childExceptions = world.resolve(child.getExceptions());
		ResolvedTypeX[] parentExceptions = world.resolve(parent.getExceptions());
		ResolvedTypeX runtimeException = world.resolve("java.lang.RuntimeException");
		ResolvedTypeX error = world.resolve("java.lang.Error");
		
		outer: for (int i=0, leni = childExceptions.length; i < leni; i++) {
			//System.err.println("checking: " + childExceptions[i]);
			if (runtimeException.isAssignableFrom(childExceptions[i])) continue;
			if (error.isAssignableFrom(childExceptions[i])) continue;
			
			for (int j = 0, lenj = parentExceptions.length; j < lenj; j++) {
				if (parentExceptions[j].isAssignableFrom(childExceptions[i])) continue outer;
			}
			
			world.showMessage(IMessage.ERROR, "overriden method doesn't throw " 
					+ childExceptions[i].getName(), child.getSourceLocation(), null);
						
			return false;
		}
		
		return true;
		
	}
	
	private int compareMemberPrecedence(ResolvedMember m1, ResolvedMember m2) {
		//if (!m1.getReturnType().equals(m2.getReturnType())) return 0;
		
		if (Modifier.isAbstract(m1.getModifiers())) return -1;
		if (Modifier.isAbstract(m2.getModifiers())) return +1;
	
		if (m1.getDeclaringType().equals(m2.getDeclaringType())) return 0;
		
		ResolvedTypeX t1 = m1.getDeclaringType().resolve(world);
		ResolvedTypeX t2 = m2.getDeclaringType().resolve(world);
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
		ConcreteTypeMunger m2)
	{
		//XXX this works only if we ignore separate compilation issues
		//XXX dual errors possible if (this instanceof BcelObjectType) return;
		
		//System.err.println("conflict at " + m2.getSourceLocation());
		getWorld().showMessage(IMessage.ERROR,
			"intertype declaration from "
				+ m1.getAspectType().getName()
				+ " conflicts with intertype declaration: "
				+ m2.getSignature()
				+ " from "
				+ m2.getAspectType().getName(),
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
		return null;
	}

	public void clearInterTypeMungers() {
		interTypeMungers = new ArrayList();
	}


	public boolean isTopmostImplementor(ResolvedTypeX interfaceType) {
		if (isInterface()) return false;
		if (!interfaceType.isAssignableFrom(this)) return false;
		// check that I'm truly the topmost implementor
		if (interfaceType.isAssignableFrom(this.getSuperclass())) {
			return false;
		}
		return true;
	}
	
	public List getExposedPointcuts() {
		List ret = new ArrayList();
		if (getSuperclass() != null) ret.addAll(getSuperclass().getExposedPointcuts());
		
		for (Iterator i = Arrays.asList(getDeclaredInterfaces()).iterator(); i.hasNext(); ) {
			ResolvedTypeX t = (ResolvedTypeX)i.next();
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
						"inherited abstract " + inherited + 
						" is not made concrete in " + this.getName(),
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
							"conflicting inherited pointcuts in "
								+ this.getName() + toAdd.getSignature(),
							existing.getSourceLocation(),
							toAdd.getSourceLocation());
						j.remove();
					}
				}
			}
			acc.add(toAdd);
		}
	}
	
	public ISourceLocation getSourceLocation() { return null; }
	public boolean isExposedToWeaver() { return false; }
	public boolean isWovenBy(ResolvedTypeX aspectType) {
		return false;
	}

}
