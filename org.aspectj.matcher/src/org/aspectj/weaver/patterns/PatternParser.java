/* *******************************************************************
 * Copyright (c) 2002,2010
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation
 *     Adrian Colyer, IBM
 *     Andy Clement, IBM, SpringSource
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberKind;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.internal.tools.PointcutDesignatorHandlerBasedPointcut;
import org.aspectj.weaver.tools.ContextBasedMatcher;
import org.aspectj.weaver.tools.PointcutDesignatorHandler;

/**
 * @author PARC
 * @author Adrian Colyer
 * @author Andy Clement
 */
// XXX doesn't handle errors for extra tokens very well (sometimes ignores)
public class PatternParser {

	private ITokenSource tokenSource;
	private ISourceContext sourceContext;

	/** not thread-safe, but this class is not intended to be... */
	private boolean allowHasTypePatterns = false;

	/** extension handlers used in weaver tools API only */
	private Set<PointcutDesignatorHandler> pointcutDesignatorHandlers = Collections.emptySet();
	private World world;

	/**
	 * Constructor for PatternParser.
	 */
	public PatternParser(ITokenSource tokenSource) {
		super();
		this.tokenSource = tokenSource;
		this.sourceContext = tokenSource.getSourceContext();
	}

	/** only used by weaver tools API */
	public void setPointcutDesignatorHandlers(Set<PointcutDesignatorHandler> handlers, World world) {
		this.pointcutDesignatorHandlers = handlers;
		this.world = world;
	}

	public PerClause maybeParsePerClause() {
		IToken tok = tokenSource.peek();
		if (tok == IToken.EOF) {
			return null;
		}
		if (tok.isIdentifier()) {
			String name = tok.getString();
			if (name.equals("issingleton")) {
				return parsePerSingleton();
			} else if (name.equals("perthis")) {
				return parsePerObject(true);
			} else if (name.equals("pertarget")) {
				return parsePerObject(false);
			} else if (name.equals("percflow")) {
				return parsePerCflow(false);
			} else if (name.equals("percflowbelow")) {
				return parsePerCflow(true);
			} else if (name.equals("pertypewithin")) { // PTWIMPL Parse the pertypewithin clause
				return parsePerTypeWithin();
			} else {
				return null;
			}
		}
		return null;
	}

	private PerClause parsePerCflow(boolean isBelow) {
		parseIdentifier();
		eat("(");
		Pointcut entry = parsePointcut();
		eat(")");
		return new PerCflow(entry, isBelow);
	}

	private PerClause parsePerObject(boolean isThis) {
		parseIdentifier();
		eat("(");
		Pointcut entry = parsePointcut();
		eat(")");
		return new PerObject(entry, isThis);
	}

	private PerClause parsePerTypeWithin() {
		parseIdentifier();
		eat("(");
		TypePattern withinTypePattern = parseTypePattern();
		eat(")");
		return new PerTypeWithin(withinTypePattern);
	}

	private PerClause parsePerSingleton() {
		parseIdentifier();
		eat("(");
		eat(")");
		return new PerSingleton();
	}

	public Declare parseDeclare() {
		int startPos = tokenSource.peek().getStart();

		eatIdentifier("declare");
		String kind = parseIdentifier();
		Declare ret;
		if (kind.equals("error")) {
			eat(":");
			ret = parseErrorOrWarning(true);
		} else if (kind.equals("warning")) {
			eat(":");
			ret = parseErrorOrWarning(false);
		} else if (kind.equals("precedence")) {
			eat(":");
			ret = parseDominates();
		} else if (kind.equals("dominates")) {
			throw new ParserException("name changed to declare precedence", tokenSource.peek(-2));
		} else if (kind.equals("parents")) {
			ret = parseParents();
		} else if (kind.equals("soft")) {
			eat(":");
			ret = parseSoft();
		} else {
			throw new ParserException(
					"expected one of error, warning, parents, soft, precedence, @type, @method, @constructor, @field",
					tokenSource.peek(-1));
		}
		int endPos = tokenSource.peek(-1).getEnd();
		ret.setLocation(sourceContext, startPos, endPos);
		return ret;
	}

	public Declare parseDeclareAnnotation() {
		int startPos = tokenSource.peek().getStart();

		eatIdentifier("declare");
		eat("@");
		String kind = parseIdentifier();
		eat(":");
		Declare ret;
		if (kind.equals("type")) {
			ret = parseDeclareAtType();
		} else if (kind.equals("method")) {
			ret = parseDeclareAtMethod(true);
		} else if (kind.equals("field")) {
			ret = parseDeclareAtField();
		} else if (kind.equals("constructor")) {
			ret = parseDeclareAtMethod(false);
		} else {
			throw new ParserException("one of type, method, field, constructor", tokenSource.peek(-1));
		}
		eat(";");
		int endPos = tokenSource.peek(-1).getEnd();
		ret.setLocation(sourceContext, startPos, endPos);
		return ret;

	}

	public DeclareAnnotation parseDeclareAtType() {
		allowHasTypePatterns = true;
		TypePattern p = parseTypePattern();
		allowHasTypePatterns = false;
		return new DeclareAnnotation(DeclareAnnotation.AT_TYPE, p);
	}

	public DeclareAnnotation parseDeclareAtMethod(boolean isMethod) {
		ISignaturePattern sp = parseCompoundMethodOrConstructorSignaturePattern(isMethod);// parseMethodOrConstructorSignaturePattern();

		if (!isMethod) {
			return new DeclareAnnotation(DeclareAnnotation.AT_CONSTRUCTOR, sp);
		} else {
			return new DeclareAnnotation(DeclareAnnotation.AT_METHOD, sp);
		}
	}

	public DeclareAnnotation parseDeclareAtField() {
		ISignaturePattern compoundFieldSignaturePattern = parseCompoundFieldSignaturePattern();
		DeclareAnnotation da = new DeclareAnnotation(DeclareAnnotation.AT_FIELD, compoundFieldSignaturePattern);
		return da;
	}

	public ISignaturePattern parseCompoundFieldSignaturePattern() {
		int index = tokenSource.getIndex();
		try {
			ISignaturePattern atomicFieldSignaturePattern = parseMaybeParenthesizedFieldSignaturePattern();

			while (isEitherAndOrOr()) {
				if (maybeEat("&&")) {
					atomicFieldSignaturePattern = new AndSignaturePattern(atomicFieldSignaturePattern,
							parseMaybeParenthesizedFieldSignaturePattern());
				}
				if (maybeEat("||")) {
					atomicFieldSignaturePattern = new OrSignaturePattern(atomicFieldSignaturePattern,
							parseMaybeParenthesizedFieldSignaturePattern());
				}
			}
			return atomicFieldSignaturePattern;
		} catch (ParserException e) {
			// fallback in the case of a regular single field signature pattern that just happened to start with '('
			int nowAt = tokenSource.getIndex();
			tokenSource.setIndex(index);
			try {
				ISignaturePattern fsp = parseFieldSignaturePattern();
				return fsp;
			} catch (Exception e2) {
				tokenSource.setIndex(nowAt);
				// throw the original
				throw e;
			}
		}
	}

	private boolean isEitherAndOrOr() {
		String tokenstring = tokenSource.peek().getString();
		return tokenstring.equals("&&") || tokenstring.equals("||");
	}

	public ISignaturePattern parseCompoundMethodOrConstructorSignaturePattern(boolean isMethod) {
		ISignaturePattern atomicMethodCtorSignaturePattern = parseMaybeParenthesizedMethodOrConstructorSignaturePattern(isMethod);

		while (isEitherAndOrOr()) {
			if (maybeEat("&&")) {
				atomicMethodCtorSignaturePattern = new AndSignaturePattern(atomicMethodCtorSignaturePattern,
						parseMaybeParenthesizedMethodOrConstructorSignaturePattern(isMethod));
			}
			if (maybeEat("||")) {
				atomicMethodCtorSignaturePattern = new OrSignaturePattern(atomicMethodCtorSignaturePattern,
						parseMaybeParenthesizedMethodOrConstructorSignaturePattern(isMethod));
			}
		}
		return atomicMethodCtorSignaturePattern;
	}

