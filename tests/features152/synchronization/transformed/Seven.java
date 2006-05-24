import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// matching lock in a transformed non-static method...

public aspect Seven {
  public static void main(String[] args) {
    new C().b();
  }

  before(): !within(Seven) && lock() { 
    System.err.println("Locking occurring at "+thisJoinPoint);
    System.err.println(thisJoinPoint.getSourceLocation().getFileName());
  }
}
  
class C {
  public synchronized void b() {
    System.err.println("hello");
  }
}
