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


package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseTypeMunger;
import org.aspectj.ajdt.internal.compiler.lookup.InterTypeScope;
import org.aspectj.ajdt.internal.core.builder.EclipseSourceContext;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.bridge.context.ContextToken;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.Shadow;

/**
 * Base type for all inter-type declarations including methods, fields and constructors.
 *
 * @author Jim Hugunin
 */
public abstract class InterTypeDeclaration extends AjMethodDeclaration {
	protected TypeReference onType;
	protected ReferenceBinding onTypeBinding;
	protected ResolvedTypeMunger munger;
	public int declaredModifiers; // so others can see (these differ from the modifiers in the superclass)
	protected char[] declaredSelector;
	
	/** 
	 * If targetting a generic type and wanting to use its type variables, an ITD can use an alternative name for
	 *  them.  This is a list of strings representing the alternative names - the position in the list is used to
	 *  match it to the real type variable in the target generic type.
	 */
	protected List<String> typeVariableAliases; 
	
	protected InterTypeScope interTypeScope;
	
	/**
	 * When set to true, the scope hierarchy for the field/method declaration has been correctly modified to
	 * include an intertypescope which resolves things relative to the targetted type.
	 */
    private boolean scopeSetup = false;
	
	// XXXAJ5 - When the compiler is changed, these will exist somewhere in it...
	private final static short ACC_ANNOTATION   = 0x2000;
	private final static short ACC_ENUM         = 0x4000;


	public InterTypeDeclaration(CompilationResult result, TypeReference onType) {
		super(result);
		setOnType(onType);
		modifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccStatic;
	}

	public void setOnType(TypeReference onType) {
		this.onType = onType;
		determineTypeVariableAliases();
	}
	
	public void setDeclaredModifiers(int modifiers) {
		this.declaredModifiers = modifiers;
	}
	
	public void setSelector(char[] selector) {
		declaredSelector = selector;
		this.selector = CharOperation.concat(selector, Integer.toHexString(sourceStart).toCharArray());
		this.selector = CharOperation.concat(getPrefix(),this.selector);
	}
	
	// return the selector prefix for this itd that is to be used before resolution replaces it with a "proper" name
	protected abstract char[] getPrefix();
	
	
	public void addAtAspectJAnnotations() {
		if (munger == null) return;
		Annotation ann = AtAspectJAnnotationFactory.createITDAnnotation(
				munger.getSignature().getDeclaringType().getName().toCharArray(),
				declaredModifiers,declaredSelector,declarationSourceStart);
		AtAspectJAnnotationFactory.addAnnotation(this,ann,this.scope);
	}

	/**
	 * Checks that the target for the ITD is not an annotation.  If it is, an error message
	 * is signaled.  We return true if it is annotation so the caller knows to stop processing.
	 * kind is 'constructor', 'field', 'method'
	 */
	public boolean isTargetAnnotation(ClassScope classScope,String kind) {
		if ((onTypeBinding.getAccessFlags() & ACC_ANNOTATION)!=0) { 
			classScope.problemReporter().signalError(sourceStart,sourceEnd,
			  "can't make inter-type "+kind+" declarations on annotation types.");
			ignoreFurtherInvestigation = true;
			return true;
		}
		return false;
	}
	
	/**
	 * Checks that the target for the ITD is not an enum.  If it is, an error message
	 * is signaled.  We return true if it is enum so the caller knows to stop processing.
	 */
	public boolean isTargetEnum(ClassScope classScope,String kind) {
		if ((onTypeBinding.getAccessFlags() & ACC_ENUM)!=0) { 
			classScope.problemReporter().signalError(sourceStart,sourceEnd,
			  "can't make inter-type "+kind+" declarations on enum types.");
			ignoreFurtherInvestigation = true;
			return true;
		}
		return false;
	}
	
	@Override
	public void resolve(ClassScope upperScope) {
		if (ignoreFurtherInvestigation) return;
		
		if (!scopeSetup) {
		  interTypeScope = new InterTypeScope(upperScope, onTypeBinding,typeVariableAliases);
		  scope.parent = interTypeScope;
		  this.scope.isStatic = Modifier.isStatic(declaredModifiers);
		  scopeSetup = true;
		}
		fixSuperCallsForInterfaceContext(upperScope);
		if (ignoreFurtherInvestigation) return;
		
		super.resolve((ClassScope)scope.parent);//newParent);
		fixSuperCallsInBody();
	}

