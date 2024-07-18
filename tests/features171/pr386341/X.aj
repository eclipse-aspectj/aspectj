/*******************************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Andy Clement - repro test case
 *    Abraham Nevado
 *    Alexander Kriegisch - repro for GitHub 314
 *******************************************************************************/

aspect X {
  after(): execution(* *.*()) {
    System.out.println("It Worked-after");
  }

  before(): execution(* *.*()) {
    System.out.println("It Worked-before");
  }

  // Around advice reproduces GitHub 314 in connection with per-classloader cache
  Object around(): execution(* *.*()) {
    System.out.println("It Worked-around");
    return proceed();
  }
}
