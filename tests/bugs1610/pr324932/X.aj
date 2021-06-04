/*******************************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
