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


package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.FieldSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.AnnotationTargetKind;
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
    private transient int hashcode = -1;
    	
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
			checkForIncorrectTargetKind(returnType,scope,false);
		} 
		if (declaringType != null) {
			declaringType = declaringType.resolveBindings(scope, bindings, false, false);
			checkForIncorrectTargetKind(declaringType,scope,false);
		}
		if (parameterTypes != null) {
			parameterTypes = parameterTypes.resolveBindings(scope, bindings, false, false);
			checkForIncorrectTargetKind(parameterTypes,scope,false,true);
		}
		if (throwsPattern != null) {
			throwsPattern = throwsPattern.resolveBindings(scope, bindings);
			if (throwsPattern.getForbidden().getTypePatterns().length > 0 
					|| throwsPattern.getRequired().getTypePatterns().length > 0) {
				checkForIncorrectTargetKind(throwsPattern,scope,false);				
			}
		}
		if (annotationPattern != null) {
			annotationPattern = annotationPattern.resolveBindings(scope,bindings,false);
			checkForIncorrectTargetKind(annotationPattern,scope,true);
		}
		hashcode =-1;
    	return this;
    }
    private void checkForIncorrectTargetKind(PatternNode patternNode, IScope scope, boolean targetsOtherThanTypeAllowed) {
    	checkForIncorrectTargetKind(patternNode, scope, targetsOtherThanTypeAllowed, false);
    	
    }
    
    // bug 115252 - adding an xlint warning if the annnotation target type is 
    // wrong. This logic, or similar, may have to be applied elsewhere in the case
    // of pointcuts which don't go through SignaturePattern.resolveBindings(..)
    private void checkForIncorrectTargetKind(PatternNode patternNode, IScope scope, boolean targetsOtherThanTypeAllowed, boolean parameterTargettingAnnotationsAllowed) {
    	// return if we're not in java5 mode, if the unmatchedTargetKind Xlint
    	// warning has been turned off, or if the patternNode is *
    	if (!scope.getWorld().isInJava5Mode()
    			|| scope.getWorld().getLint().unmatchedTargetKind == null
    			|| (patternNode instanceof AnyTypePattern)) {
			return;
		}
		if (patternNode instanceof ExactAnnotationTypePattern) {
			ResolvedType resolvedType = ((ExactAnnotationTypePattern)patternNode).getAnnotationType().resolve(scope.getWorld());
			if (targetsOtherThanTypeAllowed) {
				AnnotationTargetKind[] targetKinds = resolvedType.getAnnotationTargetKinds();
				if (targetKinds == null) return;
				reportUnmatchedTargetKindMessage(targetKinds,patternNode,scope,true);
			} else if (!targetsOtherThanTypeAllowed && !resolvedType.canAnnotationTargetType()) {
				// everything is incorrect since we've already checked whether we have the TYPE target annotation
				AnnotationTargetKind[] targetKinds = resolvedType.getAnnotationTargetKinds();
				if (targetKinds == null) return;
				reportUnmatchedTargetKindMessage(targetKinds,patternNode,scope,false);
			}
		} else {
			TypePatternVisitor visitor = new TypePatternVisitor(scope,targetsOtherThanTypeAllowed,parameterTargettingAnnotationsAllowed);
			patternNode.traverse(visitor,null);
			if (visitor.containedIncorrectTargetKind()) {
				Set keys = visitor.getIncorrectTargetKinds().keySet();				
				for (Iterator iter = keys.iterator(); iter.hasNext();) {
					PatternNode node = (PatternNode)iter.next();
					AnnotationTargetKind[] targetKinds =  (AnnotationTargetKind[]) visitor.getIncorrectTargetKinds().get(node);
					reportUnmatchedTargetKindMessage(targetKinds,node,scope,false);
				}
			}
		}
    }
    
    private void reportUnmatchedTargetKindMessage(
    		AnnotationTargetKind[] annotationTargetKinds,  
    		PatternNode node,
    		IScope scope,
    		boolean checkMatchesMemberKindName) {
    	StringBuffer targetNames = new StringBuffer("{");
    	for (int i = 0; i < annotationTargetKinds.length; i++) {
			AnnotationTargetKind targetKind = annotationTargetKinds[i];
			if (checkMatchesMemberKindName && kind.getName().equals(targetKind.getName())) {
				return;
			}
			if (i < (annotationTargetKinds.length - 1)) {
				targetNames.append("ElementType." + targetKind.getName() + ",");
			} else {
				targetNames.append("ElementType." + targetKind.getName() + "}");
			}
		}
		scope.getWorld().getLint().unmatchedTargetKind.signal(new String[] {node.toString(),targetNames.toString()}, getSourceLocation(), new ISourceLocation[0]);
    }
    
    /**
     * Class which visits the nodes in the TypePattern tree until an
     * ExactTypePattern is found. Once this is found it creates a new
     * ExactAnnotationTypePattern and checks whether the targetKind
     * (created via the @Target annotation) matches ElementType.TYPE if
     * this is the only target kind which is allowed, or matches the
     * signature pattern kind if there is no restriction.
     */
    private class TypePatternVisitor extends AbstractPatternNodeVisitor {

    	private IScope scope;
    	private Map incorrectTargetKinds /* PatternNode -> AnnotationTargetKind[] */ = new HashMap();
    	private boolean targetsOtherThanTypeAllowed;
		private boolean parameterTargettingAnnotationsAllowed;

    	/**
    	 * @param requiredTarget - the signature pattern Kind
    	 * @param scope
    	 * @param parameterTargettingAnnotationsAllowed 
    	 */
    	public TypePatternVisitor(IScope scope, boolean targetsOtherThanTypeAllowed, boolean parameterTargettingAnnotationsAllowed) {
    		this.scope = scope;
    		this.targetsOtherThanTypeAllowed = targetsOtherThanTypeAllowed;
    		this.parameterTargettingAnnotationsAllowed = parameterTargettingAnnotationsAllowed;
    	}
    	
    	public Object visit(WildAnnotationTypePattern node, Object data) {
    		node.getTypePattern().accept(this,data);
    		return node;
    	}
    	
    	/**
    	 * Do the ExactAnnotationTypePatterns have the incorrect target?
    	 */
    	public Object visit(ExactAnnotationTypePattern node, Object data) {
    		ResolvedType resolvedType = node.getAnnotationType().resolve(scope.getWorld());
			if (targetsOtherThanTypeAllowed) {
				AnnotationTargetKind[] targetKinds = resolvedType.getAnnotationTargetKinds();
				if (targetKinds == null) return data;
				List incorrectTargets = new ArrayList();
				for (int i = 0; i < targetKinds.length; i++) {
					if (targetKinds[i].getName().equals(kind.getName()) ||
							(targetKinds[i].getName().equals("PARAMETER") && node.isForParameterAnnotationMatch())
							) {
						return data;
					}
					incorrectTargets.add(targetKinds[i]);
				}
				if (incorrectTargets.isEmpty()) return data;
				AnnotationTargetKind[] kinds = new AnnotationTargetKind[incorrectTargets.size()];
				incorrectTargetKinds.put(node,(AnnotationTargetKind[]) incorrectTargets.toArray(kinds));	
			} else if (!targetsOtherThanTypeAllowed && !resolvedType.canAnnotationTargetType()) {
				AnnotationTargetKind[] targetKinds = resolvedType.getAnnotationTargetKinds();
				if (targetKinds == null) return data;
				// exception here is if parameter annotations are allowed
				if (parameterTargettingAnnotationsAllowed) {
					for (int i = 0; i < targetKinds.length; i++) {
						AnnotationTargetKind annotationTargetKind = targetKinds[i];
						if (annotationTargetKind.getName().equals("PARAMETER")  && node.isForParameterAnnotationMatch()) return data;
					}
				}
				incorrectTargetKinds.put(node,targetKinds);
			}
    		return data;
    	}
    	
    	public Object visit(ExactTypePattern node, Object data) {
    		ExactAnnotationTypePattern eatp =  new ExactAnnotationTypePattern(node.getExactType().resolve(scope.getWorld()),null);
    		eatp.accept(this,data);		
    		return data;
    	}
    	
    	public Object visit(AndTypePattern node, Object data) {
    		node.getLeft().accept(this,data);
    		node.getRight().accept(this,data);
    		return node;
    	}

    	public Object visit(OrTypePattern node, Object data) {
    		node.getLeft().accept(this,data);
    		node.getRight().accept(this,data);
    		return node;
    	}

    	public Object visit(AnyWithAnnotationTypePattern node, Object data) {
    		node.getAnnotationPattern().accept(this,data);
    		return node;
    	}
    	
    	public boolean containedIncorrectTargetKind() {
    		return (incorrectTargetKinds.size() != 0);
    	}
    	
    	public Map getIncorrectTargetKinds() {
    		return incorrectTargetKinds;
    	}
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
	public SignaturePattern parameterizeWith(Map typeVariableMap,World w) {
		SignaturePattern ret = new SignaturePattern(
						kind,
						modifiers,
						returnType.parameterizeWith(typeVariableMap,w),
						declaringType.parameterizeWith(typeVariableMap,w),
						name,
						parameterTypes.parameterizeWith(typeVariableMap,w),
						throwsPattern.parameterizeWith(typeVariableMap,w),
						annotationPattern.parameterizeWith(typeVariableMap,w));
		ret.copyLocationFrom(this);
		return ret;
	}
	
	public boolean matches(Member joinPointSignature, World world, boolean allowBridgeMethods) {
		// fail (or succeed!) fast tests...
		if (joinPointSignature == null) return false;
		if (kind != joinPointSignature.getKind()) return false;
		if (kind == Member.ADVICE) return true;
		
		// do the hard work then...
		boolean subjectMatch = true;
		Iterator candidateMatches = joinPointSignature.getJoinPointSignatures(world);
		while(candidateMatches.hasNext()) {
			JoinPointSignature aSig = (JoinPointSignature) candidateMatches.next();
			FuzzyBoolean matchResult = matchesExactly(aSig,world,allowBridgeMethods,subjectMatch); 
			if (matchResult.alwaysTrue()) {
				return true;
			} else if (matchResult.alwaysFalse()) {
				return false;
			}
			// if we got a "MAYBE" it's worth looking at the other signatures
			subjectMatch = false;
		}
		return false;
	}
	
	// Does this pattern match this exact signature (no declaring type mucking about
	// or chasing up the hierarchy)
	// return YES if it does, NO if it doesn't and no ancester member could match either,
	// and MAYBE if it doesn't but an ancester member could.
	private FuzzyBoolean matchesExactly(JoinPointSignature aMember, World inAWorld, boolean allowBridgeMethods,boolean subjectMatch) {
		// Java5 introduces bridge methods, we match a call to them but nothing else...
		if (aMember.isBridgeMethod() && !allowBridgeMethods) {
			return FuzzyBoolean.MAYBE;
		}
			
		// modifiers match on the *subject*
		if (subjectMatch && !modifiers.matches(aMember.getModifiers())) {
			return FuzzyBoolean.NO;
//			if (aMember.isPrivate()) return FuzzyBoolean.NO;
//			else return FuzzyBoolean.MAYBE;
		}
		
		
		FuzzyBoolean matchesIgnoringAnnotations = FuzzyBoolean.YES;
		if (kind == Member.STATIC_INITIALIZATION) {
			matchesIgnoringAnnotations = matchesExactlyStaticInitialization(aMember, inAWorld);
		} else if (kind == Member.FIELD) {
			matchesIgnoringAnnotations = matchesExactlyField(aMember,inAWorld);
		} else if (kind == Member.METHOD) {
			matchesIgnoringAnnotations = matchesExactlyMethod(aMember,inAWorld, subjectMatch);
		} else if (kind == Member.CONSTRUCTOR) {
			matchesIgnoringAnnotations = matchesExactlyConstructor(aMember, inAWorld);
		}
		if (matchesIgnoringAnnotations.alwaysFalse()) return FuzzyBoolean.NO;
		
		// annotations match on the *subject* 
		if (subjectMatch && !matchesAnnotations(aMember,inAWorld).alwaysTrue()) {
			return FuzzyBoolean.NO;
		} else {
			return matchesIgnoringAnnotations;
		}
		
	}
	
	/**
	 * Matches on declaring type
	 */
	private FuzzyBoolean matchesExactlyStaticInitialization(JoinPointSignature aMember,World world) {
		return FuzzyBoolean.fromBoolean(declaringType.matchesStatically(aMember.getDeclaringType().resolve(world)));
	}
	
	/**
	 * Matches on name, declaring type, field type
	 */
	private FuzzyBoolean matchesExactlyField(JoinPointSignature aField, World world) {
		if (!name.matches(aField.getName())) return FuzzyBoolean.NO;
		ResolvedType fieldDeclaringType = aField.getDeclaringType().resolve(world);
		if (!declaringType.matchesStatically(fieldDeclaringType)) {
			return FuzzyBoolean.MAYBE;
		}
		if (!returnType.matchesStatically(aField.getReturnType().resolve(world))) {
			// looking bad, but there might be parameterization to consider...
			if (!returnType.matchesStatically(aField.getGenericReturnType().resolve(world))) {
				// ok, it's bad.
				return FuzzyBoolean.MAYBE;
			}
		}
		// passed all the guards...
		return FuzzyBoolean.YES;
	}
	
	/**
	 * Matches on name, declaring type, return type, parameter types, throws types
	 */
	private FuzzyBoolean matchesExactlyMethod(JoinPointSignature aMethod, World world, boolean subjectMatch) {
		if (!name.matches(aMethod.getName())) return FuzzyBoolean.NO;
		// Check the throws pattern
		if (subjectMatch && !throwsPattern.matches(aMethod.getExceptions(), world)) return FuzzyBoolean.NO;
		
		if (!declaringType.matchesStatically(aMethod.getDeclaringType().resolve(world))) return FuzzyBoolean.MAYBE;
		if (!returnType.matchesStatically(aMethod.getReturnType().resolve(world))) {
			// looking bad, but there might be parameterization to consider...
			if (!returnType.matchesStatically(aMethod.getGenericReturnType().resolve(world))) {
				// ok, it's bad.
				return FuzzyBoolean.MAYBE;
			}
		}
		if (!parameterTypes.canMatchSignatureWithNParameters(aMethod.getParameterTypes().length)) return FuzzyBoolean.NO;
		ResolvedType[] resolvedParameters = world.resolve(aMethod.getParameterTypes());
		ResolvedType[][] parameterAnnotationTypes = aMethod.getParameterAnnotationTypes();
		if (parameterAnnotationTypes==null || parameterAnnotationTypes.length==0) parameterAnnotationTypes=null;
		if (!parameterTypes.matches(resolvedParameters, TypePattern.STATIC,parameterAnnotationTypes).alwaysTrue()) {
			// It could still be a match based on the generic sig parameter types of a parameterized type
			if (!parameterTypes.matches(world.resolve(aMethod.getGenericParameterTypes()),TypePattern.STATIC).alwaysTrue()) {
				return FuzzyBoolean.MAYBE;
				// It could STILL be a match based on the erasure of the parameter types??
				// to be determined via test cases...
			}
		}
		
		// check that varargs specifications match
		if (!matchesVarArgs(aMethod,world)) return FuzzyBoolean.MAYBE;
		
		// passed all the guards..
		return FuzzyBoolean.YES;
	}
	


	/**
	 * match on declaring type, parameter types, throws types
	 */
	private FuzzyBoolean matchesExactlyConstructor(JoinPointSignature aConstructor, World world) {
		if (!declaringType.matchesStatically(aConstructor.getDeclaringType().resolve(world))) return FuzzyBoolean.NO;

		if (!parameterTypes.canMatchSignatureWithNParameters(aConstructor.getParameterTypes().length)) return FuzzyBoolean.NO;
		ResolvedType[] resolvedParameters = world.resolve(aConstructor.getParameterTypes());
		
		ResolvedType[][] parameterAnnotationTypes = aConstructor.getParameterAnnotationTypes();

		if (parameterAnnotationTypes==null || parameterAnnotationTypes.length==0) parameterAnnotationTypes=null;

		if (!parameterTypes.matches(resolvedParameters, TypePattern.STATIC,parameterAnnotationTypes).alwaysTrue()) {
			// It could still be a match based on the generic sig parameter types of a parameterized type
			if (!parameterTypes.matches(world.resolve(aConstructor.getGenericParameterTypes()),TypePattern.STATIC).alwaysTrue()) {
				return FuzzyBoolean.MAYBE;
				// It could STILL be a match based on the erasure of the parameter types??
				// to be determined via test cases...
			}
		}
		
		// check that varargs specifications match
		if (!matchesVarArgs(aConstructor,world)) return FuzzyBoolean.NO;
		
		// Check the throws pattern
		if (!throwsPattern.matches(aConstructor.getExceptions(), world)) return FuzzyBoolean.NO;
		
		// passed all the guards..
		return FuzzyBoolean.YES;		
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
		
	private FuzzyBoolean matchesAnnotations(ResolvedMember member,World world) {
	  if (member == null) {
	        if (member.getName().startsWith(NameMangler.PREFIX)) {
				return FuzzyBoolean.NO;
			}
			world.getLint().unresolvableMember.signal(member.toString(), getSourceLocation());
			return FuzzyBoolean.NO;
	  }
	  annotationPattern.resolve(world);
	 
	  // optimization before we go digging around for annotations on ITDs
	  if (annotationPattern instanceof AnyAnnotationTypePattern) return FuzzyBoolean.YES;
	  
	  // fake members represent ITD'd fields - for their annotations we should go and look up the
	  // relevant member in the original aspect
	  if (member.isAnnotatedElsewhere() && member.getKind()==Member.FIELD) {
	    // FIXME asc duplicate of code in AnnotationPointcut.matchInternal()?  same fixmes apply here.
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
	  
	  if (annotationPattern.matches(member).alwaysTrue()) {
		  return FuzzyBoolean.YES;
	  } else {
		  return FuzzyBoolean.NO;  // do NOT look at ancestor members...
	  }
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
    	if (hashcode==-1) {
          hashcode = 17;
          hashcode = 37*hashcode + kind.hashCode();
          hashcode = 37*hashcode + modifiers.hashCode();
          hashcode = 37*hashcode + returnType.hashCode();
          hashcode = 37*hashcode + declaringType.hashCode();
          hashcode = 37*hashcode + name.hashCode();
          hashcode = 37*hashcode + parameterTypes.hashCode();
          hashcode = 37*hashcode + throwsPattern.hashCode();
          hashcode = 37*hashcode + annotationPattern.hashCode();
    	}
        return hashcode;
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
