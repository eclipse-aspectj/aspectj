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
package org.aspectj.weaver.patterns;

import java.io.IOException;

import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.VersionedDataInputStream;

/**
 * @author colyer A list of type variable specifications, eg. &lt;T,S&gt;
 */
public class TypeVariablePatternList extends PatternNode {

	public static final TypeVariablePatternList EMPTY = new TypeVariablePatternList(new TypeVariablePattern[0]);

	private TypeVariablePattern[] patterns;

	public TypeVariablePatternList(TypeVariablePattern[] typeVars) {
		this.patterns = typeVars;
	}

	public TypeVariablePattern[] getTypeVariablePatterns() {
		return this.patterns;
	}

	public TypeVariablePattern lookupTypeVariable(String name) {
		for (int i = 0; i < patterns.length; i++) {
			if (patterns[i].getName().equals(name)) {
				return patterns[i];
			}
		}
		return null;
	}

	public boolean isEmpty() {
		return ((patterns == null) || (patterns.length == 0));
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeInt(patterns.length);
		for (int i = 0; i < patterns.length; i++) {
			patterns[i].write(s);
		}
		writeLocation(s);
	}

	public static TypeVariablePatternList read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		TypeVariablePatternList ret = EMPTY;
		int length = s.readInt();
		if (length > 0) {
			TypeVariablePattern[] patterns = new TypeVariablePattern[length];
			for (int i = 0; i < patterns.length; i++) {
				patterns[i] = TypeVariablePattern.read(s, context);
			}
			ret = new TypeVariablePatternList(patterns);
		}
		ret.readLocation(context, s); // redundant but safe to read location for EMPTY
		return ret;
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public Object traverse(PatternNodeVisitor visitor, Object data) {
		Object ret = accept(visitor, data);
		for (int i = 0; i < patterns.length; i++) {
			patterns[i].traverse(visitor, ret);
		}
		return ret;
	}

}