	public DeclarePrecedence parseDominates() {
		List<TypePattern> l = new ArrayList<TypePattern>();
		do {
			l.add(parseTypePattern());
		} while (maybeEat(","));

		return new DeclarePrecedence(l);
	}

	private Declare parseParents() {
		/*
		 * simplified design requires use of raw types for declare parents, no generic spec. allowed String[] typeParameters =
		 * maybeParseSimpleTypeVariableList();
		 */
		eat(":");
		allowHasTypePatterns = true;
		TypePattern p = parseTypePattern(false, false);
		allowHasTypePatterns = false;
		IToken t = tokenSource.next();
		if (!(t.getString().equals("extends") || t.getString().equals("implements"))) {
			throw new ParserException("extends or implements", t);
		}
		boolean isExtends = t.getString().equals("extends");

		List<TypePattern> l = new ArrayList<TypePattern>();
		do {
			l.add(parseTypePattern());
		} while (maybeEat(","));

		// XXX somewhere in the chain we need to enforce that we have only ExactTypePatterns

		DeclareParents decp = new DeclareParents(p, l, isExtends);
		return decp;
	}

	private Declare parseSoft() {
		TypePattern p = parseTypePattern();
		eat(":");
		Pointcut pointcut = parsePointcut();
		return new DeclareSoft(p, pointcut);
	}

	/**
	 * Attempt to parse a pointcut, if that fails then try again for a type pattern.
	 * 
	 * @param isError true if it is declare error rather than declare warning
	 * @return the new declare
	 */
	private Declare parseErrorOrWarning(boolean isError) {
		Pointcut pointcut = null;
		int index = tokenSource.getIndex();
		try {
			pointcut = parsePointcut();
		} catch (ParserException pe) {
			try {
				tokenSource.setIndex(index);
				boolean oldValue = allowHasTypePatterns;
				TypePattern typePattern = null;
				try {
					allowHasTypePatterns = true;
					typePattern = parseTypePattern();
				} finally {
					allowHasTypePatterns = oldValue;
				}
				eat(":");
				String message = parsePossibleStringSequence(true);
				return new DeclareTypeErrorOrWarning(isError, typePattern, message);
			} catch (ParserException pe2) {
				// deliberately throw the original problem
				throw pe;
			}
		}
		eat(":");
		String message = parsePossibleStringSequence(true);
		return new DeclareErrorOrWarning(isError, pointcut, message);
	}

	public Pointcut parsePointcut() {
		Pointcut p = parseAtomicPointcut();
		if (maybeEat("&&")) {
			p = new AndPointcut(p, parseNotOrPointcut());
		}

		if (maybeEat("||")) {
			p = new OrPointcut(p, parsePointcut());
		}

		return p;
	}

	private Pointcut parseNotOrPointcut() {
		Pointcut p = parseAtomicPointcut();
		if (maybeEat("&&")) {
			p = new AndPointcut(p, parseNotOrPointcut());
		}
		return p;
	}

	private Pointcut parseAtomicPointcut() {
		if (maybeEat("!")) {
			int startPos = tokenSource.peek(-1).getStart();
			Pointcut p = new NotPointcut(parseAtomicPointcut(), startPos);
			return p;
		}
		if (maybeEat("(")) {
			Pointcut p = parsePointcut();
			eat(")");
			return p;
		}
		if (maybeEat("@")) {
			int startPos = tokenSource.peek().getStart();
			Pointcut p = parseAnnotationPointcut();
			int endPos = tokenSource.peek(-1).getEnd();
			p.setLocation(sourceContext, startPos, endPos);
			return p;
		}
		int startPos = tokenSource.peek().getStart();
		Pointcut p = parseSinglePointcut();
		int endPos = tokenSource.peek(-1).getEnd();
		p.setLocation(sourceContext, startPos, endPos);
		return p;
	}

	public Pointcut parseSinglePointcut() {
		int start = tokenSource.getIndex();
		IToken t = tokenSource.peek();
		Pointcut p = t.maybeGetParsedPointcut();
		if (p != null) {
			tokenSource.next();
			return p;
		}

		String kind = parseIdentifier();
		// IToken possibleTypeVariableToken = tokenSource.peek();
		// String[] typeVariables = maybeParseSimpleTypeVariableList();
		if (kind.equals("execution") || kind.equals("call") || kind.equals("get") || kind.equals("set")) {
			p = parseKindedPointcut(kind);
		} else if (kind.equals("args")) {
			p = parseArgsPointcut();
		} else if (kind.equals("this")) {
			p = parseThisOrTargetPointcut(kind);
		} else if (kind.equals("target")) {
			p = parseThisOrTargetPointcut(kind);
		} else if (kind.equals("within")) {
			p = parseWithinPointcut();
		} else if (kind.equals("withincode")) {
			p = parseWithinCodePointcut();
		} else if (kind.equals("cflow")) {
			p = parseCflowPointcut(false);
		} else if (kind.equals("cflowbelow")) {
			p = parseCflowPointcut(true);
		} else if (kind.equals("adviceexecution")) {
			eat("(");
			eat(")");
			p = new KindedPointcut(Shadow.AdviceExecution, new SignaturePattern(Member.ADVICE, ModifiersPattern.ANY,
					TypePattern.ANY, TypePattern.ANY, NamePattern.ANY, TypePatternList.ANY, ThrowsPattern.ANY,
					AnnotationTypePattern.ANY));
		} else if (kind.equals("handler")) {
			eat("(");
			TypePattern typePat = parseTypePattern(false, false);
			eat(")");
			p = new HandlerPointcut(typePat);
		} else if (kind.equals("lock") || kind.equals("unlock")) {
			p = parseMonitorPointcut(kind);
		} else if (kind.equals("initialization")) {
			eat("(");
			SignaturePattern sig = parseConstructorSignaturePattern();
			eat(")");
			p = new KindedPointcut(Shadow.Initialization, sig);
		} else if (kind.equals("staticinitialization")) {
			eat("(");
			TypePattern typePat = parseTypePattern(false, false);
			eat(")");
			p = new KindedPointcut(Shadow.StaticInitialization, new SignaturePattern(Member.STATIC_INITIALIZATION,
					ModifiersPattern.ANY, TypePattern.ANY, typePat, NamePattern.ANY, TypePatternList.EMPTY, ThrowsPattern.ANY,
					AnnotationTypePattern.ANY));
		} else if (kind.equals("preinitialization")) {
			eat("(");
			SignaturePattern sig = parseConstructorSignaturePattern();
			eat(")");
			p = new KindedPointcut(Shadow.PreInitialization, sig);
		} else if (kind.equals("if")) {
			// - annotation style only allows if(), if(true) or if(false)
			// - if() means the body of the annotated method represents the if expression
			// - anything else is an error because code cannot be put into the if()
			// - code style will already have been processed and the call to maybeGetParsedPointcut()
			// at the top of this method will have succeeded.
			eat("(");
			if (maybeEatIdentifier("true")) {
				eat(")");
				p = new IfPointcut.IfTruePointcut();
			} else if (maybeEatIdentifier("false")) {
				eat(")");
				p = new IfPointcut.IfFalsePointcut();
			} else {
				if (!maybeEat(")")) {
					throw new ParserException(
							"in annotation style, if(...) pointcuts cannot contain code. Use if() and put the code in the annotated method",
							t);
				}
				// TODO - Alex has some token stuff going on here to get a readable name in place of ""...
				p = new IfPointcut("");
			}
		} else {
			boolean matchedByExtensionDesignator = false;
			// see if a registered handler wants to parse it, otherwise
			// treat as a reference pointcut
			for (PointcutDesignatorHandler pcd : pointcutDesignatorHandlers) {
				if (pcd.getDesignatorName().equals(kind)) {
					p = parseDesignatorPointcut(pcd);
					matchedByExtensionDesignator = true;
				}

			}
			if (!matchedByExtensionDesignator) {
				tokenSource.setIndex(start);
				p = parseReferencePointcut();
			}
		}
		return p;
	}

	private void assertNoTypeVariables(String[] tvs, String errorMessage, IToken token) {
		if (tvs != null) {
			throw new ParserException(errorMessage, token);
		}
	}

