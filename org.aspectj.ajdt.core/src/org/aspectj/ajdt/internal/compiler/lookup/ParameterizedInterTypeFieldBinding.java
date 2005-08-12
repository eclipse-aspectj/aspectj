/*******************************************************************************
 * Copyright (c) 2005 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.compiler.lookup;

import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedFieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Like the Eclipse type ParameterizedFieldBinding which wraps a FieldBinding, making
 * it appear as a particular parameterized form, this type wraps an InterTypeFieldBinding
 * delegating to the InterTypeFieldBinding for answering some questions about visibility
 * and access methods.
 */
public class ParameterizedInterTypeFieldBinding extends ParameterizedFieldBinding {

	public ParameterizedInterTypeFieldBinding(ParameterizedTypeBinding parameterizedDeclaringClass, FieldBinding originalField) {
		super(parameterizedDeclaringClass, originalField);
	}

	/*
	 * These methods override the supertypes methods and delegate to the original
	 * field binding which is an InterTypeFieldBinding.
	 */
	
	public boolean canBeSeenBy(TypeBinding receiverType, InvocationSite invocationSite, Scope scope) {
		return originalField.canBeSeenBy(receiverType, invocationSite, scope);
	}
	
	public SyntheticMethodBinding getAccessMethod(boolean isReadAccess) {
		return originalField.getAccessMethod(isReadAccess);
	}
	
	public boolean alwaysNeedsAccessMethod(boolean isReadAccess) {
		return originalField.alwaysNeedsAccessMethod(isReadAccess);
	}	

	public ReferenceBinding getTargetType() {
		return ((InterTypeFieldBinding)originalField).getTargetType();
	}
	
}
