import java.lang.reflect.*;

public class TheWholeShow {

  private int f;

  public void foo() {}

  private void bar() {}
  
  public static void main(String[] args) {
    Field[] twsFields = TheWholeShow.class.getDeclaredFields();
    for (Field f : twsFields) {
      if (!f.getName().equals("f") && !f.getName().equals("x")  && !f.getName().startsWith("ajc$interField$")) {
        if (!f.isSynthetic()) {
          System.err.println("Found non-synthetic field: " + f.getName());
          throw new IllegalStateException("Found non-synthetic field: " + f.getName());
        }
        if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())) {
          System.err.println("Found non-transient field: " + f.getName());
          throw new IllegalStateException("Found non-transient field: " + f.getName());        
        }
      }
    }
    
    Method[] twsMethods = TheWholeShow.class.getDeclaredMethods();
    for (Method m: twsMethods) {
      if (! (m.getName().equals("foo") || m.getName().equals("bar") || m.getName().equals("<init>") ||
             m.getName().equals("main") || m.getName().equals("checkOnlyHasAdviceMembers") || m.getName().equals("getX")) ) {
        if (!m.isSynthetic()) {
          System.err.println("Found non-synthetic method: " + m.getName());
          throw new IllegalStateException("Found non-synthetic method: " + m.getName());        
        }
      }
    }
    
    checkOnlyHasAdviceMembers(MakeITDs.class);
    checkOnlyHasAdviceMembers(Declares.class);
    checkOnlyHasAdviceMembers(Advises.class);
    checkOnlyHasAdviceMembers(PerObject.class);
    checkOnlyHasAdviceMembers(PTW.class);
    checkOnlyHasAdviceMembers(Priv.class);
    
  }
  
  
  private static void checkOnlyHasAdviceMembers(Class c) {
    Method[] ms = c.getDeclaredMethods();
    Field[] fs = c.getDeclaredFields();
    
    for (Field f : fs) {
      if (!f.isSynthetic()) {
          System.err.println("Found non-synthetic field: " + f.getName() + " in " + c.getName());
          throw new IllegalStateException("Found non-synthetic field: " + f.getName());      
      }
    }
    
    for (Method m : ms) {
      if (!m.isSynthetic()) {
        String name = m.getName();
        if (name.equals("aspectOf") || name.equals("hasAspect") || name.equals("getWithinTypeName")) continue;
        if ( ! (name.startsWith("ajc$before") || name.startsWith("ajc$after") || name.startsWith("ajc$around")  ||
             name.startsWith("ajc$interMethod$"))) {
          System.err.println("Found non-synthetic method: " + m.getName() + " in " + c.getName());
          throw new IllegalStateException("Found non-synthetic method: " + m.getName());                
        } else if (name.startsWith("ajc$around") && name.endsWith("proceed")) {
          System.err.println("Found non-synthetic method: " + m.getName() + " in " + c.getName());
          throw new IllegalStateException("Found non-synthetic method: " + m.getName());                        
        }
      }
    }
  }
}


aspect MakeITDs {

  public int TheWholeShow.x = 5;
  private int TheWholeShow.y = 6;
  int TheWholeShow.z = 7;
  
  public int TheWholeShow.getX() { return x; }
  
  private int TheWholeShow.getY() { return y; }
  
  int TheWholeShow.getZ() { return z; }

}

aspect Declares {

  interface Foo {}
  
  declare parents : TheWholeShow implements Foo;
  
  declare warning : execution(* TheWholeShow.notThere(..)) : "foo";

  declare soft : Exception : execution(* TheWholeShow.foo(..));
  
}

aspect Advises {

  pointcut pc() : execution(* TheWholeShow.*(..));

  before() : pc() {}

  Object around(Object tws) : pc() && this(tws) {
    return proceed(new TheWholeShow());
  }
  
  after() : pc() {}
  
  after() returning : pc() {}
  
  after() throwing : pc() {}
  
  
}

aspect PerObject perthis(execution(* TheWholeShow.*(..))) {

}

aspect PTW pertypewithin(TheWholeShow) {}

aspect Cflow {

  before() : set(* x) && cflow(execution(* TheWholeShow.*(..))) {}
  
}

privileged aspect Priv {

 before(TheWholeShow tws) : execution(* TheWholeShow.foo()) && this(tws) {
 	tws.bar();
 	tws.f = 12;
 }

}