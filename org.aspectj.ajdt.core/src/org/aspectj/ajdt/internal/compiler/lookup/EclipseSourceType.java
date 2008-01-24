/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     PARC                 initial implementation
 *     Alexandre Vasseur    support for @AJ perClause
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.DeclareAnnotationDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.DeclareDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.ajdt.internal.core.builder.EclipseSourceContext;
import org.aspectj.bridge.IMessage;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.Constant;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.weaver.AbstractReferenceTypeDelegate;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.AnnotationNameValuePair;
import org.aspectj.weaver.AnnotationTargetKind;
import org.aspectj.weaver.AnnotationValue;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.ArrayAnnotationValue;
import org.aspectj.weaver.EnumAnnotationValue;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.AtAjAttributes.LazyResolvedPointcutDefinition;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.ParserException;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerFromSuper;
import org.aspectj.weaver.patterns.PerSingleton;
import org.aspectj.weaver.patterns.Pointcut;

/**
 * Supports viewing eclipse TypeDeclarations/SourceTypeBindings as a ResolvedType
 * 
 * @author Jim Hugunin
 */
public class EclipseSourceType extends AbstractReferenceTypeDelegate {
	private static final char[] pointcutSig = "Lorg/aspectj/lang/annotation/Pointcut;".toCharArray();
    private static final char[] aspectSig = "Lorg/aspectj/lang/annotation/Aspect;".toCharArray();
	protected ResolvedPointcutDefinition[] declaredPointcuts = null;
	protected ResolvedMember[] declaredMethods = null;
	protected ResolvedMember[] declaredFields = null;
	
	public List declares = new ArrayList();
	public List typeMungers = new ArrayList();
	
	private EclipseFactory factory;
	
	private SourceTypeBinding binding;
	private TypeDeclaration declaration;
	private CompilationUnitDeclaration unit;
	private boolean annotationsResolved = false;
	private ResolvedType[] resolvedAnnotations = null;
		
	private boolean discoveredAnnotationTargetKinds = false;
	private AnnotationTargetKind[] annotationTargetKinds;
	private AnnotationX[] annotations = null;
	private final static ResolvedType[] NO_ANNOTATION_TYPES = new ResolvedType[0];
	private final static AnnotationX[] NO_ANNOTATIONS = new AnnotationX[0];
	
	protected EclipseFactory eclipseWorld() {
		return factory;
	}

	public EclipseSourceType(ReferenceType resolvedTypeX, EclipseFactory factory,
								SourceTypeBinding binding, TypeDeclaration declaration,
								CompilationUnitDeclaration unit)
	{
		super(resolvedTypeX, true);
		this.factory = factory;
		this.binding = binding;
		this.declaration = declaration;
		this.unit = unit;
		
		setSourceContext(new EclipseSourceContext(declaration.compilationResult));
		resolvedTypeX.setStartPos(declaration.sourceStart);
		resolvedTypeX.setEndPos(declaration.sourceEnd);
	}


	public boolean isAspect() {
		final boolean isCodeStyle = declaration instanceof AspectDeclaration;
        return isCodeStyle?isCodeStyle:isAnnotationStyleAspect();
	}
	
	public boolean isAnonymous() {
		return ((declaration.modifiers & (ASTNode.IsAnonymousType | ASTNode.IsLocalType)) != 0);
	}
	
	public boolean isNested() {
		if (declaration.binding!=null) return (declaration.binding.isMemberType());
		return ((declaration.modifiers & ASTNode.IsMemberType) != 0);
	}
	
	public ResolvedType getOuterClass() {
		if (declaration.enclosingType==null) return null;
		return eclipseWorld().fromEclipse(declaration.enclosingType.binding);
	}

    public boolean isAnnotationStyleAspect() {
        if (declaration.annotations == null) {
            return false;
        }
        ResolvedType[] annotations = getAnnotationTypes();
        for (int i = 0; i < annotations.length; i++) {
            if ("org.aspectj.lang.annotation.Aspect".equals(annotations[i].getName())) {
                return true;
            }
        }
        return false;
    }
    
    /** Returns "" if there is a problem */
    private String getPointcutStringFromAnnotationStylePointcut(AbstractMethodDeclaration amd) {
    	Annotation[] ans = amd.annotations;
	    if (ans == null) return "";
		for (int i = 0; i < ans.length; i++) {
			if (ans[i].resolvedType == null) continue; // XXX happens if we do this very early from buildInterTypeandPerClause
			                                           // may prevent us from resolving references made in @Pointcuts to
			                                           // an @Pointcut in a code-style aspect
			char[] sig = ans[i].resolvedType.signature();
			if (CharOperation.equals(pointcutSig,sig)) {
				if (ans[i].memberValuePairs().length==0) return ""; // empty pointcut expression
				StringLiteral sLit = ((StringLiteral)(ans[i].memberValuePairs()[0].value));
				return new String(sLit.source());
			}
		}
		return "";
    }

