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


package org.aspectj.weaver;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


public class MemberImpl implements Comparable, Member {

    protected MemberKind kind;
    protected String name;

    protected UnresolvedType declaringType;
    protected int modifiers; 
    protected UnresolvedType returnType;
    protected UnresolvedType[] parameterTypes;
    private final String signature;
    private String paramSignature;
    
    // OPTIMIZE move out of the member!
    private boolean reportedCantFindDeclaringType = false;
    private boolean reportedUnresolvableMember = false;


    public AnnotationX[] getAnnotations() {
    	throw new IllegalStateException("Cannot answer getAnnotations() for MemberImpl "+this.toString());
    }
    
    /**
     * All the signatures that a join point with this member as its signature has.
     * The fact that this has to go on MemberImpl and not ResolvedMemberImpl says a lot about
     * how broken the Member/ResolvedMember distinction currently is.
     */
    private JoinPointSignatureIterator joinPointSignatures = null;

    public MemberImpl(
        MemberKind kind, 
        UnresolvedType declaringType,
        int modifiers,
        String name,
        String signature) 
    {
        this.kind = kind;
        this.declaringType = declaringType;
        this.modifiers = modifiers;
        this.name = name;
        if (kind!=STATIC_INITIALIZATION && name!=null && name.equals("<clinit>")) {
        	throw new RuntimeException("!");
        }
        this.signature = signature;
        if (kind == FIELD) {
            this.returnType = UnresolvedType.forSignature(signature);
            this.parameterTypes = UnresolvedType.NONE;
        } else {
            Object[] returnAndParams = signatureToTypes(signature,false);
            this.returnType = (UnresolvedType) returnAndParams[0];
            this.parameterTypes = (UnresolvedType[]) returnAndParams[1];
// always safe not to do this ?!?            
//          String oldsig=new String(signature);
//			signature = typesToSignature(returnType,parameterTypes,true);
        }
    }

    public  MemberImpl(
        MemberKind kind, 
        UnresolvedType declaringType, 
        int modifiers,
        UnresolvedType returnType, 
        String name, 
        UnresolvedType[] parameterTypes) 
    {
        super();
        this.kind = kind;
        this.declaringType = declaringType;
        this.modifiers = modifiers;      
        if (name!=null && name.equals("<clinit>") && kind!=STATIC_INITIALIZATION) {
        	throw new RuntimeException("!");
        	}

        this.returnType = returnType;
        this.name = name;
        this.parameterTypes = parameterTypes;
        if (kind == FIELD) {
            this.signature         = returnType.getErasureSignature();
        } else {
            this.signature         = typesToSignature(returnType, parameterTypes,true);
        }
    }
    
    /* (non-Javadoc)
	 * @see org.aspectj.weaver.Member#resolve(org.aspectj.weaver.World)
	 */
    public ResolvedMember resolve(World world) {
    	return world.resolve(this);
    }

    // ---- utility methods
    
    /** returns an Object[] pair of UnresolvedType, UnresolvedType[] representing return type, 
     * argument types parsed from the JVM bytecode signature of a method.  Yes,
     * this should actually return a nice statically-typed pair object, but we
     * don't have one of those.  
     *
     * <blockquote><pre>
     *   UnresolvedType.signatureToTypes("()[Z")[0].equals(Type.forSignature("[Z"))
     *   UnresolvedType.signatureToTypes("(JJ)I")[1]
     *      .equals(UnresolvedType.forSignatures(new String[] {"J", "J"}))
     * </pre></blockquote>
     *
     * @param      signature the JVM bytecode method signature string we want to break apart
     * @return     a pair of UnresolvedType, UnresolvedType[] representing the return types and parameter types. 
     */
    public static String typesToSignature(UnresolvedType returnType, UnresolvedType[] paramTypes, boolean useRawTypes) {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        for (int i = 0, len = paramTypes.length; i < len; i++) {
			if (paramTypes[i].isParameterizedType() && useRawTypes) buf.append(paramTypes[i].getErasureSignature());
			else if (paramTypes[i].isTypeVariableReference() && useRawTypes) buf.append(paramTypes[i].getErasureSignature());
			else                                                buf.append(paramTypes[i].getSignature());
        }
        buf.append(")");
        if (returnType.isParameterizedType() && useRawTypes) buf.append(returnType.getErasureSignature());
		else if (returnType.isTypeVariableReference() && useRawTypes) buf.append(returnType.getErasureSignature());
        else 											 buf.append(returnType.getSignature());
        return buf.toString();        
    }
    
