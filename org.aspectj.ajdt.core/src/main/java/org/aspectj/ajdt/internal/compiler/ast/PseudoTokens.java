/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.patterns.BasicTokenSource;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.IToken;
import org.aspectj.weaver.patterns.ParserException;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerSingleton;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.TerminalTokens;


public class PseudoTokens extends ASTNode {
	BasicTokenSource tokenSource;
	PseudoToken[] tokens;  //XXX this is redundant with the field above
	String endToken;
	

	public PseudoTokens(PseudoToken[] tokens, ISourceContext sourceContext) {
		super();
		this.tokens = tokens;
		this.tokenSource = new BasicTokenSource(tokens, sourceContext);
		endToken = tokens[tokens.length-1].getString();
		sourceStart = tokens[0].sourceStart;
		sourceEnd = tokens[tokens.length-2].sourceEnd;
	}
	
	public Pointcut parsePointcut(Parser parser) {
		PatternParser patternParser = new PatternParser(tokenSource);
		try {
			Pointcut ret = patternParser.parsePointcut();
			checkEof(parser);
			return ret;
		} catch (ParserException pe) {
			reportError(parser, pe);
			return Pointcut.makeMatchesNothing(Pointcut.SYMBOLIC);
		}
	}

	private void checkEof(Parser parser) {
		IToken last = tokenSource.next();
		if (tokenSource.next() != IToken.EOF) {
			parser.problemReporter().parseError(last.getStart(), last.getEnd(),
											TerminalTokens.TokenNameIdentifier,
		                                    last.getString().toCharArray(),
		                                    last.getString(),
		                                    new String[] {endToken});
		}
	}

	
	private void reportError(Parser parser, ParserException pe) {
		IHasPosition tok = pe.getLocation();
		int start, end;
		if (tok == IToken.EOF) {
			start = sourceEnd+1;
			end = sourceEnd+1;
		} else {
			start = tok.getStart();
			end = tok.getEnd();
		}
		String found = "<unknown>";
		if (tok instanceof IToken) {
			found = ((IToken)tok).getString();
		}
		
		parser.problemReporter().parseError(start, end,
											TerminalTokens.TokenNameIdentifier,
		                                    found.toCharArray(),
		                                    found,
		                                    new String[] {pe.getMessage()});
	}
	
	
	public TypePattern maybeParseDominatesPattern(Parser parser) {
		PatternParser patternParser = new PatternParser(tokenSource);
		try {
			if (patternParser.maybeEatIdentifier("dominates")) {
				// there is no eof check here
				return patternParser.parseTypePattern();
			} else {
				return null;
			}
		} catch (ParserException pe) {
			reportError(parser, pe);
			return null;
		}
	}		
	
	
	public PerClause parsePerClause(Parser parser) {
		PatternParser patternParser = new PatternParser(tokenSource);
		try {
			PerClause ret = patternParser.maybeParsePerClause();
			checkEof(parser);
			if (ret == null) return new PerSingleton();
			else return ret;
		} catch (ParserException pe) {
			reportError(parser, pe);
			return new PerSingleton();
		}
		
	}
	
	
//	public TypePattern parseTypePattern(Parser parser) {
//	}
//	
	public Declare parseDeclare(Parser parser) {
		PatternParser patternParser = new PatternParser(tokenSource);
		try {
			Declare ret = patternParser.parseDeclare();
			checkEof(parser);
			return ret;
		} catch (ParserException pe) {
			reportError(parser, pe);
			return null;
		}
	}
	
	public Declare parseAnnotationDeclare(Parser parser) {
		PatternParser patternParser = new PatternParser(tokenSource);
		try {
			Declare ret = patternParser.parseDeclareAnnotation();
			checkEof(parser);
			return ret;
		} catch (ParserException pe) {
			reportError(parser, pe);
			return null;
		}	}
	
	
	public void postParse(TypeDeclaration typeDec, MethodDeclaration enclosingDec) {
		int counter = 0; // Counter can be used by postParse as a value to compute uniqueness (if required)
		for (PseudoToken token : tokens) {
			counter += token.postParse(typeDec, enclosingDec, counter);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.ASTNode#print(int, java.lang.StringBuffer)
	 */
	public StringBuffer print(int indent, StringBuffer output) {
		output.append(tokenSource.toString());
		return output;
	}

}