	private boolean isAnnotationStylePointcut(Annotation[] annotations) {
		if (annotations == null) return false;
		for (int i = 0; i < annotations.length; i++) {
			if (annotations[i].resolvedType == null) continue; // XXX happens if we do this very early from buildInterTypeandPerClause
			                                                                     // may prevent us from resolving references made in @Pointcuts to
			                                                                     // an @Pointcut in a code-style aspect
			char[] sig = annotations[i].resolvedType.signature();
			if (CharOperation.equals(pointcutSig,sig)) {
				return true;
			}
		}
		return false;
	}
	
	public WeaverStateInfo getWeaverState() {
		return null;
	}
	
	public ResolvedType getSuperclass() {
		if (binding.isInterface()) return getResolvedTypeX().getWorld().getCoreType(UnresolvedType.OBJECT);
		//XXX what about java.lang.Object
		return eclipseWorld().fromEclipse(binding.superclass());
	}
	
	public ResolvedType[] getDeclaredInterfaces() {
		return eclipseWorld().fromEclipse(binding.superInterfaces());
	}


	protected void fillDeclaredMembers() {
		List declaredPointcuts = new ArrayList();
		List declaredMethods = new ArrayList();
		List declaredFields = new ArrayList();
		
		binding.methods();  // the important side-effect of this call is to make sure bindings are completed
		AbstractMethodDeclaration[] methods = declaration.methods;
		if (methods != null) {
			for (int i=0, len=methods.length; i < len; i++) {
				AbstractMethodDeclaration amd = methods[i];
				if (amd == null || amd.ignoreFurtherInvestigation) continue;
				if (amd instanceof PointcutDeclaration) {
					PointcutDeclaration d = (PointcutDeclaration)amd;
					ResolvedPointcutDefinition df = d.makeResolvedPointcutDefinition(factory);
					declaredPointcuts.add(df);
				} else if (amd instanceof InterTypeDeclaration) {				
					// these are handled in a separate pass
					continue;
				} else if (amd instanceof DeclareDeclaration && 
				           !(amd instanceof DeclareAnnotationDeclaration)) { // surfaces the annotated ajc$ method
					// these are handled in a separate pass
					continue;
				} else if (amd instanceof AdviceDeclaration) {
					// these are ignored during compilation and only used during weaving
					continue;
				}  else if ((amd.annotations != null) && isAnnotationStylePointcut(amd.annotations)) {
					// consider pointcuts defined via annotations
					ResolvedPointcutDefinition df = makeResolvedPointcutDefinition(amd);
					if (df!=null) declaredPointcuts.add(df);
				} else {
					if (amd.binding == null || !amd.binding.isValidBinding()) continue;
					ResolvedMember member = factory.makeResolvedMember(amd.binding);
					if (unit != null) {
						member.setSourceContext(new EclipseSourceContext(unit.compilationResult,amd.binding.sourceStart()));
						member.setPosition(amd.binding.sourceStart(),amd.binding.sourceEnd());
					}
					declaredMethods.add(member);
				}
			}
		}

		FieldBinding[] fields = binding.fields();
		for (int i=0, len=fields.length; i < len; i++) {
			FieldBinding f = fields[i];
			declaredFields.add(factory.makeResolvedMember(f));
		}
			
		this.declaredPointcuts = (ResolvedPointcutDefinition[])
			declaredPointcuts.toArray(new ResolvedPointcutDefinition[declaredPointcuts.size()]);
		this.declaredMethods = (ResolvedMember[])
			declaredMethods.toArray(new ResolvedMember[declaredMethods.size()]);
		this.declaredFields = (ResolvedMember[])
			declaredFields.toArray(new ResolvedMember[declaredFields.size()]);
	}

	private ResolvedPointcutDefinition makeResolvedPointcutDefinition(AbstractMethodDeclaration md) {
		if (md.binding==null) return null; // there is another error that has caused this... pr138143

		EclipseSourceContext eSourceContext = new EclipseSourceContext(md.compilationResult);
		Pointcut pc = null;
		if (!md.isAbstract()) {
			String expression = getPointcutStringFromAnnotationStylePointcut(md);
			try { 
				pc = new PatternParser(expression,eSourceContext).parsePointcut();
			} catch (ParserException pe) { // error will be reported by other means...
				pc = Pointcut.makeMatchesNothing(Pointcut.SYMBOLIC);
			}
		}
		
		FormalBinding[] bindings = buildFormalAdviceBindingsFrom(md);
		
		ResolvedPointcutDefinition rpd = new LazyResolvedPointcutDefinition(
				factory.fromBinding(md.binding.declaringClass), 
	            md.modifiers, 
	            new String(md.selector),
				factory.fromBindings(md.binding.parameters),
				factory.fromBinding(md.binding.returnType),
				 pc,new EclipseScope(bindings,md.scope));

		rpd.setPosition(md.sourceStart, md.sourceEnd);
		rpd.setSourceContext(eSourceContext);
		return rpd;
	}