    /**
     * Returns "(<signaturesOfParamTypes>,...)" - unlike the other typesToSignature
     * that also includes the return type, this one just deals with the parameter types.
     */
    public static String typesToSignature(UnresolvedType[] paramTypes) {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        for(int i=0;i<paramTypes.length;i++) {
         buf.append(paramTypes[i].getSignature());
        }
        buf.append(")");
        return buf.toString();   
    }
    
    /** 
     * returns an Object[] pair of UnresolvedType, UnresolvedType[] representing return type, 
     * argument types parsed from the JVM bytecode signature of a method.  Yes,
     * this should actually return a nice statically-typed pair object, but we
     * don't have one of those.  
     *
     * <blockquote><pre>
     *   UnresolvedType.signatureToTypes("()[Z")[0].equals(Type.forSignature("[Z"))
     *   UnresolvedType.signatureToTypes("(JJ)I")[1]
     *      .equals(UnresolvedType.forSignatures(new String[] {"J", "J"}))
     * </pre></blockquote>
     *
     * @param      signature the JVM bytecode method signature string we want to break apart
     * @return     a pair of UnresolvedType, UnresolvedType[] representing the return types and parameter types. 
     */
    private static Object[] signatureToTypes(String sig,boolean keepParameterizationInfo) {
        List l = new ArrayList();
        int i = 1;
        boolean hasAnyAnglies = sig.indexOf('<')!=-1;
        while (true) {
            char c = sig.charAt(i);
            if (c == ')') break; // break out when the hit the ')'
            int start = i;
            while (c == '[') c = sig.charAt(++i);
            if (c == 'L' || c == 'P') {
				int nextSemicolon = sig.indexOf(';',start);
				int firstAngly = (hasAnyAnglies?sig.indexOf('<',start):-1);
				if (!hasAnyAnglies || firstAngly == -1 || firstAngly>nextSemicolon) {
                  i = nextSemicolon + 1;
                  l.add(UnresolvedType.forSignature(sig.substring(start, i)));
				} else {
					// generics generics generics
					// Have to skip to the *correct* ';'
					boolean endOfSigReached = false;
					int posn = firstAngly;
					int genericDepth=0;
					while (!endOfSigReached) {
						switch (sig.charAt(posn)) {
						  case '<': genericDepth++;break;
						  case '>': genericDepth--;break;
						  case ';': if (genericDepth==0) endOfSigReached=true;break;
						  default:
						}
						posn++;
					}
					// posn now points to the correct nextSemicolon :)
					i=posn;
					l.add(UnresolvedType.forSignature(sig.substring(start,i)));					
				}
            } else if (c=='T') { // assumed 'reference' to a type variable, so just "Tname;"
				int nextSemicolon = sig.indexOf(';',start);
				String nextbit = sig.substring(start,nextSemicolon);
				l.add(UnresolvedType.forSignature(nextbit));
				i=nextSemicolon+1;
            } else {
            	i++;
                l.add(UnresolvedType.forSignature(sig.substring(start, i)));
            }
        }
        UnresolvedType[] paramTypes = (UnresolvedType[]) l.toArray(new UnresolvedType[l.size()]);
        UnresolvedType returnType = UnresolvedType.forSignature(sig.substring(i+1, sig.length()));
        return new Object[] { returnType, paramTypes };
    }            

    // ---- factory methods
    public static MemberImpl field(String declaring, int mods, String name, String signature) {
        return field(declaring, mods, UnresolvedType.forSignature(signature), name);
    }
    public static Member field(UnresolvedType declaring, int mods, String name, UnresolvedType type) {
        return new MemberImpl(FIELD, declaring, mods, type, name, UnresolvedType.NONE);
    }    
    public static MemberImpl method(UnresolvedType declaring, int mods, String name, String signature) {
        Object[] pair = signatureToTypes(signature,false);
        return method(declaring, mods, (UnresolvedType) pair[0], name, (UnresolvedType[]) pair[1]);
    }

    public static MemberImpl monitorEnter() {
    	return new MemberImpl(MONITORENTER,UnresolvedType.OBJECT,Modifier.STATIC,ResolvedType.VOID,"<lock>",UnresolvedType.ARRAY_WITH_JUST_OBJECT);
    }

