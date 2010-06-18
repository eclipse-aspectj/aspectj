/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.weaver.tools;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.weaver.BindingScope;
import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.internal.tools.StandardPointcutExpressionImpl;
import org.aspectj.weaver.internal.tools.TypePatternMatcherImpl;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.CflowPointcut;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.IScope;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.ParserException;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.SimpleScope;
import org.aspectj.weaver.patterns.ThisOrTargetAnnotationPointcut;
import org.aspectj.weaver.patterns.ThisOrTargetPointcut;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.reflect.PointcutParameterImpl;
import org.aspectj.weaver.reflect.ReflectionWorld;

/**
 * A PointcutParser can be used to build PointcutExpressions for a user-defined subset of AspectJ's pointcut language
 */
public class StandardPointcutParser {

	private World world;
	private final Set<PointcutPrimitive> supportedPrimitives;
	private final Set<PointcutDesignatorHandler> pointcutDesignators = new HashSet<PointcutDesignatorHandler>();

	/**
	 * @return a Set containing every PointcutPrimitive except if, cflow, and cflowbelow (useful for passing to PointcutParser
	 *         constructor).
	 */
	public static Set<PointcutPrimitive> getAllSupportedPointcutPrimitives() {
		Set<PointcutPrimitive> primitives = new HashSet<PointcutPrimitive>();
		primitives.add(PointcutPrimitive.ADVICE_EXECUTION);
		primitives.add(PointcutPrimitive.ARGS);
		primitives.add(PointcutPrimitive.CALL);
		primitives.add(PointcutPrimitive.EXECUTION);
		primitives.add(PointcutPrimitive.GET);
		primitives.add(PointcutPrimitive.HANDLER);
		primitives.add(PointcutPrimitive.INITIALIZATION);
		primitives.add(PointcutPrimitive.PRE_INITIALIZATION);
		primitives.add(PointcutPrimitive.SET);
		primitives.add(PointcutPrimitive.STATIC_INITIALIZATION);
		primitives.add(PointcutPrimitive.TARGET);
		primitives.add(PointcutPrimitive.THIS);
		primitives.add(PointcutPrimitive.WITHIN);
		primitives.add(PointcutPrimitive.WITHIN_CODE);
		primitives.add(PointcutPrimitive.AT_ANNOTATION);
		primitives.add(PointcutPrimitive.AT_THIS);
		primitives.add(PointcutPrimitive.AT_TARGET);
		primitives.add(PointcutPrimitive.AT_ARGS);
		primitives.add(PointcutPrimitive.AT_WITHIN);
		primitives.add(PointcutPrimitive.AT_WITHINCODE);
		primitives.add(PointcutPrimitive.REFERENCE);

		return primitives;
	}

	/**
	 * Returns a pointcut parser that can parse the full AspectJ pointcut language with the following exceptions:
	 * <ul>
	 * <li>The <code>if, cflow, and cflowbelow</code> pointcut designators are not supported
	 * <li>Pointcut expressions must be self-contained :- they cannot contain references to other named pointcuts
	 * <li>The pointcut expression must be anonymous with no formals allowed.
	 * </ul>
	 * <p>
	 * When resolving types in pointcut expressions, the context classloader is used to find types.
	 * </p>
	 */
	public static StandardPointcutParser getPointcutParserSupportingAllPrimitives(World world) {
		StandardPointcutParser p = new StandardPointcutParser(world);
		return p;
	}

	/**
	 * Returns a pointcut parser that can parse pointcut expressions built from a user-defined subset of AspectJ's supported
	 * pointcut primitives. The following restrictions apply:
	 * <ul>
	 * <li>The <code>if, cflow, and cflowbelow</code> pointcut designators are not supported
	 * <li>Pointcut expressions must be self-contained :- they cannot contain references to other named pointcuts
	 * <li>The pointcut expression must be anonymous with no formals allowed.
	 * </ul>
	 * <p>
	 * When resolving types in pointcut expressions, the given classloader is used to find types.
	 * </p>
	 * 
	 * @param supportedPointcutKinds a set of PointcutPrimitives this parser should support
	 * @throws UnsupportedOperationException if the set contains if, cflow, or cflow below
	 */
	public static StandardPointcutParser getPointcutParserSupportingSpecifiedPrimitives(Set supportedPointcutKinds, World world) {
		StandardPointcutParser p = new StandardPointcutParser(supportedPointcutKinds, world);
		return p;
	}