	public Pointcut parseAnnotationPointcut() {
		int start = tokenSource.getIndex();
		IToken t = tokenSource.peek();
		String kind = parseIdentifier();
		IToken possibleTypeVariableToken = tokenSource.peek();
		String[] typeVariables = maybeParseSimpleTypeVariableList();
		if (typeVariables != null) {
			String message = "(";
			assertNoTypeVariables(typeVariables, message, possibleTypeVariableToken);
		}
		tokenSource.setIndex(start);
		if (kind.equals("annotation")) {
			return parseAtAnnotationPointcut();
		} else if (kind.equals("args")) {
			return parseArgsAnnotationPointcut();
		} else if (kind.equals("this") || kind.equals("target")) {
			return parseThisOrTargetAnnotationPointcut();
		} else if (kind.equals("within")) {
			return parseWithinAnnotationPointcut();
		} else if (kind.equals("withincode")) {
			return parseWithinCodeAnnotationPointcut();
		}
		throw new ParserException("pointcut name", t);
	}

	private Pointcut parseAtAnnotationPointcut() {
		parseIdentifier();
		eat("(");
		if (maybeEat(")")) {
			throw new ParserException("@AnnotationName or parameter", tokenSource.peek());
		}
		ExactAnnotationTypePattern type = parseAnnotationNameOrVarTypePattern();
		eat(")");
		return new AnnotationPointcut(type);
	}

	private SignaturePattern parseConstructorSignaturePattern() {
		SignaturePattern ret = parseMethodOrConstructorSignaturePattern();
		if (ret.getKind() == Member.CONSTRUCTOR) {
			return ret;
		}

		throw new ParserException("constructor pattern required, found method pattern", ret);
	}

	private Pointcut parseWithinCodePointcut() {
		// parseIdentifier();
		eat("(");
		SignaturePattern sig = parseMethodOrConstructorSignaturePattern();
		eat(")");
		return new WithincodePointcut(sig);
	}

	private Pointcut parseCflowPointcut(boolean isBelow) {
		// parseIdentifier();
		eat("(");
		Pointcut entry = parsePointcut();
		eat(")");
		return new CflowPointcut(entry, isBelow, null);
	}

	/**
	 * Method parseWithinPointcut.
	 * 
	 * @return Pointcut
	 */
	private Pointcut parseWithinPointcut() {
		// parseIdentifier();
		eat("(");
		TypePattern type = parseTypePattern();
		eat(")");
		return new WithinPointcut(type);
	}

	/**
	 * Method parseThisOrTargetPointcut.
	 * 
	 * @return Pointcut
	 */
	private Pointcut parseThisOrTargetPointcut(String kind) {
		eat("(");
		TypePattern type = parseTypePattern();
		eat(")");
		return new ThisOrTargetPointcut(kind.equals("this"), type);
	}

	private Pointcut parseThisOrTargetAnnotationPointcut() {
		String kind = parseIdentifier();
		eat("(");
		if (maybeEat(")")) {
			throw new ParserException("expecting @AnnotationName or parameter, but found ')'", tokenSource.peek());
		}
		ExactAnnotationTypePattern type = parseAnnotationNameOrVarTypePattern();
		eat(")");
		return new ThisOrTargetAnnotationPointcut(kind.equals("this"), type);
	}

	private Pointcut parseWithinAnnotationPointcut() {
		/* String kind = */parseIdentifier();
		eat("(");
		if (maybeEat(")")) {
			throw new ParserException("expecting @AnnotationName or parameter, but found ')'", tokenSource.peek());
		}
		AnnotationTypePattern type = parseAnnotationNameOrVarTypePattern();
		eat(")");
		return new WithinAnnotationPointcut(type);
	}

	private Pointcut parseWithinCodeAnnotationPointcut() {
		/* String kind = */parseIdentifier();
		eat("(");
		if (maybeEat(")")) {
			throw new ParserException("expecting @AnnotationName or parameter, but found ')'", tokenSource.peek());
		}
		ExactAnnotationTypePattern type = parseAnnotationNameOrVarTypePattern();
		eat(")");
		return new WithinCodeAnnotationPointcut(type);
	}

	/**
	 * Method parseArgsPointcut.
	 * 
	 * @return Pointcut
	 */
	private Pointcut parseArgsPointcut() {
		// parseIdentifier();
		TypePatternList arguments = parseArgumentsPattern(false);
		return new ArgsPointcut(arguments);
	}

	private Pointcut parseArgsAnnotationPointcut() {
		parseIdentifier();
		AnnotationPatternList arguments = parseArgumentsAnnotationPattern();
		return new ArgsAnnotationPointcut(arguments);
	}

	private Pointcut parseReferencePointcut() {
		TypePattern onType = parseTypePattern();
		NamePattern name = null;
		if (onType.typeParameters.size() > 0) {
			eat(".");
			name = parseNamePattern();
		} else {
			name = tryToExtractName(onType);
		}
		if (name == null) {
			throw new ParserException("name pattern", tokenSource.peek());
		}
		if (onType.toString().equals("")) {
			onType = null;
		}

		String simpleName = name.maybeGetSimpleName();
		if (simpleName == null) {
			throw new ParserException("(", tokenSource.peek(-1));
		}

		TypePatternList arguments = parseArgumentsPattern(false);
		return new ReferencePointcut(onType, simpleName, arguments);
	}

	private Pointcut parseDesignatorPointcut(PointcutDesignatorHandler pcdHandler) {
		eat("(");
		int parenCount = 1;
		StringBuffer pointcutBody = new StringBuffer();
		while (parenCount > 0) {
			if (maybeEat("(")) {
				parenCount++;
				pointcutBody.append("(");
			} else if (maybeEat(")")) {
				parenCount--;
				if (parenCount > 0) {
					pointcutBody.append(")");
				}
			} else {
				pointcutBody.append(nextToken().getString());
			}
		}
		ContextBasedMatcher pcExpr = pcdHandler.parse(pointcutBody.toString());
		return new PointcutDesignatorHandlerBasedPointcut(pcExpr, world);
	}

	public List<String> parseDottedIdentifier() {
		List<String> ret = new ArrayList<String>();
		ret.add(parseIdentifier());
		while (maybeEat(".")) {
			ret.add(parseIdentifier());
		}
		return ret;
	}

	private KindedPointcut parseKindedPointcut(String kind) {
		eat("(");
		SignaturePattern sig;

		Shadow.Kind shadowKind = null;
		if (kind.equals("execution")) {
			sig = parseMethodOrConstructorSignaturePattern();
			if (sig.getKind() == Member.METHOD) {
				shadowKind = Shadow.MethodExecution;
			} else if (sig.getKind() == Member.CONSTRUCTOR) {
				shadowKind = Shadow.ConstructorExecution;
			}
		} else if (kind.equals("call")) {
			sig = parseMethodOrConstructorSignaturePattern();
			if (sig.getKind() == Member.METHOD) {
				shadowKind = Shadow.MethodCall;
			} else if (sig.getKind() == Member.CONSTRUCTOR) {
				shadowKind = Shadow.ConstructorCall;
			}
		} else if (kind.equals("get")) {
			sig = parseFieldSignaturePattern();
			shadowKind = Shadow.FieldGet;
		} else if (kind.equals("set")) {
			sig = parseFieldSignaturePattern();
			shadowKind = Shadow.FieldSet;
		} else {
			throw new ParserException("bad kind: " + kind, tokenSource.peek());
		}
		eat(")");
		return new KindedPointcut(shadowKind, sig);
	}

	/** Covers the 'lock()' and 'unlock()' pointcuts */
	private KindedPointcut parseMonitorPointcut(String kind) {
		eat("(");
		// TypePattern type = TypePattern.ANY;
		eat(")");

		if (kind.equals("lock")) {
			return new KindedPointcut(Shadow.SynchronizationLock, new SignaturePattern(Member.MONITORENTER, ModifiersPattern.ANY,
					TypePattern.ANY, TypePattern.ANY,
					// type,
					NamePattern.ANY, TypePatternList.ANY, ThrowsPattern.ANY, AnnotationTypePattern.ANY));
		} else {
			return new KindedPointcut(Shadow.SynchronizationUnlock, new SignaturePattern(Member.MONITORENTER, ModifiersPattern.ANY,
					TypePattern.ANY, TypePattern.ANY,
					// type,
					NamePattern.ANY, TypePatternList.ANY, ThrowsPattern.ANY, AnnotationTypePattern.ANY));
		}
	}

