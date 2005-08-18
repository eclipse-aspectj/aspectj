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


package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.AdviceSignature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.FieldSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.Constants;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.JoinPointSignature;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.NewFieldTypeMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelTypeMunger;


public class SignaturePattern extends PatternNode {
	private Member.Kind kind;
	private ModifiersPattern modifiers;
	private TypePattern returnType;
    private TypePattern declaringType;
	private NamePattern name;
    private TypePatternList parameterTypes;
    private ThrowsPattern throwsPattern;
    private AnnotationTypePattern annotationPattern;
    	
	public SignaturePattern(Member.Kind kind, ModifiersPattern modifiers,
	                         TypePattern returnType, TypePattern declaringType,
	                         NamePattern name, TypePatternList parameterTypes,
	                         ThrowsPattern throwsPattern,
							 AnnotationTypePattern annotationPattern) {
		this.kind = kind;
		this.modifiers = modifiers;
		this.returnType = returnType;
		this.name = name;
		this.declaringType = declaringType;
		this.parameterTypes = parameterTypes;
		this.throwsPattern = throwsPattern;
		this.annotationPattern = annotationPattern;
	}
	
	
    public SignaturePattern resolveBindings(IScope scope, Bindings bindings) { 
		if (returnType != null) {
			returnType = returnType.resolveBindings(scope, bindings, false, false);
		} 
		if (declaringType != null) {
			declaringType = declaringType.resolveBindings(scope, bindings, false, false);
		}
		if (parameterTypes != null) {
			parameterTypes = parameterTypes.resolveBindings(scope, bindings, false, false);
		}
		if (throwsPattern != null) {
			throwsPattern = throwsPattern.resolveBindings(scope, bindings);
		}
		if (annotationPattern != null) {
			annotationPattern = annotationPattern.resolveBindings(scope,bindings,false);
		}
		
    	return this;
    }
    
    public SignaturePattern resolveBindingsFromRTTI() {
		if (returnType != null) {
			returnType = returnType.resolveBindingsFromRTTI(false, false);
		} 
		if (declaringType != null) {
			declaringType = declaringType.resolveBindingsFromRTTI(false, false);
		}
		if (parameterTypes != null) {
			parameterTypes = parameterTypes.resolveBindingsFromRTTI(false, false);
		}
		if (throwsPattern != null) {
			throwsPattern = throwsPattern.resolveBindingsFromRTTI();
		}
		
    	return this;    	
    }
    
    
	public void postRead(ResolvedType enclosingType) {
		if (returnType != null) {
			returnType.postRead(enclosingType);
		} 
		if (declaringType != null) {
			declaringType.postRead(enclosingType);
		}
		if (parameterTypes != null) {
			parameterTypes.postRead(enclosingType);
		}
	}
	
	/**
	 * return a copy of this signature pattern in which every type variable reference
	 * is replaced by the corresponding entry in the map.
	 */
	public SignaturePattern parameterizeWith(Map typeVariableMap) {
		SignaturePattern ret = new SignaturePattern(
						kind,
						modifiers,
						returnType.parameterizeWith(typeVariableMap),
						declaringType.parameterizeWith(typeVariableMap),
						name,
						parameterTypes.parameterizeWith(typeVariableMap),
						throwsPattern.parameterizeWith(typeVariableMap),
						annotationPattern.parameterizeWith(typeVariableMap));
		ret.copyLocationFrom(this);
		return ret;
	}
	
	public boolean matches(Member joinPointSignature, World world, boolean allowBridgeMethods) {
		// fail (or succeed!) fast tests...
		if (joinPointSignature == null) return false;
		if (kind != joinPointSignature.getKind()) return false;
		if (kind == Member.ADVICE) return true;
		
		// do the hard work then...
		JoinPointSignature[] candidateMatches = joinPointSignature.getJoinPointSignatures(world);
		for (int i = 0; i < candidateMatches.length; i++) {
			if (matchesExactly(candidateMatches[i],world,allowBridgeMethods)) return true;
		}
		return false;
	}
	