	private static final char[] joinPoint = "Lorg/aspectj/lang/JoinPoint;".toCharArray();
	private static final char[] joinPointStaticPart = "Lorg/aspectj/lang/JoinPoint$StaticPart;".toCharArray();
	private static final char[] joinPointEnclosingStaticPart = "Lorg/aspectj/lang/JoinPoint$EnclosingStaticPart;".toCharArray();
	private static final char[] proceedingJoinPoint = "Lorg/aspectj/lang/ProceedingJoinPoint;".toCharArray();

	private FormalBinding[] buildFormalAdviceBindingsFrom(AbstractMethodDeclaration mDecl) {
		if (mDecl.arguments == null) return new FormalBinding[0];
		if (mDecl.binding == null) return new FormalBinding[0];
		EclipseFactory factory = EclipseFactory.fromScopeLookupEnvironment(mDecl.scope);
		String extraArgName = null;//maybeGetExtraArgName();
		if (extraArgName == null) extraArgName = "";
		FormalBinding[] ret = new FormalBinding[mDecl.arguments.length];
		for (int i = 0; i < mDecl.arguments.length; i++) {
            Argument arg = mDecl.arguments[i];
            String name = new String(arg.name);
            TypeBinding argTypeBinding = mDecl.binding.parameters[i];
            UnresolvedType type = factory.fromBinding(argTypeBinding);
			if  (CharOperation.equals(joinPoint,argTypeBinding.signature()) ||
				 CharOperation.equals(joinPointStaticPart,argTypeBinding.signature()) ||
				 CharOperation.equals(joinPointEnclosingStaticPart,argTypeBinding.signature()) ||
				 CharOperation.equals(proceedingJoinPoint,argTypeBinding.signature()) ||
				 name.equals(extraArgName)) {
				ret[i] = new FormalBinding.ImplicitFormalBinding(type,name,i);
			} else {
	            ret[i] = new FormalBinding(type, name, i, arg.sourceStart, arg.sourceEnd, "unknown");						
			}
		}
		return ret;
	}
	
	/**
	 * This method may not return all fields, for example it may
	 * not include the ajc$initFailureCause or ajc$perSingletonInstance
	 * fields - see bug 129613
	 */
	public ResolvedMember[] getDeclaredFields() {
		if (declaredFields == null) fillDeclaredMembers();
		return declaredFields;
	}

	/**
	 * This method may not return all methods, for example it may
	 * not include clinit, aspectOf, hasAspect or ajc$postClinit 
	 * methods - see bug 129613
	 */
	public ResolvedMember[] getDeclaredMethods() {
		if (declaredMethods == null) fillDeclaredMembers();
		return declaredMethods;
	}

	public ResolvedMember[] getDeclaredPointcuts() {
		if (declaredPointcuts == null) fillDeclaredMembers();
		return declaredPointcuts;
	}

	
	public int getModifiers() {
		// only return the real Java modifiers, not the extra eclipse ones
		return binding.modifiers & ExtraCompilerModifiers.AccJustFlag;
	}
	
	public String toString() {
		return "EclipseSourceType(" + new String(binding.sourceName()) + ")";
	}


	//XXX make sure this is applied to classes and interfaces
	public void checkPointcutDeclarations() {
		ResolvedMember[] pointcuts = getDeclaredPointcuts();
		boolean sawError = false;
		for (int i=0, len=pointcuts.length; i < len; i++) {
			if (pointcuts[i].isAbstract()) {
				if (!this.isAspect()) {
					eclipseWorld().showMessage(IMessage.ERROR,
						"abstract pointcut only allowed in aspect" + pointcuts[i].getName(),
						pointcuts[i].getSourceLocation(), null);
					sawError = true;
				} else if (!binding.isAbstract()) {
					eclipseWorld().showMessage(IMessage.ERROR,
						"abstract pointcut in concrete aspect" + pointcuts[i],
						pointcuts[i].getSourceLocation(), null);
					sawError = true;
				}
			}
				
			for (int j=i+1; j < len; j++) {
				if (pointcuts[i].getName().equals(pointcuts[j].getName())) {
					eclipseWorld().showMessage(IMessage.ERROR,
						"duplicate pointcut name: " + pointcuts[j].getName(),
						pointcuts[i].getSourceLocation(), pointcuts[j].getSourceLocation());
					sawError = true;
				}
			}
		}
		
		//now check all inherited pointcuts to be sure that they're handled reasonably
		if (sawError || !isAspect()) return;
		

		
		// find all pointcuts that override ones from super and check override is legal
		//    i.e. same signatures and greater or equal visibility
		// find all inherited abstract pointcuts and make sure they're concretized if I'm concrete
		// find all inherited pointcuts and make sure they don't conflict
		getResolvedTypeX().getExposedPointcuts();  //??? this is an odd construction

	}
	
