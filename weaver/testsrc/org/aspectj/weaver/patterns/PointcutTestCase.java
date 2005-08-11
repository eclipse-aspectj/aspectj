/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.runtime.reflect.Factory;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Test;

import junit.framework.TestCase;


public class PointcutTestCase extends TestCase {
	
	public void testMatchJP() {
		Pointcut p = new Pointcut() {

			public Object accept(PatternNodeVisitor visitor, Object data) {
				return visitor.visit(this,data);
			}
			
			public Set couldMatchKinds() {
				return null;
			}
			
			public FuzzyBoolean fastMatch(FastMatchInfo info) {
				return null;
			}
			
			public FuzzyBoolean fastMatch(Class targetClass) {
				return null;
			}

			protected FuzzyBoolean matchInternal(Shadow shadow) {
				return null;
			}

			protected void resolveBindings(IScope scope, Bindings bindings) {
			}
			
			protected void resolveBindingsFromRTTI() {}

			protected Pointcut concretize1(ResolvedType inAspect, IntMap bindings) {
				return null;
			}
			
			public Pointcut parameterizeWith(Map typeVariableMap) {
				return null;
			}

			protected Test findResidueInternal(Shadow shadow, ExposedState state) {
				return null;
			}

			public void write(DataOutputStream s) throws IOException {
			}};
		
		Factory f = new Factory("PointcutTestCase.java",PointcutTestCase.class);
			
		Signature methodSig = f.makeMethodSig("void aMethod()");
		JoinPoint.StaticPart jpsp = f.makeSJP(JoinPoint.METHOD_EXECUTION,methodSig,1);
		JoinPoint jp = Factory.makeJP(jpsp,this,this);
		
		try {
			p.match(jp,null);
			fail("Expected UnsupportedOperationException to be thrown");
		} catch (UnsupportedOperationException unEx) {
			// ok
		}
		
		try {
			p.match(jpsp);
			fail("Expected UnsupportedOperationException to be thrown");
		} catch (UnsupportedOperationException unEx) {
			// ok
		}
		
	}
	
}
