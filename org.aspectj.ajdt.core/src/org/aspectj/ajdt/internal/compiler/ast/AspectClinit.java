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

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.ExceptionLabel;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.aspectj.weaver.AjcMemberMaker;

public class AspectClinit extends Clinit {
	private boolean hasPre, hasPost;
	private FieldBinding initFailureField;
	
	public AspectClinit(Clinit old, CompilationResult compilationResult, boolean hasPre, boolean hasPost, FieldBinding initFailureField) {
		super(compilationResult);
		// CHECK do we need all the bits or just the needfreereturn bit?
	//	if ((old.bits & ASTNode.NeedFreeReturn)!=0) this.bits |= ASTNode.NeedFreeReturn;
		this.bits = old.bits;
		this.sourceEnd = old.sourceEnd;
		this.sourceStart = old.sourceStart;
		this.declarationSourceEnd = old.declarationSourceEnd;
		this.declarationSourceStart = old.declarationSourceStart;
		
		this.hasPre = hasPre;
		this.hasPost = hasPost;
		this.initFailureField = initFailureField;
	}
	
	private ExceptionLabel handlerLabel;

	protected void generateSyntheticCode(
		ClassScope classScope,
		CodeStream codeStream) 
	{
		if (initFailureField != null) {
			handlerLabel = new ExceptionLabel(codeStream, classScope.getJavaLangThrowable());	
			handlerLabel.placeStart();
		}
		
		if (hasPre) {
			final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(classScope);

			codeStream.invokestatic(world.makeMethodBindingForCall(
				AjcMemberMaker.ajcPreClinitMethod(
					world.fromBinding(classScope.referenceContext.binding)
				)));
		}
		super.generateSyntheticCode(classScope, codeStream);
	}
	
	protected void generatePostSyntheticCode(
		ClassScope classScope,
		CodeStream codeStream)
	{
		super.generatePostSyntheticCode(classScope, codeStream);
		if (hasPost) {
			final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(classScope);

			codeStream.invokestatic(world.makeMethodBindingForCall(
				AjcMemberMaker.ajcPostClinitMethod(
					world.fromBinding(classScope.referenceContext.binding)
				)));
		}
		
		if (initFailureField != null) {
			// Changes to this exception handling code may require changes to
			// BcelClassWeaver.isInitFailureHandler()
			handlerLabel.placeEnd();
			BranchLabel endLabel = new BranchLabel(codeStream);
			codeStream.goto_(endLabel);
			handlerLabel.place();
			codeStream.astore_0(); // Bug #52394
			// CHECK THIS...
			codeStream.addVariable(new LocalVariableBinding("caughtException".toCharArray(),initFailureField.type,ClassFileConstants.AccPrivate,false));
		    codeStream.aload_0();
			codeStream.putstatic(initFailureField);
			endLabel.place();
		}
		
	}

}
