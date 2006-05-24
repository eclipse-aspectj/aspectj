import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public aspect Six {
  public static void main(String[] args) {
    C.bbb();
    C.c();
  }

  before(): !within(Six) && call(* println(..)) { System.err.println("test");}
}
  
class C {
  public static synchronized void bbb() {
    System.err.println("hello");
  }

  public static void c() {
    synchronized (C.class) {
      System.err.println("hello");
    }
  }
}

aspect SixX { pointcut p(): unlock(); }
