import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public aspect Two {
  public static void main(String[] args) {
     new C().ma();
  }
  before(): execution(* ma(..)) { System.err.println("execution advice running");}
}

class C {
  
  public synchronized void ma() {
    System.err.println("hello");
  }
}

aspect TwoX { pointcut p(): lock(); }