	public TypePattern parseTypePattern() {
		return parseTypePattern(false, false);
	}

	public TypePattern parseTypePattern(boolean insideTypeParameters, boolean parameterAnnotationsPossible) {
		TypePattern p = parseAtomicTypePattern(insideTypeParameters, parameterAnnotationsPossible);
		if (maybeEat("&&")) {
			p = new AndTypePattern(p, parseNotOrTypePattern(insideTypeParameters, parameterAnnotationsPossible));
		}

		if (maybeEat("||")) {
			p = new OrTypePattern(p, parseTypePattern(insideTypeParameters, parameterAnnotationsPossible));
		}
		return p;
	}

	private TypePattern parseNotOrTypePattern(boolean insideTypeParameters, boolean parameterAnnotationsPossible) {
		TypePattern p = parseAtomicTypePattern(insideTypeParameters, parameterAnnotationsPossible);
		if (maybeEat("&&")) {
			p = new AndTypePattern(p, parseTypePattern(insideTypeParameters, parameterAnnotationsPossible));
		}
		return p;
	}

	// Need to differentiate in here between two kinds of annotation pattern - depending on where the ( is

	private TypePattern parseAtomicTypePattern(boolean insideTypeParameters, boolean parameterAnnotationsPossible) {
		AnnotationTypePattern ap = maybeParseAnnotationPattern(); // might be parameter annotation pattern or type annotation
		// pattern
		if (maybeEat("!")) {
			// int startPos = tokenSource.peek(-1).getStart();
			// ??? we lose source location for true start of !type

			// An annotation, if processed, is outside of the Not - so here we have to build
			// an And pattern containing the annotation and the not as left and right children
			// *unless* the annotation pattern was just 'Any' then we can skip building the
			// And and just return the Not directly (pr228980)
			TypePattern p = null;
			TypePattern tp = parseAtomicTypePattern(insideTypeParameters, parameterAnnotationsPossible);
			if (!(ap instanceof AnyAnnotationTypePattern)) {
				p = new NotTypePattern(tp);
				p = new AndTypePattern(setAnnotationPatternForTypePattern(TypePattern.ANY, ap, false), p);
			} else {
				p = new NotTypePattern(tp);
			}
			return p;
		}
		if (maybeEat("(")) {
			int openParenPos = tokenSource.peek(-1).getStart();
			TypePattern p = parseTypePattern(insideTypeParameters, false);
			if ((p instanceof NotTypePattern) && !(ap instanceof AnyAnnotationTypePattern)) {
				// dont set the annotation on it, we don't want the annotation to be
				// considered as part of the not, it is outside the not (pr228980)
				TypePattern tp = setAnnotationPatternForTypePattern(TypePattern.ANY, ap, parameterAnnotationsPossible);
				p = new AndTypePattern(tp, p);
			} else {
				p = setAnnotationPatternForTypePattern(p, ap, parameterAnnotationsPossible);
			}
			eat(")");
			int closeParenPos = tokenSource.peek(-1).getStart();
			boolean isVarArgs = maybeEat("...");
			if (isVarArgs) {
				p.setIsVarArgs(isVarArgs);
			}
			boolean isIncludeSubtypes = maybeEat("+");
			if (isIncludeSubtypes) {
				p.includeSubtypes = true; // need the test because (A+) should not set subtypes to false!
			}
			p.start = openParenPos;
			p.end = closeParenPos;
			return p;
		}
		int startPos = tokenSource.peek().getStart();
		if (ap.start != -1) {
			startPos = ap.start;
		}
		TypePattern p = parseSingleTypePattern(insideTypeParameters);
		int endPos = tokenSource.peek(-1).getEnd();
		p = setAnnotationPatternForTypePattern(p, ap, false);
		p.setLocation(sourceContext, startPos, endPos);
		return p;
	}

	private TypePattern setAnnotationPatternForTypePattern(TypePattern t, AnnotationTypePattern ap,
			boolean parameterAnnotationsPattern) {
		TypePattern ret = t;
		if (parameterAnnotationsPattern) {
			ap.setForParameterAnnotationMatch();
		}
		if (ap != AnnotationTypePattern.ANY) {
			if (t == TypePattern.ANY) {
				if (t.annotationPattern == AnnotationTypePattern.ANY) {
					return new AnyWithAnnotationTypePattern(ap);
				} else {
					return new AnyWithAnnotationTypePattern(new AndAnnotationTypePattern(ap, t.annotationPattern));
				}
				// ret = new WildTypePattern(new NamePattern[] { NamePattern.ANY }, false, 0, false, null);
			}
			if (t.annotationPattern == AnnotationTypePattern.ANY) {
				ret.setAnnotationTypePattern(ap);
			} else {
				ret.setAnnotationTypePattern(new AndAnnotationTypePattern(ap, t.annotationPattern)); // ???
			}
		}
		return ret;
	}

	public AnnotationTypePattern maybeParseAnnotationPattern() {
		AnnotationTypePattern ret = AnnotationTypePattern.ANY;
		AnnotationTypePattern nextPattern = null;
		while ((nextPattern = maybeParseSingleAnnotationPattern()) != null) {
			if (ret == AnnotationTypePattern.ANY) {
				ret = nextPattern;
			} else {
				ret = new AndAnnotationTypePattern(ret, nextPattern);
			}
		}
		return ret;
	}

	// PVAL cope with annotation values at other places in this code
	public AnnotationTypePattern maybeParseSingleAnnotationPattern() {
		AnnotationTypePattern ret = null;
		Map<String, String> values = null;
		// LALR(2) - fix by making "!@" a single token
		int startIndex = tokenSource.getIndex();
		if (maybeEat("!")) {
			if (maybeEat("@")) {
				if (maybeEat("(")) {
					TypePattern p = parseTypePattern();
					ret = new NotAnnotationTypePattern(new WildAnnotationTypePattern(p));
					eat(")");
					return ret;
				} else {
					TypePattern p = parseSingleTypePattern();
					if (maybeEatAdjacent("(")) {
						values = parseAnnotationValues();
						eat(")");
						ret = new NotAnnotationTypePattern(new WildAnnotationTypePattern(p, values));
					} else {
						ret = new NotAnnotationTypePattern(new WildAnnotationTypePattern(p));
					}
					return ret;
				}
			} else {
				tokenSource.setIndex(startIndex); // not for us!
				return ret;
			}
		}
		if (maybeEat("@")) {
			if (maybeEat("(")) {
				TypePattern p = parseTypePattern();
				ret = new WildAnnotationTypePattern(p);
				eat(")");
				return ret;
			} else {
				int atPos = tokenSource.peek(-1).getStart();
				TypePattern p = parseSingleTypePattern();
				if (maybeEatAdjacent("(")) {
					values = parseAnnotationValues();
					eat(")");
					ret = new WildAnnotationTypePattern(p, values);
				} else {
					ret = new WildAnnotationTypePattern(p);
				}
				ret.start = atPos;
				return ret;
			}
		} else {
			tokenSource.setIndex(startIndex); // not for us!
			return ret;
		}
	}

	// Parse annotation values. In an expression in @A(a=b,c=d) this method will be
	// parsing the a=b,c=d.)
	public Map<String, String> parseAnnotationValues() {
		Map<String, String> values = new HashMap<String, String>();
		boolean seenDefaultValue = false;
		do {
			String possibleKeyString = parseAnnotationNameValuePattern();
			if (possibleKeyString == null) {
				throw new ParserException("expecting simple literal ", tokenSource.peek(-1));
			}
			// did they specify just a single entry 'v' or a keyvalue pair 'k=v'
			if (maybeEat("=")) {
				// it was a key!
				String valueString = parseAnnotationNameValuePattern();
				if (valueString == null) {
					throw new ParserException("expecting simple literal ", tokenSource.peek(-1));
				}
				values.put(possibleKeyString, valueString);
			} else if (maybeEat("!=")) {
				// it was a key, with a !=
				String valueString = parseAnnotationNameValuePattern();
				if (valueString == null) {
					throw new ParserException("expecting simple literal ", tokenSource.peek(-1));
				}
				// negation is captured by adding a trailing ! to the key name
				values.put(possibleKeyString + "!", valueString);
			} else {
				if (seenDefaultValue) {
					throw new ParserException("cannot specify two default values", tokenSource.peek(-1));
				}
				seenDefaultValue = true;
				values.put("value", possibleKeyString);
			}
		} while (maybeEat(",")); // keep going whilst there are ','
		return values;
	}

