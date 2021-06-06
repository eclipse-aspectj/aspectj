/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.lookup;

import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Binding;

/**
 */
public class PointcutBinding extends Binding {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.lookup.BindingPattern#bindingType()
	 */
	public int bindingType() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.lookup.BindingPattern#readableName()
	 */
	public char[] readableName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Binding#kind()
	 */
	public int kind() {
		return ASTNode.Bit14;
	}

}
