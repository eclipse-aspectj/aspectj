import java.util.*;
import java.lang.reflect.*;

// interface ITD
aspect Foo {
   public List<String> IFace.getStrings() {
     return null;
   }
}

interface IFace {}

class Goo implements IFace {}

public class GenericsLost4 {
   public static void main(String[]argv) throws Exception {
	   Method m = Goo.class.getDeclaredMethod("getStrings");
	   Type t = m.getGenericReturnType();
	   if (!t.toString().equals("java.util.List<java.lang.String>")) 
		   throw new RuntimeException("Incorrect signature. Signature is "+t);

	   m = IFace.class.getDeclaredMethod("getStrings");
	   t = m.getGenericReturnType();
	   if (!t.toString().equals("java.util.List<java.lang.String>")) 
		   throw new RuntimeException("Incorrect signature. Signature is "+t);
   }
}
	