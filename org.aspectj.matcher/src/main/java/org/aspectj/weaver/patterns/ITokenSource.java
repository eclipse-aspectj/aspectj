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

import org.aspectj.weaver.ISourceContext;


public interface ITokenSource {
	IToken next();
	IToken peek();
	IToken peek(int offset);
	int getIndex();
	void setIndex(int newIndex);
	ISourceContext getSourceContext();
	boolean hasMoreTokens();
}
