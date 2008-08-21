import java.util.*;
import java.lang.reflect.*;

aspect Foo {

   public List<String> Goo.getStrings() {
     return null;
   }
   
}

class Goo {
}
	
public class GenericsLost {

	   public static void main(String[]argv) throws Exception {
		   Method m = Goo.class.getDeclaredMethod("getStrings");
		   Type t = m.getGenericReturnType();
		   if (!t.toString().equals("java.util.List<java.lang.String>")) 
			   throw new RuntimeException("Incorrect signature. Signature is "+t);
	   }
}
