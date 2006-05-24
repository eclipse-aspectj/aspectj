import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public aspect Five {
  public static void main(String[] args) {
    C.b();
    C.c();
    C.d();
    C.e();
  }

  before(): !within(Five) && call(* println(..)) { System.err.println("test");}
}
  
class C {
  public static synchronized void b() {
    System.err.println("hello");
  }

  public static void c() {
    synchronized (C.class) {
      System.err.println("hello");
    }
  }
  public static void d() {
    synchronized (String.class) {
      System.err.println("hello");
    }
  }
  public static void e() {
    synchronized (Five.class) {
      System.err.println("hello");
    }
  }
}

aspect FiveX { pointcut p(): unlock(); }