	//???
//	public CrosscuttingMembers collectCrosscuttingMembers() {
//		return crosscuttingMembers;
//	}

//	public ISourceLocation getSourceLocation() {
//		TypeDeclaration dec = binding.scope.referenceContext;
//		return new EclipseSourceLocation(dec.compilationResult, dec.sourceStart, dec.sourceEnd);
//	}

	public boolean isInterface() {
		return binding.isInterface();
	}

	// XXXAJ5: Should be constants in the eclipse compiler somewhere, once it supports 1.5
	public final static short ACC_ANNOTATION   = 0x2000;
	public final static short ACC_ENUM         = 0x4000;
	
	public boolean isEnum() {
		return (binding.getAccessFlags() & ACC_ENUM)!=0;
	}
	
	public boolean isAnnotation() {
		return (binding.getAccessFlags() & ACC_ANNOTATION)!=0;
	}
	
	public void addAnnotation(AnnotationX annotationX) {
		// XXX Big hole here - annotationX holds a BCEL annotation but
		// we need an Eclipse one here, we haven't written the conversion utils
		// yet.  Not sure if this method will be called in practice...
		throw new RuntimeException("EclipseSourceType.addAnnotation() not implemented");
	}
	
	public boolean isAnnotationWithRuntimeRetention() {
	    if (!isAnnotation()) {
	        return false;
	    } else {
		   return (binding.getAnnotationTagBits() & TagBits.AnnotationRetentionMASK) == TagBits.AnnotationRuntimeRetention;        
	    }
	}
	
	public String getRetentionPolicy() {
		if (isAnnotation()) {
			if ((binding.getAnnotationTagBits() & TagBits.AnnotationRetentionMASK) == TagBits.AnnotationRuntimeRetention) return "RUNTIME";
			if ((binding.getAnnotationTagBits() & TagBits.AnnotationRetentionMASK) == TagBits.AnnotationSourceRetention) return "SOURCE";
			if ((binding.getAnnotationTagBits() & TagBits.AnnotationRetentionMASK) == TagBits.AnnotationClassRetention) return "CLASS";
		}
		return null;
	}
	
	public boolean canAnnotationTargetType() {
		if (isAnnotation()) {
			return ((binding.getAnnotationTagBits() & TagBits.AnnotationForType) != 0 );
		}
		return false;
	}
	
	public AnnotationTargetKind[] getAnnotationTargetKinds() {
		if (discoveredAnnotationTargetKinds) return annotationTargetKinds;
		discoveredAnnotationTargetKinds = true;
		annotationTargetKinds = null; // null means we have no idea or the @Target annotation hasn't been used
//		if (isAnnotation()) {
//	        Annotation[] annotationsOnThisType = declaration.annotations;
//	        if (annotationsOnThisType != null) {
//		        for (int i = 0; i < annotationsOnThisType.length; i++) {
//		            Annotation a = annotationsOnThisType[i];
//		            if (a.resolvedType != null) {
//		            	String packageName = new String(a.resolvedType.qualifiedPackageName()).concat(".");
//			            String sourceName = new String(a.resolvedType.qualifiedSourceName());
//			            if ((packageName + sourceName).equals(UnresolvedType.AT_TARGET.getName())) {
//			            	MemberValuePair[] pairs = a.memberValuePairs();
//			            	for (int j = 0; j < pairs.length; j++) {
//								MemberValuePair pair = pairs[j];
//								targetKind = pair.value.toString();
//								return targetKind;
//							}
//			            }
//					}
//				}
//	        }
//		}
//	    return targetKind;
		if (isAnnotation()) {
			List targetKinds = new ArrayList();
			
			if ((binding.getAnnotationTagBits() & TagBits.AnnotationForAnnotationType) != 0) targetKinds.add(AnnotationTargetKind.ANNOTATION_TYPE);
			if ((binding.getAnnotationTagBits() & TagBits.AnnotationForConstructor)    != 0) targetKinds.add(AnnotationTargetKind.CONSTRUCTOR);
			if ((binding.getAnnotationTagBits() & TagBits.AnnotationForField)          != 0) targetKinds.add(AnnotationTargetKind.FIELD);
			if ((binding.getAnnotationTagBits() & TagBits.AnnotationForLocalVariable)  != 0) targetKinds.add(AnnotationTargetKind.LOCAL_VARIABLE);
			if ((binding.getAnnotationTagBits() & TagBits.AnnotationForMethod)         != 0) targetKinds.add(AnnotationTargetKind.METHOD);
			if ((binding.getAnnotationTagBits() & TagBits.AnnotationForPackage)        != 0) targetKinds.add(AnnotationTargetKind.PACKAGE);
			if ((binding.getAnnotationTagBits() & TagBits.AnnotationForParameter)      != 0) targetKinds.add(AnnotationTargetKind.PARAMETER);
			if ((binding.getAnnotationTagBits() & TagBits.AnnotationForType)           != 0) targetKinds.add(AnnotationTargetKind.TYPE);
			
			if (!targetKinds.isEmpty()) {
				annotationTargetKinds = new AnnotationTargetKind[targetKinds.size()];
				return (AnnotationTargetKind[]) targetKinds.toArray(annotationTargetKinds);	
			}
		}
		return annotationTargetKinds;
	}
	
