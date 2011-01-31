/*******************************************************************************
 * Copyright (c) 2010 Contributors All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
