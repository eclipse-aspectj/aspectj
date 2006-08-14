/*******************************************************************************
 * Copyright (c) 2006 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
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