	public boolean hasAnnotation(UnresolvedType ofType) {

		// Make sure they are resolved
		if (!annotationsResolved) {
			TypeDeclaration.resolveAnnotations(declaration.staticInitializerScope, declaration.annotations, binding);
			annotationsResolved = true;
		}
		Annotation[] as = declaration.annotations;
		if (as == null) return false;
		for (int i = 0; i < as.length; i++) {
			Annotation annotation = as[i];
			if (annotation.resolvedType == null) {
				// Something has gone wrong - probably we have a 1.4 rt.jar around
				// which will result in a separate error message.
				return false;
			}
			String tname = CharOperation.charToString(annotation.resolvedType.constantPoolName());
			if (UnresolvedType.forName(tname).equals(ofType)) {
				return true;
			}			
		}
		return false;
	}

	/**
	 * WARNING: This method does not have a complete implementation.  
	 * 
	 * The aim is that it converts Eclipse annotation objects to the AspectJ form of 
	 * annotations (the type AnnotationAJ).  The AnnotationX objects returned are wrappers 
	 * over either a Bcel annotation type or the AspectJ AnnotationAJ type.
	 * The minimal implementation provided here is for processing the RetentionPolicy and 
	 * Target annotation types - these are the only ones which the weaver will attempt to
	 * process from an EclipseSourceType.  
	 * 
	 * More notes:
	 * The pipeline has required us to implement this.  With the pipeline we can be weaving
	 * a type and asking questions of annotations before they have been turned into Bcel
	 * objects - ie. when they are still in EclipseSourceType form.  Without the pipeline we
	 * would have converted everything to Bcel objects before proceeding with weaving.
	 * Because the pipeline won't start weaving until all aspects have been compiled and 
	 * the fact that no AspectJ constructs match on the values within annotations, this code
	 * only needs to deal with converting system annotations that the weaver needs to process
	 * (RetentionPolicy, Target).
	 */
	public AnnotationX[] getAnnotations() {
		if (annotations!=null) return annotations; // only do this once
		getAnnotationTypes(); // forces resolution and sets resolvedAnnotations
		Annotation[] as = declaration.annotations;
		if (as==null || as.length==0) {
			annotations = NO_ANNOTATIONS;
		} else {
			annotations = new AnnotationX[as.length];
			for (int i = 0; i < as.length; i++) {
				annotations[i]=convertEclipseAnnotation(as[i],factory.getWorld());
			}
		}
		return annotations;
	}

	/** 
	 * Convert one eclipse annotation into an AnnotationX object containing an AnnotationAJ object.
	 * 
	 * This code and the helper methods used by it will go *BANG* if they encounter anything
	 * not currently supported - this is safer than limping along with a malformed annotation.  When
	 * the *BANG* is encountered the bug reporter should indicate the kind of annotation they
	 * were working with and this code can be enhanced to support it.
	 */
	public AnnotationX convertEclipseAnnotation(Annotation eclipseAnnotation,World w) {		
		// TODO if it is sourcevisible, we shouldn't let it through!!!!!!!!! testcase!
		ResolvedType annotationType = factory.fromTypeBindingToRTX(eclipseAnnotation.type.resolvedType);
		long bs = (eclipseAnnotation.bits & TagBits.AnnotationRetentionMASK);
		boolean isRuntimeVisible = (eclipseAnnotation.bits & TagBits.AnnotationRetentionMASK) == TagBits.AnnotationRuntimeRetention;
		AnnotationAJ annotationAJ = new AnnotationAJ(annotationType.getSignature(),isRuntimeVisible);
		generateAnnotation(eclipseAnnotation,annotationAJ);		
		return new AnnotationX(annotationAJ,w);
	}
	
	static class MissingImplementationException extends RuntimeException {
		MissingImplementationException(String reason) {
			super(reason);
		}
	}

