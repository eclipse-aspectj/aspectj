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

import java.util.ArrayList;
import java.util.List;

import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.TypeX;

//XXX doesn't handle errors for extra tokens very well (sometimes ignores)
public class PatternParser {
		
	private ITokenSource tokenSource;	
	private ISourceContext sourceContext;

	/**
	 * Constructor for PatternParser.
	 */
	public PatternParser(ITokenSource tokenSource) {
		super();
		this.tokenSource = tokenSource;
		this.sourceContext = tokenSource.getSourceContext();
	}

	public PerClause maybeParsePerClause() {
		IToken tok = tokenSource.peek();
		if (tok == IToken.EOF) return null;
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
			throw new ParserException("expected one of error, warning, parents, soft, dominates",
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
			throw new ParserException("one of type, method, field, constructor",tokenSource.peek(-1));
		}
		eat(";");
	    int endPos = tokenSource.peek(-1).getEnd();
		ret.setLocation(sourceContext, startPos, endPos);
		return ret;
		
	}
	
	public DeclareAnnotation parseDeclareAtType() {
		return new DeclareAnnotation(DeclareAnnotation.AT_TYPE,parseTypePattern());
	}

	public DeclareAnnotation parseDeclareAtMethod(boolean isMethod) {
		SignaturePattern sp = parseMethodOrConstructorSignaturePattern();
		boolean isConstructorPattern = (sp.getKind() == Member.CONSTRUCTOR);
		if (isMethod && isConstructorPattern) {
				throw new ParserException("method signature pattern",tokenSource.peek(-1));
		}
		if (!isMethod && !isConstructorPattern) {
			throw new ParserException("constructor signature pattern",tokenSource.peek(-1));
		}
		if (isConstructorPattern) return new DeclareAnnotation(DeclareAnnotation.AT_CONSTRUCTOR,sp);
		else                      return new DeclareAnnotation(DeclareAnnotation.AT_METHOD,sp);
	}

	public DeclareAnnotation parseDeclareAtField() {
		return new DeclareAnnotation(DeclareAnnotation.AT_FIELD,parseFieldSignaturePattern());		
	}

	public DeclarePrecedence parseDominates() {
		List l = new ArrayList();
		do {
			l.add(parseTypePattern());
		} while (maybeEat(","));
		
		return new DeclarePrecedence(l);
	}

	private Declare parseParents() {
		TypeVariablePatternList typeParameters = maybeParseTypeVariableList();
		eat(":");
		TypePattern p = parseTypePattern(false,false);
		IToken t = tokenSource.next();
		if (!(t.getString().equals("extends") || t.getString().equals("implements"))) {
			throw new ParserException("extends or implements", t);
		}
		
		List l = new ArrayList();
		do {
			l.add(parseTypePattern());
		} while (maybeEat(","));
		
		//XXX somewhere in the chain we need to enforce that we have only ExactTypePatterns
		
		DeclareParents decp = new DeclareParents(p, l);
		if (typeParameters != null) {
			decp.setTypeParameters(typeParameters);
		}
		return decp;
	}

	private Declare parseSoft() {
		TypePattern p = parseTypePattern();
		eat(":");
		Pointcut pointcut = parsePointcut();
		return new DeclareSoft(p, pointcut);
	}