	// Does this pattern match this exact signature (no declaring type mucking about
	// or chasing up the hierarchy)
	private boolean matchesExactly(JoinPointSignature aMember, World inAWorld, boolean allowBridgeMethods) {
		// Java5 introduces bridge methods, we match a call to them but nothing else...
		if (aMember.isBridgeMethod() && !allowBridgeMethods) {
			return false;
		}
			
		if (!modifiers.matches(aMember.getModifiers())) return false;
		
		boolean matchesIgnoringAnnotations = true;
		if (kind == Member.STATIC_INITIALIZATION) {
			matchesIgnoringAnnotations = matchesExactlyStaticInitialization(aMember, inAWorld);
		} else if (kind == Member.FIELD) {
			matchesIgnoringAnnotations = matchesExactlyField(aMember,inAWorld);
		} else if (kind == Member.METHOD) {
			matchesIgnoringAnnotations = matchesExactlyMethod(aMember,inAWorld);
		} else if (kind == Member.CONSTRUCTOR) {
			matchesIgnoringAnnotations = matchesExactlyConstructor(aMember, inAWorld);
		}
		if (!matchesIgnoringAnnotations) return false;
		
		return matchesAnnotations(aMember, inAWorld);
	}
	
	/**
	 * Matches on declaring type
	 */
	private boolean matchesExactlyStaticInitialization(JoinPointSignature aMember,World world) {
		return declaringType.matchesStatically(aMember.getDeclaringType().resolve(world));
	}
	
	/**
	 * Matches on name, declaring type, field type
	 */
	private boolean matchesExactlyField(JoinPointSignature aField, World world) {
		if (!name.matches(aField.getName())) return false;
		if (!declaringType.matchesStatically(aField.getDeclaringType().resolve(world))) return false;
		if (!returnType.matchesStatically(aField.getReturnType().resolve(world))) {
			// looking bad, but there might be parameterization to consider...
			if (!returnType.matchesStatically(aField.getGenericReturnType().resolve(world))) {
				// ok, it's bad.
				return false;
			}
		}
		// passed all the guards...
		return true;
	}
	
	/**
	 * Matches on name, declaring type, return type, parameter types, throws types
	 */
	private boolean matchesExactlyMethod(JoinPointSignature aMethod, World world) {
		if (!name.matches(aMethod.getName())) return false;
		if (!declaringType.matchesStatically(aMethod.getDeclaringType().resolve(world))) return false;
		if (!returnType.matchesStatically(aMethod.getReturnType().resolve(world))) {
			// looking bad, but there might be parameterization to consider...
			if (!returnType.matchesStatically(aMethod.getGenericReturnType().resolve(world))) {
				// ok, it's bad.
				return false;
			}
		}
		ResolvedType[] resolvedParameters = world.resolve(aMethod.getParameterTypes());
		if (!parameterTypes.matches(resolvedParameters, TypePattern.STATIC).alwaysTrue()) {
			// It could still be a match based on the generic sig parameter types of a parameterized type
			if (!parameterTypes.matches(world.resolve(aMethod.getGenericParameterTypes()),TypePattern.STATIC).alwaysTrue()) {
				return false;
				// It could STILL be a match based on the erasure of the parameter types??
				// to be determined via test cases...
			}
		}
		
		// check that varargs specifications match
		if (!matchesVarArgs(aMethod,world)) return false;
		
		// Check the throws pattern
		if (!throwsPattern.matches(aMethod.getExceptions(), world)) return false;
		
		// passed all the guards..
		return true;
	}
	
	/**
	 * match on declaring type, parameter types, throws types
	 */
	private boolean matchesExactlyConstructor(JoinPointSignature aConstructor, World world) {
		if (!declaringType.matchesStatically(aConstructor.getDeclaringType().resolve(world))) return false;

		ResolvedType[] resolvedParameters = world.resolve(aConstructor.getParameterTypes());
		if (!parameterTypes.matches(resolvedParameters, TypePattern.STATIC).alwaysTrue()) {
			// It could still be a match based on the generic sig parameter types of a parameterized type
			if (!parameterTypes.matches(world.resolve(aConstructor.getGenericParameterTypes()),TypePattern.STATIC).alwaysTrue()) {
				return false;
				// It could STILL be a match based on the erasure of the parameter types??
				// to be determined via test cases...
			}
		}
		
		// check that varargs specifications match
		if (!matchesVarArgs(aConstructor,world)) return false;
		
		// Check the throws pattern
		if (!throwsPattern.matches(aConstructor.getExceptions(), world)) return false;
		
		// passed all the guards..
		return true;		
	}
	
