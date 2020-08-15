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
 *     Alexandre Vasseur    if() implementation for @AJ style
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

public class IfPointcut extends Pointcut {
	public ResolvedMember testMethod;
	public int extraParameterFlags;

	/**
	 * A token source dump that looks like a pointcut (but is NOT parseable at all - just here to help debugging etc)
	 */
	private final String enclosingPointcutHint;

	public Pointcut residueSource;
	int baseArgsCount;

	// XXX some way to compute args

	public IfPointcut(ResolvedMember testMethod, int extraParameterFlags) {
		this.testMethod = testMethod;
		this.extraParameterFlags = extraParameterFlags;
		this.pointcutKind = IF;
		this.enclosingPointcutHint = null;
	}

	/**
	 * No-arg constructor for @AJ style, where the if() body is actually the @Pointcut annotated method
	 */
	public IfPointcut(String enclosingPointcutHint) {
		this.pointcutKind = IF;
		this.enclosingPointcutHint = enclosingPointcutHint;
		this.testMethod = null;// resolved during concretize
		this.extraParameterFlags = -1;// allows to keep track of the @Aj style
	}

	@Override
	public int couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS_BITS;
	}

	@Override
	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return FuzzyBoolean.MAYBE;
	}

	@Override
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		if ((extraParameterFlags & Advice.ConstantReference) != 0) {
			if ((extraParameterFlags & Advice.ConstantValue) != 0) {
				return FuzzyBoolean.YES;
			} else {
				return FuzzyBoolean.NO;
			}
		}
		// ??? this is not maximally efficient
		return FuzzyBoolean.MAYBE;
	}

	public boolean alwaysFalse() {
		return false;
	}

	public boolean alwaysTrue() {
		return false;
	}

	// enh 76055
	public Pointcut getResidueSource() {
		return residueSource;
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Pointcut.IF);
		s.writeBoolean(testMethod != null); // do we have a test method?
		if (testMethod != null) {
			testMethod.write(s);
		}
		s.writeByte(extraParameterFlags);
		writeLocation(s);
	}

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		boolean hasTestMethod = s.readBoolean();
		ResolvedMember resolvedTestMethod = null;
		if (hasTestMethod) { // should always have a test method unless @AJ style
			resolvedTestMethod = ResolvedMemberImpl.readResolvedMember(s, context);
		}
		IfPointcut ret = new IfPointcut(resolvedTestMethod, s.readByte());
		ret.readLocation(context, s);
		return ret;
	}

	@Override
	public void resolveBindings(IScope scope, Bindings bindings) {
		// ??? all we need is good error messages in here in cflow contexts
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof IfPointcut)) {
			return false;
		}
		IfPointcut o = (IfPointcut) other;
		if (o.testMethod == null) {
			return (this.testMethod == null);
		}
		return o.testMethod.equals(this.testMethod);
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + testMethod.hashCode();
		return result;
	}

	@Override
	public String toString() {
		if (extraParameterFlags < 0) {
			// @AJ style
			return "if()";
		} else {
			return "if(" + testMethod + ")";// FIXME AV - bad, this makes it unparsable. Perhaps we can use if() for code style
			// behind the scene!
		}
	}

	// ??? The implementation of name binding and type checking in if PCDs is very convoluted
	// There has to be a better way...
	private boolean findingResidue = false;

	// Similar to lastMatchedShadowId - but only for if PCDs.
	private int ifLastMatchedShadowId;
	private Test ifLastMatchedShadowResidue;

	/**
	 * At each shadow that matched, the residue can be different.
	 */
	@Override
	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		if (findingResidue) {
			return Literal.TRUE;
		}
		findingResidue = true;
		try {

			// Have we already been asked this question?
			if (shadow.shadowId == ifLastMatchedShadowId) {
				return ifLastMatchedShadowResidue;
			}

			Test ret = Literal.TRUE;
			List<Var> args = new ArrayList<>();

			// code style
			if (extraParameterFlags >= 0) {
				if ((extraParameterFlags & Advice.ConstantReference) != 0) {
					// it is either always true or always false, no need for test
					if ((extraParameterFlags & Advice.ConstantValue) != 0) {
						ret = Literal.TRUE;
						ifLastMatchedShadowId = shadow.shadowId;
						ifLastMatchedShadowResidue = ret;
						return ret;
					} else {
						// Dont think we should be in here as the match cannot have succeeded...
						ret = Literal.FALSE;
						ifLastMatchedShadowId = shadow.shadowId;
						ifLastMatchedShadowResidue = ret;
						return ret;
					}
				}

				// If there are no args to sort out, don't bother with the recursive call
				if (baseArgsCount > 0) {
					ExposedState myState = new ExposedState(baseArgsCount);
					myState.setConcreteAspect(state.getConcreteAspect());
					// ??? we throw out the test that comes from this walk. All we want here
					// is bindings for the arguments
					residueSource.findResidue(shadow, myState);

					UnresolvedType[] pTypes = (testMethod == null ? null : testMethod.getParameterTypes());
					if (pTypes != null && baseArgsCount > pTypes.length) { // pr155347
						throw new BCException("Unexpected problem with testMethod " + testMethod + ": expecting " + baseArgsCount
								+ " arguments");
					}
					// pr118149
					// It is possible for vars in myState (which would normally be set
					// in the call to residueSource.findResidue) to not be set (be null)
					// in an Or pointcut with if expressions in both branches, and where
					// one branch is known statically to not match. In this situation we
					// simply return Test.
					for (int i = 0; i < baseArgsCount; i++) {
						Var v = myState.get(i);
						if (v == null) {
							continue; // pr118149
						}
						args.add(v);
						ret = Test.makeAnd(ret, Test.makeInstanceof(v, pTypes[i].resolve(shadow.getIWorld())));
					}
				}

				// handle thisJoinPoint parameters
				if ((extraParameterFlags & Advice.ThisJoinPoint) != 0) {
					args.add(shadow.getThisJoinPointVar());
				}

				if ((extraParameterFlags & Advice.ThisJoinPointStaticPart) != 0) {
					args.add(shadow.getThisJoinPointStaticPartVar());
				}

				if ((extraParameterFlags & Advice.ThisEnclosingJoinPointStaticPart) != 0) {
					args.add(shadow.getThisEnclosingJoinPointStaticPartVar());
				}

				if ((extraParameterFlags & Advice.ThisAspectInstance) != 0) {
					args.add(shadow.getThisAspectInstanceVar(state.getConcreteAspect()));
				}
			} else {
				// @style is slightly different
				int currentStateIndex = 0;
				// FIXME AV - "args(jp)" test(jp, thejp) will fail here
				for (int i = 0; i < testMethod.getParameterTypes().length; i++) {
					String argSignature = testMethod.getParameterTypes()[i].getSignature();
					if (AjcMemberMaker.TYPEX_JOINPOINT.getSignature().equals(argSignature)) {
						args.add(shadow.getThisJoinPointVar());
					} else if (AjcMemberMaker.TYPEX_PROCEEDINGJOINPOINT.getSignature().equals(argSignature)) {
						args.add(shadow.getThisJoinPointVar());
					} else if (AjcMemberMaker.TYPEX_STATICJOINPOINT.getSignature().equals(argSignature)) {
						args.add(shadow.getThisJoinPointStaticPartVar());
					} else if (AjcMemberMaker.TYPEX_ENCLOSINGSTATICJOINPOINT.getSignature().equals(argSignature)) {
						args.add(shadow.getThisEnclosingJoinPointStaticPartVar());
					} else {
						if (state.size() == 0 || currentStateIndex > state.size()) {
							String[] paramNames = testMethod.getParameterNames();
							StringBuffer errorParameter = new StringBuffer();
							if (paramNames != null) {
								errorParameter.append(testMethod.getParameterTypes()[i].getName()).append(" ");
								errorParameter.append(paramNames[i]);
								shadow.getIWorld()
										.getMessageHandler()
										.handleMessage(
												MessageUtil.error("Missing binding for if() pointcut method.  Parameter " + (i + 1)
														+ "(" + errorParameter.toString()
														+ ") must be bound - even in reference pointcuts  (compiler limitation)",
														testMethod.getSourceLocation()));
							} else {
								shadow.getIWorld()
										.getMessageHandler()
										.handleMessage(
												MessageUtil.error("Missing binding for if() pointcut method.  Parameter " + (i + 1)
														+ " must be bound - even in reference pointcuts (compiler limitation)",
														testMethod.getSourceLocation()));
							}
							return Literal.TRUE; // exit quickly
						}
						// we don't use i as JoinPoint.* can be anywhere in the signature in @style
						Var v = state.get(currentStateIndex++);

						while (v == null && currentStateIndex < state.size()) { // pr162135
							v = state.get(currentStateIndex++);
						}
						args.add(v);

						ret = Test.makeAnd(ret,
								Test.makeInstanceof(v, testMethod.getParameterTypes()[i].resolve(shadow.getIWorld())));
					}
				}
			}

			ret = Test.makeAnd(ret, Test.makeCall(testMethod, (Expr[]) args.toArray(new Expr[0])));

			// Remember...
			ifLastMatchedShadowId = shadow.shadowId;
			ifLastMatchedShadowResidue = ret;
			return ret;
		} finally {
			findingResidue = false;
		}
	}

	// amc - the only reason this override seems to be here is to stop the copy, but
	// that can be prevented by overriding shouldCopyLocationForConcretization,
	// allowing me to make the method final in Pointcut.
	// public Pointcut concretize(ResolvedType inAspect, IntMap bindings) {
	// return this.concretize1(inAspect, bindings);
	// }

	@Override
	protected boolean shouldCopyLocationForConcretize() {
		return false;
	}

	private IfPointcut partiallyConcretized = null;

	@Override
	public Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
		// System.err.println("concretize: " + this + " already: " + partiallyConcretized);

		if (isDeclare(bindings.getEnclosingAdvice())) {
			// Enforce rule about which designators are supported in declare
			inAspect.getWorld().showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.IF_IN_DECLARE),
					bindings.getEnclosingAdvice().getSourceLocation(), null);
			return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}

		if (partiallyConcretized != null) {
			return partiallyConcretized;
		}

		final IfPointcut ret;
		if (extraParameterFlags < 0 && testMethod == null) {
			// @AJ style, we need to find the testMethod in the aspect defining the "if()" enclosing pointcut
			ResolvedPointcutDefinition def = bindings.peekEnclosingDefinition();
			if (def != null) {
				ResolvedType aspect = inAspect.getWorld().resolve(def.getDeclaringType());
				for (Iterator memberIter = aspect.getMethods(true, true); memberIter.hasNext();) {
					ResolvedMember method = (ResolvedMember) memberIter.next();
					if (def.getName().equals(method.getName())
							&& def.getParameterTypes().length == method.getParameterTypes().length) {
						boolean sameSig = true;
						for (int j = 0; j < method.getParameterTypes().length; j++) {
							UnresolvedType argJ = method.getParameterTypes()[j];
							if (!argJ.equals(def.getParameterTypes()[j])) {
								sameSig = false;
								break;
							}
						}
						if (sameSig) {
							testMethod = method;
							break;
						}
					}
				}
				if (testMethod == null) {
					inAspect.getWorld().showMessage(IMessage.ERROR,
							"Cannot find if() body from '" + def.toString() + "' for '" + enclosingPointcutHint + "'",
							this.getSourceLocation(), null);
					return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
				}
			} else {
				testMethod = inAspect.getWorld().resolve(bindings.getAdviceSignature());
			}
			ret = new IfPointcut(enclosingPointcutHint);
			ret.testMethod = testMethod;
		} else {
			ret = new IfPointcut(testMethod, extraParameterFlags);
		}
		ret.copyLocationFrom(this);
		partiallyConcretized = ret;

		// It is possible to directly code your pointcut expression in a per clause
		// rather than defining a pointcut declaration and referencing it in your
		// per clause. If you do this, we have problems (bug #62458). For now,
		// let's police that you are trying to code a pointcut in a per clause and
		// put out a compiler error.
		if (bindings.directlyInAdvice() && bindings.getEnclosingAdvice() == null) {
			// Assumption: if() is in a per clause if we say we are directly in advice
			// but we have no enclosing advice.
			inAspect.getWorld().showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.IF_IN_PERCLAUSE),
					this.getSourceLocation(), null);
			return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}

		if (bindings.directlyInAdvice()) {
			ShadowMunger advice = bindings.getEnclosingAdvice();
			if (advice instanceof Advice) {
				ret.baseArgsCount = ((Advice) advice).getBaseParameterCount();
			} else {
				ret.baseArgsCount = 0;
			}
			ret.residueSource = advice.getPointcut().concretize(inAspect, inAspect, ret.baseArgsCount, advice);
		} else {
			ResolvedPointcutDefinition def = bindings.peekEnclosingDefinition();
			if (def == CflowPointcut.CFLOW_MARKER) {
				inAspect.getWorld().showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.IF_LEXICALLY_IN_CFLOW),
						getSourceLocation(), null);
				return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
			}
			ret.baseArgsCount = def.getParameterTypes().length;

			// for @style, we have implicit binding for JoinPoint.* things
			// FIXME AV - will lead to failure for "args(jp)" test(jp, thejp) / see args() implementation
			if (ret.extraParameterFlags < 0) {
				ret.baseArgsCount = 0;
				for (int i = 0; i < testMethod.getParameterTypes().length; i++) {
					String argSignature = testMethod.getParameterTypes()[i].getSignature();
					if (AjcMemberMaker.TYPEX_JOINPOINT.getSignature().equals(argSignature)
							|| AjcMemberMaker.TYPEX_PROCEEDINGJOINPOINT.getSignature().equals(argSignature)
							|| AjcMemberMaker.TYPEX_STATICJOINPOINT.getSignature().equals(argSignature)
							|| AjcMemberMaker.TYPEX_ENCLOSINGSTATICJOINPOINT.getSignature().equals(argSignature)) {

					} else {
						ret.baseArgsCount++;
					}
				}
			}

			IntMap newBindings = IntMap.idMap(ret.baseArgsCount);
			newBindings.copyContext(bindings);
			ret.residueSource = def.getPointcut().concretize(inAspect, declaringType, newBindings);
		}

		return ret;
	}

	// we can't touch "if" methods
	@Override
	public Pointcut parameterizeWith(Map typeVariableMap, World w) {
		return this;
	}

	// public static Pointcut MatchesNothing = new MatchesNothingPointcut();
	// ??? there could possibly be some good optimizations to be done at this point
	public static IfPointcut makeIfFalsePointcut(State state) {
		IfPointcut ret = new IfFalsePointcut();
		ret.state = state;
		return ret;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public static class IfFalsePointcut extends IfPointcut {

		public IfFalsePointcut() {
			super(null, 0);
			this.pointcutKind = Pointcut.IF_FALSE;
		}

		@Override
		public int couldMatchKinds() {
			return Shadow.NO_SHADOW_KINDS_BITS;
		}

		@Override
		public boolean alwaysFalse() {
			return true;
		}

		@Override
		protected Test findResidueInternal(Shadow shadow, ExposedState state) {
			return Literal.FALSE; // can only get here if an earlier error occurred
		}

		@Override
		public FuzzyBoolean fastMatch(FastMatchInfo type) {
			return FuzzyBoolean.NO;
		}

		@Override
		protected FuzzyBoolean matchInternal(Shadow shadow) {
			return FuzzyBoolean.NO;
		}

		@Override
		public void resolveBindings(IScope scope, Bindings bindings) {
		}

		@Override
		public void postRead(ResolvedType enclosingType) {
		}

		@Override
		public Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
			if (isDeclare(bindings.getEnclosingAdvice())) {
				// Enforce rule about which designators are supported in declare
				inAspect.getWorld().showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.IF_IN_DECLARE),
						bindings.getEnclosingAdvice().getSourceLocation(), null);
				return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
			}
			return makeIfFalsePointcut(state);
		}

		@Override
		public void write(CompressingDataOutputStream s) throws IOException {
			s.writeByte(Pointcut.IF_FALSE);
		}

		@Override
		public int hashCode() {
			int result = 17;
			return result;
		}

		@Override
		public String toString() {
			return "if(false)";
		}
	}

	public static IfPointcut makeIfTruePointcut(State state) {
		IfPointcut ret = new IfTruePointcut();
		ret.state = state;
		return ret;
	}

	public static class IfTruePointcut extends IfPointcut {

		public IfTruePointcut() {
			super(null, 0);
			this.pointcutKind = Pointcut.IF_TRUE;
		}

		@Override
		public boolean alwaysTrue() {
			return true;
		}

		@Override
		protected Test findResidueInternal(Shadow shadow, ExposedState state) {
			return Literal.TRUE; // can only get here if an earlier error occurred
		}

		@Override
		public FuzzyBoolean fastMatch(FastMatchInfo type) {
			return FuzzyBoolean.YES;
		}

		@Override
		protected FuzzyBoolean matchInternal(Shadow shadow) {
			return FuzzyBoolean.YES;
		}

		@Override
		public void resolveBindings(IScope scope, Bindings bindings) {
		}

		@Override
		public void postRead(ResolvedType enclosingType) {
		}

		@Override
		public Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
			if (isDeclare(bindings.getEnclosingAdvice())) {
				// Enforce rule about which designators are supported in declare
				inAspect.getWorld().showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.IF_IN_DECLARE),
						bindings.getEnclosingAdvice().getSourceLocation(), null);
				return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
			}
			return makeIfTruePointcut(state);
		}

		@Override
		public void write(CompressingDataOutputStream s) throws IOException {
			s.writeByte(IF_TRUE);
		}

		@Override
		public int hashCode() {
			int result = 37;
			return result;
		}

		@Override
		public String toString() {
			return "if(true)";
		}
	}

	/**
	 * Called when it is determined that the pointcut refers to a constant value of TRUE or FALSE - enabling exact matching and no
	 * unnecessary calls to the method representing the if body.
	 */
	public void setAlways(boolean matches) {
		extraParameterFlags |= Advice.ConstantReference;
		if (matches) {
			extraParameterFlags |= Advice.ConstantValue;
		}
	}

}
