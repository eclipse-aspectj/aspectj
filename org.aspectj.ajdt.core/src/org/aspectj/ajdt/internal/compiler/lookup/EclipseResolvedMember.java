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
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;

/**
 * In the pipeline world, we can be weaving before all types have come through from compilation.
 * In some cases this means the weaver will want to ask questions of eclipse types and this
 * subtype of ResolvedMemberImpl is here to answer some of those questions - it is backed by 
 * the real eclipse MethodBinding object and can translate from Eclipse -> Weaver information.
 */
public class EclipseResolvedMember extends ResolvedMemberImpl {
	
	private static String[] NO_ARGS = new String[]{};
	
	private MethodBinding realBinding;
	private String[] argumentNames;
	
	public EclipseResolvedMember(MethodBinding binding, Kind memberKind, ResolvedType realDeclaringType, int modifiers, UnresolvedType type, String string, UnresolvedType[] types, UnresolvedType[] types2) {
		super(memberKind,realDeclaringType,modifiers,type,string,types,types2);
		this.realBinding = binding;
	}

	public AnnotationX[] getAnnotations() {
		long abits = realBinding.getAnnotationTagBits(); // ensure resolved
		TypeDeclaration typeDecl = ((SourceTypeBinding)realBinding.declaringClass).scope.referenceContext;
		AbstractMethodDeclaration methodDecl = typeDecl.declarationOf(realBinding);
		Annotation[] annos = methodDecl.annotations; // this is what to add
		if (annos==null) return null;
		return super.getAnnotations();
	}

	public ResolvedType[] getAnnotationTypes() {
		long abits = realBinding.getAnnotationTagBits(); // ensure resolved
		TypeDeclaration typeDecl = ((SourceTypeBinding)realBinding.declaringClass).scope.referenceContext;
		AbstractMethodDeclaration methodDecl = typeDecl.declarationOf(realBinding);
		Annotation[] annos = methodDecl.annotations; // this is what to add
		if (annos==null) return null;
		return super.getAnnotationTypes();
	}

	
	public String[] getParameterNames() {
		if (argumentNames!=null) return argumentNames;
		TypeDeclaration typeDecl = ((SourceTypeBinding)realBinding.declaringClass).scope.referenceContext;
		AbstractMethodDeclaration methodDecl = typeDecl.declarationOf(realBinding);
		Argument[] args = (methodDecl==null?null:methodDecl.arguments); // dont like this - why isnt the method found sometimes? is it because other errors are being reported?
		if (args==null) {
			argumentNames=NO_ARGS;
		} else {
			argumentNames = new String[args.length];
			for (int i = 0; i < argumentNames.length; i++) {
				argumentNames[i] = new String(methodDecl.arguments[i].name);
			}
		}
		return argumentNames;
	}
	
}