	/**
	 * We've matched against this method or constructor so far, but without considering
	 * varargs (which has been matched as a simple array thus far). Now we do the additional
	 * checks to see if the parties agree on whether the last parameter is varargs or a 
	 * straight array. 
	 */
	private boolean matchesVarArgs(JoinPointSignature aMethodOrConstructor, World inAWorld) {
		if (parameterTypes.size() == 0) return true;
		
		TypePattern lastPattern = parameterTypes.get(parameterTypes.size()-1);
		boolean canMatchVarArgsSignature = lastPattern.isStar() || 
		                                    lastPattern.isVarArgs() ||
		                                    (lastPattern == TypePattern.ELLIPSIS);
		
		if (aMethodOrConstructor.isVarargsMethod()) {
			// we have at least one parameter in the pattern list, and the method has a varargs signature
			if (!canMatchVarArgsSignature) {
				// XXX - Ideally the shadow would be included in the msg but we don't know it...
				inAWorld.getLint().cantMatchArrayTypeOnVarargs.signal(aMethodOrConstructor.toString(),getSourceLocation());
				return false;
			}
		} else {
			// the method ends with an array type, check that we don't *require* a varargs
			if (lastPattern.isVarArgs()) return false;
		}

		return true;
	}
		
	private boolean matchesAnnotations(ResolvedMember member,World world) {
	  if (member == null) {
	        if (member.getName().startsWith(NameMangler.PREFIX)) {
				return false;
			}
			world.getLint().unresolvableMember.signal(member.toString(), getSourceLocation());
			return false;
	  }
	  annotationPattern.resolve(world);
	 
	  // optimization before we go digging around for annotations on ITDs
	  if (annotationPattern instanceof AnyAnnotationTypePattern) return true;
	  
	  // fake members represent ITD'd fields - for their annotations we should go and look up the
	  // relevant member in the original aspect
	  if (member.isAnnotatedElsewhere() && member.getKind()==Member.FIELD) {
	    // FIXME asc duplicate of code in AnnotationPattern.matchInternal()?  same fixmes apply here.
	    ResolvedMember [] mems = member.getDeclaringType().resolve(world).getDeclaredFields(); // FIXME asc should include supers with getInterTypeMungersIncludingSupers?
	    List mungers = member.getDeclaringType().resolve(world).getInterTypeMungers(); 
		for (Iterator iter = mungers.iterator(); iter.hasNext();) {
	        BcelTypeMunger typeMunger = (BcelTypeMunger) iter.next();
			if (typeMunger.getMunger() instanceof NewFieldTypeMunger) {
			  ResolvedMember fakerm = typeMunger.getSignature();
			  ResolvedMember ajcMethod = AjcMemberMaker.interFieldInitializer(fakerm,typeMunger.getAspectType());
		  	  ResolvedMember rmm       = findMethod(typeMunger.getAspectType(),ajcMethod);
			  if (fakerm.equals(member)) {
				member = rmm;
			  }
			}
		}
	  }
	  
	  return annotationPattern.matches(member).alwaysTrue();
	}
	
	private ResolvedMember findMethod(ResolvedType aspectType, ResolvedMember ajcMethod) {
	       ResolvedMember decMethods[] = aspectType.getDeclaredMethods();
	       for (int i = 0; i < decMethods.length; i++) {
			ResolvedMember member = decMethods[i];
			if (member.equals(ajcMethod)) return member;
	   }
			return null;
	}
	
	public boolean declaringTypeMatchAllowingForCovariance(Member member, UnresolvedType shadowDeclaringType, World world,TypePattern returnTypePattern,ResolvedType sigReturn) {
		
		ResolvedType onType = shadowDeclaringType.resolve(world);
			
		// fastmatch
		if (declaringType.matchesStatically(onType) && returnTypePattern.matchesStatically(sigReturn)) 
			return true;
			
		Collection declaringTypes = member.getDeclaringTypes(world);
		
		boolean checkReturnType = true;
		// XXX Possible enhancement?  Doesn't seem to speed things up
		//	if (returnTypePattern.isStar()) {
		//		if (returnTypePattern instanceof WildTypePattern) {
		//			if (((WildTypePattern)returnTypePattern).getDimensions()==0) checkReturnType = false;
		//		}
		//	}
			
		// Sometimes that list includes types that don't explicitly declare the member we are after -
		// they are on the list because their supertype is on the list, that's why we use
		// lookupMethod rather than lookupMemberNoSupers()
		for (Iterator i = declaringTypes.iterator(); i.hasNext(); ) {
			ResolvedType type = (ResolvedType)i.next();
			if (declaringType.matchesStatically(type)) {
			  if (!checkReturnType) return true;
			  ResolvedMember rm = type.lookupMethod(member);
			  if (rm==null)  rm = type.lookupMethodInITDs(member); // It must be in here, or we have *real* problems
			  if (rm==null) continue; // might be currently looking at the generic type and we need to continue searching in case we hit a parameterized version of this same type...
			  UnresolvedType returnTypeX = rm.getReturnType();
			  ResolvedType returnType = returnTypeX.resolve(world);
			  if (returnTypePattern.matchesStatically(returnType)) return true;
			}
		}
		return false;
	}
	