	/**
	 * Create a pointcut parser that can parse the full AspectJ pointcut language with the following exceptions:
	 * <ul>
	 * <li>The <code>if, cflow, and cflowbelow</code> pointcut designators are not supported
	 * <li>Pointcut expressions must be self-contained :- they cannot contain references to other named pointcuts
	 * <li>The pointcut expression must be anonymous with no formals allowed.
	 * </ul>
	 */
	protected StandardPointcutParser(World world) {
		supportedPrimitives = getAllSupportedPointcutPrimitives();
		this.world = world;
	}

	/**
	 * Create a pointcut parser that can parse pointcut expressions built from a user-defined subset of AspectJ's supported pointcut
	 * primitives. The following restrictions apply:
	 * <ul>
	 * <li>The <code>if, cflow, and cflowbelow</code> pointcut designators are not supported
	 * <li>Pointcut expressions must be self-contained :- they cannot contain references to other named pointcuts
	 * <li>The pointcut expression must be anonymous with no formals allowed.
	 * </ul>
	 * 
	 * @param supportedPointcutKinds a set of PointcutPrimitives this parser should support
	 * @throws UnsupportedOperationException if the set contains if, cflow, or cflow below
	 */
	private StandardPointcutParser(Set/* <PointcutPrimitives> */supportedPointcutKinds, World world) {
		supportedPrimitives = supportedPointcutKinds;
		for (Iterator iter = supportedPointcutKinds.iterator(); iter.hasNext();) {
			PointcutPrimitive element = (PointcutPrimitive) iter.next();
			if ((element == PointcutPrimitive.IF) || (element == PointcutPrimitive.CFLOW)
					|| (element == PointcutPrimitive.CFLOW_BELOW)) {
				throw new UnsupportedOperationException("Cannot handle if, cflow, and cflowbelow primitives");
			}
		}
		this.world = world;
	}

	// /**
	// * Set the lint properties for this parser from the given resource on the classpath.
	// *
	// * @param resourcePath path to a file containing aspectj lint properties
	// */
	// public void setLintProperties(String resourcePath) throws IOException {
	// URL url = this.classLoaderReference.getClassLoader().getResource(resourcePath);
	// InputStream is = url.openStream();
	// Properties p = new Properties();
	// p.load(is);
	// setLintProperties(p);
	// }

	/**
	 * Set the lint properties for this parser from the given properties set.
	 * 
	 * @param properties
	 */
	public void setLintProperties(Properties properties) {
		getWorld().getLint().setFromProperties(properties);
	}

	/**
	 * Register a new pointcut designator handler with this parser. This provides an extension mechansim for the integration of
	 * domain-specific pointcut designators with the AspectJ pointcut language.
	 * 
	 * @param designatorHandler
	 */
	public void registerPointcutDesignatorHandler(PointcutDesignatorHandler designatorHandler) {
		this.pointcutDesignators.add(designatorHandler);
		if (world != null) {
			world.registerPointcutHandler(designatorHandler);
		}
	}

	/**
	 * Create a pointcut parameter of the given name and type.
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	public PointcutParameter createPointcutParameter(String name, Class type) {
		return new PointcutParameterImpl(name, type);
	}

	/**
	 * Parse the given pointcut expression. A global scope is assumed for resolving any type references, and the pointcut must
	 * contain no formals (variables to be bound).
	 * 
	 * @throws UnsupportedPointcutPrimitiveException if the parser encounters a primitive pointcut expression of a kind not
	 *         supported by this PointcutParser.
	 * @throws IllegalArgumentException if the expression is not a well-formed pointcut expression
	 */
	public StandardPointcutExpression parsePointcutExpression(String expression) throws UnsupportedPointcutPrimitiveException,
			IllegalArgumentException {
		return parsePointcutExpression(expression, null, new PointcutParameter[0]);
	}

