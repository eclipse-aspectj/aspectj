/*******************************************************************************
 * Copyright (c) 2011 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abraham Nevado - Lucierna	initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime.definition;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LightXMLParser {

	private final static char NULL_CHAR = '\0';
	private Map<String, Object> attributes;
	private ArrayList children;
	private String name;
	private char pushedBackChar;
	private Reader reader;

	private static Map<String, char[]> entities = new HashMap<String, char[]>();

	static {
		entities.put("amp", new char[] { '&' });
		entities.put("quot", new char[] { '"' });
		entities.put("apos", new char[] { '\'' });
		entities.put("lt", new char[] { '<' });
		entities.put("gt", new char[] { '>' });
	}

	public LightXMLParser() {
		this.name = null;
		this.attributes = new HashMap<String, Object>();
		this.children = new ArrayList();
	}

	public ArrayList getChildrens() {
		return this.children;
	}

	public String getName() {
		return this.name;
	}

	public void parseFromReader(Reader reader) throws Exception {
		this.pushedBackChar = NULL_CHAR;
		this.attributes = new HashMap<String, Object>();
		this.name = null;
		this.children = new ArrayList();
		this.reader = reader;

		while (true) {
			// Skips whiteSpaces, blanks, \r\n..
			char c = this.skipBlanks();

			// All xml should start by <xml, a <!-- or <nodeName, if not throw
			// exception
			if (c != '<') {
				throw new Exception("LightParser Exception: Expected < but got: " + c);
			}

			// read next character
			c = this.getNextChar();

			// if starts with ! or ? it is <?xml or a comment: skip
			if ((c == '!') || (c == '?')) {
				this.skipCommentOrXmlTag(0);
			} else {
				// it is a node, pusch character back
				this.pushBackChar(c);
				// parse node
				this.parseNode(this);
				// Only one root node, so finsh.
				return;
			}
		}
	}

	private char skipBlanks() throws Exception {
		while (true) {
			char c = this.getNextChar();
			switch (c) {
			case '\n':
			case '\r':
			case ' ':
			case '\t':
				break;
			default:
				return c;
			}
		}
	}

	private char getWhitespaces(StringBuffer result) throws Exception {
		while (true) {
			char c = this.getNextChar();
			switch (c) {
			case ' ':
			case '\t':
			case '\n':
				result.append(c);
			case '\r':
				break;
			default:
				return c;
			}
		}
	}

	private void getNodeName(StringBuffer result) throws Exception {
		char c;
		while (true) {
			// Iterate while next character is not [a-z] [A-Z] [0-9] [ .:_-] not
			// null
			c = this.getNextChar();
			if (((c < 'a') || (c > 'z')) && ((c > 'Z') || (c < 'A')) && ((c > '9') || (c < '0')) && (c != '_') && (c != '-')
					&& (c != '.') && (c != ':')) {
				this.pushBackChar(c);
				return;
			}
			result.append(c);
		}
	}

	private void getString(StringBuffer string) throws Exception {
		char delimiter = this.getNextChar();
		if ((delimiter != '\'') && (delimiter != '"')) {
			throw new Exception("Parsing error. Expected ' or \"  but got: " + delimiter);

		}

		while (true) {
			char c = this.getNextChar();
			if (c == delimiter) {
				return;
			} else if (c == '&') {
				this.mapEntity(string);
			} else {
				string.append(c);
			}
		}
	}

	private void getPCData(StringBuffer data) throws Exception {
		while (true) {
			char c = this.getNextChar();
			if (c == '<') {
				c = this.getNextChar();
				if (c == '!') {
					this.checkCDATA(data);
				} else {
					this.pushBackChar(c);
					return;
				}
			} else {
				data.append(c);
			}
		}
	}

	private boolean checkCDATA(StringBuffer buf) throws Exception {
		char c = this.getNextChar();
		if (c != '[') {
			this.pushBackChar(c);
			this.skipCommentOrXmlTag(0);
			return false;
		} else if (!this.checkLiteral("CDATA[")) {
			this.skipCommentOrXmlTag(1); // one [ has already been read
			return false;
		} else {
			int delimiterCharsSkipped = 0;
			while (delimiterCharsSkipped < 3) {
				c = this.getNextChar();
				switch (c) {
				case ']':
					if (delimiterCharsSkipped < 2) {
						delimiterCharsSkipped++;
					} else {
						buf.append(']');
						buf.append(']');
						delimiterCharsSkipped = 0;
					}
					break;
				case '>':
					if (delimiterCharsSkipped < 2) {
						for (int i = 0; i < delimiterCharsSkipped; i++) {
							buf.append(']');
						}
						delimiterCharsSkipped = 0;
						buf.append('>');
					} else {
						delimiterCharsSkipped = 3;
					}
					break;
				default:
					for (int i = 0; i < delimiterCharsSkipped; i++) {
						buf.append(']');
					}
					buf.append(c);
					delimiterCharsSkipped = 0;
				}
			}
			return true;
		}
	}

	private void skipCommentOrXmlTag(int bracketLevel) throws Exception {
		char delim = NULL_CHAR;
		int level = 1;
		char c;
		if (bracketLevel == 0) {
			c = this.getNextChar();
			if (c == '-') {
				c = this.getNextChar();
				if (c == ']') {
					bracketLevel--;
				} else if (c == '[') {
					bracketLevel++;
				} else if (c == '-') {
					this.skipComment();
					return;
				}
			} else if (c == '[') {
				bracketLevel++;
			}
		}
		while (level > 0) {
			c = this.getNextChar();
			if (delim == NULL_CHAR) {
				if ((c == '"') || (c == '\'')) {
					delim = c;
				} else if (bracketLevel <= 0) {
					if (c == '<') {
						level++;
					} else if (c == '>') {
						level--;
					}
				}
				if (c == '[') {
					bracketLevel++;
				} else if (c == ']') {
					bracketLevel--;
				}
			} else {
				if (c == delim) {
					delim = NULL_CHAR;
				}
			}
		}
	}

	private void parseNode(LightXMLParser elt) throws Exception {
		// Now we are in a new node element. Get its name
		StringBuffer buf = new StringBuffer();
		this.getNodeName(buf);
		String name = buf.toString();
		elt.setName(name);

		char c = this.skipBlanks();
		while ((c != '>') && (c != '/')) {
			// Get attributes
			emptyBuf(buf);
			this.pushBackChar(c);
			this.getNodeName(buf);
			String key = buf.toString();
			c = this.skipBlanks();
			if (c != '=') {
				throw new Exception("Parsing error. Expected = but got: " + c);
			}
			// Go up to " character and push it back
			this.pushBackChar(this.skipBlanks());

			emptyBuf(buf);
			this.getString(buf);

			elt.setAttribute(key, buf);

			// Skip blanks
			c = this.skipBlanks();
		}
		if (c == '/') {
			c = this.getNextChar();
			if (c != '>') {
				throw new Exception("Parsing error. Expected > but got: " + c);
			}
			return;
		}

		// Now see if we got content, or CDATA, if content get it: it is free...
		emptyBuf(buf);
		c = this.getWhitespaces(buf);
		if (c != '<') {
			// It is PCDATA
			this.pushBackChar(c);
			this.getPCData(buf);
		} else {
			// It is content: get it, or CDATA.
			while (true) {
				c = this.getNextChar();
				if (c == '!') {
					if (this.checkCDATA(buf)) {
						this.getPCData(buf);
						break;
					} else {
						c = this.getWhitespaces(buf);
						if (c != '<') {
							this.pushBackChar(c);
							this.getPCData(buf);
							break;
						}
					}
				} else {
					if (c != '/') {
						emptyBuf(buf);
					}
					if (c == '/') {
						this.pushBackChar(c);
					}
					break;
				}
			}
		}
		if (buf.length() == 0) {
			// It is a comment
			while (c != '/') {
				if (c == '!') {
					for (int i = 0; i < 2; i++) {
						c = this.getNextChar();
						if (c != '-') {
							throw new Exception("Parsing error. Expected element or comment");
						}
					}
					this.skipComment();
				} else {
					// it is a new node
					this.pushBackChar(c);
					LightXMLParser child = this.createAnotherElement();
					this.parseNode(child);
					elt.addChild(child);
				}
				c = this.skipBlanks();
				if (c != '<') {
					throw new Exception("Parsing error. Expected <, but got: " + c);
				}
				c = this.getNextChar();
			}
			this.pushBackChar(c);
		} // Here content could be grabbed

		c = this.getNextChar();
		if (c != '/') {
			throw new Exception("Parsing error. Expected /, but got: " + c);
		}
		this.pushBackChar(this.skipBlanks());
		if (!this.checkLiteral(name)) {
			throw new Exception("Parsing error. Expected " + name);
		}
		if (this.skipBlanks() != '>') {
			throw new Exception("Parsing error. Expected >, but got: " + c);
		}
	}

	private void skipComment() throws Exception {
		int dashes = 2;
		while (dashes > 0) {
			char ch = this.getNextChar();
			if (ch == '-') {
				dashes -= 1;
			} else {
				dashes = 2;
			}
		}

		char nextChar = this.getNextChar();
		if (nextChar != '>') {
			throw new Exception("Parsing error. Expected > but got: " + nextChar);
		}
	}

	private boolean checkLiteral(String literal) throws Exception {
		int length = literal.length();
		for (int i = 0; i < length; i++) {
			if (this.getNextChar() != literal.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	private char getNextChar() throws Exception {
		if (this.pushedBackChar != NULL_CHAR) {
			char c = this.pushedBackChar;
			this.pushedBackChar = NULL_CHAR;
			return c;
		} else {
			int i = this.reader.read();
			if (i < 0) {
				throw new Exception("Parsing error. Unexpected end of data");
			} else {
				return (char) i;
			}
		}
	}

	private void mapEntity(StringBuffer buf) throws Exception {
		char c = this.NULL_CHAR;
		StringBuffer keyBuf = new StringBuffer();
		while (true) {
			c = this.getNextChar();
			if (c == ';') {
				break;
			}
			keyBuf.append(c);
		}
		String key = keyBuf.toString();
		if (key.charAt(0) == '#') {
			try {
				if (key.charAt(1) == 'x') {
					c = (char) Integer.parseInt(key.substring(2), 16);
				} else {
					c = (char) Integer.parseInt(key.substring(1), 10);
				}
			} catch (NumberFormatException e) {
				throw new Exception("Unknown entity: " + key);
			}
			buf.append(c);
		} else {
			char[] value = (char[]) entities.get(key);
			if (value == null) {
				throw new Exception("Unknown entity: " + key);
			}
			buf.append(value);
		}
	}

	private void pushBackChar(char c) {
		this.pushedBackChar = c;
	}

	private void addChild(LightXMLParser child) {
		this.children.add(child);
	}

	private void setAttribute(String name, Object value) {
		this.attributes.put(name, value.toString());
	}

	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	private LightXMLParser createAnotherElement() {
		return new LightXMLParser();
	}

	private void setName(String name) {
		this.name = name;
	}

	private void emptyBuf(StringBuffer buf) {
		buf.setLength(0);
	}

}