	// for dynamic join point matching
	public boolean matches(JoinPoint.StaticPart jpsp) {
		Signature sig = jpsp.getSignature();
	    if (kind == Member.ADVICE && !(sig instanceof AdviceSignature)) return false;
	    if (kind == Member.CONSTRUCTOR && !(sig instanceof ConstructorSignature)) return false;
	    if (kind == Member.FIELD && !(sig instanceof FieldSignature)) return false;
	    if (kind == Member.METHOD && !(sig instanceof MethodSignature)) return false;
	    if (kind == Member.STATIC_INITIALIZATION && !(jpsp.getKind().equals(JoinPoint.STATICINITIALIZATION))) return false;
	    if (kind == Member.POINTCUT) return false;
			
	    if (kind == Member.ADVICE) return true;

	    if (!modifiers.matches(sig.getModifiers())) return false;
		
		if (kind == Member.STATIC_INITIALIZATION) {
			//System.err.println("match static init: " + sig.getDeclaringType() + " with " + this);
			return declaringType.matchesStatically(sig.getDeclaringType());
		} else if (kind == Member.FIELD) {
			Class returnTypeClass = ((FieldSignature)sig).getFieldType();
			if (!returnType.matchesStatically(returnTypeClass)) return false;
			if (!name.matches(sig.getName())) return false;
			boolean ret = declaringTypeMatch(sig);
			//System.out.println("   ret: " + ret);
			return ret;
		} else if (kind == Member.METHOD) {
			MethodSignature msig = ((MethodSignature)sig);
			Class returnTypeClass = msig.getReturnType();
			Class[] params = msig.getParameterTypes();
			Class[] exceptionTypes = msig.getExceptionTypes();
			if (!returnType.matchesStatically(returnTypeClass)) return false;
			if (!name.matches(sig.getName())) return false;
			if (!parameterTypes.matches(params, TypePattern.STATIC).alwaysTrue()) {
				return false;
			}
			if (matchedArrayAgainstVarArgs(parameterTypes,msig.getModifiers())) { return false; }
			
			if (!throwsPattern.matches(exceptionTypes)) return false;
			return declaringTypeMatch(sig); // XXXAJ5 - Need to make this a covariant aware version for dynamic JP matching to work
		} else if (kind == Member.CONSTRUCTOR) {
			ConstructorSignature csig = (ConstructorSignature)sig;
			Class[] params = csig.getParameterTypes();
			Class[] exceptionTypes = csig.getExceptionTypes();
			if (!parameterTypes.matches(params, TypePattern.STATIC).alwaysTrue()) {
				return false;
			}
			if (matchedArrayAgainstVarArgs(parameterTypes,csig.getModifiers())) { return false; }
			
			if (!throwsPattern.matches(exceptionTypes)) return false;
			return declaringType.matchesStatically(sig.getDeclaringType());
			//return declaringTypeMatch(member.getDeclaringType(), member, world);			
		}
			    
		return false;
	}
	
	public boolean couldMatch(Class declaringClass) {
		return declaringTypeMatch(declaringClass);
	}
	
