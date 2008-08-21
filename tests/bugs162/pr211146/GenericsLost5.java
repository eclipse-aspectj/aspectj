import java.util.*;
import java.lang.reflect.*;

// generic field itd
aspect Foo {
   public List<String> Goo.ls;
}

class Goo {}

public class GenericsLost5 {
   public static void main(String[]argv) throws Exception {
	   Field f = Goo.class.getDeclaredField("ls");
	   Type t = f.getGenericType();
	   if (!t.toString().equals("java.util.List<java.lang.String>")) 
		   throw new RuntimeException("Incorrect signature. Signature is "+t);
   }
}
	