	private Declare parseErrorOrWarning(boolean isError) {
		Pointcut pointcut = parsePointcut();
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
			p = new AndPointcut(p, parsePointcut());
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
		IToken possibleTypeVariableToken = tokenSource.peek();
		TypeVariablePatternList typeVariables = maybeParseSimpleTypeVariableList();
		if (kind.equals("execution") || kind.equals("call") || 
						kind.equals("get") || kind.equals("set")) {
			p = parseKindedPointcut(kind);
		} else if (kind.equals("args")) {
			assertNoTypeVariables(typeVariables,
					"( - type variables not allowed with args pointcut designator"
					, possibleTypeVariableToken);			
			p = parseArgsPointcut();
		} else if (kind.equals("this")) {
			assertNoTypeVariables(typeVariables,
					"( - type variables not allowed with 'this' pointcut designator"
					, possibleTypeVariableToken);
			p = parseThisOrTargetPointcut(kind);
		} else if (kind.equals("target")) {
			assertNoTypeVariables(typeVariables,
					"( - type variables not allowed with target pointcut designator"
					, possibleTypeVariableToken);
			p = parseThisOrTargetPointcut(kind);			
		} else if (kind.equals("within")) {
			p = parseWithinPointcut();
		} else if (kind.equals("withincode")) {
			p = parseWithinCodePointcut();
		} else if (kind.equals("cflow")) {
			assertNoTypeVariables(typeVariables,
					"( - type variables not allowed with cflow pointcut designator"
					, possibleTypeVariableToken);
			p = parseCflowPointcut(false);
		} else if (kind.equals("cflowbelow")) {
			assertNoTypeVariables(typeVariables,
					"( - type variables not allowed with cflowbelow pointcut designator"
					, possibleTypeVariableToken);
			p = parseCflowPointcut(true);
		} else  if (kind.equals("adviceexecution")) {
			assertNoTypeVariables(typeVariables,
					"( - type variables not allowed with adviceexecution pointcut designator"
					, possibleTypeVariableToken);
			eat("(");
			eat(")");
			p = new KindedPointcut(Shadow.AdviceExecution,
				new SignaturePattern(Member.ADVICE, ModifiersPattern.ANY, 
					TypePattern.ANY, TypePattern.ANY, NamePattern.ANY, 
					TypePatternList.ANY, 
					ThrowsPattern.ANY,
					AnnotationTypePattern.ANY));
		} else  if (kind.equals("handler")) {
			assertNoTypeVariables(typeVariables,"( - type variables not allowed with handler pointcut designator",possibleTypeVariableToken);
			eat("(");
			TypePattern typePat = parseTypePattern(false,true);
			eat(")");
			p = new HandlerPointcut(typePat);
		} else  if (kind.equals("initialization")) {
			eat("(");
			SignaturePattern sig = parseConstructorSignaturePattern();
			eat(")");
			p = new KindedPointcut(Shadow.Initialization, sig);
		} else  if (kind.equals("staticinitialization")) {
			eat("(");
			TypePattern typePat = parseTypePattern(false,true);
			eat(")");
			p = new KindedPointcut(Shadow.StaticInitialization,
					new SignaturePattern(Member.STATIC_INITIALIZATION, ModifiersPattern.ANY, 
					TypePattern.ANY, typePat, NamePattern.ANY, TypePatternList.EMPTY, 
					ThrowsPattern.ANY,AnnotationTypePattern.ANY));
		}  else  if (kind.equals("preinitialization")) {
			eat("(");
			SignaturePattern sig = parseConstructorSignaturePattern();
			eat(")");
			p = new KindedPointcut(Shadow.PreInitialization, sig);
		} else  if (kind.equals("if")) {
			// @style support allows if(), if(true), if(false)	
			assertNoTypeVariables(typeVariables,
					"( - type variables not allowed with if pointcut designator"
					, possibleTypeVariableToken);
			eat("(");
			if (maybeEatIdentifier("true")) {
				eat(")");
				p = new IfPointcut.IfTruePointcut();
			} else if (maybeEatIdentifier("false")) {
			    eat(")");
				p = new IfPointcut.IfFalsePointcut();
			} else {
				eat(")");
				// TODO - Alex has some token stuff going on here to get a readable name in place of ""...
				p = new IfPointcut("");
			}
		}
		else {
			tokenSource.setIndex(start);
			p = parseReferencePointcut();
			if (typeVariables != null) 
				throw new ParserException("type variable specification not allowed for reference pointcuts",possibleTypeVariableToken);
		}
		if (typeVariables != null) p.setTypeVariables(typeVariables);
		return p;
	}

	private void assertNoTypeVariables(TypeVariablePatternList tvs, String errorMessage,IToken token) {
		if ( tvs != null ) throw new ParserException(errorMessage,token);
	}
	