    public static MemberImpl monitorExit() {
    	return new MemberImpl(MONITOREXIT,UnresolvedType.OBJECT,Modifier.STATIC,ResolvedType.VOID,"<unlock>",UnresolvedType.ARRAY_WITH_JUST_OBJECT);
    }

    public static Member pointcut(UnresolvedType declaring, String name, String signature) {
        Object[] pair = signatureToTypes(signature,false);
        return pointcut(declaring, 0, (UnresolvedType) pair[0], name, (UnresolvedType[]) pair[1]);
    }


    private static MemberImpl field(String declaring, int mods, UnresolvedType ty, String name) {
        return new MemberImpl(FIELD, UnresolvedType.forName(declaring), mods, ty, name, UnresolvedType.NONE);
    }
    
    public static MemberImpl method(UnresolvedType declTy, int mods, UnresolvedType rTy, String name, UnresolvedType[] paramTys) {
        return new MemberImpl(
        	//??? this calls <clinit> a method
            name.equals("<init>") ? CONSTRUCTOR : METHOD,
            declTy,
            mods,
            rTy,
            name,
            paramTys);
    }
    private static Member pointcut(UnresolvedType declTy, int mods, UnresolvedType rTy, String name, UnresolvedType[] paramTys) {
        return new MemberImpl(
            POINTCUT,
            declTy,
            mods,
            rTy,
            name,
            paramTys);
    }
    
	public static ResolvedMemberImpl makeExceptionHandlerSignature(UnresolvedType inType, UnresolvedType catchType) {
		return new ResolvedMemberImpl(
			HANDLER,
			inType,
			Modifier.STATIC,
			"<catch>",
			"(" + catchType.getSignature() + ")V");
	}
    

    // ---- things we know without resolution
    
    public boolean equals(Object other) {
        if (! (other instanceof Member)) return false;
        Member o = (Member) other;
        return (getKind() == o.getKind() 
            && getName().equals(o.getName()) 
            && getSignature().equals(o.getSignature())
            && getDeclaringType().equals(o.getDeclaringType()));
    }
    
    /** 
     * Equality is checked based on the underlying signature, so the hash code
     * of a member is based on its kind, name, signature, and declaring type.  The
     * algorithm for this was taken from page 38 of effective java.
     */
    private volatile int hashCode = 0;
    public int hashCode() {
        if (hashCode == 0) {
            int result = 17;
            result = 37*result + getKind().hashCode();
            result = 37*result + getName().hashCode();
            result = 37*result + getSignature().hashCode();
            result = 37*result + getDeclaringType().hashCode();
            hashCode = result;
        } 
        return hashCode;
    }

