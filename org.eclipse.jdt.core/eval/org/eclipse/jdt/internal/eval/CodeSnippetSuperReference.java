/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * A super reference inside a code snippet denotes a reference to the super type of 
 * the remote receiver object (i.e.&nbsp;the one of the context in the stack frame). This is 
 * used to report an error through JavaModelException according to the fact that super
 * reference are not supported in code snippet.
 */
public class CodeSnippetSuperReference extends SuperReference implements EvaluationConstants, InvocationSite {
	EvaluationContext evaluationContext;
	
public CodeSnippetSuperReference(int pos, int sourceEnd, 	EvaluationContext evaluationContext) {
	super(pos, sourceEnd);
	this.evaluationContext = evaluationContext;
}

public TypeBinding resolveType(BlockScope scope) {
		scope.problemReporter().cannotUseSuperInCodeSnippet(this.sourceStart, this.sourceEnd); //$NON-NLS-1$
		return null;
}
public boolean isSuperAccess(){
	return false;
}
public boolean isTypeAccess(){
	return false;
}
public void setActualReceiverType(ReferenceBinding receiverType) {
	// ignored
}
public void setDepth(int depth){
	// ignored
}
public void setFieldIndex(int index){
	// ignored
}

}