	public Pointcut parseAnnotationPointcut() {		
		int start = tokenSource.getIndex();
		IToken t = tokenSource.peek();
		String kind = parseIdentifier();
		IToken possibleTypeVariableToken = tokenSource.peek();
		TypeVariablePatternList typeVariables = maybeParseSimpleTypeVariableList();
		if (typeVariables != null) {
			String message = "( - type variables not allowed with @" +
			                           kind + " pointcut designator";
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
		} throw new ParserException("pointcut name", t);
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
		if (ret.getKind() == Member.CONSTRUCTOR) return ret;
		
		throw new ParserException("constructor pattern required, found method pattern",
				ret);
	}

	
	private Pointcut parseWithinCodePointcut() {
		//parseIdentifier();
		eat("(");
		SignaturePattern sig = parseMethodOrConstructorSignaturePattern();
		eat(")");
		return new WithincodePointcut(sig);
	}

	private Pointcut parseCflowPointcut(boolean isBelow) {
		//parseIdentifier();
		eat("(");
		Pointcut entry = parsePointcut();
		eat(")");
		return new CflowPointcut(entry, isBelow, null);
	}

	/**
	 * Method parseWithinPointcut.
	 * @return Pointcut
	 */
	private Pointcut parseWithinPointcut() {
		//parseIdentifier();
		eat("(");
		TypePattern type = parseTypePattern();
		eat(")");
		return new WithinPointcut(type);
	}


	/**
	 * Method parseThisOrTargetPointcut.
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
		return new ThisOrTargetAnnotationPointcut(kind.equals("this"),type);		
	}

	private Pointcut parseWithinAnnotationPointcut() {
		String kind = parseIdentifier();
		eat("(");
		if (maybeEat(")")) {
			throw new ParserException("expecting @AnnotationName or parameter, but found ')'", tokenSource.peek());
		}
		AnnotationTypePattern type = parseAnnotationNameOrVarTypePattern(); 
		eat(")");
		return new WithinAnnotationPointcut(type);		
	}

	private Pointcut parseWithinCodeAnnotationPointcut() {
		String kind = parseIdentifier();
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
	 * @return Pointcut
	 */
	private Pointcut parseArgsPointcut() {
		//parseIdentifier();
		TypePatternList arguments = parseArgumentsPattern();
		return new ArgsPointcut(arguments);
	}
	
	private Pointcut parseArgsAnnotationPointcut() {
		parseIdentifier();
		AnnotationPatternList arguments = parseArgumentsAnnotationPattern();
		return new ArgsAnnotationPointcut(arguments);
	}

	private Pointcut parseReferencePointcut() {
		TypePattern onType = parseTypePattern();
		NamePattern name = tryToExtractName(onType);
		if (name == null) {
    		throw new ParserException("name pattern", tokenSource.peek());
    	}
    	if (onType.toString().equals("")) {
    		onType = null;
    	}
		
		TypePatternList arguments = parseArgumentsPattern();
		return new ReferencePointcut(onType, name.maybeGetSimpleName(), arguments);
	}


