import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// matching unlock in a transformed non-static method...

public aspect Eight {
  public static void main(String[] args) {
    new C().b();
  }

  before(): !within(Eight) && unlock() { 
    System.err.println("Unlocking occurring at "+thisJoinPoint);
    System.err.println(thisJoinPoint.getSourceLocation().getFileName());
  }
}
  
class C {
  public synchronized void b() {
    System.err.println("hello");
  }
}
