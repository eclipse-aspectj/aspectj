/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Matthew Webster         initial implementation
 *******************************************************************************/

public abstract aspect AbstractSuperAspect {
	
		protected abstract pointcut scope ();
	
		before () : execution(public static void main(String[])) && scope() {
			System.out.println("? " + thisJoinPoint.getSignature());
		}
}
