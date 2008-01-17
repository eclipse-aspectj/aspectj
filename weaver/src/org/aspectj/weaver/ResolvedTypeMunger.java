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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.util.TypeSafeEnum;

/** This is an abstraction over method/field introduction.  It might not have the chops
 * to handle other inter-type declarations.  This is the thing that is used on the 
 * eclipse side and serialized into a ConcreteTypeMunger.
 */
public abstract class ResolvedTypeMunger {
	protected Kind kind;
	protected ResolvedMember signature;
	
	/**
	 * The declared signature is filled in when a type munger is parameterized for application to
	 * a particular type.  It represents the signature originally declared in the source file.
	 */
	protected ResolvedMember declaredSignature;
	
	
	
	// This list records the occurences (in order) of any names specified in the <> 
	// for a target type for the ITD.  So for example, for List<C,B,A> this list
	// will be C,B,A - the list is used later to map other occurrences of C,B,A
	// across the intertype declaration to the right type variables in the generic
	// type upon which the itd is being made.
	// might need serializing the class file for binary weaving.
	protected List /*String*/ typeVariableAliases;
	
	private Set /* resolvedMembers */ superMethodsCalled = Collections.EMPTY_SET;
	
	private ISourceLocation location; // Lost during serialize/deserialize !

	public ResolvedTypeMunger(Kind kind, ResolvedMember signature) {
		this.kind = kind;
		this.signature = signature;
		UnresolvedType declaringType = signature != null ? signature.getDeclaringType() : null;
		if (declaringType != null) {
			if (declaringType.isRawType()) throw new IllegalStateException("Use generic type, not raw type");
			if (declaringType.isParameterizedType()) throw new IllegalStateException("Use generic type, not parameterized type");
		}
//		boolean aChangeOccurred = false;
//		
//		UnresolvedType rt = signature.getReturnType();
//		if (rt.isParameterizedType() || rt.isGenericType()) {rt = rt.getRawType();aChangeOccurred=true;}
//		UnresolvedType[] pt = signature.getParameterTypes();
//		for (int i = 0; i < pt.length; i++) {
//			if (pt[i].isParameterizedType() || pt[i].isGenericType()) { pt[i] = pt[i].getRawType();aChangeOccurred=true;}
//		}
//		if (aChangeOccurred) {
//			this.signature = new ResolvedMemberImpl(signature.getKind(),signature.getDeclaringType(),signature.getModifiers(),rt,signature.getName(),pt,signature.getExceptions());
//		}
	}
	
	public void setSourceLocation(ISourceLocation isl) {
		location = isl;
	}
	
	public ISourceLocation getSourceLocation() {
		return location;
	}

	// ----

    // fromType is guaranteed to be a non-abstract aspect
    public ConcreteTypeMunger concretize(World world, ResolvedType aspectType) {
    	
		ConcreteTypeMunger munger = world.concreteTypeMunger(this, aspectType);
        return munger;
    }
    
    
    public boolean matches(ResolvedType matchType, ResolvedType aspectType) {
    	ResolvedType onType = matchType.getWorld().resolve(signature.getDeclaringType());
    	if (onType.isRawType()) onType = onType.getGenericType();
    	//System.err.println("matching: " + this + " to " + matchType + " onType = " + onType);
   		if (matchType.equals(onType)) { 
   			if (!onType.isExposedToWeaver()) {
   				// if the onType is an interface, and it already has the member we are about
   				// to munge, then this is ok...
   				boolean ok = (onType.isInterface() && (onType.lookupMemberWithSupersAndITDs(getSignature()) != null));
   				
   				if (!ok && onType.getWeaverState() == null) {
	   				if (matchType.getWorld().getLint().typeNotExposedToWeaver.isEnabled()) {
	   					matchType.getWorld().getLint().typeNotExposedToWeaver.signal(
	   						matchType.getName(), signature.getSourceLocation());
	   				}
   				}
   			}
   			return true;
   		}
   		//System.err.println("NO MATCH DIRECT");
   		
    	if (onType.isInterface()) {
    		return matchType.isTopmostImplementor(onType);
    	} else {
    		return false;
    	}
    }

	// ----

	public String toString() {
		return "ResolvedTypeMunger(" + getKind() + ", " + getSignature() +")";
		//.superMethodsCalled + ")";
	}

	// ----

