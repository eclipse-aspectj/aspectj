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
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.NewFieldTypeMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
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
    
    
	public void postRead(ResolvedTypeX enclosingType) {
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
	
	public boolean matches(Member member, World world) {
		return (matchesIgnoringAnnotations(member,world) &&
				matchesAnnotations(member,world));
	}
	
	public boolean matchesAnnotations(Member member,World world) {
	  ResolvedMember rMember = member.resolve(world);
	  if (rMember == null) {
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
	  if (rMember.isAnnotatedElsewhere() && member.getKind()==Member.FIELD) {
	    // FIXME asc duplicate of code in AnnotationPattern.matchInternal()?  same fixmes apply here.
	    ResolvedMember [] mems = rMember.getDeclaringType().resolve(world).getDeclaredFields(); // FIXME asc should include supers with getInterTypeMungersIncludingSupers?
	    List mungers = rMember.getDeclaringType().resolve(world).getInterTypeMungers(); 
		for (Iterator iter = mungers.iterator(); iter.hasNext();) {
	        BcelTypeMunger typeMunger = (BcelTypeMunger) iter.next();
			if (typeMunger.getMunger() instanceof NewFieldTypeMunger) {
			  ResolvedMember fakerm = typeMunger.getSignature();
			  ResolvedMember ajcMethod = AjcMemberMaker.interFieldInitializer(fakerm,typeMunger.getAspectType());
		  	  ResolvedMember rmm       = findMethod(typeMunger.getAspectType(),ajcMethod);
			  if (fakerm.equals(member)) {
				rMember = rmm;
			  }
			}
		}
	  }
	  
	  return annotationPattern.matches(rMember).alwaysTrue();
	}
	
	private ResolvedMember findMethod(ResolvedTypeX aspectType, ResolvedMember ajcMethod) {
	       ResolvedMember decMethods[] = aspectType.getDeclaredMethods();
	       for (int i = 0; i < decMethods.length; i++) {
			ResolvedMember member = decMethods[i];
			if (member.equals(ajcMethod)) return member;
	   }
			return null;
		}
	
	public boolean matchesIgnoringAnnotations(Member member, World world) {
		//XXX performance gains would come from matching on name before resolving
		//    to fail fast.  ASC 30th Nov 04 => Not necessarily, it didn't make it faster for me.
		//    Here is the code I used:
		//		String n1 = member.getName();
		//		String n2 = this.getName().maybeGetSimpleName();
		//		if (n2!=null && !n1.equals(n2)) return false;

		if (member == null) return false;
		ResolvedMember sig = member.resolve(world);
		
		if (sig == null) {
			//XXX
			if (member.getName().startsWith(NameMangler.PREFIX)) {
				return false;
			}
			world.getLint().unresolvableMember.signal(member.toString(), getSourceLocation());
			return false;
		}
		
		// Java5 introduces bridge methods, we don't want to match on them at all...
		if (sig.isBridgeMethod()) {
			return false;
		}
		
		// This check should only matter when used from WithincodePointcut as KindedPointcut
		// has already effectively checked this with the shadows kind.
		if (kind != member.getKind()) {
			return false;
		}
		
		if (kind == Member.ADVICE) return true;
		
		if (!modifiers.matches(sig.getModifiers())) return false;
		
		if (kind == Member.STATIC_INITIALIZATION) {
			//System.err.println("match static init: " + sig.getDeclaringType() + " with " + this);
			return declaringType.matchesStatically(sig.getDeclaringType().resolve(world));
		} else if (kind == Member.FIELD) {
			
			if (!returnType.matchesStatically(sig.getReturnType().resolve(world))) return false;
			if (!name.matches(sig.getName())) return false;
			boolean ret = declaringTypeMatch(member.getDeclaringType(), member, world);
			//System.out.println("   ret: " + ret);
			return ret;
		} else if (kind == Member.METHOD) {
			// Change all this in the face of covariance...
			
			// Check the name
			if (!name.matches(sig.getName())) return false;
			
			// Check the parameters
			if (!parameterTypes.matches(world.resolve(sig.getParameterTypes()), TypePattern.STATIC).alwaysTrue()) {
				return false;
			}
			
			// If we have matched on parameters, let's just check it isn't because the last parameter in the pattern
			// is an array type and the method is declared with varargs
			// XXX - Ideally the shadow would be included in the msg but we don't know it...
			if (isNotMatchBecauseOfVarargsIssue(parameterTypes,sig.getModifiers())) { 
				world.getLint().cantMatchArrayTypeOnVarargs.signal(sig.toString(),getSourceLocation());
				return false;
			}

			if (parameterTypes.size()>0 && (sig.isVarargsMethod()^parameterTypes.get(parameterTypes.size()-1).isVarArgs)) 
				return false;
			
			// Check the throws pattern
			if (!throwsPattern.matches(sig.getExceptions(), world)) return false;

			return declaringTypeMatchAllowingForCovariance(member,world,returnType,sig.getReturnType().resolve(world));
		} else if (kind == Member.CONSTRUCTOR) {
			if (!parameterTypes.matches(world.resolve(sig.getParameterTypes()), TypePattern.STATIC).alwaysTrue()) {
				return false;
			}
			
			// If we have matched on parameters, let's just check it isn't because the last parameter in the pattern
			// is an array type and the method is declared with varargs
			// XXX - Ideally the shadow would be included in the msg but we don't know it...
			if (isNotMatchBecauseOfVarargsIssue(parameterTypes,sig.getModifiers())) { 
				world.getLint().cantMatchArrayTypeOnVarargs.signal(sig.toString(),getSourceLocation());
				return false;
			}
			
			if (!throwsPattern.matches(sig.getExceptions(), world)) return false;
			return declaringType.matchesStatically(member.getDeclaringType().resolve(world));
			//return declaringTypeMatch(member.getDeclaringType(), member, world);			
		}
		
		return false;
	}
	
	public boolean declaringTypeMatchAllowingForCovariance(Member member,World world,TypePattern returnTypePattern,ResolvedTypeX sigReturn) {
		TypeX onTypeUnresolved = member.getDeclaringType();
		
		ResolvedTypeX onType = onTypeUnresolved.resolve(world);
			
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
			ResolvedTypeX type = (ResolvedTypeX)i.next();
			if (declaringType.matchesStatically(type)) {
			  if (!checkReturnType) return true;
			  ResolvedMember rm = type.lookupMethod(member);
			  if (rm==null)  rm = type.lookupMethodInITDs(member); // It must be in here, or we have *real* problems
			  TypeX returnTypeX = rm.getReturnType();
			  ResolvedTypeX returnType = returnTypeX.resolve(world);
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
			if (isNotMatchBecauseOfVarargsIssue(parameterTypes,msig.getModifiers())) { return false; }
			
			if (!throwsPattern.matches(exceptionTypes)) return false;
			return declaringTypeMatch(sig); // XXXAJ5 - Need to make this a covariant aware version for dynamic JP matching to work
		} else if (kind == Member.CONSTRUCTOR) {
			ConstructorSignature csig = (ConstructorSignature)sig;
			Class[] params = csig.getParameterTypes();
			Class[] exceptionTypes = csig.getExceptionTypes();
			if (!parameterTypes.matches(params, TypePattern.STATIC).alwaysTrue()) {
				return false;
			}
			if (isNotMatchBecauseOfVarargsIssue(parameterTypes,csig.getModifiers())) { return false; }
			
			if (!throwsPattern.matches(exceptionTypes)) return false;
			return declaringType.matchesStatically(sig.getDeclaringType());
			//return declaringTypeMatch(member.getDeclaringType(), member, world);			
		}
			    
		return false;
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
			if (isNotMatchBecauseOfVarargsIssue(parameterTypes,member.getModifiers())) { return false; }
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
			if (isNotMatchBecauseOfVarargsIssue(parameterTypes,member.getModifiers())) { return false; }
			if (!throwsPattern.matches(exceptionTypes)) return false;
			return declaringType.matchesStatically(declaringClass);
		}
		return false;
	}
	
// For methods, the above covariant aware version (declaringTypeMatchAllowingForCovariance) is used - this version is still here for fields
	private boolean declaringTypeMatch(TypeX onTypeUnresolved, Member member, World world) {
		ResolvedTypeX onType = onTypeUnresolved.resolve(world);
		
		// fastmatch
		if (declaringType.matchesStatically(onType)) return true;
		
		Collection declaringTypes = member.getDeclaringTypes(world);
		
		for (Iterator i = declaringTypes.iterator(); i.hasNext(); ) {
			ResolvedTypeX type = (ResolvedTypeX)i.next();
			if (declaringType.matchesStatically(type)) return true;
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
					Method m =  (superType.getDeclaredMethod(memberName,paramTypes));
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
    		buf.append(".<clinit>()");
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
	private boolean isNotMatchBecauseOfVarargsIssue(TypePatternList params,int modifiers) {
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

    public Object accept(PointcutVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
