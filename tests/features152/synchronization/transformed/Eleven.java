import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// matching lock on transformed static method ( pre J5)

public aspect Eleven {
  public static void main(String[] args) {
    C.b();
  }

  before(): !within(Eleven) && lock() { 
    System.err.println("Locking occurring at "+thisJoinPoint);
    System.err.println(thisJoinPoint.getSourceLocation().getFileName());
  }
}
  
class C {
  public static synchronized void b() {
    System.err.println("hello");
  }
}
