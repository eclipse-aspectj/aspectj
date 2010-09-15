/*******************************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - Repro test case
 *    Abraham Nevado 
 *******************************************************************************/

aspect X {
  after(): execution(* A.m()) {
    System.out.println(thisJoinPoint.getArgs().toString());
  }
  before(): execution(* A.m()) {
    System.out.println(thisJoinPointStaticPart);
  }
}
