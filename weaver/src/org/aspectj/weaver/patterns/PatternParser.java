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
		eat(":");
		Declare ret;
		//XXX beta add soft, dominates
		if (kind.equals("error")) {
			ret = parseErrorOrWarning(true);
		} else if (kind.equals("warning")) {
			ret = parseErrorOrWarning(false);
		} else if (kind.equals("precedence")) {
			ret = parseDominates();
		} else if (kind.equals("dominates")) {
			throw new ParserException("name changed to declare precedence", tokenSource.peek(-2));
		} else if (kind.equals("parents")) {
			ret = parseParents();
		} else if (kind.equals("soft")) {
			ret = parseSoft();
		} else {
			throw new ParserException("expected one of error, warning, parents, soft, dominates",
				tokenSource.peek(-1));
		}
	    int endPos = tokenSource.peek(-1).getEnd();
		ret.setLocation(sourceContext, startPos, endPos);
		return ret;
	}

	public DeclarePrecedence parseDominates() {
		List l = new ArrayList();
		do {
			l.add(parseTypePattern());
		} while (maybeEat(","));
		
		return new DeclarePrecedence(l);
	}

	private Declare parseParents() {
		TypePattern p = parseTypePattern();
		IToken t = tokenSource.next();
		if (!(t.getString().equals("extends") || t.getString().equals("implements"))) {
			throw new ParserException("extends or implements", t);
		}
		
		List l = new ArrayList();
		do {
			l.add(parseTypePattern());
		} while (maybeEat(","));
		
		//XXX somewhere in the chain we need to enforce that we have only ExactTypePatterns
		
		return new DeclareParents(p, l);
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
		String message = parseStringLiteral();
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
		tokenSource.setIndex(start);
		if (kind.equals("execution") || kind.equals("call") || 
						kind.equals("get") || kind.equals("set")) {
			return parseKindedPointcut();
		} else if (kind.equals("args")) {
			return parseArgsPointcut();
		} else if (kind.equals("this") || kind.equals("target")) {
			return parseThisOrTargetPointcut();
		} else if (kind.equals("within")) {
			return parseWithinPointcut();
		} else if (kind.equals("withincode")) {
			return parseWithinCodePointcut();
		} else if (kind.equals("cflow")) {
			return parseCflowPointcut(false);
		} else if (kind.equals("cflowbelow")) {
			return parseCflowPointcut(true);
		} else  if (kind.equals("adviceexecution")) {
			parseIdentifier(); eat("(");
			eat(")");
			return new KindedPointcut(Shadow.AdviceExecution,
				new SignaturePattern(Member.ADVICE, ModifiersPattern.ANY, 
					TypePattern.ANY, TypePattern.ANY, NamePattern.ANY, 
					TypePatternList.ANY, 
					ThrowsPattern.ANY));
		} else  if (kind.equals("handler")) {
			parseIdentifier(); eat("(");
			TypePattern typePat = parseTypePattern();
			eat(")");
			return new HandlerPointcut(typePat);
		} else  if (kind.equals("initialization")) {
			parseIdentifier(); eat("(");
			SignaturePattern sig = parseConstructorSignaturePattern();
			eat(")");
			return new KindedPointcut(Shadow.Initialization, sig);
		} else  if (kind.equals("staticinitialization")) {
			parseIdentifier(); eat("(");
			TypePattern typePat = parseTypePattern();
			eat(")");
			return new KindedPointcut(Shadow.StaticInitialization,
					new SignaturePattern(Member.STATIC_INITIALIZATION, ModifiersPattern.ANY, 
					TypePattern.ANY, typePat, NamePattern.ANY, TypePatternList.EMPTY, 
					ThrowsPattern.ANY));
		} else  if (kind.equals("preinitialization")) {
			parseIdentifier(); eat("(");
			SignaturePattern sig = parseConstructorSignaturePattern();
			eat(")");
			return new KindedPointcut(Shadow.PreInitialization, sig);
		} else {
			return parseReferencePointcut();
		}
	}

	private SignaturePattern parseConstructorSignaturePattern() {
		SignaturePattern ret = parseMethodOrConstructorSignaturePattern();
		if (ret.getKind() == Member.CONSTRUCTOR) return ret;
		
		throw new ParserException("constructor pattern required, found method pattern",
				ret);
	}

	
	private Pointcut parseWithinCodePointcut() {
		parseIdentifier();
		eat("(");
		SignaturePattern sig = parseMethodOrConstructorSignaturePattern();
		eat(")");
		return new WithincodePointcut(sig);
	}

	private Pointcut parseCflowPointcut(boolean isBelow) {
		parseIdentifier();
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
		parseIdentifier();
		eat("(");
		TypePattern type = parseTypePattern();
		eat(")");
		return new WithinPointcut(type);
	}


	/**
	 * Method parseThisOrTargetPointcut.
	 * @return Pointcut
	 */
	private Pointcut parseThisOrTargetPointcut() {
		String kind = parseIdentifier();
		eat("(");
		TypePattern type = parseTypePattern();
		eat(")");
		return new ThisOrTargetPointcut(kind.equals("this"), type);
	}


	/**
	 * Method parseArgsPointcut.
	 * @return Pointcut
	 */
	private Pointcut parseArgsPointcut() {
		parseIdentifier();
		TypePatternList arguments = parseArgumentsPattern();
		return new ArgsPointcut(arguments);
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


	
	private KindedPointcut parseKindedPointcut() {
		String kind = parseIdentifier();  
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
		TypePattern p = parseAtomicTypePattern(); 
		if (maybeEat("&&")) {
			p = new AndTypePattern(p, parseNotOrTypePattern());
		}  
		
		if (maybeEat("||")) {
			p = new OrTypePattern(p, parseTypePattern());
		}		
		return p;
	}
	
	private TypePattern parseNotOrTypePattern() {
		TypePattern p = parseAtomicTypePattern();
		if (maybeEat("&&")) {			
			p = new AndTypePattern(p, parseTypePattern());
		} 
		return p;		
	}
	
	private TypePattern parseAtomicTypePattern() {
		if (maybeEat("!")) {
			int startPos = tokenSource.peek(-1).getStart();
			//??? we lose source location for true start of !type
			TypePattern p = new NotTypePattern(parseAtomicTypePattern());
			return p;			
		}
		if (maybeEat("(")) {
			TypePattern p = parseTypePattern();
			eat(")");
			return p;
		}
		int startPos = tokenSource.peek().getStart();
	    TypePattern p = parseSingleTypePattern();
	    int endPos = tokenSource.peek(-1).getEnd();
	    p.setLocation(sourceContext, startPos, endPos);
	    return p;
	}

	public TypePattern parseSingleTypePattern() {
		List names = parseDottedNamePattern(); 
//		new ArrayList();
//		NamePattern p1 = parseNamePattern();
//		names.add(p1);
//		while (maybeEat(".")) {
//			if (maybeEat(".")) {
//				names.add(NamePattern.ELLIPSIS);
//			}
//			NamePattern p2 = parseNamePattern();
//			names.add(p2);
//		}
		int dim = 0;
		while (maybeEat("[")) {
			eat("]");
			dim++;
		}
			
		
		boolean includeSubtypes = maybeEat("+");
		int endPos = tokenSource.peek(-1).getEnd();
		
		//??? what about the source location of any's????
		if (names.size() == 1 && ((NamePattern)names.get(0)).isAny() && dim == 0) return TypePattern.ANY;
		
		return new WildTypePattern(names, includeSubtypes, dim, endPos);
	}
	
	public List parseDottedNamePattern() {
		List names = new ArrayList();
		StringBuffer buf = new StringBuffer();
		IToken previous = null;
		while (true) {
			IToken tok;
			int startPos = tokenSource.peek().getStart();
			String afterDot = null;
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
				throw new ParserException("expected name pattern", tok);
			} 
			
			if (buf.length() == 0) {
				names.add(NamePattern.ELLIPSIS);
			} else {
				checkLegalName(buf.toString(), previous);
				NamePattern ret = new NamePattern(buf.toString());
				ret.setLocation(sourceContext, startPos, endPos);
				names.add(ret);
			}
			
			if (afterDot == null) {
				buf.setLength(0);
				if (!maybeEat(".")) break;
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
			throw new ParserException("expected name pattern", tok);
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
		ModifiersPattern modifiers = parseModifiersPattern();
		TypePattern returnType = parseTypePattern();
		
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
			declaringType = parseTypePattern();
			if (maybeEat(".")) {
			    name = parseNamePattern();
		    } else {
		    	name = tryToExtractName(declaringType);
		    	if (name == null) {
		    		throw new ParserException("name pattern", tokenSource.peek());
		    	}
		    	String simpleName = name.maybeGetSimpleName();
		    	//XXX should add check for any Java keywords
		    	if (simpleName != null && simpleName.equals("new")) {
		    		throw new ParserException("constructor patterns have no return type", 
		    							tokenSource.peek());
		    	}
		    	if (declaringType.toString().equals("")) {
		    		declaringType = declaringType.ANY;
		    	}
			}
		}
		
		TypePatternList parameterTypes = parseArgumentsPattern();
		
		ThrowsPattern throwsPattern = parseOptionalThrowsPattern();
		SignaturePattern ret = new SignaturePattern(kind, modifiers, returnType, declaringType, name, parameterTypes, throwsPattern);
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
	    		declaringType = declaringType.ANY;
	    	}
		}
		SignaturePattern ret = new SignaturePattern(Member.FIELD, modifiers, returnType,
					declaringType, name, TypePatternList.ANY, ThrowsPattern.ANY);
					
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
		IToken next = tokenSource.next();
		if (next.getString() != expectedValue) {
			throw new ParserException(expectedValue, next);
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
		this(BasicTokenSource.makeTokenSource(data));
	}
}
