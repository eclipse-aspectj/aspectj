import java.io.*;
import org.aspectj.lang.*;

public class Code2 {
  public static void main(String[]argv) {
    try {
      new Code2().m();
    } catch (SoftException se) {
      System.out.println(se.getWrappedThrowable().getMessage());
    }
  }
  
  public void m() { 
    try (MyReader reader = new MyReader()) {
      System.out.println("");
    }
  }
}
aspect X {
  declare soft: MyException: within(Code2);
}

class MyReader implements AutoCloseable {
  public void close() throws MyException {
    throw new MyException("foo");
  }
}

class MyException extends Exception {
  public MyException(String s) {
	super(s);
  }
}
