/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

/*
 * Completion node build by the parser in any case it was intending to
 * reduce an exception type reference containing the completion identifier 
 * as part of a qualified name.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      try {
 *        bar();
 *      } catch (java.io.IOExc[cursor] e) {
 *      }
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           try {
 *             bar();
 *           } catch (<CompleteOnException:java.io.IOExc> e) {
 *           }
 *         }
 *       }
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */
public class CompletionOnQualifiedExceptionReference extends CompletionOnQualifiedTypeReference {
public CompletionOnQualifiedExceptionReference(char[][] previousIdentifiers, char[] completionIdentifier, long[] positions) {
	super(previousIdentifiers, completionIdentifier, positions);
}
public String toStringExpression(int tab) {

	StringBuffer buffer = new StringBuffer();
	buffer. append("<CompleteOnException:"); //$NON-NLS-1$
	for (int i = 0; i < tokens.length; i++) {
		buffer.append(tokens[i]);
		buffer.append("."); //$NON-NLS-1$
	}
	buffer.append(completionIdentifier).append(">"); //$NON-NLS-1$
	return buffer.toString();
}
}