	public TypePattern parseSingleTypePattern() {
		return parseSingleTypePattern(false);
	}

	public TypePattern parseSingleTypePattern(boolean insideTypeParameters) {
		if (insideTypeParameters && maybeEat("?")) {
			return parseGenericsWildcardTypePattern();
		}
		if (allowHasTypePatterns) {
			if (maybeEatIdentifier("hasmethod")) {
				return parseHasMethodTypePattern();
			}
			if (maybeEatIdentifier("hasfield")) {
				return parseHasFieldTypePattern();
			}
		}

		// // Check for a type category
		// IToken token = tokenSource.peek();
		// if (token.isIdentifier()) {
		// String category = token.getString();
		// TypeCategoryTypePattern typeIsPattern = null;
		// if (category.equals("isClass")) {
		// typeIsPattern = new TypeCategoryTypePattern(TypeCategoryTypePattern.CLASS);
		// } else if (category.equals("isAspect")) {
		// typeIsPattern = new TypeCategoryTypePattern(TypeCategoryTypePattern.ASPECT);
		// } else if (category.equals("isInterface")) {
		// typeIsPattern = new TypeCategoryTypePattern(TypeCategoryTypePattern.INTERFACE);
		// } else if (category.equals("isInner")) {
		// typeIsPattern = new TypeCategoryTypePattern(TypeCategoryTypePattern.INNER);
		// } else if (category.equals("isAnonymous")) {
		// typeIsPattern = new TypeCategoryTypePattern(TypeCategoryTypePattern.ANONYMOUS);
		// } else if (category.equals("isEnum")) {
		// typeIsPattern = new TypeCategoryTypePattern(TypeCategoryTypePattern.ENUM);
		// } else if (category.equals("isAnnotation")) {
		// typeIsPattern = new TypeCategoryTypePattern(TypeCategoryTypePattern.ANNOTATION);
		// }
		// if (typeIsPattern != null) {
		// tokenSource.next();
		// typeIsPattern.setLocation(tokenSource.getSourceContext(), token.getStart(), token.getEnd());
		// return typeIsPattern;
		// }
		// }
		if (maybeEatIdentifier("is")) {
			int pos = tokenSource.getIndex() - 1;
			TypePattern typeIsPattern = parseIsTypePattern();
			if (typeIsPattern != null) {
				return typeIsPattern;
			}
			// rewind as if we never tried to parse it as a typeIs
			tokenSource.setIndex(pos);
		}

		List<NamePattern> names = parseDottedNamePattern();

		int dim = 0;
		while (maybeEat("[")) {
			eat("]");
			dim++;
		}

		TypePatternList typeParameters = maybeParseTypeParameterList();
		int endPos = tokenSource.peek(-1).getEnd();

		boolean includeSubtypes = maybeEat("+");

		// TODO do we need to associate the + with either the type or the array?
		while (maybeEat("[")) {
			eat("]");
			dim++;
		}

		boolean isVarArgs = maybeEat("...");

		// ??? what about the source location of any's????
		if (names.size() == 1 && names.get(0).isAny() && dim == 0 && !isVarArgs && typeParameters == null) {
			return TypePattern.ANY;
		}

		// Notice we increase the dimensions if varargs is set. this is to allow type matching to
		// succeed later: The actual signature at runtime of a method declared varargs is an array type of
		// the original declared type (so Integer... becomes Integer[] in the bytecode). So, here for the
		// pattern 'Integer...' we create a WildTypePattern 'Integer[]' with varargs set. If this matches
		// during shadow matching, we confirm that the varargs flags match up before calling it a successful
		// match.
		return new WildTypePattern(names, includeSubtypes, dim + (isVarArgs ? 1 : 0), endPos, isVarArgs, typeParameters);
	}

	public TypePattern parseHasMethodTypePattern() {
		int startPos = tokenSource.peek(-1).getStart();
		eat("(");
		SignaturePattern sp = parseMethodOrConstructorSignaturePattern();
		eat(")");
		int endPos = tokenSource.peek(-1).getEnd();
		HasMemberTypePattern ret = new HasMemberTypePattern(sp);
		ret.setLocation(sourceContext, startPos, endPos);
		return ret;
	}

	/**
	 * Attempt to parse a typeIs(<category>) construct. If it cannot be parsed we just return null and that should cause the caller
	 * to reset their position and attempt to consume it in another way. This means we won't have problems here: execution(*
	 * typeIs(..)) because someone has decided to call a method the same as our construct.
	 * 
	 * @return a TypeIsTypePattern or null if could not be parsed
	 */
	public TypePattern parseIsTypePattern() {
		int startPos = tokenSource.peek(-1).getStart(); // that will be the start of the 'typeIs'
		if (!maybeEatAdjacent("(")) {
			return null;
		}
		IToken token = tokenSource.next();
		TypeCategoryTypePattern typeIsPattern = null;
		if (token.isIdentifier()) {
			String category = token.getString();
			if (category.equals("ClassType")) {
				typeIsPattern = new TypeCategoryTypePattern(TypeCategoryTypePattern.CLASS);
			} else if (category.equals("AspectType")) {
				typeIsPattern = new TypeCategoryTypePattern(TypeCategoryTypePattern.ASPECT);
			} else if (category.equals("InterfaceType")) {
				typeIsPattern = new TypeCategoryTypePattern(TypeCategoryTypePattern.INTERFACE);
			} else if (category.equals("InnerType")) {
				typeIsPattern = new TypeCategoryTypePattern(TypeCategoryTypePattern.INNER);
			} else if (category.equals("AnonymousType")) {
				typeIsPattern = new TypeCategoryTypePattern(TypeCategoryTypePattern.ANONYMOUS);
			} else if (category.equals("EnumType")) {
				typeIsPattern = new TypeCategoryTypePattern(TypeCategoryTypePattern.ENUM);
			} else if (category.equals("AnnotationType")) {
				typeIsPattern = new TypeCategoryTypePattern(TypeCategoryTypePattern.ANNOTATION);
			}
		}
		if (typeIsPattern == null) {
			return null;
		}
		if (!maybeEat(")")) {
			throw new ParserException(")", tokenSource.peek());
		}
		int endPos = tokenSource.peek(-1).getEnd();
		typeIsPattern.setLocation(tokenSource.getSourceContext(), startPos, endPos);
		return typeIsPattern;
	}

	// if (names.size() == 1 && !names.get(0).isAny()) {
	// if (maybeEatAdjacent("(")) {
	// if (maybeEat(")")) {
	// // likely to be one of isClass()/isInterface()/isInner()/isAnonymous()/isAspect()
	// if (names.size() == 1) {
	// NamePattern np = names.get(0);
	// String simpleName = np.maybeGetSimpleName();
	// if (simpleName != null) {

	// return new TypeCategoryTypePattern(TypeCategoryTypePattern.ANNOTATION, np);
	// } else {
	// throw new ParserException(
	// "not a supported type category, supported are isClass/isInterface/isEnum/isAnnotation/isInner/isAnonymous",
	// tokenSource.peek(-3));
	// }
	// }
	// int stop = 1;
	// // return new WildTypePattern(names, includeSubtypes, dim + (isVarArgs ? 1 : 0), endPos, isVarArgs,
	// // typeParameters);
	// }
	// } else {
	// throw new ParserException("category type pattern is missing closing parentheses", tokenSource.peek(-2));
	// }
	// }
	// }

	public TypePattern parseHasFieldTypePattern() {
		int startPos = tokenSource.peek(-1).getStart();
		eat("(");
		SignaturePattern sp = parseFieldSignaturePattern();
		eat(")");
		int endPos = tokenSource.peek(-1).getEnd();
		HasMemberTypePattern ret = new HasMemberTypePattern(sp);
		ret.setLocation(sourceContext, startPos, endPos);
		return ret;
	}