	public List parseDottedIdentifier() {
		List ret = new ArrayList();
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
	
	public TypePattern parseTypePattern() {
		return parseTypePattern(false,false);
	}
	
	public TypePattern parseTypePattern(boolean insideTypeParameters, boolean allowTypeVariableDeclarations) {
		TypePattern p = parseAtomicTypePattern(insideTypeParameters,allowTypeVariableDeclarations); 
		if (maybeEat("&&")) {
			p = new AndTypePattern(p, parseNotOrTypePattern(insideTypeParameters,allowTypeVariableDeclarations));
		}  
		
		if (maybeEat("||")) {
			p = new OrTypePattern(p, parseTypePattern(insideTypeParameters,allowTypeVariableDeclarations));
		}		
		return p;
	}
	
	private TypePattern parseNotOrTypePattern(boolean insideTypeParameters,boolean allowTypeVariableDeclarations) {
		TypePattern p = parseAtomicTypePattern(insideTypeParameters,allowTypeVariableDeclarations);
		if (maybeEat("&&")) {			
			p = new AndTypePattern(p, parseTypePattern(insideTypeParameters,allowTypeVariableDeclarations));
		} 
		return p;		
	}
	
	private TypePattern parseAtomicTypePattern(boolean insideTypeParameters, boolean allowTypeVariableDeclarations) {
		AnnotationTypePattern ap = maybeParseAnnotationPattern();
		if (maybeEat("!")) {
			//int startPos = tokenSource.peek(-1).getStart();
			//??? we lose source location for true start of !type
			TypePattern p = new NotTypePattern(parseAtomicTypePattern(insideTypeParameters,allowTypeVariableDeclarations));
			p = setAnnotationPatternForTypePattern(p,ap);
			return p;			
		}
		if (maybeEat("(")) {
			TypePattern p = parseTypePattern(insideTypeParameters,allowTypeVariableDeclarations);
			p = setAnnotationPatternForTypePattern(p,ap);
			eat(")");
			boolean isVarArgs = maybeEat("...");
			p.setIsVarArgs(isVarArgs);
			return p;
		}
		int startPos = tokenSource.peek().getStart();
	    TypePattern p = parseSingleTypePattern(insideTypeParameters,allowTypeVariableDeclarations);
	    int endPos = tokenSource.peek(-1).getEnd();
	    p = setAnnotationPatternForTypePattern(p,ap);
	    p.setLocation(sourceContext, startPos, endPos);
	    return p;
	}
	
	private TypePattern setAnnotationPatternForTypePattern(TypePattern t, AnnotationTypePattern ap) {
		TypePattern ret = t;
		if (ap != AnnotationTypePattern.ANY) {
			if (t == TypePattern.ANY) {
				ret = new WildTypePattern(new NamePattern[] {NamePattern.ANY},false,0,false,null);
			}
			if (t.annotationPattern == AnnotationTypePattern.ANY) {
				ret.setAnnotationTypePattern(ap);				
			} else {
				ret.setAnnotationTypePattern(new AndAnnotationTypePattern(ap,t.annotationPattern)); //???
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
				ret = new AndAnnotationTypePattern(ret,nextPattern);
			}
		}
		return ret;
	}

	public AnnotationTypePattern maybeParseSingleAnnotationPattern() {
		AnnotationTypePattern ret = null;
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
					ret = new NotAnnotationTypePattern(new WildAnnotationTypePattern(p));
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
				TypePattern p = parseSingleTypePattern();
				ret = new WildAnnotationTypePattern(p);
				return ret;
			}
		} else {
			tokenSource.setIndex(startIndex); // not for us!
			return ret;
		}		
	}
	
	public TypePattern parseSingleTypePattern() {
		return parseSingleTypePattern(false, false);
	}
	
	public TypePattern parseSingleTypePattern(boolean insideTypeParameters, boolean allowTypeVariableDeclarations) {
		if (insideTypeParameters && maybeEat("?")) return parseGenericsWildcardTypePattern();
		
		List names = parseDottedNamePattern(); 
		
		TypePattern upperBound = null;
		TypePattern[] additionalInterfaceBounds = new TypePattern[0];
		TypePattern lowerBound = null;
		if ((names.size() == 1) && allowTypeVariableDeclarations) {
			if (maybeEatIdentifier("extends")) {
				upperBound = parseTypePattern(false,false);
				additionalInterfaceBounds = maybeParseAdditionalInterfaceBounds();
			}
			if (maybeEatIdentifier("super")) {
				lowerBound = parseTypePattern(false,false);
			}
		}

		int dim = 0;
		while (maybeEat("[")) {
			eat("]");
			dim++;
		}

        boolean isVarArgs = maybeEat("...");

		boolean includeSubtypes = maybeEat("+");
		
		TypePatternList typeParameters = maybeParseTypeParameterList(allowTypeVariableDeclarations);
		int endPos = tokenSource.peek(-1).getEnd();
		
		//??? what about the source location of any's????
		if (names.size() == 1 && ((NamePattern)names.get(0)).isAny() && 
				dim == 0 && !isVarArgs && typeParameters == null && upperBound == null &&
				lowerBound == null) return TypePattern.ANY;
		
		// Notice we increase the dimensions if varargs is set.  this is to allow type matching to
		// succeed later: The actual signature at runtime of a method declared varargs is an array type of
		// the original declared type (so Integer... becomes Integer[] in the bytecode).  So, here for the
		// pattern 'Integer...' we create a WildTypePattern 'Integer[]' with varargs set.  If this matches
		// during shadow matching, we confirm that the varargs flags match up before calling it a successful
		// match.
		return new WildTypePattern(names, includeSubtypes, dim+(isVarArgs?1:0), endPos,isVarArgs,typeParameters,
				upperBound,additionalInterfaceBounds,lowerBound);
	}
	
	public TypePattern parseGenericsWildcardTypePattern() {
		List names = new ArrayList();
		names.add(new NamePattern("?"));
		TypePattern upperBound = null;
		TypePattern[] additionalInterfaceBounds = new TypePattern[0];
		TypePattern lowerBound = null;
		if (maybeEatIdentifier("extends")) {
			upperBound = parseTypePattern(false,false);
			additionalInterfaceBounds = maybeParseAdditionalInterfaceBounds();
		}
		if (maybeEatIdentifier("super")) {
			lowerBound = parseTypePattern(false,false);
		}
		int endPos = tokenSource.peek(-1).getEnd();
		return new WildTypePattern(names,false,0,endPos,false,null,upperBound,additionalInterfaceBounds,lowerBound);
	}
	
