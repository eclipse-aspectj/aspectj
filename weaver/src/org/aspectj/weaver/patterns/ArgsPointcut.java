/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.BetaException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.internal.tools.PointcutExpressionImpl;

/**
 * args(arguments)
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public class ArgsPointcut extends NameBindingPointcut {
	private static final String ASPECTJ_JP_SIGNATURE_PREFIX = "Lorg/aspectj/lang/JoinPoint";
	private static final String ASPECTJ_SYNTHETIC_SIGNATURE_PREFIX = "Lorg/aspectj/runtime/internal/";
	
	private TypePatternList arguments;
	
	public ArgsPointcut(TypePatternList arguments) {
		this.arguments = arguments;
		this.pointcutKind = ARGS;
	}

    public TypePatternList getArguments() {
        return arguments;
    }

	public Set couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS;  // empty args() matches jps with no args
	}

    public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return FuzzyBoolean.MAYBE;
	}
	
	public FuzzyBoolean fastMatch(Class targetType) {
		return FuzzyBoolean.MAYBE;
	}

	protected FuzzyBoolean matchInternal(Shadow shadow) {
		ResolvedType[] argumentsToMatchAgainst = getArgumentsToMatchAgainst(shadow);
		FuzzyBoolean ret =
			arguments.matches(argumentsToMatchAgainst, TypePattern.DYNAMIC);
		return ret;
	}
	
	private ResolvedType[] getArgumentsToMatchAgainst(Shadow shadow) {
		ResolvedType[] argumentsToMatchAgainst = shadow.getIWorld().resolve(shadow.getGenericArgTypes());

		// special treatment for adviceexecution which may have synthetic arguments we
		// want to ignore.
		if (shadow.getKind() == Shadow.AdviceExecution) {
			int numExtraArgs = 0;
			for (int i = 0; i < argumentsToMatchAgainst.length; i++) {
				String argumentSignature = argumentsToMatchAgainst[i].getSignature();
				if (argumentSignature.startsWith(ASPECTJ_JP_SIGNATURE_PREFIX) || argumentSignature.startsWith(ASPECTJ_SYNTHETIC_SIGNATURE_PREFIX)) {
					numExtraArgs++;
				} else {
					// normal arg after AJ type means earlier arg was NOT synthetic
					numExtraArgs = 0;
				}
			}
			if (numExtraArgs > 0) {
				int newArgLength = argumentsToMatchAgainst.length - numExtraArgs;
				ResolvedType[] argsSubset = new ResolvedType[newArgLength];
				System.arraycopy(argumentsToMatchAgainst, 0, argsSubset, 0, newArgLength);
				argumentsToMatchAgainst = argsSubset;
			}
		} else if (shadow.getKind() == Shadow.ConstructorExecution) {		
			if (shadow.getMatchingSignature().getParameterTypes().length < argumentsToMatchAgainst.length) {
				// there are one or more synthetic args on the end, caused by non-public itd constructor 
				int newArgLength = shadow.getMatchingSignature().getParameterTypes().length;
				ResolvedType[] argsSubset = new ResolvedType[newArgLength];
				System.arraycopy(argumentsToMatchAgainst, 0, argsSubset, 0, newArgLength);
				argumentsToMatchAgainst = argsSubset;				
			}
		}
		
		return argumentsToMatchAgainst;
	}
	
	public FuzzyBoolean match(JoinPoint jp, JoinPoint.StaticPart jpsp) {
		FuzzyBoolean ret = arguments.matches(jp.getArgs(),TypePattern.DYNAMIC);
		// this may have given a false match (e.g. args(int) may have matched a call to doIt(Integer x)) due to boxing
		// check for this...
		if (ret == FuzzyBoolean.YES) {
			// are the sigs compatible too...
			CodeSignature sig = (CodeSignature)jp.getSignature();
			Class[] pTypes = sig.getParameterTypes();
			ret = checkSignatureMatch(pTypes);
		}
		return ret;
	}
	
	/**
	 * @param ret
	 * @param pTypes
	 * @return
	 */
	private FuzzyBoolean checkSignatureMatch(Class[] pTypes) {
		Collection tps = arguments.getExactTypes();
		int sigIndex = 0;
		for (Iterator iter = tps.iterator(); iter.hasNext();) {
			UnresolvedType tp = (UnresolvedType) iter.next();
			Class lookForClass = getPossiblyBoxed(tp);
			if (lookForClass != null) {
				boolean foundMatchInSig = false;
				while (sigIndex < pTypes.length && !foundMatchInSig) {
					if (pTypes[sigIndex++] == lookForClass) foundMatchInSig = true;
				}
				if (!foundMatchInSig) {
					return FuzzyBoolean.NO;
				}
			}
		}
		return FuzzyBoolean.YES;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesDynamically(java.lang.Object, java.lang.Object, java.lang.Object[])
	 */
	public boolean matchesDynamically(Object thisObject, Object targetObject,
			Object[] args) {
		return (arguments.matches(args,TypePattern.DYNAMIC) == FuzzyBoolean.YES);
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesStatically(java.lang.String, java.lang.reflect.Member, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesStatically(String joinpointKind, Member member,
			Class thisClass, Class targetClass, Member withinCode) {
		Class[] paramTypes = new Class[0];
		if (member instanceof Method) {
			paramTypes = ((Method)member).getParameterTypes();
		} else if (member instanceof Constructor) {
			paramTypes = ((Constructor)member).getParameterTypes();
		} else if (member instanceof PointcutExpressionImpl.Handler){
			paramTypes = new Class[] {((PointcutExpressionImpl.Handler)member).getHandledExceptionType()};
		} else if (member instanceof Field) {
			if (joinpointKind.equals(Shadow.FieldGet.getName())) return FuzzyBoolean.NO; // no args here
			paramTypes = new Class[] {((Field)member).getType()};
		} else {
			return FuzzyBoolean.NO;
		}
		return arguments.matchesArgsPatternSubset(paramTypes);
	}
	private Class getPossiblyBoxed(UnresolvedType tp) {
		Class ret = (Class) ExactTypePattern.primitiveTypesMap.get(tp.getName());
		if (ret == null) ret = (Class) ExactTypePattern.boxedPrimitivesMap.get(tp.getName());
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.NameBindingPointcut#getBindingAnnotationTypePatterns()
	 */
	public List getBindingAnnotationTypePatterns() {
		return Collections.EMPTY_LIST; 
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.NameBindingPointcut#getBindingTypePatterns()
	 */
	public List getBindingTypePatterns() {
		List l = new ArrayList();
		TypePattern[] pats = arguments.getTypePatterns();
		for (int i = 0; i < pats.length; i++) {
			if (pats[i] instanceof BindingTypePattern) {
				l.add(pats[i]);
			}
		}
		return l;
	}
	
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.ARGS);
		arguments.write(s);
		writeLocation(s);
	}
	
	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		ArgsPointcut ret = new ArgsPointcut(TypePatternList.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	
	public boolean equals(Object other) {
		if (!(other instanceof ArgsPointcut)) return false;
		ArgsPointcut o = (ArgsPointcut)other;
		return o.arguments.equals(this.arguments);
	}

    public int hashCode() {
        return arguments.hashCode();
    }
  
	public void resolveBindings(IScope scope, Bindings bindings) {
		arguments.resolveBindings(scope, bindings, true, true);
		if (arguments.ellipsisCount > 1) {
			scope.message(IMessage.ERROR, this,
					"uses more than one .. in args (compiler limitation)");
		}
	}
	
	public void resolveBindingsFromRTTI() {
		arguments.resolveBindingsFromRTTI(true, true);
		if (arguments.ellipsisCount > 1) {
			throw new UnsupportedOperationException("uses more than one .. in args (compiler limitation)");
		}		
	}
	
	public void postRead(ResolvedType enclosingType) {
		arguments.postRead(enclosingType);
	}


	public Pointcut concretize1(ResolvedType inAspect, IntMap bindings) {
		if (isDeclare(bindings.getEnclosingAdvice())) {
		  // Enforce rule about which designators are supported in declare
		  inAspect.getWorld().showMessage(IMessage.ERROR,
		  		WeaverMessages.format(WeaverMessages.ARGS_IN_DECLARE),
				bindings.getEnclosingAdvice().getSourceLocation(), null);
		  return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}
		TypePatternList args = arguments.resolveReferences(bindings);
		if (inAspect.crosscuttingMembers != null) {
			inAspect.crosscuttingMembers.exposeTypes(args.getExactTypes());
		}
		Pointcut ret = new ArgsPointcut(args);
		ret.copyLocationFrom(this);
		return ret;
	}

	private Test findResidueNoEllipsis(Shadow shadow, ExposedState state, TypePattern[] patterns) {
		ResolvedType[] argumentsToMatchAgainst = getArgumentsToMatchAgainst(shadow);
		int len = argumentsToMatchAgainst.length;
		//System.err.println("boudn to : " + len + ", " + patterns.length);
		if (patterns.length != len) {
			return Literal.FALSE;
		}
		
		Test ret = Literal.TRUE;
		
		for (int i=0; i < len; i++) {
			UnresolvedType argType = shadow.getGenericArgTypes()[i];
			TypePattern type = patterns[i];
            ResolvedType argRTX = shadow.getIWorld().resolve(argType,true);
			if (!(type instanceof BindingTypePattern)) {
                if (argRTX == ResolvedType.MISSING) {
                  IMessage msg = new Message(
                    WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_ARG_TYPE,argType.getName()),
                    "",IMessage.ERROR,shadow.getSourceLocation(),null,new ISourceLocation[]{getSourceLocation()});
                }
				if (type.matchesInstanceof(argRTX).alwaysTrue()) {
					continue;
				}
			} else {
			  BindingTypePattern btp = (BindingTypePattern)type;
			  // Check if we have already bound something to this formal
			  if ((state.get(btp.getFormalIndex())!=null) &&(lastMatchedShadowId != shadow.shadowId)) {
//			  	ISourceLocation isl = getSourceLocation();
//				Message errorMessage = new Message(
//                    "Ambiguous binding of type "+type.getExactType().toString()+
//                    " using args(..) at this line - formal is already bound"+
//                    ".  See secondary source location for location of args(..)",
//					shadow.getSourceLocation(),true,new ISourceLocation[]{getSourceLocation()});
//				shadow.getIWorld().getMessageHandler().handleMessage(errorMessage);
				state.setErroneousVar(btp.getFormalIndex());
			  }
			}

			World world = shadow.getIWorld();
			ResolvedType typeToExpose = type.getExactType().resolve(world);
			if (typeToExpose.isParameterizedType()) {
				boolean inDoubt = (type.matchesInstanceof(argRTX) == FuzzyBoolean.MAYBE);				
				if (inDoubt && world.getLint().uncheckedArgument.isEnabled()) {
					String uncheckedMatchWith = typeToExpose.getSimpleBaseName();
					if (argRTX.isParameterizedType() && (argRTX.getRawType() == typeToExpose.getRawType())) {
						uncheckedMatchWith = argRTX.getSimpleName();
					}
					if (!isUncheckedArgumentWarningSuppressed()) {
						world.getLint().uncheckedArgument.signal(
								new String[] {
										typeToExpose.getSimpleName(),
										uncheckedMatchWith,
										typeToExpose.getSimpleBaseName(),
										shadow.toResolvedString(world)},
								getSourceLocation(),
								new ISourceLocation[] {shadow.getSourceLocation()});
						}
				}
			}			
			
			ret = Test.makeAnd(ret,
				exposeStateForVar(shadow.getArgVar(i), type, state,shadow.getIWorld()));
		}
		
		return ret;		
	}

	/**
	 * We need to find out if someone has put the @SuppressAjWarnings{"uncheckedArgument"}
	 * annotation somewhere. That somewhere is going to be an a piece of advice that uses this
	 * pointcut. But how do we find it???
	 * @return
	 */
	private boolean isUncheckedArgumentWarningSuppressed() {
		return false;
	}
	
	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		if (arguments.matches(getArgumentsToMatchAgainst(shadow), TypePattern.DYNAMIC).alwaysFalse()) {
			return Literal.FALSE;
		}
		int ellipsisCount = arguments.ellipsisCount;
		if (ellipsisCount == 0) {
			return findResidueNoEllipsis(shadow, state, arguments.getTypePatterns());		
		} else if (ellipsisCount == 1) {
			TypePattern[] patternsWithEllipsis = arguments.getTypePatterns();
			TypePattern[] patternsWithoutEllipsis = new TypePattern[shadow.getArgCount()];
			int lenWithEllipsis = patternsWithEllipsis.length;
			int lenWithoutEllipsis = patternsWithoutEllipsis.length;
			// l1+1 >= l0
			int indexWithEllipsis = 0;
			int indexWithoutEllipsis = 0;
			while (indexWithoutEllipsis < lenWithoutEllipsis) {
				TypePattern p = patternsWithEllipsis[indexWithEllipsis++];
				if (p == TypePattern.ELLIPSIS) {
					int newLenWithoutEllipsis =
						lenWithoutEllipsis - (lenWithEllipsis-indexWithEllipsis);
					while (indexWithoutEllipsis < newLenWithoutEllipsis) {
						patternsWithoutEllipsis[indexWithoutEllipsis++] = TypePattern.ANY;
					}
				} else {
					patternsWithoutEllipsis[indexWithoutEllipsis++] = p;
				}
			}
			return findResidueNoEllipsis(shadow, state, patternsWithoutEllipsis);
		} else {
			throw new BetaException("unimplemented");
		}
	}
	
	public String toString() {
		return "args" + arguments.toString() + "";
	}

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