	/**
	 * Parse the given pointcut expression. The pointcut is resolved as if it had been declared inside the inScope class (this
	 * allows the pointcut to contain unqualified references to other pointcuts declared in the same type for example). The pointcut
	 * may contain zero or more formal parameters to be bound at matched join points.
	 * 
	 * @throws UnsupportedPointcutPrimitiveException if the parser encounters a primitive pointcut expression of a kind not
	 *         supported by this PointcutParser.
	 * @throws IllegalArgumentException if the expression is not a well-formed pointcut expression
	 */
	public StandardPointcutExpression parsePointcutExpression(String expression, Class inScope, PointcutParameter[] formalParameters)
			throws UnsupportedPointcutPrimitiveException, IllegalArgumentException {
		StandardPointcutExpressionImpl pcExpr = null;
		try {
			Pointcut pc = resolvePointcutExpression(expression, inScope, formalParameters);
			pc = concretizePointcutExpression(pc, inScope, formalParameters);
			validateAgainstSupportedPrimitives(pc, expression); // again, because we have now followed any ref'd pcuts
			pcExpr = new StandardPointcutExpressionImpl(pc, expression, formalParameters, getWorld());
		} catch (ParserException pEx) {
			throw new IllegalArgumentException(buildUserMessageFromParserException(expression, pEx));
		} catch (ReflectionWorld.ReflectionWorldException rwEx) {
			rwEx.printStackTrace();
			throw new IllegalArgumentException(rwEx.getMessage());
		}
		return pcExpr;
	}

	protected Pointcut resolvePointcutExpression(String expression, Class<?> inScope, PointcutParameter[] formalParameters) {
		try {
			PatternParser parser = new PatternParser(expression);
			parser.setPointcutDesignatorHandlers(pointcutDesignators, world);
			Pointcut pc = parser.parsePointcut();
			validateAgainstSupportedPrimitives(pc, expression);
			IScope resolutionScope = buildResolutionScope((inScope == null ? Object.class : inScope), formalParameters);
			pc = pc.resolve(resolutionScope);
			return pc;
		} catch (ParserException pEx) {
			throw new IllegalArgumentException(buildUserMessageFromParserException(expression, pEx));
		}
	}

	protected Pointcut concretizePointcutExpression(Pointcut pc, Class<?> inScope, PointcutParameter[] formalParameters) {
		ResolvedType declaringTypeForResolution = null;
		if (inScope != null) {
			declaringTypeForResolution = getWorld().resolve(inScope.getName());
		} else {
			declaringTypeForResolution = ResolvedType.OBJECT.resolve(getWorld());
		}
		IntMap arity = new IntMap(formalParameters.length);
		for (int i = 0; i < formalParameters.length; i++) {
			arity.put(i, i);
		}
		return pc.concretize(declaringTypeForResolution, declaringTypeForResolution, arity);
	}

	/**
	 * Parse the given aspectj type pattern, and return a matcher that can be used to match types using it.
	 * 
	 * @param typePattern an aspectj type pattern
	 * @return a type pattern matcher that matches using the given pattern
	 * @throws IllegalArgumentException if the type pattern cannot be successfully parsed.
	 */
	public TypePatternMatcher parseTypePattern(String typePattern) throws IllegalArgumentException {
		try {
			TypePattern tp = new PatternParser(typePattern).parseTypePattern();
			tp.resolve(world);
			return new TypePatternMatcherImpl(tp, world);
		} catch (ParserException pEx) {
			throw new IllegalArgumentException(buildUserMessageFromParserException(typePattern, pEx));
		} catch (ReflectionWorld.ReflectionWorldException rwEx) {
			throw new IllegalArgumentException(rwEx.getMessage());
		}
	}

	private World getWorld() {
		return world;
	}

