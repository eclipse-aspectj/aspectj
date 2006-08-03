import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// matching lock on transformed static method ( pre J5)

public aspect Fifteen {
  public static void main(String[] args) {
    Blah.b();
  }

  before(): !within(Fifteen) && lock() { 
    System.err.println("Locking occurring at "+thisJoinPoint);
    System.err.println(thisJoinPoint.getSourceLocation().getFileName());
  }
}
  
class Blah {
  public static synchronized void b() {
    System.err.println("hello");
  }
}
