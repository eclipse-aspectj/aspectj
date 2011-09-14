/* *******************************************************************
 * Copyright (c) 2011 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Andy Clement			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.List;

import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.ResolvedType;

/**
 * pr354470. This is a special subtype of HasMemberTypePattern. In order to optimize this situation: <br>
 * <code><pre>
 * aspect X perthis(transactional()) {<br>
 * pointcut transactional: execution(@Foo * *(..));<br>
 * </pre></code>
 * <p>
 * When this occurs we obviously only want an aspect instance when there is a method annotated with @Foo. For a regular execution
 * pointcut we couldn't really do this due to the multiple joinpoint signatures for each joinpoint (and so lots of types get the
 * ajcMightHaveAspect interface). However, for an execution pointcut involving an annotation we can do something clever. Annotations
 * must match against the first primary joinpoint signature - so when computing the type pattern to use for matching when processing
 * the perthis() clause above, we can use the HasMemberTypePattern - because that effectively does what we want. We want an aspect
 * instance if the type hasmethod(...) with the appropriate annotation. This would be great... but breaks in the face of ITDs. If
 * the method that hasmethod() would match is introduced via an ITD we come unstuck, the code in HasMemberTypePattern.hasMethod()
 * does look at ITDs but it won't see annotations, they aren't visible (at least through EclipseResolvedMember objects). And so this
 * subclass is created to say 'if the supertype thinks it is a match, great, but if it doesnt then if there are ITDs on the target,
 * they might match so just say 'true''. Note that returning true is just confirming whether the 'mightHaveAspect' interface (and
 * friends) are getting added.
 * 
 * @author Andy Clement
 */
public class HasMemberTypePatternForPerThisMatching extends HasMemberTypePattern {

	public HasMemberTypePatternForPerThisMatching(SignaturePattern aSignaturePattern) {
		super(aSignaturePattern);
	}

	protected boolean hasMethod(ResolvedType type) {
		boolean b = super.hasMethod(type);
		if (b) {
			return true;
		}
		// If there are ITDs, have to be safe and just assume one of them might match
		List<ConcreteTypeMunger> mungers = type.getInterTypeMungersIncludingSupers();
		if (mungers.size() != 0) {
			return true;
		}
		return false;
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		throw new IllegalAccessError("Should never be called, these are transient and don't get serialized");
	}
}
