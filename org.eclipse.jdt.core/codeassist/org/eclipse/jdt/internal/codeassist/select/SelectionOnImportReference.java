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
package org.eclipse.jdt.internal.codeassist.select;

/*
 * Selection node build by the parser in any case it was intending to
 * reduce an import reference containing the assist identifier.
 * e.g.
 *
 *  import java.[start]io[end].*;
 *	class X {
 *    void foo() {
 *    }
 *  }
 *
 *	---> <SelectOnImport:java.io>
 *		 class X {
 *         void foo() {
 *         }
 *       }
 *
 */
 
import org.eclipse.jdt.internal.compiler.ast.ImportReference;

public class SelectionOnImportReference extends ImportReference {

public SelectionOnImportReference(char[][] tokens , long[] positions) {
	super(tokens, positions, false);
}
public String toString(int tab, boolean withOnDemand) {

	StringBuffer buffer = new StringBuffer(tabString(tab));
	buffer.	append("<SelectOnImport:"); //$NON-NLS-1$
	for (int i = 0; i < tokens.length; i++) {
		buffer.append(tokens[i]);
		if (i < (tokens.length - 1)) {
			buffer.append("."); //$NON-NLS-1$
		}
	}
	buffer.append(">"); //$NON-NLS-1$
	return buffer.toString();
}
}