    /* (non-Javadoc)
	 * @see org.aspectj.weaver.Member#compareTo(java.lang.Object)
	 */
    public int compareTo(Object other) {
    	Member o = (Member) other;
    	
    	int i = getName().compareTo(o.getName());
    	if (i != 0) return i;
    	return getSignature().compareTo(o.getSignature());
    }
    
    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	buf.append(returnType.getName());
    	buf.append(' ');
   		buf.append(declaringType.getName());
        buf.append('.');
   		buf.append(name);
    	if (kind != FIELD) {
    		buf.append("(");
            if (parameterTypes.length != 0) {
                buf.append(parameterTypes[0]);
        		for (int i=1, len = parameterTypes.length; i < len; i++) {
                    buf.append(", ");
        		    buf.append(parameterTypes[i].getName());
        		}
            }
    		buf.append(")");
    	}
    	return buf.toString();
    }
    
    // Overridden by subclasses - a method can be advice
    public MemberKind getKind() { 
    	return kind; 
    }
    
    public UnresolvedType getDeclaringType() { return declaringType; }
    
    public UnresolvedType getReturnType() { return returnType; }
    
    public UnresolvedType getGenericReturnType() { return getReturnType(); }
    public UnresolvedType[] getGenericParameterTypes() { return getParameterTypes(); }

    public UnresolvedType getType() { 
    	return returnType; 
    }
    
    public String getName() { 
    	return name; 
    }

    public UnresolvedType[]  getParameterTypes() { return parameterTypes; }
    
    public String getSignature() { return signature; }
	
    public int getArity() { return parameterTypes.length; }
  
    public String getParameterSignature() {
    	if (paramSignature != null) return paramSignature;
    	StringBuffer sb = new StringBuffer();
    	sb.append("(");
    	for (int i = 0; i < parameterTypes.length; i++) {
			UnresolvedType tx = parameterTypes[i];
			sb.append(tx.getSignature());
		}
    	sb.append(")");
    	paramSignature = sb.toString();
    	return paramSignature;
    }
    
    /* (non-Javadoc)
	 * @see org.aspectj.weaver.Member#isCompatibleWith(org.aspectj.weaver.Member)
	 */
    public boolean isCompatibleWith(Member am) {
        if (kind != METHOD || am.getKind() != METHOD) return true;
        if (! name.equals(am.getName())) return true;
        if (! equalTypes(getParameterTypes(), am.getParameterTypes())) return true;
        return getReturnType().equals(am.getReturnType());
    }
    
    private static boolean equalTypes(UnresolvedType[] a, UnresolvedType[] b) {
        int len = a.length;
        if (len != b.length) return false;
        for (int i = 0; i < len; i++) {
            if (!a[i].equals(b[i])) return false;
        }
        return true;
    }
    
    // ---- things we know only with resolution
    
    /* (non-Javadoc)
	 * @see org.aspectj.weaver.Member#getModifiers(org.aspectj.weaver.World)
	 */
    public int getModifiers(World world) {
    	ResolvedMember resolved = resolve(world);
    	if (resolved == null) {
       		reportDidntFindMember(world);
    		return 0;
		}
		return resolved.getModifiers();
    }
    
    public UnresolvedType[] getExceptions(World world) {
    	ResolvedMember resolved = resolve(world);
    	if (resolved == null) {
       		reportDidntFindMember(world);
    		return UnresolvedType.NONE;
		}
		return resolved.getExceptions();
    }
        
    public final boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }    
    
    public final boolean isInterface() {
        return Modifier.isInterface(modifiers);
    }    
    
    public final boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }    
    
    public boolean canBeParameterized() {
    	return false;
    }

	public final int getCallsiteModifiers() {
		return modifiers & ~ Modifier.INTERFACE;
	}
	
	public int getModifiers() {
		return modifiers;
	}

    public final String getExtractableName() {
    	if (kind==CONSTRUCTOR/*name.equals("<init>")*/) return "init$";
    	else if (kind==STATIC_INITIALIZATION/*name.equals("<clinit>")*/) return "clinit$";
    	else return name;
    }
    
