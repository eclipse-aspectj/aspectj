import java.util.*;
import java.lang.reflect.*;

aspect Foo {

   // return type
   public List<String> Goo.getStrings() {
     return null;
   }
   
   // parameters
   public void Goo.putStrings(List<String> ls, List<Integer> lls) {
	   
   }
   
   // type variables
   public <T extends Number> List<T> Goo.numerics(T t) {
	   return null;
   }

   // type variables 2
   public <T extends List<String>> List<T> Goo.nightmare(T t) {
	   return null;
   }

   // type variables 3
   public <T extends List<Q>,Q extends Number> List<T> Goo.holyCow(Q t) {
	   return null;
   }
}

class Goo {
}
	
public class GenericsLost3 {

	   public static void main(String[]argv) throws Exception {
		   Method m = Goo.class.getDeclaredMethod("getStrings");
		   Type t = m.getGenericReturnType();
		   if (!t.toString().equals("java.util.List<java.lang.String>")) 
			   throw new RuntimeException("Incorrect signature1. Signature is "+t);
		   

		   m = Goo.class.getDeclaredMethod("putStrings",new Class[]{List.class,List.class});
		   Type[] ps = m.getGenericParameterTypes();
		   if (!ps[0].toString().equals("java.util.List<java.lang.String>")) 
			   throw new RuntimeException("Incorrect signature2. Signature is "+t);
		   if (!ps[1].toString().equals("java.util.List<java.lang.Integer>")) 
			   throw new RuntimeException("Incorrect signature3. Signature is "+t);
		   

		   m = Goo.class.getDeclaredMethod("numerics", new Class[]{Number.class});
		   t = m.getGenericReturnType();
		   if (!t.toString().equals("java.util.List<T>")) 
			   throw new RuntimeException("Incorrect signature4. Signature is "+t);
		   t = m.getGenericParameterTypes()[0];
		   if (!t.toString().equals("T")) 
			   throw new RuntimeException("Incorrect signature5. Signature is "+t);

		   m = Goo.class.getDeclaredMethod("nightmare", new Class[]{List.class});
		   t = m.getGenericReturnType();
		   if (!t.toString().equals("java.util.List<T>")) 
			   throw new RuntimeException("Incorrect signature4. Signature is "+t);
		   t = m.getGenericParameterTypes()[0];
		   if (!t.toString().equals("T")) 
			   throw new RuntimeException("Incorrect signature5. Signature is "+t);
		   

		   m = Goo.class.getDeclaredMethod("holyCow", new Class[]{Number.class});
		   t = m.getGenericReturnType();
		   if (!t.toString().equals("java.util.List<T>")) 
			   throw new RuntimeException("Incorrect signature4. Signature is "+t);
		   t = m.getGenericParameterTypes()[0];
		   if (!t.toString().equals("Q")) 
			   throw new RuntimeException("Incorrect signature5. Signature is "+t);
		   

	   }
}
