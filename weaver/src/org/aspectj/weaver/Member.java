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

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.aspectj.util.TypeSafeEnum;

public class Member implements Comparable {

    private final Kind kind;
    private final TypeX declaringType;
    protected final int modifiers; // protected because ResolvedMember uses it
    private final TypeX returnType;
    private final String name;
    private final TypeX[] parameterTypes;
    private final String signature;

    public Member(
        Kind kind, 
        TypeX declaringType,
        int modifiers,
        String name,
        String signature) 
    {
        this.kind = kind;
        this.declaringType = declaringType;
        this.modifiers = modifiers;
        this.name = name;
        this.signature = signature;
        if (kind == FIELD) {
            this.returnType = TypeX.forSignature(signature);
            this.parameterTypes = TypeX.NONE;
        } else {
            Object[] returnAndParams = signatureToTypes(signature);
            this.returnType = (TypeX) returnAndParams[0];
            this.parameterTypes = (TypeX[]) returnAndParams[1];
        }
    }

    public  Member(
        Kind kind, 
        TypeX declaringType, 
        int modifiers,
        TypeX returnType, 
        String name, 
        TypeX[] parameterTypes) 
    {
        super();
        this.kind = kind;
        this.declaringType = declaringType;
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.name = name;
        this.parameterTypes = parameterTypes;
        if (kind == FIELD) {
            this.signature = returnType.getSignature();
        } else {
            this.signature = typesToSignature(returnType, parameterTypes);
        }
    }
    
    public ResolvedMember resolve(World world) {
    	return world.resolve(this);
    }

    // ---- utility methods
    
    /** returns an Object[] pair of TypeX, TypeX[] representing return type, 
     * argument types parsed from the JVM bytecode signature of a method.  Yes,
     * this should actually return a nice statically-typed pair object, but we
     * don't have one of those.  
     *
     * <blockquote><pre>
     *   TypeX.signatureToTypes("()[Z")[0].equals(Type.forSignature("[Z"))
     *   TypeX.signatureToTypes("(JJ)I")[1]
     *      .equals(TypeX.forSignatures(new String[] {"J", "J"}))
     * </pre></blockquote>
     *
     * @param      signature the JVM bytecode method signature string we want to break apart
     * @return     a pair of TypeX, TypeX[] representing the return types and parameter types. 
     */
    public static String typesToSignature(TypeX returnType, TypeX[] paramTypes) {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        for (int i = 0, len = paramTypes.length; i < len; i++) {
            buf.append(paramTypes[i].getSignature());
        }
        buf.append(")");
        buf.append(returnType.getSignature());
        return buf.toString();        
    }
    
    /** returns an Object[] pair of TypeX, TypeX[] representing return type, 
     * argument types parsed from the JVM bytecode signature of a method.  Yes,
     * this should actually return a nice statically-typed pair object, but we
     * don't have one of those.  
     *
     * <blockquote><pre>
     *   TypeX.signatureToTypes("()[Z")[0].equals(Type.forSignature("[Z"))
     *   TypeX.signatureToTypes("(JJ)I")[1]
     *      .equals(TypeX.forSignatures(new String[] {"J", "J"}))
     * </pre></blockquote>
     *
     * @param      signature the JVM bytecode method signature string we want to break apart
     * @return     a pair of TypeX, TypeX[] representing the return types and parameter types. 
     */
    private static Object[] signatureToTypes(String sig) {
        List l = new ArrayList();
        int i = 1;
        while (true) {
            char c = sig.charAt(i);
            if (c == ')') break;
            int start = i;
            while (c == '[') c = sig.charAt(++i);
            if (c == 'L') {
                i = sig.indexOf(';', start) + 1;
                l.add(TypeX.forSignature(sig.substring(start, i)));
            } else {
                l.add(TypeX.forSignature(sig.substring(start, ++i)));
            }
        }
        TypeX[] paramTypes = (TypeX[]) l.toArray(new TypeX[l.size()]);
        TypeX returnType = TypeX.forSignature(sig.substring(i+1, sig.length()));
        return new Object[] { returnType, paramTypes };
    }            

