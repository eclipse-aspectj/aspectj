/*
Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.

This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.
*/

package tjp;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;

aspect GetInfo {

   static final void println(String s){ System.out.println(s); }

   pointcut goCut(): cflow(this(Demo) && execution(void go()));

   pointcut demoExecs(): within(Demo) && execution(* *(..));

   Object around(): demoExecs() && !execution(* go()) && goCut() {
      println("Intercepted message: " +
          thisJoinPointStaticPart.getSignature().getName());
      println("in class: " +
          thisJoinPointStaticPart.getSignature().getDeclaringType().getName());
      printParameters(thisJoinPoint);
      println("Running original method: \n" );
      Object result = proceed();
      println("  result: " + result );
      return result;
   }

   static private void printParameters(JoinPoint jp) {
      println("Arguments: " );
      Object[] args = jp.getArgs();
      String[] names = ((CodeSignature)jp.getSignature()).getParameterNames();
      Class[] types = ((CodeSignature)jp.getSignature()).getParameterTypes();
      for (int i = 0; i < args.length; i++) {
         println("  "  + i + ". " + names[i] +
             " : " +            types[i].getName() +
             " = " +            args[i]);
      }
   }
}