	public boolean matches(Class declaringClass, java.lang.reflect.Member member) {
	    if (kind == Member.ADVICE) return true;
	    if (kind == Member.POINTCUT) return false;
		if ((member != null) && !(modifiers.matches(member.getModifiers()))) return false;
		if (kind == Member.STATIC_INITIALIZATION) {
			return declaringType.matchesStatically(declaringClass);
		}
		if (kind == Member.FIELD) {
			if (!(member instanceof Field)) return false;
			
			Class fieldTypeClass = ((Field)member).getType();
			if (!returnType.matchesStatically(fieldTypeClass)) return false;
			if (!name.matches(member.getName())) return false;
			return declaringTypeMatch(member.getDeclaringClass());
		}
		if (kind == Member.METHOD) {
			if (! (member instanceof Method)) return false;
			
			Class returnTypeClass = ((Method)member).getReturnType();
			Class[] params = ((Method)member).getParameterTypes();
			Class[] exceptionTypes = ((Method)member).getExceptionTypes();
			if (!returnType.matchesStatically(returnTypeClass)) return false;
			if (!name.matches(member.getName())) return false;
			if (!parameterTypes.matches(params, TypePattern.STATIC).alwaysTrue()) {
				return false;
			}
			if (matchedArrayAgainstVarArgs(parameterTypes,member.getModifiers())) { return false; }
			if (!throwsPattern.matches(exceptionTypes)) return false;
			return declaringTypeMatch(member.getDeclaringClass()); // XXXAJ5 - Need to make this a covariant aware version for dynamic JP matching to work
		}
		if (kind == Member.CONSTRUCTOR) {
			if (! (member instanceof Constructor)) return false;
			
			Class[] params = ((Constructor)member).getParameterTypes();
			Class[] exceptionTypes = ((Constructor)member).getExceptionTypes();
			if (!parameterTypes.matches(params, TypePattern.STATIC).alwaysTrue()) {
				return false;
			}
			if (matchedArrayAgainstVarArgs(parameterTypes,member.getModifiers())) { return false; }
			if (!throwsPattern.matches(exceptionTypes)) return false;
			return declaringType.matchesStatically(declaringClass);
		}
		return false;
	}
	
	private boolean declaringTypeMatch(Signature sig) {
		Class onType = sig.getDeclaringType();
		if (declaringType.matchesStatically(onType)) return true;
		
		Collection declaringTypes = getDeclaringTypes(sig);
		
		for (Iterator it = declaringTypes.iterator(); it.hasNext(); ) {
			Class pClass = (Class) it.next();
			if (declaringType.matchesStatically(pClass)) return true;
		}
		
		return false;
	}
	
	private boolean declaringTypeMatch(Class clazz) {
		if (clazz == null) return false;
		if (declaringType.matchesStatically(clazz)) return true;
		Class[] ifs = clazz.getInterfaces();
		for (int i = 0; i<ifs.length; i++) {
			if (declaringType.matchesStatically(ifs[i])) return true;
		}
		return declaringTypeMatch(clazz.getSuperclass());
	}
	
	private Collection getDeclaringTypes(Signature sig) {
		List l = new ArrayList();
		Class onType = sig.getDeclaringType();
		String memberName = sig.getName();
		if (sig instanceof FieldSignature) {
			Class fieldType = ((FieldSignature)sig).getFieldType();
			Class superType = onType;
			while(superType != null) {
				try {
					Field f =  (superType.getDeclaredField(memberName));
					if (f.getType() == fieldType) {
						l.add(superType);
					}
				} catch (NoSuchFieldException nsf) {}
				superType = superType.getSuperclass();
			}
		} else if (sig instanceof MethodSignature) {
			Class[] paramTypes = ((MethodSignature)sig).getParameterTypes();
			Class superType = onType;
			while(superType != null) {
				try {
					superType.getDeclaredMethod(memberName,paramTypes);
					l.add(superType);
				} catch (NoSuchMethodException nsm) {}
				superType = superType.getSuperclass();
			}
		}
		return l;
	}
	
    public NamePattern getName() { return name; }
    public TypePattern getDeclaringType() { return declaringType; }
    
    public Member.Kind getKind() {
    	return kind;
    }
    
    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	
    	if (annotationPattern != AnnotationTypePattern.ANY) {
			buf.append(annotationPattern.toString());
			buf.append(' ');
    	}
    	
    	if (modifiers != ModifiersPattern.ANY) {
    		buf.append(modifiers.toString());
    		buf.append(' ');
    	}
    	