	public TypePattern parseGenericsWildcardTypePattern() {
		List<NamePattern> names = new ArrayList<NamePattern>();
		names.add(new NamePattern("?"));
		TypePattern upperBound = null;
		TypePattern[] additionalInterfaceBounds = new TypePattern[0];
		TypePattern lowerBound = null;
		if (maybeEatIdentifier("extends")) {
			upperBound = parseTypePattern(false, false);
			additionalInterfaceBounds = maybeParseAdditionalInterfaceBounds();
		}
		if (maybeEatIdentifier("super")) {
			lowerBound = parseTypePattern(false, false);
		}
		int endPos = tokenSource.peek(-1).getEnd();
		return new WildTypePattern(names, false, 0, endPos, false, null, upperBound, additionalInterfaceBounds, lowerBound);
	}

	// private AnnotationTypePattern completeAnnotationPattern(AnnotationTypePattern p) {
	// if (maybeEat("&&")) {
	// return new AndAnnotationTypePattern(p,parseNotOrAnnotationPattern());
	// }
	// if (maybeEat("||")) {
	// return new OrAnnotationTypePattern(p,parseAnnotationTypePattern());
	// }
	// return p;
	// }
	//
	// protected AnnotationTypePattern parseAnnotationTypePattern() {
	// AnnotationTypePattern ap = parseAtomicAnnotationPattern();
	// if (maybeEat("&&")) {
	// ap = new AndAnnotationTypePattern(ap, parseNotOrAnnotationPattern());
	// }
	//
	// if (maybeEat("||")) {
	// ap = new OrAnnotationTypePattern(ap, parseAnnotationTypePattern());
	// }
	// return ap;
	// }
	//
	// private AnnotationTypePattern parseNotOrAnnotationPattern() {
	// AnnotationTypePattern p = parseAtomicAnnotationPattern();
	// if (maybeEat("&&")) {
	// p = new AndAnnotationTypePattern(p,parseAnnotationTypePattern());
	// }
	// return p;
	// }

	protected ExactAnnotationTypePattern parseAnnotationNameOrVarTypePattern() {
		ExactAnnotationTypePattern p = null;
		int startPos = tokenSource.peek().getStart();
		if (maybeEat("@")) {
			throw new ParserException("@Foo form was deprecated in AspectJ 5 M2: annotation name or var ", tokenSource.peek(-1));
		}
		p = parseSimpleAnnotationName();
		int endPos = tokenSource.peek(-1).getEnd();
		p.setLocation(sourceContext, startPos, endPos);
		// For optimized syntax that allows binding directly to annotation values (pr234943)
		if (maybeEat("(")) {
			String formalName = parseIdentifier();
			p = new ExactAnnotationFieldTypePattern(p, formalName);
			eat(")");
		}
		return p;
	}

	/**
	 * @return
	 */
	private ExactAnnotationTypePattern parseSimpleAnnotationName() {
		// the @ has already been eaten...
		ExactAnnotationTypePattern p;
		StringBuffer annotationName = new StringBuffer();
		annotationName.append(parseIdentifier());
		while (maybeEat(".")) {
			annotationName.append('.');
			annotationName.append(parseIdentifier());
		}
		UnresolvedType type = UnresolvedType.forName(annotationName.toString());
		p = new ExactAnnotationTypePattern(type, null);
		return p;
	}

	// private AnnotationTypePattern parseAtomicAnnotationPattern() {
	// if (maybeEat("!")) {
	// //int startPos = tokenSource.peek(-1).getStart();
	// //??? we lose source location for true start of !type
	// AnnotationTypePattern p = new NotAnnotationTypePattern(parseAtomicAnnotationPattern());
	// return p;
	// }
	// if (maybeEat("(")) {
	// AnnotationTypePattern p = parseAnnotationTypePattern();
	// eat(")");
	// return p;
	// }
	// int startPos = tokenSource.peek().getStart();
	// eat("@");
	// StringBuffer annotationName = new StringBuffer();
	// annotationName.append(parseIdentifier());
	// while (maybeEat(".")) {
	// annotationName.append('.');
	// annotationName.append(parseIdentifier());
	// }
	// UnresolvedType type = UnresolvedType.forName(annotationName.toString());
	// AnnotationTypePattern p = new ExactAnnotationTypePattern(type);
	// int endPos = tokenSource.peek(-1).getEnd();
	// p.setLocation(sourceContext, startPos, endPos);
	// return p;
	// }

	public List<NamePattern> parseDottedNamePattern() {
		List<NamePattern> names = new ArrayList<NamePattern>();
		StringBuffer buf = new StringBuffer();
		IToken previous = null;
		boolean justProcessedEllipsis = false; // Remember if we just dealt with an ellipsis (PR61536)
		boolean justProcessedDot = false;
		boolean onADot = false;

		while (true) {
			IToken tok = null;
			int startPos = tokenSource.peek().getStart();
			String afterDot = null;
			while (true) {
				if (previous != null && previous.getString().equals(".")) {
					justProcessedDot = true;
				}
				tok = tokenSource.peek();
				onADot = (tok.getString().equals("."));
				if (previous != null) {
					if (!isAdjacent(previous, tok)) {
						break;
					}
				}
				if (tok.getString() == "*" || (tok.isIdentifier() && tok.getString() != "...")) {
					buf.append(tok.getString());
				} else if (tok.getString() == "...") {
					break;
				} else if (tok.getLiteralKind() != null) {
					// System.err.println("literal kind: " + tok.getString());
					String s = tok.getString();
					int dot = s.indexOf('.');
					if (dot != -1) {
						buf.append(s.substring(0, dot));
						afterDot = s.substring(dot + 1);
						previous = tokenSource.next();
						break;
					}
					buf.append(s); // ??? so-so
				} else {
					break;
				}
				previous = tokenSource.next();
				// XXX need to handle floats and other fun stuff
			}
			int endPos = tokenSource.peek(-1).getEnd();
			if (buf.length() == 0 && names.isEmpty()) {
				throw new ParserException("name pattern", tok);
			}

			if (buf.length() == 0 && justProcessedEllipsis) {
				throw new ParserException("name pattern cannot finish with ..", tok);
			}
			if (buf.length() == 0 && justProcessedDot && !onADot) {
				throw new ParserException("name pattern cannot finish with .", tok);
			}

			if (buf.length() == 0) {
				names.add(NamePattern.ELLIPSIS);
				justProcessedEllipsis = true;
			} else {
				checkLegalName(buf.toString(), previous);
				NamePattern ret = new NamePattern(buf.toString());
				ret.setLocation(sourceContext, startPos, endPos);
				names.add(ret);
				justProcessedEllipsis = false;
			}

			if (afterDot == null) {
				buf.setLength(0);
				// no elipsis or dotted name part
				if (!maybeEat(".")) {
					break;
					// go on
				} else {
					previous = tokenSource.peek(-1);
				}
			} else {
				buf.setLength(0);
				buf.append(afterDot);
				afterDot = null;
			}
		}
		// System.err.println("parsed: " + names);
		return names;
	}

	// supported form 'a.b.c.d' or just 'a'
	public String parseAnnotationNameValuePattern() {
		StringBuffer buf = new StringBuffer();
		IToken tok;
		// int startPos =
		tokenSource.peek().getStart();
		boolean dotOK = false;
		int depth = 0;
		while (true) {
			tok = tokenSource.peek();
			// keep going until we hit ')' or '=' or ','
			if (tok.getString() == ")" && depth == 0) {
				break;
			}
			if (tok.getString() == "!=" && depth == 0) {
				break;
			}
			if (tok.getString() == "=" && depth == 0) {
				break;
			}
			if (tok.getString() == "," && depth == 0) {
				break;
			}
			if (tok == IToken.EOF) {
				throw new ParserException("eof", tokenSource.peek());
			}

			// keep track of nested brackets
			if (tok.getString() == "(") {
				depth++;
			}
			if (tok.getString() == ")") {
				depth--;
			}
			if (tok.getString() == "{") {
				depth++;
			}
			if (tok.getString() == "}") {
				depth--;
			}

			if (tok.getString() == "." && !dotOK) {
				throw new ParserException("dot not expected", tok);
			}
			buf.append(tok.getString());
			tokenSource.next();
			dotOK = true;
		}
		return buf.toString();
	}

