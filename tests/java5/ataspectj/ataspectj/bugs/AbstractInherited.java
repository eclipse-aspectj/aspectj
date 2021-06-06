/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package ataspectj.bugs;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
//@Aspect
public abstract aspect AbstractInherited {

    //@Around("execution(* foo(..))")
    //public Object around() {
    //    return null;
    //}

  void around(): execution(* foo(..)) {
  }

}

@Aspect
class Sub extends AbstractInherited {
}

class C {
    void foo() {
    }
}


