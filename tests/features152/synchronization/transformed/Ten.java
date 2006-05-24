import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// matching unlock on transformed static method (J5)

public aspect Ten {
  public static void main(String[] args) {
    C.b();
  }

  before(): !within(Ten) && unlock() { 
    System.err.println("Unlocking occurring at "+thisJoinPoint);
    System.err.println(thisJoinPoint.getSourceLocation().getFileName());
  }
}
  
class C {
  public static synchronized void b() {
    System.err.println("hello");
  }
}
