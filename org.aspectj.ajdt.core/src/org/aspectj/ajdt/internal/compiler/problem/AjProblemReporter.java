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

 
 package org.aspectj.ajdt.internal.compiler.problem;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.Proceed;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.InterTypeMethodBinding;
import org.aspectj.ajdt.internal.compiler.lookup.PrivilegedFieldBinding;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.core.compiler.IProblem;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.aspectj.org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.DeclareSoft;
import org.aspectj.weaver.patterns.TypePattern;

/**
 * Extends problem reporter to support compiler-side implementation of declare soft. 
 * Also overrides error reporting for the need to implement abstract methods to
 * account for inter-type declarations and pointcut declarations.  This second
 * job might be better done directly in the SourceTypeBinding/ClassScope classes.
 * 
 * @author Jim Hugunin
 */
public class AjProblemReporter extends ProblemReporter {
    
	private static final boolean DUMP_STACK = false;
	
	public EclipseFactory factory;

	public AjProblemReporter(
		IErrorHandlingPolicy policy,
		CompilerOptions options,
		IProblemFactory problemFactory) {
		super(policy, options, problemFactory);
	}
	
	

	public void unhandledException(
		TypeBinding exceptionType,
		ASTNode location)
	{
		if (!factory.getWorld().getDeclareSoft().isEmpty()) {
			Shadow callSite = factory.makeShadow(location, referenceContext);
			Shadow enclosingExec = factory.makeShadow(referenceContext);
			// PR 72157 - calls to super / this within a constructor are not part of the cons join point.
			if ((callSite == null) && (enclosingExec.getKind() == Shadow.ConstructorExecution)
			        && (location instanceof ExplicitConstructorCall)) {
				super.unhandledException(exceptionType, location);
				return;
			}
//			System.err.println("about to show error for unhandled exception: "  + new String(exceptionType.sourceName()) + 
//					" at " + location + " in " + referenceContext);		
			
			for (Iterator i = factory.getWorld().getDeclareSoft().iterator(); i.hasNext(); ) {
				DeclareSoft d = (DeclareSoft)i.next();
				// We need the exceptionType to match the type in the declare soft statement
				// This means it must either be the same type or a subtype
				ResolvedType throwException = factory.fromEclipse((ReferenceBinding)exceptionType);
				FuzzyBoolean isExceptionTypeOrSubtype = 
					d.getException().matchesInstanceof(throwException);
				if (!isExceptionTypeOrSubtype.alwaysTrue() ) continue;

				if (callSite != null) {
					FuzzyBoolean match = d.getPointcut().match(callSite);
					if (match.alwaysTrue()) {
						//System.err.println("matched callSite: "  + callSite + " with " + d);
						return;
					} else if (!match.alwaysFalse()) {
						//!!! need this check to happen much sooner
						//throw new RuntimeException("unimplemented, shouldn't have fuzzy match here");
					}
				}
				if (enclosingExec != null) {
					FuzzyBoolean match = d.getPointcut().match(enclosingExec);
					if (match.alwaysTrue()) {
						//System.err.println("matched enclosingExec: "  + enclosingExec + " with " + d);
						return;
					} else if (!match.alwaysFalse()) {
						//!!! need this check to happen much sooner
						//throw new RuntimeException("unimplemented, shouldn't have fuzzy match here");
					}
				}
			}
		}
		
		//??? is this always correct
		if (location instanceof Proceed) {
			return;
		}

		super.unhandledException(exceptionType, location);
	}

	private boolean isPointcutDeclaration(MethodBinding binding) {
		return CharOperation.prefixEquals(PointcutDeclaration.mangledPrefix, binding.selector);
	}
	
    private boolean isIntertypeDeclaration(MethodBinding binding) {
    	return (binding instanceof InterTypeMethodBinding);
    }
    