    	if (kind == Member.STATIC_INITIALIZATION) {
    		buf.append(declaringType.toString());
    		buf.append(".<clinit>()");//FIXME AV - bad, cannot be parsed again
    	} else if (kind == Member.HANDLER) {
    		buf.append("handler(");
    		buf.append(parameterTypes.get(0));
    		buf.append(")");
    	} else {
    		if (!(kind == Member.CONSTRUCTOR)) {
    			buf.append(returnType.toString());
    		    buf.append(' ');
    		}
    		if (declaringType != TypePattern.ANY) {
    			buf.append(declaringType.toString());
    			buf.append('.');
    		}
    		if (kind == Member.CONSTRUCTOR) {
    			buf.append("new");
    		} else {
    		    buf.append(name.toString());
    		}
    		if (kind == Member.METHOD || kind == Member.CONSTRUCTOR) {
    			buf.append(parameterTypes.toString());
    		}
            //FIXME AV - throws is not printed here, weird
    	}
    	return buf.toString();
    }
    
    public boolean equals(Object other) {
    	if (!(other instanceof SignaturePattern)) return false;
    	SignaturePattern o = (SignaturePattern)other;
    	return o.kind.equals(this.kind)
    		&& o.modifiers.equals(this.modifiers)
    		&& o.returnType.equals(this.returnType)
    		&& o.declaringType.equals(this.declaringType)
    		&& o.name.equals(this.name)
    		&& o.parameterTypes.equals(this.parameterTypes)
			&& o.throwsPattern.equals(this.throwsPattern)
			&& o.annotationPattern.equals(this.annotationPattern);
    }
    public int hashCode() {
        int result = 17;
        result = 37*result + kind.hashCode();
        result = 37*result + modifiers.hashCode();
        result = 37*result + returnType.hashCode();
        result = 37*result + declaringType.hashCode();
        result = 37*result + name.hashCode();
        result = 37*result + parameterTypes.hashCode();
        result = 37*result + throwsPattern.hashCode();
        result = 37*result + annotationPattern.hashCode();
        return result;
    }
    
	public void write(DataOutputStream s) throws IOException {
		kind.write(s);
		modifiers.write(s);
		returnType.write(s);
		declaringType.write(s);
		name.write(s);
		parameterTypes.write(s);
		throwsPattern.write(s);
		annotationPattern.write(s);
		writeLocation(s);
	}

	public static SignaturePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		Member.Kind kind = Member.Kind.read(s);
		ModifiersPattern modifiers = ModifiersPattern.read(s);
		TypePattern returnType = TypePattern.read(s, context);
		TypePattern declaringType = TypePattern.read(s, context);
		NamePattern name = NamePattern.read(s);
		TypePatternList parameterTypes = TypePatternList.read(s, context);
		ThrowsPattern throwsPattern = ThrowsPattern.read(s, context);
		
		AnnotationTypePattern annotationPattern = AnnotationTypePattern.ANY;
		
		if (s.getMajorVersion()>=AjAttribute.WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ150) {
		  annotationPattern = AnnotationTypePattern.read(s,context);
		}

		SignaturePattern ret = new SignaturePattern(kind, modifiers, returnType, declaringType,
					name, parameterTypes, throwsPattern,annotationPattern);
		ret.readLocation(context, s);
		return ret;
	}

	/**
	 * @return
	 */
	public ModifiersPattern getModifiers() {
		return modifiers;
	}

	/**
	 * @return
	 */
	public TypePatternList getParameterTypes() {
		return parameterTypes;
	}

	/**
	 * @return
	 */
	public TypePattern getReturnType() {
		return returnType;
	}

	/**
	 * @return
	 */
	public ThrowsPattern getThrowsPattern() {
		return throwsPattern;
	}
	
	/**
	 * return true if last argument in params is an Object[] but the modifiers say this method
	 * was declared with varargs (Object...).  We shouldn't be matching if this is the case.
	 */
	private boolean matchedArrayAgainstVarArgs(TypePatternList params,int modifiers) {
		if (params.size()>0 && (modifiers & Constants.ACC_VARARGS)!=0) {
			// we have at least one parameter in the pattern list, and the method has a varargs signature
			TypePattern lastPattern = params.get(params.size()-1);
			if (lastPattern.isArray() && !lastPattern.isVarArgs) return true;
		}
	    return false;
	}
	
	public AnnotationTypePattern getAnnotationPattern() {
		return annotationPattern;
	}


	public boolean isStarAnnotation() {
		return annotationPattern == AnnotationTypePattern.ANY;
	}

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
