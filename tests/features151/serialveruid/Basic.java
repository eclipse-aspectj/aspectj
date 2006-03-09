import java.io.Serializable;
import java.lang.reflect.Field;

public class Basic implements Serializable {
  public static void main(String[] args) {
    try {
      Basic b = (Basic)Basic.class.newInstance();
      Field f = Basic.class.getDeclaredField("serialVersionUID");
      long l = f.getLong(b);
      System.err.println("SerialVersionUID is "+l);
    } catch (Exception e) {
      System.err.println("Problem: "+e.toString());
    }
  }
}

aspect X {
	before(): staticinitialization(Basic) {}
}