	private void fixSuperCallsForInterfaceContext(ClassScope scope) {
		if (onTypeBinding.isInterface()) {
			ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.FIXING_SUPER_CALLS, selector);
			InterSuperFixerVisitor v =
				new InterSuperFixerVisitor(this, 
						EclipseFactory.fromScopeLookupEnvironment(scope), scope);
			this.traverse(v, scope);
			CompilationAndWeavingContext.leavingPhase(tok);
		}
	}

	/**
	 * Called from AspectDeclarations.buildInterTypeAndPerClause
	 */
	public abstract EclipseTypeMunger build(ClassScope classScope);

	public void fixSuperCallsInBody() {
		ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.FIXING_SUPER_CALLS_IN_ITDS, selector);
		SuperFixerVisitor v = new SuperFixerVisitor(this, onTypeBinding);
		this.traverse(v, (ClassScope)null);
		munger.setSuperMethodsCalled(v.superMethodsCalled);
		CompilationAndWeavingContext.leavingPhase(tok);
	}

	protected void resolveOnType(ClassScope classScope) {
		checkSpec();
		
		if (onType==null) return; // error reported elsewhere.
		
		// If they did supply a parameterized single type reference, we need to do
		// some extra checks...
		if (onType instanceof ParameterizedSingleTypeReference  || onType instanceof ParameterizedQualifiedTypeReference) {
			resolveTypeParametersForITDOnGenericType(classScope);
		} else {
			onTypeBinding = (ReferenceBinding)onType.getTypeBindingPublic(classScope);
			if (!onTypeBinding.isValidBinding()) {
				classScope.problemReporter().invalidType(onType, onTypeBinding);
				ignoreFurtherInvestigation = true;
			}
			if (onTypeBinding.isParameterizedType()) {
				// might be OK... pr132349
				ParameterizedTypeBinding ptb = (ParameterizedTypeBinding)onTypeBinding;
				if (ptb.isNestedType()) {
					if (ptb.typeVariables()==null || ptb.typeVariables().length==0) {
						if (ptb.enclosingType().isRawType()) onTypeBinding = ptb.type;
					}
				}
			}
		}
	}

    /**
     * Transform the parameterized type binding (e.g. SomeType<A,B,C>) to a 
     * real type (e.g. SomeType).  The only kind of parameterization allowed
     * is with type variables and those are references to type variables on
     * the target type.  Once we have worked out the base generic type intended
     * then we do lots of checks to verify the declaration was well formed.
     */
	private void resolveTypeParametersForITDOnGenericType(ClassScope classScope) {

		// Collapse the parameterized reference to its generic type
		if (onType instanceof ParameterizedSingleTypeReference) {
			ParameterizedSingleTypeReference pref = (ParameterizedSingleTypeReference) onType;
			long pos = (((long)pref.sourceStart) << 32) | pref.sourceEnd;
			onType = new SingleTypeReference(pref.token,pos);
		} else {
			ParameterizedQualifiedTypeReference pref = (ParameterizedQualifiedTypeReference) onType;
			long pos = (((long)pref.sourceStart) << 32) | pref.sourceEnd;
			onType = new QualifiedTypeReference(pref.tokens,new long[]{pos});
			
		}
		
		onTypeBinding = (ReferenceBinding)onType.getTypeBindingPublic(classScope);		
		if (!onTypeBinding.isValidBinding()) {
			classScope.problemReporter().invalidType(onType, onTypeBinding);
			ignoreFurtherInvestigation = true;
		}

 		
		if (onTypeBinding.isRawType()) {
			onTypeBinding = ((RawTypeBinding)onTypeBinding).type;
		}
		
		int aliasCount = (typeVariableAliases==null?0:typeVariableAliases.size());
		
		// Cannot specify a parameterized target type for the ITD if the target
		// type is not generic.
		if (aliasCount!=0 && !onTypeBinding.isGenericType()) {
			scope.problemReporter().signalError(sourceStart,sourceEnd,
					"Type parameters can not be specified in the ITD target type - the target type "+onTypeBinding.debugName()+" is not generic.");
			ignoreFurtherInvestigation = true;
			return;
		}
		
		// Check they have supplied the right number of type parameters on the ITD target type
		if (aliasCount>0) {
			if (onTypeBinding.typeVariables().length != aliasCount) { // typeParameters.length) {   phantom contains the fake ones from the ontype, typeparameters will also include extra things if it is a generic method
				scope.problemReporter().signalError(sourceStart, sourceEnd,
					"Incorrect number of type parameters supplied.  The generic type "+onTypeBinding.debugName()+" has "+
					onTypeBinding.typeVariables().length+" type parameters, not "+aliasCount+".");
				ignoreFurtherInvestigation = true;
				return;
			}
		}
		
		// check if they used stupid names for type variables
		if (aliasCount>0) {
			for (int i = 0; i < aliasCount; i++) {
				String array_element = typeVariableAliases.get(i);
				SingleTypeReference str = new SingleTypeReference(array_element.toCharArray(),0);
				TypeBinding tb = str.getTypeBindingPublic(classScope);
				if (tb!=null && !(tb instanceof ProblemReferenceBinding)) {// && !(tb instanceof TypeVariableBinding)) {
					scope.problemReporter().signalError(sourceStart,sourceEnd,
							"Intertype declarations can only be made on the generic type, not on a parameterized type. The name '"+
							array_element+"' cannot be used as a type parameter, since it refers to a real type.");
					ignoreFurtherInvestigation = true;
					return;
					
				}
			}
		}
//		TypeVariableBinding[] tVarsInGenericType = onTypeBinding.typeVariables();
//		typeVariableAliases = new ArrayList(); /* Name>GenericTypeVariablePosition */ // FIXME ASC DONT THINK WE NEED TO BUILD IT HERE AS WELL...
//		TypeReference[] targs = pref.typeArguments;
//    	if (targs!=null) {
//    		for (int i = 0; i < targs.length; i++) {
//    			TypeReference tref = targs[i];
//    			typeVariableAliases.add(CharOperation.toString(tref.getTypeName()));//tVarsInGenericType[i]); 
//    		}
//		}
	}
	
	
	protected void checkSpec() {
		if (Modifier.isProtected(declaredModifiers)) {
			scope.problemReporter().signalError(sourceStart, sourceEnd,
				"protected inter-type declarations are not allowed");
			ignoreFurtherInvestigation = true;
		}
	}
	
	protected List makeEffectiveSignatureAttribute(
		ResolvedMember sig,
		Shadow.Kind kind,
		boolean weaveBody)
	{
		List l = new ArrayList(1);
		l.add(new EclipseAttributeAdapter(
				new AjAttribute.EffectiveSignatureAttribute(sig, kind, weaveBody)));
		return l;
	}
	
	protected void setMunger(ResolvedTypeMunger munger) {
		munger.getSignature().setPosition(sourceStart, sourceEnd);
		munger.getSignature().setSourceContext(new EclipseSourceContext(compilationResult));
		this.munger = munger;
	}
	
	@Override
	protected int generateInfoAttributes(ClassFile classFile) {
		List l;
		Shadow.Kind kind = getShadowKindForBody();
		if (kind != null) {
			l = makeEffectiveSignatureAttribute(munger.getSignature(), kind, true);
		} else {
			l = new ArrayList(0);
		}
		addDeclarationStartLineAttribute(l,classFile);

		return classFile.generateMethodInfoAttributes(binding, l);
	}

	protected abstract Shadow.Kind getShadowKindForBody();
	
	public ResolvedMember getSignature() { 
		if (munger==null) return null; // Can be null in an erroneous program I think
		return munger.getSignature(); 
	}

	public char[] getDeclaredSelector() {
		return declaredSelector;
	}
	
	public TypeReference getOnType() {
		return onType;
	}
	

	/** 
	 * Create the list of aliases based on what was supplied as parameters for the ontype.
	 * For example, if the declaration is 'List&lt;N&gt;  SomeType&lt;N&gt;.foo' then the alias list
	 * will simply contain 'N' and 'N' will mean 'the first type variable declared for
	 * type SomeType'
	 */
	public void determineTypeVariableAliases() {
		if (onType!=null) {
		    // TODO loses distinction about which level the type variables are at... is that a problem?
			if (onType instanceof ParameterizedSingleTypeReference) {
				ParameterizedSingleTypeReference paramRef = (ParameterizedSingleTypeReference) onType;
				TypeReference[] rb = paramRef.typeArguments;
				typeVariableAliases = new ArrayList();
				for (TypeReference typeReference : rb) {
					typeVariableAliases.add(CharOperation.toString(typeReference.getTypeName()));
				}
			} else if (onType instanceof ParameterizedQualifiedTypeReference) {
				ParameterizedQualifiedTypeReference paramRef = (ParameterizedQualifiedTypeReference) onType;
				typeVariableAliases = new ArrayList();
				for (int j = 0; j < paramRef.typeArguments.length; j++) {
					TypeReference[] rb = paramRef.typeArguments[j];
					for (int i = 0; rb!=null && i < rb.length; i++) {
						typeVariableAliases.add(CharOperation.toString(rb[i].getTypeName()));
					}
				}
			}
		}
	}  

	/**
	 * Called just before the compiler is going to start resolving elements of a declaration, this method
	 * adds an intertypescope between the methodscope and classscope so that elements of the type targetted
	 * by the ITD can be resolved.  For example, if type variables are referred to in the ontype for the ITD,
	 * they have to be resolved against the ontype, not the aspect containing the ITD.
	 */
	@Override
	public void ensureScopeSetup() {
		if (scopeSetup) return; // don't do it again
		MethodScope scope = this.scope;
		
		TypeReference ot = onType;
		ReferenceBinding rb = null;
		
		if (ot instanceof ParameterizedQualifiedTypeReference) { // pr132349
			ParameterizedQualifiedTypeReference pref = (ParameterizedQualifiedTypeReference) ot;
			if (pref.typeArguments!=null && pref.typeArguments.length!=0) {
				boolean usingNonTypeVariableInITD = false;
				// Check if any of them are not type variables
				for (int i = 0; i < pref.typeArguments.length; i++) {
					TypeReference[] refs = pref.typeArguments[i];
					for (int j = 0; refs!=null && j < refs.length; j++) {
						TypeBinding tb = refs[j].getTypeBindingPublic(scope.parent);
						if (!tb.isTypeVariable() && !(tb instanceof ProblemReferenceBinding)) {
							usingNonTypeVariableInITD = true;
						}
						
					}
				}
				if (usingNonTypeVariableInITD) {
					scope.problemReporter().signalError(sourceStart,sourceEnd,
					  "Cannot make inter-type declarations on parameterized types");
					// to prevent disgusting cascading errors after this problem - lets null out what leads to them (pr105038)
					this.arguments=null;
					this.returnType=new SingleTypeReference(TypeReference.VOID,0L);
					
					this.ignoreFurtherInvestigation=true;
					ReferenceBinding closestMatch = null;
					rb = new ProblemReferenceBinding(ot.getParameterizedTypeName(),closestMatch,0);		
					onType=null;
				}
			}
		
		}

		// Work out the real base type
		if (ot instanceof ParameterizedSingleTypeReference) {
			ParameterizedSingleTypeReference pref = (ParameterizedSingleTypeReference) ot;
			long pos = (((long)pref.sourceStart) << 32) | pref.sourceEnd;
			ot = new SingleTypeReference(pref.token,pos);
		} else if (ot instanceof ParameterizedQualifiedTypeReference) {
			ParameterizedQualifiedTypeReference pref = (ParameterizedQualifiedTypeReference) ot;
			long pos = (((long)pref.sourceStart) << 32) | pref.sourceEnd;
			ot = new QualifiedTypeReference(pref.tokens,new long[]{pos});//SingleTypeReference(pref.Quatoken,pos);
		}
		
		// resolve it
		if (rb==null) {
		  rb = (ReferenceBinding)ot.getTypeBindingPublic(scope.parent);
		}
		
		// pr203646 - if we have ended up with the raw type, get back to the underlying generic one.
		if (rb.isRawType() && rb.isMemberType()) {
			// if the real target type used a type variable alias then we can do this OK, but need to switch things around, we want the generic type
			rb = ((RawTypeBinding)rb).type;
		}
		
		if (rb instanceof TypeVariableBinding) {
			scope.problemReporter().signalError(sourceStart,sourceEnd,
					  "Cannot make inter-type declarations on type variables, use an interface and declare parents");
			// to prevent disgusting cascading errors after this problem - lets null out what leads to them (pr105038)
			this.arguments=null;
			this.returnType=new SingleTypeReference(TypeReference.VOID,0L);
			
			this.ignoreFurtherInvestigation=true;
			ReferenceBinding closestMatch = null;
			if (((TypeVariableBinding)rb).firstBound!=null) {
				closestMatch = ((TypeVariableBinding)rb).firstBound.enclosingType();
			}
			rb = new ProblemReferenceBinding(rb.compoundName,closestMatch,0);
		}

		
		// if resolution failed, give up - someone else is going to report an error
		if (rb instanceof ProblemReferenceBinding) return;
		
		interTypeScope = new InterTypeScope(scope.parent, rb, typeVariableAliases);
		// FIXME asc verify the choice of lines here...
		// Two versions of this next line.  
		// First one tricks the JDT variable processing code so that it won't complain if
		// you refer to a type variable from a static ITD - it *is* a problem and it *will* be caught, but later and 
		// by the AJDT code so we can put out a much nicer message.
		scope.isStatic = (typeVariableAliases!=null?false:Modifier.isStatic(declaredModifiers));
		// this is the original version in case tricking the JDT causes grief (if you reinstate this variant, you
		// will need to change the expected messages output for some of the generic ITD tests)
		// scope.isStatic = Modifier.isStatic(declaredModifiers);
		scope.parent = interTypeScope;
	    scopeSetup = true;
	}
}