	public NamePattern parseNamePattern() {
		StringBuffer buf = new StringBuffer();
		IToken previous = null;
		IToken tok;
		int startPos = tokenSource.peek().getStart();
		while (true) {
			tok = tokenSource.peek();
			if (previous != null) {
				if (!isAdjacent(previous, tok)) {
					break;
				}
			}
			if (tok.getString() == "*" || tok.isIdentifier()) {
				buf.append(tok.getString());
			} else if (tok.getLiteralKind() != null) {
				// System.err.println("literal kind: " + tok.getString());
				String s = tok.getString();
				if (s.indexOf('.') != -1) {
					break;
				}
				buf.append(s); // ??? so-so
			} else {
				break;
			}
			previous = tokenSource.next();
			// XXX need to handle floats and other fun stuff
		}
		int endPos = tokenSource.peek(-1).getEnd();
		if (buf.length() == 0) {
			throw new ParserException("name pattern", tok);
		}

		checkLegalName(buf.toString(), previous);
		NamePattern ret = new NamePattern(buf.toString());
		ret.setLocation(sourceContext, startPos, endPos);
		return ret;
	}

	private void checkLegalName(String s, IToken tok) {
		char ch = s.charAt(0);
		if (!(ch == '*' || Character.isJavaIdentifierStart(ch))) {
			throw new ParserException("illegal identifier start (" + ch + ")", tok);
		}

		for (int i = 1, len = s.length(); i < len; i++) {
			ch = s.charAt(i);
			if (!(ch == '*' || Character.isJavaIdentifierPart(ch))) {
				throw new ParserException("illegal identifier character (" + ch + ")", tok);
			}
		}

	}

	private boolean isAdjacent(IToken first, IToken second) {
		return first.getEnd() == second.getStart() - 1;
	}

	public ModifiersPattern parseModifiersPattern() {
		int requiredFlags = 0;
		int forbiddenFlags = 0;
		int start;
		while (true) {
			start = tokenSource.getIndex();
			boolean isForbidden = false;
			isForbidden = maybeEat("!");
			IToken t = tokenSource.next();
			int flag = ModifiersPattern.getModifierFlag(t.getString());
			if (flag == -1) {
				break;
			}
			if (isForbidden) {
				forbiddenFlags |= flag;
			} else {
				requiredFlags |= flag;
			}
		}

		tokenSource.setIndex(start);
		if (requiredFlags == 0 && forbiddenFlags == 0) {
			return ModifiersPattern.ANY;
		} else {
			return new ModifiersPattern(requiredFlags, forbiddenFlags);
		}
	}

	public TypePatternList parseArgumentsPattern(boolean parameterAnnotationsPossible) {
		List<TypePattern> patterns = new ArrayList<TypePattern>();
		eat("(");

		// ()
		if (maybeEat(")")) {
			return new TypePatternList();
		}

		do {
			if (maybeEat(".")) { // ..
				eat(".");
				patterns.add(TypePattern.ELLIPSIS);
			} else {
				patterns.add(parseTypePattern(false, parameterAnnotationsPossible));
			}
		} while (maybeEat(","));
		eat(")");
		return new TypePatternList(patterns);
	}

	public AnnotationPatternList parseArgumentsAnnotationPattern() {
		List<AnnotationTypePattern> patterns = new ArrayList<AnnotationTypePattern>();
		eat("(");
		if (maybeEat(")")) {
			return new AnnotationPatternList();
		}

		do {
			if (maybeEat(".")) {
				eat(".");
				patterns.add(AnnotationTypePattern.ELLIPSIS);
			} else if (maybeEat("*")) {
				patterns.add(AnnotationTypePattern.ANY);
			} else {
				patterns.add(parseAnnotationNameOrVarTypePattern());
			}
		} while (maybeEat(","));
		eat(")");
		return new AnnotationPatternList(patterns);
	}

	public ThrowsPattern parseOptionalThrowsPattern() {
		IToken t = tokenSource.peek();
		if (t.isIdentifier() && t.getString().equals("throws")) {
			tokenSource.next();
			List<TypePattern> required = new ArrayList<TypePattern>();
			List<TypePattern> forbidden = new ArrayList<TypePattern>();
			do {
				boolean isForbidden = maybeEat("!");
				// ???might want an error for a second ! without a paren
				TypePattern p = parseTypePattern();
				if (isForbidden) {
					forbidden.add(p);
				} else {
					required.add(p);
				}
			} while (maybeEat(","));
			return new ThrowsPattern(new TypePatternList(required), new TypePatternList(forbidden));
		}
		return ThrowsPattern.ANY;
	}

	public SignaturePattern parseMethodOrConstructorSignaturePattern() {
		int startPos = tokenSource.peek().getStart();
		AnnotationTypePattern annotationPattern = maybeParseAnnotationPattern();
		ModifiersPattern modifiers = parseModifiersPattern();
		TypePattern returnType = parseTypePattern(false, false);

		TypePattern declaringType;
		NamePattern name = null;
		MemberKind kind;
		// here we can check for 'new'
		if (maybeEatNew(returnType)) {
			kind = Member.CONSTRUCTOR;
			if (returnType.toString().length() == 0) {
				declaringType = TypePattern.ANY;
			} else {
				declaringType = returnType;
			}
			returnType = TypePattern.ANY;
			name = NamePattern.ANY;
		} else {
			kind = Member.METHOD;
			IToken nameToken = tokenSource.peek();
			declaringType = parseTypePattern(false, false);
			if (maybeEat(".")) {
				nameToken = tokenSource.peek();
				name = parseNamePattern();
			} else {
				name = tryToExtractName(declaringType);
				if (declaringType.toString().equals("")) {
					declaringType = TypePattern.ANY;
				}
			}
			if (name == null) {
				throw new ParserException("name pattern", tokenSource.peek());
			}
			String simpleName = name.maybeGetSimpleName();
			// XXX should add check for any Java keywords
			if (simpleName != null && simpleName.equals("new")) {
				throw new ParserException("method name (not constructor)", nameToken);
			}
		}

		TypePatternList parameterTypes = parseArgumentsPattern(true);

		ThrowsPattern throwsPattern = parseOptionalThrowsPattern();
		SignaturePattern ret = new SignaturePattern(kind, modifiers, returnType, declaringType, name, parameterTypes,
				throwsPattern, annotationPattern);
		int endPos = tokenSource.peek(-1).getEnd();
		ret.setLocation(sourceContext, startPos, endPos);
		return ret;
	}

	private boolean maybeEatNew(TypePattern returnType) {
		if (returnType instanceof WildTypePattern) {
			WildTypePattern p = (WildTypePattern) returnType;
			if (p.maybeExtractName("new")) {
				return true;
			}
		}
		int start = tokenSource.getIndex();
		if (maybeEat(".")) {
			String id = maybeEatIdentifier();
			if (id != null && id.equals("new")) {
				return true;
			}
			tokenSource.setIndex(start);
		}

		return false;
	}

	public ISignaturePattern parseMaybeParenthesizedFieldSignaturePattern() {
		boolean negated = tokenSource.peek().getString().equals("!") && tokenSource.peek(1).getString().equals("(");
		if (negated) {
			eat("!");
		}
		ISignaturePattern result = null;
		if (maybeEat("(")) {
			result = parseCompoundFieldSignaturePattern();
			eat(")", "missing ')' - unbalanced parentheses around field signature pattern in declare @field");
			if (negated) {
				result = new NotSignaturePattern(result);
			}
		} else {
			result = parseFieldSignaturePattern();
		}
		return result;
	}

	public ISignaturePattern parseMaybeParenthesizedMethodOrConstructorSignaturePattern(boolean isMethod) {
		boolean negated = tokenSource.peek().getString().equals("!") && tokenSource.peek(1).getString().equals("(");
		if (negated) {
			eat("!");
		}
		ISignaturePattern result = null;
		if (maybeEat("(")) {
			result = parseCompoundMethodOrConstructorSignaturePattern(isMethod);
			eat(")", "missing ')' - unbalanced parentheses around method/ctor signature pattern in declare annotation");
			if (negated) {
				result = new NotSignaturePattern(result);
			}
		} else {
			SignaturePattern sp = parseMethodOrConstructorSignaturePattern();
			boolean isConstructorPattern = (sp.getKind() == Member.CONSTRUCTOR);
			if (isMethod && isConstructorPattern) {
				throw new ParserException("method signature pattern", tokenSource.peek(-1));
			}
			if (!isMethod && !isConstructorPattern) {
				throw new ParserException("constructor signature pattern", tokenSource.peek(-1));
			}
			result = sp;
		}

		return result;
	}

