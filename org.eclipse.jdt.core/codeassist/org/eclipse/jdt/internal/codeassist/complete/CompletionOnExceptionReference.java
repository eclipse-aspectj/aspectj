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
 * reduce an exception type reference containing the completion identifier.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      try {
 *        bar();
 *      } catch (IOExc[cursor] e) {
 *      }
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           try {
 *             bar();
 *           } catch (<CompleteOnException:IOExc> e) {
 *           }
 *         }
 *       }
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */
public class CompletionOnExceptionReference extends CompletionOnSingleTypeReference {
public CompletionOnExceptionReference(char[] source, long pos) {
	super(source, pos);
}
public String toStringExpression(int tab) {
	return "<CompleteOnException:" + new String(token) + ">"; //$NON-NLS-2$ //$NON-NLS-1$
}
}