    // ---- factory methods
    public static Member field(String declaring, int mods, String name, String signature) {
        return field(declaring, mods, TypeX.forSignature(signature), name);
    }
    public static Member field(TypeX declaring, int mods, String name, TypeX type) {
        return new Member(FIELD, declaring, mods, type, name, TypeX.NONE);
    }    
    public static Member method(TypeX declaring, int mods, String name, String signature) {
        Object[] pair = signatureToTypes(signature);
        return method(declaring, mods, (TypeX) pair[0], name, (TypeX[]) pair[1]);
    }
    public static Member pointcut(TypeX declaring, String name, String signature) {
        Object[] pair = signatureToTypes(signature);
        return pointcut(declaring, 0, (TypeX) pair[0], name, (TypeX[]) pair[1]);
    }


    private static Member field(String declaring, int mods, TypeX ty, String name) {
        return new Member(
            FIELD,
            TypeX.forName(declaring),
            mods,
            ty,
            name,
            TypeX.NONE);
    }
    
    public static Member method(TypeX declTy, int mods, TypeX rTy, String name, TypeX[] paramTys) {
        return new Member(
        	//??? this calls <clinit> a method
            name.equals("<init>") ? CONSTRUCTOR : METHOD,
            declTy,
            mods,
            rTy,
            name,
            paramTys);
    }
    private static Member pointcut(TypeX declTy, int mods, TypeX rTy, String name, TypeX[] paramTys) {
        return new Member(
            POINTCUT,
            declTy,
            mods,
            rTy,
            name,
            paramTys);
    }
    
	public static Member makeExceptionHandlerSignature(TypeX inType, TypeX catchType) {
		return new Member(
			HANDLER,
			inType,
			Modifier.STATIC,
			"<catch>",
			"(" + catchType.getSignature() + ")V");
	}
    
    // ---- parsing methods
    
    /** Takes a string in this form:
     * 
     * <blockquote><pre>
     * static? TypeName TypeName.Id
     * </pre></blockquote>
     * Pretty much just for testing, and as such should perhaps be moved.
     */
    
    public static Member fieldFromString(String str) {
        str = str.trim();
        final int len = str.length();
        int i = 0;
        int mods = 0;
        if (str.startsWith("static", i)) {
            mods = Modifier.STATIC;
            i += 6;
            while (Character.isWhitespace(str.charAt(i))) i++;
        }
        int start = i;
        while (! Character.isWhitespace(str.charAt(i))) i++;
        TypeX retTy = TypeX.forName(str.substring(start, i));

        start = i;
        i = str.lastIndexOf('.');
        TypeX declaringTy = TypeX.forName(str.substring(start, i).trim());
        start = ++i;
        String name = str.substring(start, len).trim();
        return new Member(
            FIELD,
            declaringTy,
            mods,
            retTy,
            name,
            TypeX.NONE);
    }

    /** Takes a string in this form:
     * 
     * <blockquote><pre>
     * (static|interface|private)? TypeName TypeName . Id ( TypeName , ...)
     * </pre></blockquote>
     * Pretty much just for testing, and as such should perhaps be moved.
     */
    
    public static Member methodFromString(String str) {
        str = str.trim();
        final int len = str.length();
        int i = 0;

        int mods = 0;
        if (str.startsWith("static", i)) {
            mods = Modifier.STATIC;
            i += 6;
        } else if (str.startsWith("interface", i)) {
            mods = Modifier.INTERFACE;
            i += 9;
        } else if (str.startsWith("private", i)) {
            mods = Modifier.PRIVATE;
            i += 7;
        }            
        while (Character.isWhitespace(str.charAt(i))) i++;
        
        int start = i;
        while (! Character.isWhitespace(str.charAt(i))) i++;
        TypeX returnTy = TypeX.forName(str.substring(start, i));

        start = i;
        i = str.indexOf('(', i);
        i = str.lastIndexOf('.', i);
        TypeX declaringTy = TypeX.forName(str.substring(start, i).trim());
        
        start = ++i;
        i = str.indexOf('(', i);
        String name = str.substring(start, i).trim();
        start = ++i;
        i = str.indexOf(')', i);
    
        String[] paramTypeNames = parseIds(str.substring(start, i).trim());

        return method(declaringTy, mods, returnTy, name, TypeX.forNames(paramTypeNames));
    }

