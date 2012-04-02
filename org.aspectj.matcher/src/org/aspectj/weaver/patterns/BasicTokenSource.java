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


package org.aspectj.weaver.patterns;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;


public class BasicTokenSource implements ITokenSource {
	private int index = 0;
	private IToken[] tokens;
	private ISourceContext sourceContext;

	public BasicTokenSource(IToken[] tokens, ISourceContext sourceContext) {
		this.tokens = tokens;
		this.sourceContext = sourceContext;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int newIndex) {
		this.index = newIndex;
	}

	public IToken next() {
		try {
		    return tokens[index++];
		} catch (ArrayIndexOutOfBoundsException e) {
			return IToken.EOF;
		}
	}

	public IToken peek() {
		try {
		    return tokens[index];
		} catch (ArrayIndexOutOfBoundsException e) {
			return IToken.EOF;
		}
	}

	public IToken peek(int offset) {
		try {
		    return tokens[index+offset];
		} catch (ArrayIndexOutOfBoundsException e) {
			return IToken.EOF;
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < tokens.length; i++) {
			IToken t = tokens[i];
			if (t == null)
				break;
			if (i > 0)
				buf.append(", ");
			buf.append(t.toString());
		}
		buf.append("]");
		return buf.toString();
	}
	
	
	//////////////////////////////////////////////////////
	// Convenience, maybe just for testing
	public static ITokenSource makeTokenSource(String input, ISourceContext context) {
		char[] chars = input.toCharArray();
		
		int i = 0;
		List tokens = new ArrayList();
		
		while (i < chars.length) {
			char ch = chars[i++];			
			switch(ch) {
				case ' ':
				case '\t':
				case '\n':
				case '\r':
					continue;
				case '*':
				case '(':
				case ')':
				case '+':
				case '[':
				case ']':
				case ',':
				case '!':
				case ':':
				case '@':
				case '<':
				case '>':
				case '=':
				case 	'?':
				    tokens.add(BasicToken.makeOperator(makeString(ch), i-1, i-1));
				    continue;
				case '.':
					if ((i+2)<=chars.length) {
						// could be '...'
						char nextChar1 = chars[i];
						char nextChar2 = chars[i+1];
						if (ch==nextChar1 && ch==nextChar2) {
							// '...'
							tokens.add(BasicToken.makeIdentifier("...",i-1,i+1));
							i=i+2;
						} else {
							tokens.add(BasicToken.makeOperator(makeString(ch), i-1, i-1));
						}
					} else {
						tokens.add(BasicToken.makeOperator(makeString(ch), i-1, i-1));
					}
					continue;
				case '&':
					if ((i+1) <= chars.length && chars[i] != '&') {
						tokens.add(BasicToken.makeOperator(makeString(ch),i-1,i-1));
						continue;
					}
					// fall-through
				case '|':
				    if (i == chars.length) {
				    	throw new BCException("bad " + ch);
				    }
				    char nextChar = chars[i++];
				    if (nextChar == ch) {
				    	tokens.add(BasicToken.makeOperator(makeString(ch, 2), i-2, i-1));
				    } else {
				    	throw new RuntimeException("bad " + ch);
				    }
				    continue;
				    
				case '\"':
				    int start0 = i-1;
				    while (i < chars.length && !(chars[i]=='\"')) i++;
				    i += 1;
				    tokens.add(BasicToken.makeLiteral(new String(chars, start0+1, i-start0-2), "string", start0, i-1));
				    continue;
				default:
				    int start = i-1;
				    while (i < chars.length && Character.isJavaIdentifierPart(chars[i])) { i++; }
				    tokens.add(BasicToken.makeIdentifier(new String(chars, start, i-start), start, i-1));
				
			}
		}

		//System.out.println(tokens);
		
		return new BasicTokenSource((IToken[])tokens.toArray(new IToken[tokens.size()]), context);
	}

	private static String makeString(char ch) {
		return Character.toString(ch);
	}

	private static String makeString(char ch, int count) {
		// slightly inefficient ;-)
		char[] chars = new char[count];
		for (int i=0; i<count; i++) { chars[i] = ch; }
		return new String(chars);
	}
	public ISourceContext getSourceContext() {
		return sourceContext;
	}
	public void setSourceContext(ISourceContext context) {
		this.sourceContext = context;
	}

}
