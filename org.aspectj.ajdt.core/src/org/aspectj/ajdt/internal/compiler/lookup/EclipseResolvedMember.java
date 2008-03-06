/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement                 initial implementation
 * ******************************************************************/
package org.aspectj.ajdt.internal.compiler.lookup;

import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

/**
 * In the pipeline world, we can be weaving before all types have come through from compilation.
 * In some cases this means the weaver will want to ask questions of eclipse types and this
 * subtype of ResolvedMemberImpl is here to answer some of those questions - it is backed by 
 * the real eclipse MethodBinding object and can translate from Eclipse -> Weaver information.
 */ 
public class EclipseResolvedMember extends ResolvedMemberImpl {
	
	private static String[] NO_ARGS = new String[]{};
	
	private Binding realBinding;
	private String[] argumentNames;
	private World w;
	private ResolvedType[] cachedAnnotationTypes;
	private EclipseFactory eclipseFactory;
	
	public EclipseResolvedMember(MethodBinding binding, Kind memberKind, ResolvedType realDeclaringType, int modifiers, UnresolvedType type, String string, UnresolvedType[] types, UnresolvedType[] types2, EclipseFactory eclipseFactory) {
		super(memberKind,realDeclaringType,modifiers,type,string,types,types2);
		this.realBinding = binding;
		this.eclipseFactory = eclipseFactory;
		this.w = realDeclaringType.getWorld();
	}

	public EclipseResolvedMember(FieldBinding binding, Kind field, ResolvedType realDeclaringType, int modifiers, ResolvedType type, String string, UnresolvedType[] none) {
		super(field,realDeclaringType,modifiers,type,string,none);
		this.realBinding = binding;
		this.w = realDeclaringType.getWorld();
	}
	

	public boolean hasAnnotation(UnresolvedType ofType) {
		ResolvedType[] annotationTypes = getAnnotationTypes();
		if (annotationTypes==null) return false;
		for (int i = 0; i < annotationTypes.length; i++) {
			ResolvedType type = annotationTypes[i];
			if (type.equals(ofType)) return true;
		}
		return false;
	}

	public AnnotationX[] getAnnotations() {
		long abits = realBinding.getAnnotationTagBits(); // ensure resolved
		Annotation[] annos = getEclipseAnnotations();
		if (annos==null) return null;
		// TODO errr missing in action - we need to implement this! Probably using something like EclipseAnnotationConvertor - itself not finished ;)
		throw new RuntimeException("not yet implemented - please raise an AJ bug");
//		return super.getAnnotations();
	}
	

	public AnnotationX getAnnotationOfType(UnresolvedType ofType) {
		long abits = realBinding.getAnnotationTagBits(); // ensure resolved
		Annotation[] annos = getEclipseAnnotations();
		if (annos==null) return null;
		for (int i = 0; i < annos.length; i++) {
			Annotation anno = annos[i];
			UnresolvedType ut = UnresolvedType.forSignature(new String(anno.resolvedType.signature()));
			if (w.resolve(ut).equals(ofType)) {
				// Found the one
				return EclipseAnnotationConvertor.convertEclipseAnnotation(anno,w,eclipseFactory);
			}
		}
		return null;
	}
	
	public String getAnnotationDefaultValue() {
		if (realBinding instanceof MethodBinding) {
			AbstractMethodDeclaration methodDecl = getTypeDeclaration().declarationOf((MethodBinding)realBinding);
			if (methodDecl instanceof AnnotationMethodDeclaration) {
				AnnotationMethodDeclaration annoMethodDecl = (AnnotationMethodDeclaration)methodDecl;
				Expression e = annoMethodDecl.defaultValue;
				if (e.resolvedType==null)
				e.resolve(methodDecl.scope);
				// TODO does not cope with many cases...
				if (e instanceof QualifiedNameReference) {
					
					QualifiedNameReference qnr = (QualifiedNameReference)e;
					if (qnr.binding instanceof FieldBinding) {
						FieldBinding fb = (FieldBinding)qnr.binding;
						StringBuffer sb = new StringBuffer();
						sb.append(fb.declaringClass.signature());
						sb.append(fb.name);
						return sb.toString();
					}
				}
			}
		}
		return null;
	}

	public ResolvedType[] getAnnotationTypes() {
		if (cachedAnnotationTypes == null) {
			long abits = realBinding.getAnnotationTagBits(); // ensure resolved
			Annotation[] annos = getEclipseAnnotations();
			if (annos==null) { 
				cachedAnnotationTypes = ResolvedType.EMPTY_RESOLVED_TYPE_ARRAY;
			} else {
				cachedAnnotationTypes = new ResolvedType[annos.length];
				for (int i = 0; i < annos.length; i++) {
					Annotation type = annos[i];
					cachedAnnotationTypes[i] = w.resolve(UnresolvedType.forSignature(new String(type.resolvedType.signature())));
				}
			}
		}
		return cachedAnnotationTypes;
	}
	

	
	public String[] getParameterNames() {
		if (argumentNames!=null) return argumentNames;
		if (realBinding instanceof FieldBinding) {
			argumentNames=NO_ARGS;
		} else {
			TypeDeclaration typeDecl = getTypeDeclaration();
			AbstractMethodDeclaration methodDecl = typeDecl.declarationOf((MethodBinding)realBinding);
			Argument[] args = (methodDecl==null?null:methodDecl.arguments); // dont like this - why isnt the method found sometimes? is it because other errors are being reported?
			if (args==null) {
				argumentNames=NO_ARGS;
			} else {
				argumentNames = new String[args.length];
				for (int i = 0; i < argumentNames.length; i++) {
					argumentNames[i] = new String(methodDecl.arguments[i].name);
				}
			}
		}
		return argumentNames;
	}
	
	private Annotation[] getEclipseAnnotations() {
		if (realBinding instanceof MethodBinding) {
			AbstractMethodDeclaration methodDecl = getTypeDeclaration().declarationOf((MethodBinding)realBinding);
			return methodDecl.annotations;
		} else if (realBinding instanceof FieldBinding) {
			FieldDeclaration fieldDecl = getTypeDeclaration().declarationOf((FieldBinding)realBinding);
			return fieldDecl.annotations;
		}
		return null;
	}

	private TypeDeclaration getTypeDeclaration() {
		if (realBinding instanceof MethodBinding) {
			return ((SourceTypeBinding)((MethodBinding)realBinding).declaringClass).scope.referenceContext;
			
		} else if (realBinding instanceof FieldBinding) {
			return ((SourceTypeBinding)((FieldBinding)realBinding).declaringClass).scope.referenceContext;
		}
		return null;
	}
	
}