	public SignaturePattern parseFieldSignaturePattern() {
		int startPos = tokenSource.peek().getStart();

		// TypePatternList followMe = TypePatternList.ANY;

		AnnotationTypePattern annotationPattern = maybeParseAnnotationPattern();
		ModifiersPattern modifiers = parseModifiersPattern();
		TypePattern returnType = parseTypePattern();
		TypePattern declaringType = parseTypePattern();
		NamePattern name;
		// System.err.println("parsed field: " + declaringType.toString());
		if (maybeEat(".")) {
			name = parseNamePattern();
		} else {
			name = tryToExtractName(declaringType);
			if (name == null) {
				throw new ParserException("name pattern", tokenSource.peek());
			}
			if (declaringType.toString().equals("")) {
				declaringType = TypePattern.ANY;
			}
		}
		SignaturePattern ret = new SignaturePattern(Member.FIELD, modifiers, returnType, declaringType, name, TypePatternList.ANY,
				ThrowsPattern.ANY, annotationPattern);

		int endPos = tokenSource.peek(-1).getEnd();
		ret.setLocation(sourceContext, startPos, endPos);
		return ret;
	}

	private NamePattern tryToExtractName(TypePattern nextType) {
		if (nextType == TypePattern.ANY) {
			return NamePattern.ANY;
		} else if (nextType instanceof WildTypePattern) {
			WildTypePattern p = (WildTypePattern) nextType;
			return p.extractName();
		} else {
			return null;
		}
	}

	/**
	 * Parse type variable declarations for a generic method or at the start of a signature pointcut to identify type variable names
	 * in a generic type.
	 * 
	 * @param includeParameterizedTypes
	 * @return
	 */
	public TypeVariablePatternList maybeParseTypeVariableList() {
		if (!maybeEat("<")) {
			return null;
		}
		List<TypeVariablePattern> typeVars = new ArrayList<TypeVariablePattern>();
		TypeVariablePattern t = parseTypeVariable();
		typeVars.add(t);
		while (maybeEat(",")) {
			TypeVariablePattern nextT = parseTypeVariable();
			typeVars.add(nextT);
		}
		eat(">");
		TypeVariablePattern[] tvs = new TypeVariablePattern[typeVars.size()];
		typeVars.toArray(tvs);
		return new TypeVariablePatternList(tvs);
	}

	// of the form execution<T,S,V> - allows identifiers only
	public String[] maybeParseSimpleTypeVariableList() {
		if (!maybeEat("<")) {
			return null;
		}
		List<String> typeVarNames = new ArrayList<String>();
		do {
			typeVarNames.add(parseIdentifier());
		} while (maybeEat(","));
		eat(">", "',' or '>'");
		String[] tvs = new String[typeVarNames.size()];
		typeVarNames.toArray(tvs);
		return tvs;
	}

	public TypePatternList maybeParseTypeParameterList() {
		if (!maybeEat("<")) {
			return null;
		}
		List<TypePattern> typePats = new ArrayList<TypePattern>();
		do {
			TypePattern tp = parseTypePattern(true, false);
			typePats.add(tp);
		} while (maybeEat(","));
		eat(">");
		TypePattern[] tps = new TypePattern[typePats.size()];
		typePats.toArray(tps);
		return new TypePatternList(tps);
	}

	public TypeVariablePattern parseTypeVariable() {
		TypePattern upperBound = null;
		TypePattern[] additionalInterfaceBounds = null;
		TypePattern lowerBound = null;
		String typeVariableName = parseIdentifier();
		if (maybeEatIdentifier("extends")) {
			upperBound = parseTypePattern();
			additionalInterfaceBounds = maybeParseAdditionalInterfaceBounds();
		} else if (maybeEatIdentifier("super")) {
			lowerBound = parseTypePattern();
		}
		return new TypeVariablePattern(typeVariableName, upperBound, additionalInterfaceBounds, lowerBound);
	}

	private TypePattern[] maybeParseAdditionalInterfaceBounds() {
		List<TypePattern> boundsList = new ArrayList<TypePattern>();
		while (maybeEat("&")) {
			TypePattern tp = parseTypePattern();
			boundsList.add(tp);
		}
		if (boundsList.size() == 0) {
			return null;
		}
		TypePattern[] ret = new TypePattern[boundsList.size()];
		boundsList.toArray(ret);
		return ret;
	}

	public String parsePossibleStringSequence(boolean shouldEnd) {
		StringBuffer result = new StringBuffer();

		IToken token = tokenSource.next();
		if (token.getLiteralKind() == null) {
			throw new ParserException("string", token);
		}
		while (token.getLiteralKind().equals("string")) {
			result.append(token.getString());
			boolean plus = maybeEat("+");
			if (!plus) {
				break;
			}
			token = tokenSource.next();
			if (token.getLiteralKind() == null) {
				throw new ParserException("string", token);
			}
		}
		eatIdentifier(";");
		IToken t = tokenSource.next();
		if (shouldEnd && t != IToken.EOF) {
			throw new ParserException("<string>;", token);
		}
		// bug 125027: since we've eaten the ";" we need to set the index
		// to be one less otherwise the end position isn't set correctly.
		int currentIndex = tokenSource.getIndex();
		tokenSource.setIndex(currentIndex - 1);

		return result.toString();

	}

	public String parseStringLiteral() {
		IToken token = tokenSource.next();
		String literalKind = token.getLiteralKind();
		if (literalKind == "string") {
			return token.getString();
		}

		throw new ParserException("string", token);
	}

	public String parseIdentifier() {
		IToken token = tokenSource.next();
		if (token.isIdentifier()) {
			return token.getString();
		}
		throw new ParserException("identifier", token);
	}

	public void eatIdentifier(String expectedValue) {
		IToken next = tokenSource.next();
		if (!next.getString().equals(expectedValue)) {
			throw new ParserException(expectedValue, next);
		}
	}

	public boolean maybeEatIdentifier(String expectedValue) {
		IToken next = tokenSource.peek();
		if (next.getString().equals(expectedValue)) {
			tokenSource.next();
			return true;
		} else {
			return false;
		}
	}

	public void eat(String expectedValue) {
		eat(expectedValue, expectedValue);
	}

	private void eat(String expectedValue, String expectedMessage) {
		IToken next = nextToken();
		if (next.getString() != expectedValue) {
			if (expectedValue.equals(">") && next.getString().startsWith(">")) {
				// handle problem of >> and >>> being lexed as single tokens
				pendingRightArrows = BasicToken.makeLiteral(next.getString().substring(1).intern(), "string", next.getStart() + 1,
						next.getEnd());
				return;
			}
			throw new ParserException(expectedMessage, next);
		}
	}

	private IToken pendingRightArrows;

	private IToken nextToken() {
		if (pendingRightArrows != null) {
			IToken ret = pendingRightArrows;
			pendingRightArrows = null;
			return ret;
		} else {
			return tokenSource.next();
		}
	}

	public boolean maybeEatAdjacent(String token) {
		IToken next = tokenSource.peek();
		if (next.getString() == token) {
			if (isAdjacent(tokenSource.peek(-1), next)) {
				tokenSource.next();
				return true;
			}
		}
		return false;
	}

	public boolean maybeEat(String token) {
		IToken next = tokenSource.peek();
		if (next.getString() == token) {
			tokenSource.next();
			return true;
		} else {
			return false;
		}
	}

	public String maybeEatIdentifier() {
		IToken next = tokenSource.peek();
		if (next.isIdentifier()) {
			tokenSource.next();
			return next.getString();
		} else {
			return null;
		}
	}

	public boolean peek(String token) {
		IToken next = tokenSource.peek();
		return next.getString() == token;
	}

	public void checkEof() {
		IToken last = tokenSource.next();
		if (last != IToken.EOF) {
			throw new ParserException("unexpected pointcut element: " + last.toString(), last);
		}
	}

	public PatternParser(String data) {
		this(BasicTokenSource.makeTokenSource(data, null));
	}

	public PatternParser(String data, ISourceContext context) {
		this(BasicTokenSource.makeTokenSource(data, context));
	}
}