	/* for testing */
	Set getSupportedPrimitives() {
		return supportedPrimitives;
	}

	/* for testing */
	IMessageHandler setCustomMessageHandler(IMessageHandler aHandler) {
		IMessageHandler current = getWorld().getMessageHandler();
		getWorld().setMessageHandler(aHandler);
		return current;
	}

	private IScope buildResolutionScope(Class inScope, PointcutParameter[] formalParameters) {
		if (formalParameters == null) {
			formalParameters = new PointcutParameter[0];
		}
		FormalBinding[] formalBindings = new FormalBinding[formalParameters.length];
		for (int i = 0; i < formalBindings.length; i++) {
			formalBindings[i] = new FormalBinding(toUnresolvedType(formalParameters[i].getType()), formalParameters[i].getName(), i);
		}
		if (inScope == null) {
			SimpleScope ss = new SimpleScope(getWorld(), formalBindings);
			ss.setImportedPrefixes(new String[] { "java.lang.", "java.util." });
			return ss;
		} else {
			ResolvedType inType = getWorld().resolve(inScope.getName());
			ISourceContext sourceContext = new ISourceContext() {
				public ISourceLocation makeSourceLocation(IHasPosition position) {
					return new SourceLocation(new File(""), 0);
				}

				public ISourceLocation makeSourceLocation(int line, int offset) {
					return new SourceLocation(new File(""), line);
				}

				public int getOffset() {
					return 0;
				}

				public void tidy() {
				}
			};
			BindingScope bScope = new BindingScope(inType, sourceContext, formalBindings);
			bScope.setImportedPrefixes(new String[] { "java.lang.", "java.util." });
			return bScope;
		}
	}

	private UnresolvedType toUnresolvedType(Class clazz) {
		if (clazz.isArray()) {
			return UnresolvedType.forSignature(clazz.getName().replace('.', '/'));
		} else {
			return UnresolvedType.forName(clazz.getName());
		}
	}

