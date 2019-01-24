/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.reflect;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.And;
import org.aspectj.weaver.ast.Call;
import org.aspectj.weaver.ast.FieldGetCall;
import org.aspectj.weaver.ast.HasAnnotation;
import org.aspectj.weaver.ast.ITestVisitor;
import org.aspectj.weaver.ast.Instanceof;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Not;
import org.aspectj.weaver.ast.Or;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.internal.tools.MatchingContextBasedTest;
import org.aspectj.weaver.patterns.ExposedState;
import org.aspectj.weaver.tools.DefaultMatchingContext;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.MatchingContext;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.ShadowMatch;

/**
 * @author colyer Implementation of ShadowMatch for reflection based worlds.
 */
public class StandardShadowMatchImpl implements ShadowMatch {

	private FuzzyBoolean match;
	private ExposedState state;
	private Test residualTest;
	private PointcutParameter[] params;
	private ResolvedMember withinCode;
	private ResolvedMember subject;
	private ResolvedType withinType;
	private MatchingContext matchContext = new DefaultMatchingContext();

	public StandardShadowMatchImpl(FuzzyBoolean match, Test test, ExposedState state, PointcutParameter[] params) {
		this.match = match;
		this.residualTest = test;
		this.state = state;
		this.params = params;
	}

	public void setWithinCode(ResolvedMember aMember) {
		this.withinCode = aMember;
	}

	public void setSubject(ResolvedMember aMember) {
		this.subject = aMember;
	}

	public void setWithinType(ResolvedType aClass) {
		this.withinType = aClass;
	}

	public boolean alwaysMatches() {
		return match.alwaysTrue();
	}

	public boolean maybeMatches() {
		return match.maybeTrue();
	}

	public boolean neverMatches() {
		return match.alwaysFalse();
	}

	public JoinPointMatch matchesJoinPoint(Object thisObject, Object targetObject, Object[] args) {
		if (neverMatches())
			return JoinPointMatchImpl.NO_MATCH;
		if (new RuntimeTestEvaluator(residualTest, thisObject, targetObject, args, this.matchContext).matches()) {
			return new JoinPointMatchImpl(getPointcutParameters(thisObject, targetObject, args));
		} else {
			return JoinPointMatchImpl.NO_MATCH;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.tools.ShadowMatch#setMatchingContext(org.aspectj.weaver.tools.MatchingContext)
	 */
	public void setMatchingContext(MatchingContext aMatchContext) {
		this.matchContext = aMatchContext;
	}

	private PointcutParameter[] getPointcutParameters(Object thisObject, Object targetObject, Object[] args) {
		// Var[] vars = state.vars;
		// PointcutParameterImpl[] bindings = new PointcutParameterImpl[params.length];
		// for (int i = 0; i < bindings.length; i++) {
		// bindings[i] = new PointcutParameterImpl(params[i].getName(), params[i].getType());
		// bindings[i].setBinding(((ReflectionVar) vars[i]).getBindingAtJoinPoint(thisObject, targetObject, args, subject,
		// withinCode, withinType));
		// }
		// return bindings;
		return null;
	}

	private static class RuntimeTestEvaluator implements ITestVisitor {

		private boolean matches = true;
		private final Test test;
		private final Object thisObject;
		private final Object targetObject;
		private final Object[] args;
		private final MatchingContext matchContext;

		public RuntimeTestEvaluator(Test aTest, Object thisObject, Object targetObject, Object[] args, MatchingContext context) {
			this.test = aTest;
			this.thisObject = thisObject;
			this.targetObject = targetObject;
			this.args = args;
			this.matchContext = context;
		}

		public boolean matches() {
			test.accept(this);
			return matches;
		}

		public void visit(And e) {
			boolean leftMatches = new RuntimeTestEvaluator(e.getLeft(), thisObject, targetObject, args, matchContext).matches();
			if (!leftMatches) {
				matches = false;
			} else {
				matches = new RuntimeTestEvaluator(e.getRight(), thisObject, targetObject, args, matchContext).matches();
			}
		}

		public void visit(Instanceof i) {
			ReflectionVar v = (ReflectionVar) i.getVar();
			Object value = v.getBindingAtJoinPoint(thisObject, targetObject, args);
			World world = v.getType().getWorld();
			ResolvedType desiredType = i.getType().resolve(world);
			ResolvedType actualType = world.resolve(value.getClass().getName());
			matches = desiredType.isAssignableFrom(actualType);
		}

		public void visit(MatchingContextBasedTest matchingContextTest) {
			matches = matchingContextTest.matches(this.matchContext);
		}

		public void visit(Not not) {
			matches = !new RuntimeTestEvaluator(not.getBody(), thisObject, targetObject, args, matchContext).matches();
		}

		public void visit(Or or) {
			boolean leftMatches = new RuntimeTestEvaluator(or.getLeft(), thisObject, targetObject, args, matchContext).matches();
			if (leftMatches) {
				matches = true;
			} else {
				matches = new RuntimeTestEvaluator(or.getRight(), thisObject, targetObject, args, matchContext).matches();
			}
		}

		public void visit(Literal literal) {
			if (literal == Literal.FALSE) {
				matches = false;
			} else {
				matches = true;
			}
		}

		public void visit(Call call) {
			throw new UnsupportedOperationException("Can't evaluate call test at runtime");
		}

		public void visit(FieldGetCall fieldGetCall) {
			throw new UnsupportedOperationException("Can't evaluate fieldGetCall test at runtime");
		}

		public void visit(HasAnnotation hasAnnotation) {
			ReflectionVar v = (ReflectionVar) hasAnnotation.getVar();
			Object value = v.getBindingAtJoinPoint(thisObject, targetObject, args);
			World world = v.getType().getWorld();
			ResolvedType actualVarType = world.resolve(value.getClass().getName());
			ResolvedType requiredAnnotationType = hasAnnotation.getAnnotationType().resolve(world);
			matches = actualVarType.hasAnnotation(requiredAnnotationType);
		}

	}

}
