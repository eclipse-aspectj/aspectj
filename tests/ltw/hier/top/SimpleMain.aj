/*******************************************************************************
 * Copyright (c) 2006 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 * Ron Bodkin
 */
package top;

public class SimpleMain {
   public static void main(String args[]) throws Exception {
      new child.Executor().run();
   }
}
