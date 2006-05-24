import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public aspect Four {
  public static void main(String[] args) {
     new C().m();
     try {new C().m2(); } catch (MyException me) {int i=1;}
  }
  after() returning: execution(synchronized * m(..)) { System.err.println("execution advice running1");}
  after() throwing: execution(synchronized * m2(..)) { System.err.println("execution advice running2");}
}

class C {
  
  public synchronized void m() {
    System.err.println("hello");
  }

  public synchronized void m2() {
    System.err.println("hello");
    throw new MyException();
  }

}

class MyException extends RuntimeException { }

aspect FourX { pointcut p(): lock(); }