	private void generateAnnotation(Annotation annotation,AnnotationAJ annotationAJ) {
		if (annotation instanceof NormalAnnotation) {
			NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
			MemberValuePair[] memberValuePairs = normalAnnotation.memberValuePairs;
			if (memberValuePairs != null) {
				int memberValuePairsLength = memberValuePairs.length;
				for (int i = 0; i < memberValuePairsLength; i++) {
					MemberValuePair memberValuePair = memberValuePairs[i];
					MethodBinding methodBinding = memberValuePair.binding;
					if (methodBinding == null) {
						// is this just a marker annotation?
						throw new MissingImplementationException(
								"Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation ["+annotation+"]");
					} else {
						AnnotationValue av = generateElementValue(memberValuePair.value, methodBinding.returnType);
						AnnotationNameValuePair anvp = new AnnotationNameValuePair(new String(memberValuePair.name),av);
						annotationAJ.addNameValuePair(anvp);
					}
				}
			} else {
				throw new MissingImplementationException(
				    "Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation ["+annotation+"]");
			}
		} else if (annotation instanceof SingleMemberAnnotation) {
			// this is a single member annotation (one member value)
			SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) annotation;
			MethodBinding methodBinding = singleMemberAnnotation.memberValuePairs()[0].binding;
			if (methodBinding == null) {
				throw new MissingImplementationException(
					    "Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation ["+annotation+"]");
			} else {
				AnnotationValue av = generateElementValue(singleMemberAnnotation.memberValue, methodBinding.returnType);
				annotationAJ.addNameValuePair(
						new AnnotationNameValuePair(new String(singleMemberAnnotation.memberValuePairs()[0].name),av));
			}
		} else {
			// this is a marker annotation (no member value pairs)
			throw new MissingImplementationException(
				    "Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation ["+annotation+"]");
		}
	}
	
	private AnnotationValue generateElementValue(Expression defaultValue,TypeBinding memberValuePairReturnType) {
		Constant constant = defaultValue.constant;
		TypeBinding defaultValueBinding = defaultValue.resolvedType;
		if (defaultValueBinding == null) {
			throw new MissingImplementationException(
				    "Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value ["+defaultValue+"]");
		} else {
			if (memberValuePairReturnType.isArrayType() && !defaultValueBinding.isArrayType()) {
				if (constant != null && constant != Constant.NotAConstant) {
					throw new MissingImplementationException(
						    "Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value ["+defaultValue+"]");
//					generateElementValue(attributeOffset, defaultValue, constant, memberValuePairReturnType.leafComponentType());
				} else {
					AnnotationValue av = generateElementValueForNonConstantExpression(defaultValue, defaultValueBinding);
					return new ArrayAnnotationValue(new AnnotationValue[]{av});
				}
			} else {
				if (constant != null && constant != Constant.NotAConstant) {
					throw new MissingImplementationException(
						    "Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value ["+defaultValue+"]");
//	   				generateElementValue(attributeOffset, defaultValue, constant, memberValuePairReturnType.leafComponentType());
				} else {
					AnnotationValue av = generateElementValueForNonConstantExpression(defaultValue, defaultValueBinding);
					return av;
				}
			}
		}
	}
	
	private AnnotationValue generateElementValueForNonConstantExpression(Expression defaultValue, TypeBinding defaultValueBinding) {
		if (defaultValueBinding != null) {
			if (defaultValueBinding.isEnum()) {
				FieldBinding fieldBinding = null;
				if (defaultValue instanceof QualifiedNameReference) {
					QualifiedNameReference nameReference = (QualifiedNameReference) defaultValue;
					fieldBinding = (FieldBinding) nameReference.binding;
				} else if (defaultValue instanceof SingleNameReference) {
					SingleNameReference nameReference = (SingleNameReference) defaultValue;
					fieldBinding = (FieldBinding) nameReference.binding;
				} else {
					throw new MissingImplementationException(
						    "Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value ["+defaultValue+"]");
				}
				if (fieldBinding != null) {
					String sig = new String(fieldBinding.type.signature());
					AnnotationValue enumValue = new EnumAnnotationValue(sig,new String(fieldBinding.name));
					return enumValue;
				}
				throw new MissingImplementationException(
					    "Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value ["+defaultValue+"]");
			} else if (defaultValueBinding.isAnnotationType()) {
				throw new MissingImplementationException(
					    "Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value ["+defaultValue+"]");
//				contents[contentsOffset++] = (byte) '@';
//				generateAnnotation((Annotation) defaultValue, attributeOffset);
			} else if (defaultValueBinding.isArrayType()) {
				// array type
				if (defaultValue instanceof ArrayInitializer) {
					ArrayInitializer arrayInitializer = (ArrayInitializer) defaultValue;
					int arrayLength = arrayInitializer.expressions != null ? arrayInitializer.expressions.length : 0;
					AnnotationValue[] values = new AnnotationValue[arrayLength];
					for (int i = 0; i < arrayLength; i++) {
						values[i] = generateElementValue(arrayInitializer.expressions[i], defaultValueBinding.leafComponentType());//, attributeOffset);
					}
					ArrayAnnotationValue aav = new ArrayAnnotationValue(values);
					return aav;
				} else {
					throw new MissingImplementationException(
						    "Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value ["+defaultValue+"]");
				}
			} else {
				// class type
				throw new MissingImplementationException(
					    "Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value ["+defaultValue+"]");
//				if (contentsOffset + 3 >= this.contents.length) {
//					resizeContents(3);
//				}
//				contents[contentsOffset++] = (byte) 'c';
//				if (defaultValue instanceof ClassLiteralAccess) {
//					ClassLiteralAccess classLiteralAccess = (ClassLiteralAccess) defaultValue;
//					final int classInfoIndex = constantPool.literalIndex(classLiteralAccess.targetType.signature());
//					contents[contentsOffset++] = (byte) (classInfoIndex >> 8);
//					contents[contentsOffset++] = (byte) classInfoIndex;
//				} else {
//					contentsOffset = attributeOffset;
//				}
			}
		} else {
			throw new MissingImplementationException(
				    "Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value ["+defaultValue+"]");
//			contentsOffset = attributeOffset;
		}
	}
	
	// ---------------------------------
	
	public ResolvedType[] getAnnotationTypes() {
		if (resolvedAnnotations!=null) return resolvedAnnotations;
		// Make sure they are resolved
		if (!annotationsResolved) {
			TypeDeclaration.resolveAnnotations(declaration.staticInitializerScope, declaration.annotations, binding);
			annotationsResolved = true;
		}
		
		if (declaration.annotations == null) {
			resolvedAnnotations = NO_ANNOTATION_TYPES;//new ResolvedType[0];
		} else {
			resolvedAnnotations = new ResolvedType[declaration.annotations.length];
			Annotation[] as = declaration.annotations;
			for (int i = 0; i < as.length; i++) {
				Annotation annotation = as[i];
				resolvedAnnotations[i] =factory.fromTypeBindingToRTX(annotation.type.resolvedType);
			}
		}
		return resolvedAnnotations;
	}

	public PerClause getPerClause() {
		//should probably be: ((AspectDeclaration)declaration).perClause;
		// but we don't need this level of detail, and working with real per clauses
		// at this stage of compilation is not worth the trouble
        if (!isAnnotationStyleAspect()) {
        	   if(declaration instanceof AspectDeclaration) {
        		   PerClause pc = ((AspectDeclaration)declaration).perClause;
        		   if (pc != null) return pc;
        	   }
            return new PerSingleton();
        } else {
            // for @Aspect, we do need the real kind though we don't need the real perClause
            // at least try to get the right perclause
        	PerClause pc = null;
        	if (declaration instanceof AspectDeclaration) 
    		   pc =  ((AspectDeclaration)declaration).perClause;
    		if (pc==null) {
              PerClause.Kind kind = getPerClauseForTypeDeclaration(declaration);
              //returning a perFromSuper is enough to get the correct kind.. (that's really a hack - AV)
              return new PerFromSuper(kind);
    		}
    		return pc;
        }
	}

    PerClause.Kind getPerClauseForTypeDeclaration(TypeDeclaration typeDeclaration) {
        Annotation[] annotations = typeDeclaration.annotations;
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            if (CharOperation.equals(aspectSig, annotation.resolvedType.signature())) {
                // found @Aspect(...)
                if (annotation.memberValuePairs() == null || annotation.memberValuePairs().length == 0) {
                    // it is an @Aspect or @Aspect()
                    // needs to use PerFromSuper if declaration extends a super aspect
                    PerClause.Kind kind = lookupPerClauseKind(typeDeclaration.binding.superclass);
                    // if no super aspect, we have a @Aspect() means singleton
                    if (kind == null) {
                        return PerClause.SINGLETON;
                    } else {
                        return kind;
                    }
                } else if (annotation instanceof SingleMemberAnnotation) {
                    // it is an @Aspect(...something...)
                    SingleMemberAnnotation theAnnotation = (SingleMemberAnnotation)annotation;
                    String clause = new String(((StringLiteral)theAnnotation.memberValue).source());//TODO cast safe ?
                    return determinePerClause(typeDeclaration, clause);
                } else if (annotation instanceof NormalAnnotation) { // this kind if it was added by the visitor !
                	 // it is an @Aspect(...something...)
                    NormalAnnotation theAnnotation = (NormalAnnotation)annotation;
                    if (theAnnotation.memberValuePairs==null || theAnnotation.memberValuePairs.length<1) return PerClause.SINGLETON;
                    String clause = new String(((StringLiteral)theAnnotation.memberValuePairs[0].value).source());//TODO cast safe ?
                    return determinePerClause(typeDeclaration, clause);
                } else {
                    eclipseWorld().showMessage(IMessage.ABORT,
                        "@Aspect annotation is expected to be SingleMemberAnnotation with 'String value()' as unique element",
                        new EclipseSourceLocation(typeDeclaration.compilationResult, typeDeclaration.sourceStart, typeDeclaration.sourceEnd), null);
                    return PerClause.SINGLETON;//fallback strategy just to avoid NPE
                }
            }
        }
        return null;//no @Aspect annotation at all (not as aspect)
    }

	private PerClause.Kind determinePerClause(TypeDeclaration typeDeclaration, String clause) {
		if (clause.startsWith("perthis(")) {
		    return PerClause.PEROBJECT;
		} else if (clause.startsWith("pertarget(")) {
		    return PerClause.PEROBJECT;
		} else if (clause.startsWith("percflow(")) {
		    return PerClause.PERCFLOW;
		} else if (clause.startsWith("percflowbelow(")) {
		    return PerClause.PERCFLOW;
		} else if (clause.startsWith("pertypewithin(")) {
		    return PerClause.PERTYPEWITHIN;
		} else if (clause.startsWith("issingleton(")) {
		    return PerClause.SINGLETON;
		} else {
		    eclipseWorld().showMessage(IMessage.ABORT,
		        "cannot determine perClause '" + clause + "'",
		        new EclipseSourceLocation(typeDeclaration.compilationResult, typeDeclaration.sourceStart, typeDeclaration.sourceEnd), null);
		    return PerClause.SINGLETON;//fallback strategy just to avoid NPE
		}
	}

    // adapted from AspectDeclaration
    private PerClause.Kind lookupPerClauseKind(ReferenceBinding binding) {
        final PerClause.Kind kind;
        if (binding instanceof BinaryTypeBinding) {
            ResolvedType superTypeX = factory.fromEclipse(binding);
            PerClause perClause = superTypeX.getPerClause();
            // clause is null for non aspect classes since coming from BCEL attributes
            if (perClause != null) {
                kind = superTypeX.getPerClause().getKind();
            } else {
                kind = null;
            }
        } else if (binding instanceof SourceTypeBinding ) {
            SourceTypeBinding sourceSc = (SourceTypeBinding)binding;
            if (sourceSc.scope.referenceContext instanceof AspectDeclaration) {
                //code style
                kind = ((AspectDeclaration)sourceSc.scope.referenceContext).perClause.getKind();
            } else if (sourceSc.scope.referenceContext instanceof TypeDeclaration) {
                // if @Aspect: perFromSuper, else if @Aspect(..) get from anno value, else null
                kind = getPerClauseForTypeDeclaration((TypeDeclaration)(sourceSc.scope.referenceContext));
            } else {
                //XXX should not happen
                kind = null;
            }
        } else {
            //XXX need to handle this too
            kind = null;
        }
        return kind;
    }


	public Collection getDeclares() {
		return declares;
	}

	public Collection getPrivilegedAccesses() {
		return Collections.EMPTY_LIST;
	}

	public Collection getTypeMungers() {
		return typeMungers;
	}

	public boolean doesNotExposeShadowMungers() {
		return true;
	}
	
	public String getDeclaredGenericSignature() {
		return CharOperation.charToString(binding.genericSignature());
	}
	
	public boolean isGeneric() {
		return binding.isGenericType();
	}
	
	public TypeVariable[] getTypeVariables() {
		if (declaration.typeParameters == null) return new TypeVariable[0];
		TypeVariable[] typeVariables = new TypeVariable[declaration.typeParameters.length];
		for (int i = 0; i < typeVariables.length; i++) {
			typeVariables[i] = typeParameter2TypeVariable(declaration.typeParameters[i]);
		}
		return typeVariables;
	}
	
	private TypeVariable typeParameter2TypeVariable(TypeParameter aTypeParameter) {
		String name = new String(aTypeParameter.name);
		ReferenceBinding superclassBinding = aTypeParameter.binding.superclass;
		UnresolvedType superclass = UnresolvedType.forSignature(new String(superclassBinding.signature()));
		UnresolvedType[] superinterfaces = null;
		ReferenceBinding[] superInterfaceBindings = aTypeParameter.binding.superInterfaces;
		if (superInterfaceBindings != null) {
			superinterfaces = new UnresolvedType[superInterfaceBindings.length];
			for (int i = 0; i < superInterfaceBindings.length; i++) {
				superinterfaces[i] = UnresolvedType.forSignature(new String(superInterfaceBindings[i].signature()));
			}
		}
		// XXX what about lower binding?
		TypeVariable tv = new TypeVariable(name,superclass,superinterfaces);
		tv.setDeclaringElement(factory.fromBinding(aTypeParameter.binding.declaringElement));
		tv.setRank(aTypeParameter.binding.rank);
		return tv;
	}

	public void ensureDelegateConsistent() {
		// do nothing, currently these can't become inconsistent (phew)
	}
	
}
