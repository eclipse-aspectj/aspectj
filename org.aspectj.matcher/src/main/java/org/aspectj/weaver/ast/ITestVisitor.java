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


package org.aspectj.weaver.ast;

import org.aspectj.weaver.internal.tools.MatchingContextBasedTest;


public interface ITestVisitor {

	void visit(And e);
	void visit(Instanceof i);
	void visit(Not not);
	void visit(Or or);
	void visit(Literal literal);
	void visit(Call call);
	void visit(FieldGetCall fieldGetCall);
	void visit(HasAnnotation hasAnnotation);
	void visit(MatchingContextBasedTest matchingContextTest);

}