	private void validateAgainstSupportedPrimitives(Pointcut pc, String expression) {
		switch (pc.getPointcutKind()) {
		case Pointcut.AND:
			validateAgainstSupportedPrimitives(((AndPointcut) pc).getLeft(), expression);
			validateAgainstSupportedPrimitives(((AndPointcut) pc).getRight(), expression);
			break;
		case Pointcut.ARGS:
			if (!supportedPrimitives.contains(PointcutPrimitive.ARGS)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.ARGS);
			}
			break;
		case Pointcut.CFLOW:
			CflowPointcut cfp = (CflowPointcut) pc;
			if (cfp.isCflowBelow()) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.CFLOW_BELOW);
			} else {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.CFLOW);
			}
		case Pointcut.HANDLER:
			if (!supportedPrimitives.contains(PointcutPrimitive.HANDLER)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.HANDLER);
			}
			break;
		case Pointcut.IF:
		case Pointcut.IF_FALSE:
		case Pointcut.IF_TRUE:
			throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.IF);
		case Pointcut.KINDED:
			validateKindedPointcut(((KindedPointcut) pc), expression);
			break;
		case Pointcut.NOT:
			validateAgainstSupportedPrimitives(((NotPointcut) pc).getNegatedPointcut(), expression);
			break;
		case Pointcut.OR:
			validateAgainstSupportedPrimitives(((OrPointcut) pc).getLeft(), expression);
			validateAgainstSupportedPrimitives(((OrPointcut) pc).getRight(), expression);
			break;
		case Pointcut.THIS_OR_TARGET:
			boolean isThis = ((ThisOrTargetPointcut) pc).isThis();
			if (isThis && !supportedPrimitives.contains(PointcutPrimitive.THIS)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.THIS);
			} else if (!supportedPrimitives.contains(PointcutPrimitive.TARGET)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.TARGET);
			}
			break;
		case Pointcut.WITHIN:
			if (!supportedPrimitives.contains(PointcutPrimitive.WITHIN)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.WITHIN);
			}
			break;
		case Pointcut.WITHINCODE:
			if (!supportedPrimitives.contains(PointcutPrimitive.WITHIN_CODE)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.WITHIN_CODE);
			}
			break;
		case Pointcut.ATTHIS_OR_TARGET:
			isThis = ((ThisOrTargetAnnotationPointcut) pc).isThis();
			if (isThis && !supportedPrimitives.contains(PointcutPrimitive.AT_THIS)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.AT_THIS);
			} else if (!supportedPrimitives.contains(PointcutPrimitive.AT_TARGET)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.AT_TARGET);
			}
			break;
		case Pointcut.ATARGS:
			if (!supportedPrimitives.contains(PointcutPrimitive.AT_ARGS)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.AT_ARGS);
			}
			break;
		case Pointcut.ANNOTATION:
			if (!supportedPrimitives.contains(PointcutPrimitive.AT_ANNOTATION)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.AT_ANNOTATION);
			}
			break;
		case Pointcut.ATWITHIN:
			if (!supportedPrimitives.contains(PointcutPrimitive.AT_WITHIN)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.AT_WITHIN);
			}
			break;
		case Pointcut.ATWITHINCODE:
			if (!supportedPrimitives.contains(PointcutPrimitive.AT_WITHINCODE)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.AT_WITHINCODE);
			}
			break;
		case Pointcut.REFERENCE:
			if (!supportedPrimitives.contains(PointcutPrimitive.REFERENCE)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.REFERENCE);
			}
			break;
		case Pointcut.USER_EXTENSION:
			// always ok...
			break;
		case Pointcut.NONE: // deliberate fall-through
		default:
			throw new IllegalArgumentException("Unknown pointcut kind: " + pc.getPointcutKind());
		}
	}

	private void validateKindedPointcut(KindedPointcut pc, String expression) {
		Shadow.Kind kind = pc.getKind();
		if ((kind == Shadow.MethodCall) || (kind == Shadow.ConstructorCall)) {
			if (!supportedPrimitives.contains(PointcutPrimitive.CALL)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.CALL);
			}
		} else if ((kind == Shadow.MethodExecution) || (kind == Shadow.ConstructorExecution)) {
			if (!supportedPrimitives.contains(PointcutPrimitive.EXECUTION)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.EXECUTION);
			}
		} else if (kind == Shadow.AdviceExecution) {
			if (!supportedPrimitives.contains(PointcutPrimitive.ADVICE_EXECUTION)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.ADVICE_EXECUTION);
			}
		} else if (kind == Shadow.FieldGet) {
			if (!supportedPrimitives.contains(PointcutPrimitive.GET)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.GET);
			}
		} else if (kind == Shadow.FieldSet) {
			if (!supportedPrimitives.contains(PointcutPrimitive.SET)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.SET);
			}
		} else if (kind == Shadow.Initialization) {
			if (!supportedPrimitives.contains(PointcutPrimitive.INITIALIZATION)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.INITIALIZATION);
			}
		} else if (kind == Shadow.PreInitialization) {
			if (!supportedPrimitives.contains(PointcutPrimitive.PRE_INITIALIZATION)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.PRE_INITIALIZATION);
			}
		} else if (kind == Shadow.StaticInitialization) {
			if (!supportedPrimitives.contains(PointcutPrimitive.STATIC_INITIALIZATION)) {
				throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.STATIC_INITIALIZATION);
			}
		}
	}

	private String buildUserMessageFromParserException(String pc, ParserException ex) {
		StringBuffer msg = new StringBuffer();
		msg.append("Pointcut is not well-formed: expecting '");
		msg.append(ex.getMessage());
		msg.append("'");
		IHasPosition location = ex.getLocation();
		msg.append(" at character position ");
		msg.append(location.getStart());
		msg.append("\n");
		msg.append(pc);
		msg.append("\n");
		for (int i = 0; i < location.getStart(); i++) {
			msg.append(" ");
		}
		for (int j = location.getStart(); j <= location.getEnd(); j++) {
			msg.append("^");
		}
		msg.append("\n");
		return msg.toString();
	}
}
