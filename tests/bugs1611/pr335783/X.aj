/*******************************************************************************
 * Copyright (c) 2010 Contributors All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors: Abraham Nevado
 *******************************************************************************/

aspect X {

	public pointcut doSomething(Object o):
    	execution(* *()) && target(o);

	before(Object o) : doSomething(o) {
		try {
			String signature = thisJoinPointStaticPart.getSignature().toString();
			System.out.println("OK");
		} catch (NullPointerException npe) {
			System.out.println("KO");
		}
	}
}
