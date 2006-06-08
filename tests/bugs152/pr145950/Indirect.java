import java.io.Serializable;
import java.lang.reflect.Field;

public class Indirect implements I {
  public static void main(String[] args) {
    try {
      Indirect b = (Indirect)Indirect.class.newInstance();
      Field f = Indirect.class.getDeclaredField("serialVersionUID");
      long l = f.getLong(b);
      System.err.println("SerialVersionUID is "+l);
    } catch (Exception e) {
      System.err.println("Problem: "+e.toString());
    }
  }
}

interface I extends Serializable {}

aspect X {
	before(): staticinitialization(Indirect) {}
}