//	public AnnotationX[] getAnnotations() {
//		throw new UnsupportedOperationException("You should resolve this member '"+this+"' and call getAnnotations() on the result...");
//	}

	// ---- fields 'n' stuff


	public Collection/*ResolvedType*/ getDeclaringTypes(World world) {
		ResolvedType myType = getDeclaringType().resolve(world);
		Collection ret = new HashSet();
		if (kind == CONSTRUCTOR) {
			// this is wrong if the member doesn't exist, but that doesn't matter
			ret.add(myType);
		} else if (isStatic() || kind == FIELD) {
			walkUpStatic(ret, myType);
		} else {
			walkUp(ret, myType);
		}
		
		return ret;
	}
	
	private boolean walkUp(Collection acc, ResolvedType curr) {
		if (acc.contains(curr)) return true;
		
		boolean b = false;
		for (Iterator i = curr.getDirectSupertypes(); i.hasNext(); ) {
			b |= walkUp(acc, (ResolvedType)i.next());
		}
		
		if (!b && curr.isParameterizedType()) {
			b = walkUp(acc,curr.getGenericType());
		}
		
		if (!b) {
			b = curr.lookupMemberNoSupers(this) != null;
		} 
		if (b) acc.add(curr);
		return b;
	}
	
	private boolean walkUpStatic(Collection acc, ResolvedType curr) {
		if (curr.lookupMemberNoSupers(this) != null) {
			acc.add(curr);
			return true;
		} else {
			boolean b = false;
			for (Iterator i = curr.getDirectSupertypes(); i.hasNext(); ) {
				b |= walkUpStatic(acc, (ResolvedType)i.next());
			}
			if (!b && curr.isParameterizedType()) {
				b = walkUpStatic(acc,curr.getGenericType());
			}
			if (b) acc.add(curr);
			return b;
		}
	}

	// ---- reflective thisJoinPoint stuff
    /* (non-Javadoc)
	 * @see org.aspectj.weaver.Member#getSignatureMakerName()
	 */
    public String getSignatureMakerName() {
//    	if (getName().equals("<clinit>")) return "makeInitializerSig";
    	
    	MemberKind kind = getKind();
    	if (kind == METHOD) {
    		return "makeMethodSig";
    	} else if (kind == CONSTRUCTOR) {
    		return "makeConstructorSig";
    	} else if (kind == FIELD) {
    		return "makeFieldSig";
    	} else if (kind == HANDLER) {
    		return "makeCatchClauseSig";
    	} else if (kind == STATIC_INITIALIZATION) {
    		return "makeInitializerSig";
    	} else if (kind == ADVICE) {
    		return "makeAdviceSig";
    	} else if (kind == MONITORENTER) {
    		return "makeLockSig";
    	} else if (kind == MONITOREXIT) {
    		return "makeUnlockSig";
    	} else {
    		throw new RuntimeException("unimplemented");
    	}
    }
    	
    


	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Member#getSignatureType()
	 */
	public String getSignatureType() {
    	MemberKind kind = getKind();
//    	if (getName().equals("<clinit>")) return "org.aspectj.lang.reflect.InitializerSignature";
    	
    	if (kind == METHOD) {
    		return "org.aspectj.lang.reflect.MethodSignature";
    	} else if (kind == CONSTRUCTOR) {
    		return "org.aspectj.lang.reflect.ConstructorSignature";
    	} else if (kind == FIELD) {
    		return "org.aspectj.lang.reflect.FieldSignature";
    	} else if (kind == HANDLER) {
    		return "org.aspectj.lang.reflect.CatchClauseSignature";
    	} else if (kind == STATIC_INITIALIZATION) {
    		return "org.aspectj.lang.reflect.InitializerSignature";
    	} else if (kind == ADVICE) {
    		return "org.aspectj.lang.reflect.AdviceSignature";
    	} else if (kind == MONITORENTER) {
    		return "org.aspectj.lang.reflect.LockSignature";
    	} else if (kind == MONITOREXIT) {
    		return "org.aspectj.lang.reflect.UnlockSignature";
    	} else {
    		throw new RuntimeException("unimplemented");
    	}
    }

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Member#getSignatureString(org.aspectj.weaver.World)
	 */
	public String getSignatureString(World world) {
		if (getName().equals("<clinit>")) return getStaticInitializationSignatureString(world);
//		
    	MemberKind kind = getKind();
    	if (kind == METHOD) {
    		return getMethodSignatureString(world);
    	} else if (kind == CONSTRUCTOR) {
    		return getConstructorSignatureString(world);
    	} else if (kind == FIELD) {
    		return getFieldSignatureString(world);
    	} else if (kind == HANDLER) {
    		return getHandlerSignatureString(world);
    	} else if (kind == STATIC_INITIALIZATION) {
    		return getStaticInitializationSignatureString(world);
    	} else if (kind == ADVICE) {
    		return getAdviceSignatureString(world);
    	} else if (kind == MONITORENTER || kind == MONITOREXIT) {
    		return getMonitorSignatureString(world);
    	} else {
    		throw new RuntimeException("unimplemented");
    	}
    }

	private String getHandlerSignatureString(World world) {
        StringBuffer buf = new StringBuffer();
        buf.append(makeString(0));
        buf.append('-');
        //buf.append(getName());
        buf.append('-');
        buf.append(makeString(getDeclaringType()));
        buf.append('-');
        buf.append(makeString(getParameterTypes()[0]));
        buf.append('-');
        String pName = "<missing>";
        String[] names = getParameterNames(world);
        if (names != null) pName = names[0];
        buf.append(pName);
        buf.append('-');
        return buf.toString();
	}
	
	private String getStaticInitializationSignatureString(World world) {
        StringBuffer buf = new StringBuffer();
        buf.append(makeString(getModifiers(world)));
        buf.append('-');
        //buf.append(getName());
        buf.append('-');
        buf.append(makeString(getDeclaringType()));
        buf.append('-');
        return buf.toString();
	}



	protected String getAdviceSignatureString(World world) {
        StringBuffer buf = new StringBuffer();
        buf.append(makeString(getModifiers(world)));
        buf.append('-');
        buf.append(getName());
        buf.append('-');
        buf.append(makeString(getDeclaringType()));
        buf.append('-');
        buf.append(makeString(getParameterTypes()));
        buf.append('-');
        buf.append(makeString(getParameterNames(world)));
        buf.append('-');
        buf.append(makeString(getExceptions(world)));
        buf.append('-');
        buf.append(makeString(getReturnType()));
        buf.append('-');
        return buf.toString();
	}


	protected String getMethodSignatureString(World world) {
        StringBuffer buf = new StringBuffer();
        buf.append(makeString(getModifiers(world)));
        buf.append('-');
        buf.append(getName());
        buf.append('-');
        buf.append(makeString(getDeclaringType()));
        buf.append('-');
        buf.append(makeString(getParameterTypes()));
        buf.append('-');
        buf.append(makeString(getParameterNames(world)));
        buf.append('-');
        buf.append(makeString(getExceptions(world)));
        buf.append('-');
        buf.append(makeString(getReturnType()));
        buf.append('-');
        return buf.toString();
	}
	
	protected String getMonitorSignatureString(World world) {
        StringBuffer buf = new StringBuffer();
        buf.append(makeString(Modifier.STATIC));    // modifiers
        buf.append('-');
        buf.append(getName());                      // name
        buf.append('-');
        buf.append(makeString(getDeclaringType())); // Declaring Type
        buf.append('-');
        buf.append(makeString(getParameterTypes()[0])); // Parameter Types
        buf.append('-');
        buf.append("");                                 // Parameter names
        buf.append('-');
        return buf.toString();
	}
	


	protected String getConstructorSignatureString(World world) {
        StringBuffer buf = new StringBuffer();
        buf.append(makeString(getModifiers(world)));
        buf.append('-');
        buf.append('-');
        buf.append(makeString(getDeclaringType()));
        buf.append('-');
        buf.append(makeString(getParameterTypes()));
        buf.append('-');
        buf.append(makeString(getParameterNames(world)));
        buf.append('-');
        buf.append(makeString(getExceptions(world)));
        buf.append('-');
        return buf.toString();
    }
	
	


	protected String getFieldSignatureString(World world) {
        StringBuffer buf = new StringBuffer();
        buf.append(makeString(getModifiers(world)));
        buf.append('-');
        buf.append(getName());
        buf.append('-');
        buf.append(makeString(getDeclaringType()));
        buf.append('-');
        buf.append(makeString(getReturnType()));
        buf.append('-');
        return buf.toString();
    }

	protected String makeString(int i) {
		return Integer.toString(i, 16);
	}




	protected String makeString(UnresolvedType t) {
    	// this is the inverse of the odd behavior for Class.forName w/ arrays
    	if (t.isArray()) {
    		// this behavior matches the string used by the eclipse compiler for Foo.class literals
    		return t.getSignature().replace('/', '.');
    	} else {
    		return t.getName();
    	}
    }
    


	protected String makeString(UnresolvedType[] types) {
    	if (types == null) return "";
        StringBuffer buf = new StringBuffer();
        for (int i = 0, len=types.length; i < len; i++) {
            buf.append(makeString(types[i]));
            buf.append(':');
        }
        return buf.toString();
    }
    


	protected String makeString(String[] names) {
    	if (names == null) return "";
        StringBuffer buf = new StringBuffer();
        for (int i = 0, len=names.length; i < len; i++) {
            buf.append(names[i]);
            buf.append(':');
        }
        return buf.toString();
    }

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Member#getParameterNames(org.aspectj.weaver.World)
	 */
	public String[] getParameterNames(World world) {
    	ResolvedMember resolved = resolve(world);
    	if (resolved == null) {
       		reportDidntFindMember(world);
    		return null;
		}
		return resolved.getParameterNames();
    }
	
    /**
     * All the signatures that a join point with this member as its signature has.
     */
	public Iterator getJoinPointSignatures(World inAWorld) {
		if (joinPointSignatures == null) {
			joinPointSignatures = new JoinPointSignatureIterator(this,inAWorld);
		}
		joinPointSignatures.reset();
		return joinPointSignatures;
	}
    
	/**
	 * Raises an [Xlint:cantFindType] message if the declaring type
	 * cannot be found or an [Xlint:unresolvableMember] message if the
	 * type can be found (bug 149908)
	 */
	private void reportDidntFindMember(World world) {
       	if (reportedCantFindDeclaringType || reportedUnresolvableMember) return;
		ResolvedType rType = getDeclaringType().resolve(world);
		if (rType.isMissing()) {
			world.getLint().cantFindType.signal(WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE,rType.getName()),null);
			reportedCantFindDeclaringType = true;
		} else {
	       	world.getLint().unresolvableMember.signal(getName(),null);
	       	reportedUnresolvableMember = true;						
		}
    }

}
   
