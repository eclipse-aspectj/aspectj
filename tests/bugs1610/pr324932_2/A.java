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

interface LogNoi18n {
boolean isDebugEnabled(); 
void debug(String message);
String getString(String key);
}
class AtomicAction  {
  int status() { return 1; }
  int abort() { return 1; }
  int commit(int n) { return 1; }
  Throwable getDeferredThrowable() { return null; }
}
class RollbackException extends RuntimeException {
  RollbackException(String s) {
    super(s);
  }
}

public class A {
public static void main(String []argv) {
	System.out.println("It WORKS");
int i = 1;  
 }
  static LogNoi18n logger;
  AtomicAction _theTransaction; 
  Throwable _rollbackOnlyCallerStacktrace;
  public void m() {
    if (logger.isDebugEnabled()) {
      logger.debug("TransactionImple.commitAndDisassociate");
    }
   try {
    if (_theTransaction!=null) {
      switch (_theTransaction.status()) {
      case 2:
      case 4:
        _theTransaction.abort();
        throw new RollbackException(logger.getString("inactive"));
      case 6:
      case 7:
        _theTransaction.commit(1);
         return;
      case 3:
      case 5:
      default:
        break;
      }
        switch (_theTransaction.commit(1)) {
           case 6:case 7:  // 188
             break;
           case 13: // 191
             throw new RuntimeException();
           case 14: // 199
             throw new RuntimeException();
           case 2:case 4: case 11: // 207
             RollbackException o = new RollbackException(logger.getString("inactive"));
             if (_rollbackOnlyCallerStacktrace!=null) {
               o.initCause(_rollbackOnlyCallerStacktrace);
             } else 
               if (_theTransaction.getDeferredThrowable()!=null) {
               o.initCause(_theTransaction.getDeferredThrowable());
               }
            
             throw o;
           default:
             throw new RuntimeException(logger.getString("inactive"));
        }
     } else {
        throw new IllegalStateException(logger.getString("inactive"));
     }
   } finally {
        removeTransaction(this);
   }
  }

  public static void removeTransaction(A o) {
  }
}

