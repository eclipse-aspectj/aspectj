import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// matching unlock on transformed static method ( pre J5)

public aspect Twelve {
  public static void main(String[] args) {
    C.b();
  }

  before(): !within(Twelve) && unlock() { 
    System.err.println("Unlocking occurring at "+thisJoinPoint);
    System.err.println(thisJoinPoint.getSourceLocation().getFileName());
  }
}
  
class C {
  public static synchronized void b() {
    System.err.println("hello");
  }
}
