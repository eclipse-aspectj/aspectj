import java.util.*;

public privileged aspect TestAspect {

  pointcut TestToArray(Test mt) : target(mt) && !within(TestAspect);

  pointcut getFirstExec(Test mt) :
    execution(* getFirst(..)) && target(mt) && !within(TestAspect);


  Object[] around(Test mt, Object[] objs) :
    TestToArray(mt) && args(objs) &&
    execution(Object[] Test.toArray(Object[])) {

    System.err.println("In around advice");
    objs = proceed(mt, objs);
    return objs;
  }

  Object around(Test mt): getFirstExec(mt) {
    System.err.println("around on getFirstExec(): running");
    return proceed(mt);
  }


  public static void main(String[] argv) {
     System.err.println("TestAspect.main: Calling foo");
     new TTT().foo();   
     Object o = new TTT().getFirst();   
     System.err.println("TestAspect.main: done");
  }
}