//	private AnnotationTypePattern completeAnnotationPattern(AnnotationTypePattern p) {
//		if (maybeEat("&&")) {
//			return new AndAnnotationTypePattern(p,parseNotOrAnnotationPattern());
//		}
//		if (maybeEat("||")) {
//			return new OrAnnotationTypePattern(p,parseAnnotationTypePattern());
//		}
//		return p;
//	}
//
//	protected AnnotationTypePattern parseAnnotationTypePattern() {
//		AnnotationTypePattern ap = parseAtomicAnnotationPattern();
//		if (maybeEat("&&")) {
//			ap = new AndAnnotationTypePattern(ap, parseNotOrAnnotationPattern());
//		}  
//		
//		if (maybeEat("||")) {
//			ap = new OrAnnotationTypePattern(ap, parseAnnotationTypePattern());
//		}		
//		return ap;
//	}
//
//	private AnnotationTypePattern parseNotOrAnnotationPattern() {
//		AnnotationTypePattern p = parseAtomicAnnotationPattern();
//		if (maybeEat("&&")) {
//			p = new AndAnnotationTypePattern(p,parseAnnotationTypePattern());
//		}
//		return p;
//	}
	
	
	protected ExactAnnotationTypePattern parseAnnotationNameOrVarTypePattern() {
		ExactAnnotationTypePattern p = null;
		int startPos = tokenSource.peek().getStart();
		if (maybeEat("@")) {
			throw new ParserException("@Foo form was deprecated in AspectJ 5 M2: annotation name or var ",tokenSource.peek(-1));
		}
		p = parseSimpleAnnotationName();
		int endPos = tokenSource.peek(-1).getEnd();
		p.setLocation(sourceContext,startPos,endPos);
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
		TypeX type = TypeX.forName(annotationName.toString());
		p = new ExactAnnotationTypePattern(type);
		return p;
	}

