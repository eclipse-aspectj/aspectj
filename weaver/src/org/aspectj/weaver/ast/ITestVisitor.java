/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.ast;


public interface ITestVisitor {

	void visit(And e);
	void visit(Instanceof i);
	void visit(Not not);
	void visit(Or or);
	void visit(Literal literal);
	void visit(Call call);
	void visit(FieldGetCall fieldGetCall);

}