	public static ResolvedTypeMunger read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		Kind kind = Kind.read(s);
		if (kind == Field) {
			return NewFieldTypeMunger.readField(s, context);
		} else if (kind == Method) {
			return NewMethodTypeMunger.readMethod(s, context);
		} else if (kind == Constructor) {
			return NewConstructorTypeMunger.readConstructor(s, context);
        } else if (kind == MethodDelegate) {
            return MethodDelegateTypeMunger.readMethod(s, context);
        } else if (kind == FieldHost) {
            return MethodDelegateTypeMunger.FieldHostTypeMunger.readFieldHost(s, context);
        } else {
			throw new RuntimeException("unimplemented");
		}
	}


	
	protected static Set readSuperMethodsCalled(VersionedDataInputStream s) throws IOException {
		
		Set ret = new HashSet();
		int n = s.readInt();
		if (n<0) throw new BCException("Problem deserializing type munger");
		for (int i=0; i < n; i++) {
			ret.add(ResolvedMemberImpl.readResolvedMember(s, null));
		}
		return ret;
	}
	
	protected void writeSuperMethodsCalled(DataOutputStream s) throws IOException {
		
		if (superMethodsCalled == null || superMethodsCalled.size()==0) {
			s.writeInt(0);
			return;
		}
		
		List ret = new ArrayList(superMethodsCalled);
		Collections.sort(ret);
		int n = ret.size();
		s.writeInt(n);
		for (Iterator i = ret.iterator(); i.hasNext(); ) {
			ResolvedMember m = (ResolvedMember)i.next();
			m.write(s);
		}
		
	}

	protected static ISourceLocation readSourceLocation(VersionedDataInputStream s) throws IOException {
		// Location persistence for type mungers was added after 1.2.1 was shipped...
		if (s.getMajorVersion()<AjAttribute.WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ150) return null;
		SourceLocation ret = null;
		ObjectInputStream ois = null;
		try {
			// This logic copes with the location missing from the attribute - an EOFException will 
			// occur on the next line and we ignore it.
		    ois = new ObjectInputStream(s);
			Boolean validLocation = (Boolean)ois.readObject();
			if (validLocation.booleanValue()) {
				File f 	   = (File) ois.readObject();
				Integer ii = (Integer)ois.readObject();
				Integer offset = (Integer)ois.readObject();
				ret = new SourceLocation(f,ii.intValue());
				ret.setOffset(offset.intValue());
			}
		} catch (EOFException eof) {
			return null; // This exception occurs if processing an 'old style' file where the
			             // type munger attributes don't include the source location.
		} catch (IOException ioe) {
			// Something went wrong, maybe this is an 'old style' file that doesnt attach locations to mungers?
			// (but I thought that was just an EOFException?)
			ioe.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
		} finally {
			if (ois!=null) ois.close();
		}
		return ret;
	}
	
	protected void writeSourceLocation(DataOutputStream s) throws IOException {	
		ObjectOutputStream oos = new ObjectOutputStream(s);
		// oos.writeObject(location);
		oos.writeObject(new Boolean(location!=null));
		if (location !=null) {
		  oos.writeObject(location.getSourceFile());
		  oos.writeObject(new Integer(location.getLine()));
		  oos.writeObject(new Integer(location.getOffset()));
		}
		oos.flush();
		oos.close();
	}

	
	public abstract void write(DataOutputStream s) throws IOException;

	public Kind getKind() {
		return kind;
	}

	
	
	public static class Kind extends TypeSafeEnum {
		/* private */ Kind(String name, int key) {
			super(name, key);
		}
		
	    public static Kind read(DataInputStream s) throws IOException {
	        int key = s.readByte();
	        switch(key) {
	            case 1: return Field;
	            case 2: return Method;
	            case 5: return Constructor;
                case 9: return MethodDelegate;
                case 10: return FieldHost;
            }
	        throw new BCException("bad kind: " + key);
	    }

        public String toString() {
            // we want MethodDelegate to appear as Method in WeaveInfo messages
            //TODO we may want something for fieldhost ?
            if (MethodDelegate.getName().equals(getName())) {
                return Method.toString();
            } else {
                return super.toString();
	}
        }
    }
	
	// ---- fields
	
	public static final Kind Field = new Kind("Field", 1);
	public static final Kind Method = new Kind("Method", 2);
	public static final Kind Constructor = new Kind("Constructor", 5);
	
	// not serialized, only created during concretization of aspects
	public static final Kind PerObjectInterface = new Kind("PerObjectInterface", 3);
	public static final Kind PrivilegedAccess = new Kind("PrivilegedAccess", 4);
	
	public static final Kind Parent = new Kind("Parent", 6);
	public static final Kind PerTypeWithinInterface = new Kind("PerTypeWithinInterface",7); // PTWIMPL not serialized, used during concretization of aspects
	
	public static final Kind AnnotationOnType = new Kind("AnnotationOnType",8); // not serialized

    public static final Kind MethodDelegate = new Kind("MethodDelegate", 9);// serialized, @AJ ITDs
    public static final Kind FieldHost = new Kind("FieldHost", 10);// serialized, @AJ ITDs

    public static final String SUPER_DISPATCH_NAME = "superDispatch";


	public void setSuperMethodsCalled(Set c) {
		this.superMethodsCalled = c;
	}

	public Set getSuperMethodsCalled() {
		return superMethodsCalled;
	}
	

	public ResolvedMember getSignature() {
		return signature;
	}
	
	// ---- 

	public ResolvedMember getMatchingSyntheticMember(Member member, ResolvedType aspectType) {
		if ((getSignature() != null) && getSignature().isPublic() && member.equals(getSignature())) { 
			return getSignature();
		}
			
		return null;
	}

	public boolean changesPublicSignature() {
		return kind == Field || kind == Method || kind == Constructor;
	}
	
	public boolean needsAccessToTopmostImplementor() {
		if (kind == Field) {
			return true;
		} else if (kind == Method) {
			return !signature.isAbstract();
		} else {
			return false;
		}
	}
	
	protected static List readInTypeAliases(VersionedDataInputStream s) throws IOException {
		if (s.getMajorVersion()>=AjAttribute.WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ150) {
			int count = s.readInt();
			if (count!=0) {
				List aliases = new ArrayList();
				for (int i=0;i<count;i++) {
					aliases.add(s.readUTF());
				}
				return aliases;
			}
		}
		return null;
	}
	
	protected void writeOutTypeAliases(DataOutputStream s) throws IOException {
		// Write any type variable aliases
		if (typeVariableAliases==null || typeVariableAliases.size()==0) {
			s.writeInt(0);
		} else {
			s.writeInt(typeVariableAliases.size());
			for (Iterator iter = typeVariableAliases.iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				s.writeUTF(element);
			}
		}
	}
	
	public List getTypeVariableAliases() {
		return typeVariableAliases;
	}
	
	public boolean hasTypeVariableAliases() {
		return (typeVariableAliases!=null && typeVariableAliases.size()>0);
	}
	
	/**
	 * return true if type variables are specified with the target type for
	 * this ITD.  e.g. this would return true: "int I<A,B>.m() { return 42; }"
	 */
	public boolean sharesTypeVariablesWithGenericType() {
		return (typeVariableAliases!=null && typeVariableAliases.size()>0);
	}
	
	/**
     * Parameterizes a resolved type munger for a particular usage of
     * its target type (this is used when the target type is generic
     * and the ITD shares type variables with the target)
     * see ConcreteTypeMunger.parameterizedFor
     */
	public ResolvedTypeMunger parameterizedFor(ResolvedType target) {
		throw new BCException("Dont call parameterizedFor on a type munger of this kind: "+this.getClass());
	}