//	private AnnotationTypePattern parseAtomicAnnotationPattern() {
//		if (maybeEat("!")) {
//			//int startPos = tokenSource.peek(-1).getStart();
//			//??? we lose source location for true start of !type
//			AnnotationTypePattern p = new NotAnnotationTypePattern(parseAtomicAnnotationPattern());
//			return p;			
//		}
//		if (maybeEat("(")) {
//			AnnotationTypePattern p = parseAnnotationTypePattern();
//			eat(")");
//			return p;
//		}
//		int startPos = tokenSource.peek().getStart();
//		eat("@");
//		StringBuffer annotationName = new StringBuffer();
//		annotationName.append(parseIdentifier());
//		while (maybeEat(".")) {
//			annotationName.append('.');
//			annotationName.append(parseIdentifier());
//		}
//		TypeX type = TypeX.forName(annotationName.toString());
//		AnnotationTypePattern p = new ExactAnnotationTypePattern(type);
//	    int endPos = tokenSource.peek(-1).getEnd();
//	    p.setLocation(sourceContext, startPos, endPos);
//	    return p;		
//	}
	

	private boolean isAnnotationPattern(PatternNode p) {
		return (p instanceof AnnotationTypePattern);
	}
	
	public List parseDottedNamePattern() {
		List names = new ArrayList();
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
				if (previous !=null && previous.getString().equals(".")) justProcessedDot = true;
				tok = tokenSource.peek();
				onADot = (tok.getString().equals("."));
				if (previous != null) {
					if (!isAdjacent(previous, tok)) break;
				}
				if (tok.getString() == "*" || (tok.isIdentifier() && tok.getString()!="...")) {
					buf.append(tok.getString());
				} else if (tok.getString()=="...") {
					break;
				} else if (tok.getLiteralKind() != null) {
					//System.err.println("literal kind: " + tok.getString());
					String s = tok.getString();
					int dot = s.indexOf('.');
					if (dot != -1) {
						buf.append(s.substring(0, dot));
						afterDot = s.substring(dot+1);
						previous = tokenSource.next();
						break;
					}
					buf.append(s);  // ??? so-so
				} else {
					break;
				}
				previous = tokenSource.next();
				//XXX need to handle floats and other fun stuff
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
                if (!maybeEat(".")) break;
                // go on
				else previous = tokenSource.peek(-1);
			} else {
				buf.setLength(0);
				buf.append(afterDot);
				afterDot = null;
			}
		}
		//System.err.println("parsed: " + names);
		return names;
	}
	
	
	
	public NamePattern parseNamePattern() {
		StringBuffer buf = new StringBuffer();
		IToken previous = null;
		IToken tok;
		int startPos = tokenSource.peek().getStart();
		while (true) {
			tok = tokenSource.peek();
			if (previous != null) {
				if (!isAdjacent(previous, tok)) break;
			}
			if (tok.getString() == "*" || tok.isIdentifier()) {
				buf.append(tok.getString());
			} else if (tok.getLiteralKind() != null) {
				//System.err.println("literal kind: " + tok.getString());
				String s = tok.getString();
				if (s.indexOf('.') != -1) break;
				buf.append(s);  // ??? so-so
			} else {
				break;
			}
			previous = tokenSource.next();
			//XXX need to handle floats and other fun stuff
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
		
		for (int i=1, len=s.length(); i < len; i++) {
			ch = s.charAt(i);
			if (!(ch == '*' || Character.isJavaIdentifierPart(ch))) {
				throw new ParserException("illegal identifier character (" + ch + ")", tok);
			}
		}
		
	}


	private boolean isAdjacent(IToken first, IToken second) {
		return first.getEnd() == second.getStart()-1;
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
		    if (flag == -1) break;
		    if (isForbidden) forbiddenFlags |= flag;
		    else requiredFlags |= flag;
		}
		
		tokenSource.setIndex(start);
		if (requiredFlags == 0 && forbiddenFlags == 0) {
			return ModifiersPattern.ANY;
		} else {
			return new ModifiersPattern(requiredFlags, forbiddenFlags);
		}
	}
	
	public TypePatternList parseArgumentsPattern() {
		List patterns = new ArrayList();
		eat("(");
		if (maybeEat(")")) {
			return new TypePatternList();
		}
		
		do {
			if (maybeEat(".")) {
				eat(".");
				patterns.add(TypePattern.ELLIPSIS);
			} else {
				patterns.add(parseTypePattern());
			}
		} while (maybeEat(","));
		eat(")");
		return new TypePatternList(patterns);
	}
	
	public AnnotationPatternList parseArgumentsAnnotationPattern() {
		List patterns = new ArrayList();
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
			List required = new ArrayList();
			List forbidden = new ArrayList();
			do {
				boolean isForbidden = maybeEat("!");
				//???might want an error for a second ! without a paren
				TypePattern p = parseTypePattern();
				if (isForbidden) forbidden.add(p);
				else required.add(p);
			} while (maybeEat(","));
			return new ThrowsPattern(new TypePatternList(required), new TypePatternList(forbidden));		
		}
		return ThrowsPattern.ANY;
	}
	
	
	public SignaturePattern parseMethodOrConstructorSignaturePattern() {
		int startPos = tokenSource.peek().getStart();
		AnnotationTypePattern annotationPattern = maybeParseAnnotationPattern();
		ModifiersPattern modifiers = parseModifiersPattern();
		TypePattern returnType = parseTypePattern(false,true);
		
		TypePattern declaringType;
		NamePattern name = null;
		Member.Kind kind;
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
			declaringType = parseTypePattern(false,true);
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
	    	//XXX should add check for any Java keywords
	    	if (simpleName != null && simpleName.equals("new")) {
	    		throw new ParserException("method name (not constructor)", 
	    							nameToken);
	    	}
		}
		
		TypePatternList parameterTypes = parseArgumentsPattern();
		
		ThrowsPattern throwsPattern = parseOptionalThrowsPattern();
		SignaturePattern ret = new SignaturePattern(kind, modifiers, returnType, declaringType, name, parameterTypes, throwsPattern, annotationPattern);
	    int endPos = tokenSource.peek(-1).getEnd();
	    ret.setLocation(sourceContext, startPos, endPos);
		return ret;
	}

	private boolean maybeEatNew(TypePattern returnType) {
		if (returnType instanceof WildTypePattern) {
			WildTypePattern p = (WildTypePattern)returnType;
			if (p.maybeExtractName("new")) return true;
		}
		int start = tokenSource.getIndex();
		if (maybeEat(".")) {
			String id = maybeEatIdentifier();
			if (id != null && id.equals("new")) return true;
			tokenSource.setIndex(start);
		}
		
		return false;
	}

	
	public SignaturePattern parseFieldSignaturePattern() {
		int startPos = tokenSource.peek().getStart();
		
	//	TypePatternList followMe = TypePatternList.ANY;
		
		AnnotationTypePattern annotationPattern = maybeParseAnnotationPattern();
		ModifiersPattern modifiers = parseModifiersPattern();
		TypePattern returnType = parseTypePattern();
		TypePattern declaringType = parseTypePattern();
		NamePattern name;
		//System.err.println("parsed field: " + declaringType.toString());
		if (maybeEat(".")) {
		    name = parseNamePattern();
	    } else {
	    	name = tryToExtractName(declaringType);
	    	if (declaringType.toString().equals("")) {
	    		declaringType = TypePattern.ANY;
	    	}
		}
		SignaturePattern ret = new SignaturePattern(Member.FIELD, modifiers, returnType,
					declaringType, name, TypePatternList.ANY, ThrowsPattern.ANY,annotationPattern);
					
		int endPos = tokenSource.peek(-1).getEnd();
	    ret.setLocation(sourceContext, startPos, endPos);
		return ret;
	}
	
	
	private NamePattern tryToExtractName(TypePattern nextType) {
		if (nextType == TypePattern.ANY) {
			return NamePattern.ANY;
		} else if (nextType instanceof WildTypePattern) {
			WildTypePattern p = (WildTypePattern)nextType;
			return p.extractName();
		} else {
		    return null;
		}
	}
	
	/**
	 * Parse type variable declarations for a generic method or at the start of a signature pointcut to identify
	 * type variable names in a generic type.
	 * @param includeParameterizedTypes
	 * @return
	 */
	public TypeVariablePatternList maybeParseTypeVariableList() {
		if (!maybeEat("<")) return null;
		List typeVars = new ArrayList();
		TypeVariable t = parseTypeVariable();
		typeVars.add(t);
		while (maybeEat(",")) {
			TypeVariable nextT = parseTypeVariable();
			typeVars.add(nextT);
		}
		eat(">");
		TypeVariable[] tvs = new TypeVariable[typeVars.size()];
		typeVars.toArray(tvs);
		return new TypeVariablePatternList(tvs);
	}
	
	// of the form execution<T,S,V> - allows identifiers only
	public TypeVariablePatternList maybeParseSimpleTypeVariableList() {
		if (!maybeEat("<")) return null;
		List typeVars = new ArrayList();
		do {
			String typeVarName = parseIdentifier();
			TypeVariable tv = new TypeVariable(typeVarName);
			typeVars.add(tv);
		} while (maybeEat(","));
		eat(">","',' or '>'");
		TypeVariable[] tvs = new TypeVariable[typeVars.size()];
		typeVars.toArray(tvs);
		return new TypeVariablePatternList(tvs);		
	}
	
	public TypePatternList maybeParseTypeParameterList(boolean allowTypeVariables) {
		if (!maybeEat("<")) return null;
		List typePats = new ArrayList();
		do {
			TypePattern tp = parseTypePattern(true,allowTypeVariables);
			typePats.add(tp);
		} while(maybeEat(","));
		eat(">");
		TypePattern[] tps = new TypePattern[typePats.size()];
		typePats.toArray(tps);
		return new TypePatternList(tps);		
	}
	
	public TypeVariable parseTypeVariable() {
		TypePattern upperBound = null;
		TypePattern[] additionalInterfaceBounds = null;
		TypePattern lowerBound = null;
		String typeVariableName = null;
		if (typeVariableName == null) typeVariableName = parseIdentifier();
		if (maybeEatIdentifier("extends")) {
			upperBound = parseTypePattern();
			additionalInterfaceBounds = maybeParseAdditionalInterfaceBounds();
		} else if (maybeEatIdentifier("super")) {
			lowerBound = parseTypePattern();
		}
		return new TypeVariable(typeVariableName,upperBound,additionalInterfaceBounds,lowerBound);
	}
	
	private TypePattern[] maybeParseAdditionalInterfaceBounds() {
		List boundsList = new ArrayList();
		while (maybeEat("&")) {
			TypePattern tp = parseTypePattern();
			boundsList.add(tp);
		}
		if (boundsList.size() == 0) return null;
		TypePattern[] ret = new TypePattern[boundsList.size()];
		boundsList.toArray(ret);
		return ret;
	}

	public String parsePossibleStringSequence(boolean shouldEnd) {
		StringBuffer result = new StringBuffer();
		
		IToken token = tokenSource.next();
		if (token.getLiteralKind()==null) {
			throw new ParserException("string",token);
		}
		while (token.getLiteralKind().equals("string")) {
			result.append(token.getString());			
			boolean plus = maybeEat("+");
			if (!plus) break;
			token = tokenSource.next();
			if (token.getLiteralKind()==null) {
				throw new ParserException("string",token);
			}
		}
		eatIdentifier(";");
		IToken t = tokenSource.next();
		if (shouldEnd && t!=IToken.EOF) {
			throw new ParserException("<string>;",token);
		}

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
		if (token.isIdentifier()) return token.getString();
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
		eat(expectedValue,expectedValue);
	}
	
	private void eat(String expectedValue,String expectedMessage) {
		IToken next = tokenSource.next();
		if (next.getString() != expectedValue) {
			throw new ParserException(expectedMessage, next);
		}
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

	
	public PatternParser(String data) {
		this(BasicTokenSource.makeTokenSource(data,null));
	}
	
	public PatternParser(String data, ISourceContext context) {
		this(BasicTokenSource.makeTokenSource(data,context));
	}
}
