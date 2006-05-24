import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public aspect Three {
  public static void main(String[] args) {
     new C().m3();
     try {new C().m32(); } catch (MyException me) {int i=1;}
     new C().m33();
     try {new C().m34(); } catch (MyException me) {int i=1;}
  }
  after() returning: execution(* m3(..)) { System.err.println("execution advice running1");}
  after() throwing: execution(* m32(..)) { System.err.println("execution advice running2");}
  after() returning: execution(* m33(..)) { System.err.println("execution advice running3");}
  after() throwing: execution(* m34(..)) { System.err.println("execution advice running4");}
}

class C {
  
  public synchronized void m3() {
    System.err.println("hello");
  }

  public synchronized void m32() {
    System.err.println("hello");
    throw new MyException();
  }

  public void m33() {
    synchronized (this) { 
      System.err.println("hello");
    }
  }

  public void m34() {
    synchronized (this) {
      System.err.println("hello");
      throw new MyException();
    }
  }
}
  class MyException extends RuntimeException { }

class ThreeX { pointcut p(): lock(); }