	public void abstractMethodCannotBeOverridden(
		SourceTypeBinding type,
		MethodBinding concreteMethod)
	{
		if (isPointcutDeclaration(concreteMethod)) {
			return;
		}
		super.abstractMethodCannotBeOverridden(type, concreteMethod);
	}


	public void inheritedMethodReducesVisibility(SourceTypeBinding type, MethodBinding concreteMethod, MethodBinding[] abstractMethods) {
		// if we implemented this method by a public inter-type declaration, then there is no error
		
		ResolvedType onTypeX = null;		
		// If the type is anonymous, look at its supertype
		if (!type.isAnonymousType()) {
			onTypeX = factory.fromEclipse(type);
		} else {
			// Hmmm. If the ITD is on an interface that is being 'instantiated' using an anonymous type,
			// we sort it out elsewhere and don't come into this method - 
			// so we don't have to worry about interfaces, just the superclass.
		    onTypeX = factory.fromEclipse(type.superclass()); //abstractMethod.declaringClass);
		}
		for (Iterator i = onTypeX.getInterTypeMungersIncludingSupers().iterator(); i.hasNext(); ) {
			ConcreteTypeMunger m = (ConcreteTypeMunger)i.next();
			ResolvedMember sig = m.getSignature();
            if (!Modifier.isAbstract(sig.getModifiers())) {
				if (ResolvedType
					.matches(
						AjcMemberMaker.interMethod(
							sig,
							m.getAspectType(),
							sig.getDeclaringType().resolve(factory.getWorld()).isInterface()),
						factory.makeResolvedMember(concreteMethod))) {
					return;
				}
			}
		}

		super.inheritedMethodReducesVisibility(type,concreteMethod,abstractMethods);
	}

	// if either of the MethodBinding is an ITD, we have already reported it.
	public void staticAndInstanceConflict(MethodBinding currentMethod, MethodBinding inheritedMethod) {
		if (currentMethod instanceof InterTypeMethodBinding) return;
		if (inheritedMethod instanceof InterTypeMethodBinding) return;
		super.staticAndInstanceConflict(currentMethod, inheritedMethod);
	}
	