//		ResolvedType genericType = target;
//		if (target.isRawType() || target.isParameterizedType()) genericType = genericType.getGenericType();
//		ResolvedMember parameterizedSignature = null;
//		// If we are parameterizing it for a generic type, we just need to 'swap the letters' from the ones used 
//		// in the original ITD declaration to the ones used in the actual target type declaration.
//		if (target.isGenericType()) {
//			TypeVariable vars[] = target.getTypeVariables();
//			UnresolvedTypeVariableReferenceType[] varRefs = new UnresolvedTypeVariableReferenceType[vars.length];
//			for (int i = 0; i < vars.length; i++) {
//				varRefs[i] = new UnresolvedTypeVariableReferenceType(vars[i]);
//			}
//			parameterizedSignature = getSignature().parameterizedWith(varRefs,genericType,true,typeVariableAliases);
//		} else {
//		  // For raw and 'normal' parameterized targets  (e.g. Interface, Interface<String>)
//		  parameterizedSignature = getSignature().parameterizedWith(target.getTypeParameters(),genericType,target.isParameterizedType(),typeVariableAliases);
//		}
//		return new NewMethodTypeMunger(parameterizedSignature,getSuperMethodsCalled(),typeVariableAliases);
//	}
//	/**
//     * see ResolvedTypeMunger.parameterizedFor(ResolvedType)
//     */
//	public ResolvedTypeMunger parameterizedFor(ResolvedType target) {
//		ResolvedType genericType = target;
//		if (target.isRawType() || target.isParameterizedType()) genericType = genericType.getGenericType();
//		ResolvedMember parameterizedSignature = getSignature().parameterizedWith(target.getTypeParameters(),genericType,target.isParameterizedType(),typeVariableAliases);
//		return new NewFieldTypeMunger(parameterizedSignature,getSuperMethodsCalled(),typeVariableAliases);
//	}
	
	public void setDeclaredSignature(ResolvedMember rm) {
		declaredSignature = rm;
	}
	
	public ResolvedMember getDeclaredSignature() {
		return declaredSignature;
	}
	
	/**
	 * A late munger has to be done after shadow munging since which shadows are matched
	 * can affect the operation of the late munger. e.g. perobjectinterfacemunger
	 */
	public boolean isLateMunger() {
		return false;
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
		return false;
	}

	public ResolvedTypeMunger parameterizeWith(Map m, World w) {
		throw new BCException("Dont call parameterizeWith() on a type munger of this kind: "+this.getClass());
	}
	
}