    private static String[] parseIds(String str) {
        if (str.length() == 0) return ZERO_STRINGS;
        List l = new ArrayList();
        int start = 0;
        while (true) {
            int i = str.indexOf(',', start);
            if (i == -1) {
                l.add(str.substring(start).trim());
                break;
            }
            l.add(str.substring(start, i).trim());
            start = i+1;
        }
        return (String[]) l.toArray(new String[l.size()]);
    }

    private static final String[] ZERO_STRINGS = new String[0];

    // ---- things we know without resolution
    
    public boolean equals(Object other) {
        if (! (other instanceof Member)) return false;
        Member o = (Member) other;

        return (kind == o.kind 
            && name.equals(o.name) 
            && signature.equals(o.signature)
            && declaringType.equals(o.declaringType));
    }
    
    public int compareTo(Object other) {
    	Member o = (Member) other;
    	
    	int i = getName().compareTo(o.getName());
    	if (i != 0) return i;
    	return getSignature().compareTo(o.getSignature());
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
            result = 37*result + kind.hashCode();
            result = 37*result + name.hashCode();
            result = 37*result + signature.hashCode();
            result = 37*result + declaringType.hashCode();
            hashCode = result;
        } 
        return hashCode;
    }

    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	buf.append(returnType);
    	buf.append(' ');
   		buf.append(declaringType);
        buf.append('.');
   		buf.append(name);
    	if (kind != FIELD) {
    		buf.append("(");
            if (parameterTypes.length != 0) {
                buf.append(parameterTypes[0]);
        		for (int i=1, len = parameterTypes.length; i < len; i++) {
                    buf.append(", ");
        		    buf.append(parameterTypes[i]);
        		}
            }
    		buf.append(")");
    	}
    	return buf.toString();
    }
    
    public String toLongString() {
        StringBuffer buf = new StringBuffer();
        buf.append(kind);
        buf.append(' ');
        if (modifiers != 0) {
            buf.append(Modifier.toString(modifiers));
            buf.append(' ');
        }
        buf.append(toString());
        buf.append(" <");
        buf.append(signature);
        buf.append(" >");
        return buf.toString();
    }        

    public Kind getKind() { return kind; }
    public TypeX getDeclaringType() { return declaringType; }
    public TypeX getReturnType() { return returnType; }
    public TypeX getType() { return returnType; }
    public String getName() { return name; }
    public TypeX[]  getParameterTypes() { return parameterTypes; }
    public String getSignature() { return signature; }
    public int getArity() { return parameterTypes.length; }
    
    public boolean isCompatibleWith(Member am) {
        if (kind != METHOD || am.getKind() != METHOD) return true;
        if (! name.equals(am.getName())) return true;
        if (! equalTypes(getParameterTypes(), am.getParameterTypes())) return true;
        return getReturnType().equals(am.getReturnType());
    }
    
    private static boolean equalTypes(TypeX[] a, TypeX[] b) {
        int len = a.length;
        if (len != b.length) return false;
        for (int i = 0; i < len; i++) {
            if (!a[i].equals(b[i])) return false;
        }
        return true;
    }
    
    // ---- things we know only with resolution
    
    public int getModifiers(World world) {
        return world.getModifiers(this);
    }
    
    public TypeX[] getExceptions(World world) {
        return world.getExceptions(this);
    }
    
    public final boolean isProtected(World world) {
        return Modifier.isProtected(world.getModifiers(this));
    }
    public final boolean isStatic(World world) {
        return Modifier.isStatic(world.getModifiers(this));
    }
    public final boolean isStrict(World world) {
        return Modifier.isStrict(world.getModifiers(this));
    }
    
    public final boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }    
    
    public final boolean isInterface() {
        return Modifier.isInterface(modifiers);  // this is kinda weird
    }    
    
    public final boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }    

	public final int getCallsiteModifiers() {
		return modifiers & ~ Modifier.INTERFACE;
	}

    public final String getExtractableName() {
    	if (name.equals("<init>")) return "init$";
    	else if (name.equals("<clinit>")) return "clinit$";
    	else return name;
    }

    // ---- fields 'n' stuff

    public static final Member[] NONE = new Member[0];

    public static class Kind extends TypeSafeEnum {
        public Kind(String name, int key) { super(name, key); }
        
        public static Kind read(DataInputStream s) throws IOException {
            int key = s.readByte();
            switch(key) {
                case 1: return METHOD;
                case 2: return FIELD;
                case 3: return CONSTRUCTOR;
                case 4: return STATIC_INITIALIZATION;
                case 5: return POINTCUT;
                case 6: return ADVICE;
                case 7: return HANDLER;
            }
            throw new BCException("weird kind " + key);
        }
    }
    
    public static final Kind METHOD        = new Kind("METHOD", 1);
    public static final Kind FIELD         = new Kind("FIELD", 2);
    public static final Kind CONSTRUCTOR   = new Kind("CONSTRUCTOR", 3);
    public static final Kind STATIC_INITIALIZATION   = new Kind("STATIC_INITIALIZATION", 4);  
    public static final Kind POINTCUT      = new Kind("POINTCUT", 5);
    public static final Kind ADVICE        = new Kind("ADVICE", 6);
    public static final Kind HANDLER   = new Kind("HANDLER", 7);    
  
    

    
	public Collection/*ResolvedTypeX*/ getDeclaringTypes(World world) {
		ResolvedTypeX myType = getDeclaringType().resolve(world);
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
	
	private boolean walkUp(Collection acc, ResolvedTypeX curr) {
		if (acc.contains(curr)) return true;
		
		boolean b = false;
		for (Iterator i = curr.getDirectSupertypes(); i.hasNext(); ) {
			b |= walkUp(acc, (ResolvedTypeX)i.next());
		}
		
		if (!b) {
			b = curr.lookupMemberNoSupers(this) != null;
		} 
		if (b) acc.add(curr);
		return b;
	}
	
	private boolean walkUpStatic(Collection acc, ResolvedTypeX curr) {
		if (curr.lookupMemberNoSupers(this) != null) {
			acc.add(curr);
			return true;
		} else {
			boolean b = false;
			for (Iterator i = curr.getDirectSupertypes(); i.hasNext(); ) {
				b |= walkUp(acc, (ResolvedTypeX)i.next());
			}
			if (b) acc.add(curr);
			return b;
		}
	}

	// ---- reflective thisJoinPoint stuff
    public String getSignatureMakerName() {
    	if (getName().equals("<clinit>")) return "makeInitializerSig";
    	
    	Kind kind = getKind();
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
    	} else {
    		throw new RuntimeException("unimplemented");
    	}
    }
    	
    


	public String getSignatureType() {
    	Kind kind = getKind();
    	if (getName().equals("<clinit>")) return "org.aspectj.lang.reflect.InitializerSignature";
    	
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
    	} else {
    		throw new RuntimeException("unimplemented");
    	}
    }

	public String getSignatureString(World world) {
		if (getName().equals("<clinit>")) return getStaticInitializationSignatureString(world);
		
    	Kind kind = getKind();
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
        //XXX we don't actually try to find the handler parameter name
        //XXX it probably wouldn't be too hard
        String pName = "<missing>";
        //String[] pNames = getParameterNames(world);
        //if (pNames != null) pName = pNames[0];
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
		return Integer.toString(i, 16);  //??? expensive
	}




	protected String makeString(TypeX t) {
    	// this is the inverse of the odd behavior for Class.forName w/ arrays
    	if (t.isArray()) {
    		// this behavior matches the string used by the eclipse compiler for Foo.class literals
    		return t.getSignature().replace('/', '.');
    	} else {
    		return t.getName();
    	}
    }
    


	protected String makeString(TypeX[] types) {
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

	public String[] getParameterNames(World world) {
    	return world.getParameterNames(this);
    }
    
    

    
    
    
    // ----
    

	


    	
    
    


}
   