	public void abstractMethodMustBeImplemented(
		SourceTypeBinding type,
		MethodBinding abstractMethod)
	{
		// if this is a PointcutDeclaration then there is no error
		if (isPointcutDeclaration(abstractMethod)) return;
		
		if (isIntertypeDeclaration(abstractMethod)) return; // when there is a problem with an ITD not being implemented, it will be reported elsewhere
		
		if (CharOperation.prefixEquals("ajc$interField".toCharArray(), abstractMethod.selector)) {
			//??? think through how this could go wrong
			return;
		}
		
		// if we implemented this method by an inter-type declaration, then there is no error
		//??? be sure this is always right
		ResolvedType onTypeX = null;
		
		// If the type is anonymous, look at its supertype
		if (!type.isAnonymousType()) {
			onTypeX = factory.fromEclipse(type);
		} else {
			// Hmmm. If the ITD is on an interface that is being 'instantiated' using an anonymous type,
			// we sort it out elsewhere and don't come into this method - 
			// so we don't have to worry about interfaces, just the superclass.
		    onTypeX = factory.fromEclipse(type.superclass()); //abstractMethod.declaringClass);
		}
		
		if (onTypeX.isRawType()) onTypeX = onTypeX.getGenericType();

		for (Iterator i = onTypeX.getInterTypeMungersIncludingSupers().iterator(); i.hasNext(); ) {
			ConcreteTypeMunger m = (ConcreteTypeMunger)i.next();
			ResolvedMember sig = m.getSignature();
            if (!Modifier.isAbstract(sig.getModifiers())) {
    			ResolvedMember abstractMember = factory.makeResolvedMember(abstractMethod);
    			if (abstractMember.getName().startsWith("ajc$interMethodDispatch")) {			
	  				if (ResolvedType
						.matches(
							AjcMemberMaker.interMethod(
								sig,
								m.getAspectType(),
								sig.getDeclaringType().resolve(factory.getWorld()).isInterface()),
								abstractMember
							)) {
						return;
					}
    			} else {
    				// In this case we have something like:
    				//  interface I {}
    				//  abstract class C implements I { abstract void foo();}
    				//  class D extends C {}
    				//  ITD: public void I.foo() {...}
    				// The ITD is providing the implementation of foo in the class D but when checking for whether the abstract
    				// method is overridden, we won't be looking at whether the ITD overrides ajc$interMethodDispath$...foo but
    				// whether it overrides the foo method from class C
                	if (ResolvedType.matches(sig,factory.makeResolvedMember(abstractMethod))) return;
    			}
			}
		}

		super.abstractMethodMustBeImplemented(type, abstractMethod);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.org.eclipse.jdt.internal.compiler.problem.ProblemReporter#disallowedTargetForAnnotation(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation)
	 */
	public void disallowedTargetForAnnotation(Annotation annotation) {
		// if the annotation's recipient is an ITD, it might be allowed after all...
		if (annotation.recipient instanceof MethodBinding) {
			MethodBinding binding = (MethodBinding) annotation.recipient;
			String name = new String(binding.selector);
			if (name.startsWith("ajc$")) {
				long metaTagBits = annotation.resolvedType.getAnnotationTagBits(); // could be forward reference
				if (name.indexOf("interField") != -1) {
					if ((metaTagBits & TagBits.AnnotationForField) != 0) return;
				} else if (name.indexOf("interConstructor") != -1) { 
					if ((metaTagBits & TagBits.AnnotationForConstructor) != 0) return;
				} else if (name.indexOf("interMethod") != -1) {
					if ((metaTagBits & TagBits.AnnotationForMethod) != 0) return;
				} else if (name.indexOf("declare_"+DeclareAnnotation.AT_TYPE+"_")!=-1) {
					if ((metaTagBits & TagBits.AnnotationForAnnotationType)!=0 ||
						(metaTagBits & TagBits.AnnotationForType)!=0) return;
				} else if (name.indexOf("declare_"+DeclareAnnotation.AT_FIELD+"_")!=-1) {
					if ((metaTagBits & TagBits.AnnotationForField)!=0) return;
				} else if (name.indexOf("declare_"+DeclareAnnotation.AT_CONSTRUCTOR+"_")!=-1) {
					if ((metaTagBits & TagBits.AnnotationForConstructor)!=0) return;
				} else if (name.indexOf("declare_eow") != -1) {
					if ((metaTagBits & TagBits.AnnotationForField) != 0) return;
				}
			}
		}
		
		// not our special case, report the problem...
		super.disallowedTargetForAnnotation(annotation);
	}
	
	public void overridesPackageDefaultMethod(MethodBinding localMethod, MethodBinding inheritedMethod) {
	  if (new String(localMethod.selector).startsWith("ajc$")) return;
	  super.overridesPackageDefaultMethod(localMethod,inheritedMethod);
	}
	
	public void handle(
		int problemId,
		String[] problemArguments,
		String[] messageArguments,
		int severity,
		int problemStartPosition,
		int problemEndPosition,
		ReferenceContext referenceContext,
		CompilationResult unitResult)
	{
		if (severity != ProblemSeverities.Ignore && DUMP_STACK) {
			Thread.dumpStack();
		}
		super.handle(
			problemId,
			problemArguments,
			messageArguments,
			severity,
			problemStartPosition,
			problemEndPosition,
			referenceContext,
			unitResult);
	}
    


    // PR71076
    public void javadocMissingParamTag(char[] name, int sourceStart, int sourceEnd, int modifiers) {
        boolean reportIt = true;
        String sName = new String(name);
        if (sName.startsWith("ajc$")) reportIt = false;
        if (sName.equals("thisJoinPoint")) reportIt = false;
        if (sName.equals("thisJoinPointStaticPart")) reportIt = false;
        if (sName.equals("thisEnclosingJoinPointStaticPart")) reportIt = false;
        if (sName.equals("ajc_aroundClosure")) reportIt = false;
        if (reportIt) 
        	super.javadocMissingParamTag(name,sourceStart,sourceEnd,modifiers);
    }
    
    public void abstractMethodInAbstractClass(SourceTypeBinding type, AbstractMethodDeclaration methodDecl) {

    	String abstractMethodName = new String(methodDecl.selector);
    	if (abstractMethodName.startsWith("ajc$pointcut")) {
    		// This will already have been reported, see: PointcutDeclaration.postParse()
    		return;
    	}
    	String[] arguments = new String[] {new String(type.sourceName()), abstractMethodName};
    	super.handle(
    		IProblem.AbstractMethodInAbstractClass,
    		arguments,
    		arguments,
    		methodDecl.sourceStart,
    		methodDecl.sourceEnd,this.referenceContext, 
			this.referenceContext == null ? null : this.referenceContext.compilationResult());
    }
    
    
    /**
     * Called when there is an ITD marked @override that doesn't override a supertypes method.
     * The method and the binding are passed - some information is useful from each.  The 'method'
     * knows about source offsets for the message, the 'binding' has the signature of what the
     * ITD is trying to be in the target class.
     */
    public void itdMethodMustOverride(AbstractMethodDeclaration method,MethodBinding binding) {
		this.handle(
				IProblem.MethodMustOverride,
				new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, false), new String(binding.declaringClass.readableName()), },
				new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, true), new String(binding.declaringClass.shortReadableName()),},
				method.sourceStart,
				method.sourceEnd,
				this.referenceContext, 
				this.referenceContext == null ? null : this.referenceContext.compilationResult());
	}
    
    /**
     * Overrides the implementation in ProblemReporter and is ITD aware.
     * To report a *real* problem with an ITD marked @override, the other methodMustOverride() method is used.
     */
    public void methodMustOverride(AbstractMethodDeclaration method) {
    	MethodBinding binding = method.binding;
    	
    	// ignore ajc$ methods
    	if (new String(method.selector).startsWith("ajc$")) return;
		ResolvedMember possiblyErroneousRm = factory.makeResolvedMember(method.binding);
    	
    	ResolvedType onTypeX =  factory.fromEclipse(method.binding.declaringClass);
    	// Can't use 'getInterTypeMungersIncludingSupers()' since that will exclude abstract ITDs
    	// on any super classes - so we have to trawl up ourselves.. I wonder if this problem
    	// affects other code in the problem reporter that looks through ITDs...
    	ResolvedType supertypeToLookAt = onTypeX.getSuperclass();
    	while (supertypeToLookAt!=null) {
    		List itMungers = supertypeToLookAt.getInterTypeMungers();
	    	for (Iterator i = itMungers.iterator(); i.hasNext(); ) {
				ConcreteTypeMunger m = (ConcreteTypeMunger)i.next();
				ResolvedMember sig = m.getSignature();
				if (sig==null) continue; // we aren't interested in other kinds of munger
				UnresolvedType dType = sig.getDeclaringType();
				if (dType==null) continue;
				ResolvedType resolvedDeclaringType = dType.resolve(factory.getWorld());
				ResolvedMember rm = AjcMemberMaker.interMethod(sig,m.getAspectType(),
						resolvedDeclaringType.isInterface());
				if (ResolvedType.matches(rm,possiblyErroneousRm)) {
					// match, so dont need to report a problem!
					return;
				}
			}
	    	supertypeToLookAt = supertypeToLookAt.getSuperclass();
    	}
    	// report the error...
    	super.methodMustOverride(method);
    }
    
    
    private String typesAsString(boolean isVarargs, TypeBinding[] types, boolean makeShort) {
    	StringBuffer buffer = new StringBuffer(10);
    	for (int i = 0, length = types.length; i < length; i++) {
    		if (i != 0)
    			buffer.append(", "); //$NON-NLS-1$
    		TypeBinding type = types[i];
    		boolean isVarargType = isVarargs && i == length-1;
    		if (isVarargType) type = ((ArrayBinding)type).elementsType();
    		buffer.append(new String(makeShort ? type.shortReadableName() : type.readableName()));
    		if (isVarargType) buffer.append("..."); //$NON-NLS-1$
    	}
    	return buffer.toString();
    }
    
    public void visibilityConflict(MethodBinding currentMethod, MethodBinding inheritedMethod) {
    	// Not quite sure if the conditions on this test are right - basically I'm saying
    	// DONT WORRY if its ITDs since the error will be reported another way...
    	if (isIntertypeDeclaration(currentMethod) && 
    		isIntertypeDeclaration(inheritedMethod) && 
    		Modifier.isPrivate(currentMethod.modifiers) && 
    		Modifier.isPrivate(inheritedMethod.modifiers)) {
    		return;
    	}
    	super.visibilityConflict(currentMethod,inheritedMethod);
    }
    
    public void unusedPrivateType(TypeDeclaration typeDecl) {
    	// don't output unused type warnings for aspects!
    	if (typeDecl instanceof AspectDeclaration) return;
    	if (typeDecl.enclosingType!=null && (typeDecl.enclosingType instanceof AspectDeclaration)) {
    		AspectDeclaration ad = (AspectDeclaration)typeDecl.enclosingType;
    		if (ad.concreteName!=null) {
    			List declares = ad.concreteName.declares;
    			for (Iterator iter = declares.iterator(); iter.hasNext();) {
					Object dec = (Object) iter.next();
					if (dec instanceof DeclareParents) {
						DeclareParents decp = (DeclareParents)dec;
						TypePattern[] newparents = decp.getParents().getTypePatterns();
						for (int i = 0; i < newparents.length; i++) {
							TypePattern pattern = newparents[i];
							UnresolvedType ut = pattern.getExactType();
							if (ut==null) continue;
							if (CharOperation.compareWith(typeDecl.binding.signature(),ut.getSignature().toCharArray())==0) return;
						}
					}
				}
    		}
    	}
    	super.unusedPrivateType(typeDecl);
    }

    public void unusedPrivateMethod(AbstractMethodDeclaration methodDecl) {
    	// don't output unused warnings for pointcuts...
    	if (!(methodDecl instanceof PointcutDeclaration))
    			super.unusedPrivateMethod(methodDecl);
    }
    
    public void caseExpressionMustBeConstant(Expression expression) {
    	if (expression instanceof QualifiedNameReference) {
    		QualifiedNameReference qnr = (QualifiedNameReference)expression;
    		if (qnr.otherBindings!=null && qnr.otherBindings.length>0 && qnr.otherBindings[0] instanceof PrivilegedFieldBinding) {
    			super.signalError(expression.sourceStart,expression.sourceEnd,"Fields accessible due to an aspect being privileged can not be used in switch statements");
    			referenceContext.tagAsHavingErrors();
    			return;
    		}
    	}
    	super.caseExpressionMustBeConstant(expression);
    }
    
    public void unusedArgument(LocalDeclaration localDecl) {
    	// don't warn if this is an aj synthetic arg
    	String argType = new String(localDecl.type.resolvedType.signature());
    	if (argType.startsWith("Lorg/aspectj/runtime/internal")) return;
    	
    	// If the unused argument is in a pointcut, don't report the problem (for now... pr148219)
    	if (localDecl!=null && localDecl instanceof Argument) {
    		Argument arg = (Argument)localDecl;
    		if (arg.binding!=null && arg.binding.declaringScope!=null) {
    			ReferenceContext context = arg.binding.declaringScope.referenceContext();
    			if (context!=null && context instanceof PointcutDeclaration) return;
    		}
    	}
    	super.unusedArgument(localDecl);
    }

    /**
     * A side-effect of the way that we handle itds on default methods on top-most implementors
     * of interfaces is that a class acquiring a final default ITD will erroneously report
     * that it can't override its own member. This method detects that situation.
     */
    public void finalMethodCannotBeOverridden(MethodBinding currentMethod, MethodBinding inheritedMethod) {
    	if (currentMethod == inheritedMethod) return;
    	super.finalMethodCannotBeOverridden(currentMethod, inheritedMethod);
    }
    
    /**
     * The method verifier is a bit 'keen' and doesn't cope well with ITDMs which are
     * of course to be considered a 'default' implementation if the target type doesn't
     * supply one.  This test may not be complete - it is possible that it should read if
     * *either* is an ITD...but I dont have a testcase that shows that is required. yet.
     * (pr115788)
     */
    public void duplicateInheritedMethods(SourceTypeBinding type, MethodBinding inheritedMethod1, MethodBinding inheritedMethod2) {
    	if (!(inheritedMethod1 instanceof InterTypeMethodBinding && 
    	      inheritedMethod2 instanceof InterTypeMethodBinding)) 
    		super.duplicateInheritedMethods(type,inheritedMethod1,inheritedMethod2);
    }
    
    /**
     * All problems end up routed through here at some point...
     */
    public IProblem createProblem(char[] fileName, int problemId, String[] problemArguments, String[] messageArguments, int severity, int problemStartPosition, int problemEndPosition, int lineNumber) {
    	IProblem problem = super.createProblem(fileName, problemId, problemArguments,
    			messageArguments, severity, problemStartPosition, problemEndPosition,
    			lineNumber,0);
    	if (factory.getWorld().isInPinpointMode()) {
    		MessageIssued ex = new MessageIssued();
    		ex.fillInStackTrace();
    		StringWriter sw = new StringWriter();
    		ex.printStackTrace(new PrintWriter(sw));
    		StringBuffer sb = new StringBuffer();
    		sb.append(CompilationAndWeavingContext.getCurrentContext());
    		sb.append(sw.toString());
    		problem = new PinpointedProblem(problem,sb.toString());
    	}
    	return problem;
    }
    
    private static class MessageIssued extends RuntimeException {
    	public String getMessage() {
    		return "message issued...";
    	}
    }
    
    private static class PinpointedProblem implements IProblem {
    	
    	private IProblem delegate;
    	private String message;
    	
    	public PinpointedProblem(IProblem aProblem, String pinpoint) { 
    		this.delegate = aProblem; 
    		// if this was a problem that came via the weaver, it will already have
    		// pinpoint info, don't do it twice...
    		if (delegate.getMessage().indexOf("message issued...") == -1) {
    			this.message = delegate.getMessage() + "\n" + pinpoint;
    		} else {
    			this.message = delegate.getMessage();
    		}
    	}

		public String[] getArguments() {return delegate.getArguments();}
		public int getID() {return delegate.getID();}
		public String getMessage() { return message; }
		public char[] getOriginatingFileName() {return delegate.getOriginatingFileName();}
		public int getSourceEnd() { return delegate.getSourceEnd();}
		public int getSourceLineNumber() { return delegate.getSourceLineNumber();}
		public int getSourceStart() { return delegate.getSourceStart();}
		public boolean isError() { return delegate.isError();}
		public boolean isWarning() { return delegate.isWarning();}
		public void setSourceEnd(int sourceEnd) { delegate.setSourceEnd(sourceEnd); }
		public void setSourceLineNumber(int lineNumber) { delegate.setSourceLineNumber(lineNumber);}
		public void setSourceStart(int sourceStart) { delegate.setSourceStart(sourceStart);}
		public void setSeeAlsoProblems(IProblem[] problems) { delegate.setSeeAlsoProblems(problems);}
		public IProblem[] seeAlso() { return delegate.seeAlso();}
		public void setSupplementaryMessageInfo(String msg) { delegate.setSupplementaryMessageInfo(msg);}
		public String getSupplementaryMessageInfo() { return delegate.getSupplementaryMessageInfo();}
    }
}
