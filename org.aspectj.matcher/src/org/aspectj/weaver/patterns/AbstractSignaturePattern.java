/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement - SpringSource
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;

import org.aspectj.weaver.BCException;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.VersionedDataInputStream;

/**
 * Implements common functions to be used across ISignaturePatterns.
 * 
 * @author Andy Clement
 * @since 1.6.9
 */
public abstract class AbstractSignaturePattern implements ISignaturePattern {

	protected void writePlaceholderLocation(CompressingDataOutputStream s) throws IOException {
		s.writeInt(0);
		s.writeInt(0);
	}

	public static ISignaturePattern readCompoundSignaturePattern(VersionedDataInputStream s, ISourceContext context)
			throws IOException {
		byte key = s.readByte();
		switch (key) {
		case PATTERN:
			return SignaturePattern.read(s, context);
		case AND:
			return AndSignaturePattern.readAndSignaturePattern(s, context);
		case OR:
			return OrSignaturePattern.readOrSignaturePattern(s, context);
		case NOT:
			return NotSignaturePattern.readNotSignaturePattern(s, context);
		default:
			throw new BCException("unknown SignatureTypePattern kind: " + key);
		}
	}

	public static void writeCompoundSignaturePattern(CompressingDataOutputStream s, ISignaturePattern sigPattern)
			throws IOException {
		if (sigPattern instanceof SignaturePattern) {
			s.writeByte(PATTERN);
			((SignaturePattern) sigPattern).write(s);
		} else if (sigPattern instanceof AndSignaturePattern) {
			AndSignaturePattern andSignaturePattern = (AndSignaturePattern) sigPattern;
			s.writeByte(AND);
			writeCompoundSignaturePattern(s, andSignaturePattern.getLeft());
			writeCompoundSignaturePattern(s, andSignaturePattern.getRight());
			s.writeInt(0);
			s.writeInt(0); // TODO positions not yet set properly
		} else if (sigPattern instanceof OrSignaturePattern) {
			OrSignaturePattern orSignaturePattern = (OrSignaturePattern) sigPattern;
			s.writeByte(OR);
			writeCompoundSignaturePattern(s, orSignaturePattern.getLeft());
			writeCompoundSignaturePattern(s, orSignaturePattern.getRight());
			s.writeInt(0);
			s.writeInt(0); // TODO positions not yet set properly
		} else {
			// negated
			NotSignaturePattern notSignaturePattern = (NotSignaturePattern) sigPattern;
			s.writeByte(NOT);
			writeCompoundSignaturePattern(s, notSignaturePattern.getNegated());
			s.writeInt(0);
			s.writeInt(0); // TODO positions not yet set properly
		}
	}
}
