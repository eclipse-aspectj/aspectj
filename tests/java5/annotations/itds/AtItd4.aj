import java.lang.annotation.*;
import java.lang.reflect.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Ann {
  String id() default "hello";
  int anInt() default 5;
  long aLong() default 6;
  String[] strings() default {"c","d"};
  SimpleEnum enumval() default SimpleEnum.B;
}

enum SimpleEnum { A,B,C }

aspect X {
  @Ann(id="goodbye",anInt=4,enumval=SimpleEnum.A,strings={"a","b"}) public void AtItd4.m() {}
  //@Ann(enumval=SimpleEnum.A) public void AtItd4.m() {}
}

public class AtItd4 {
  public static void main(String []argv) {
    try {
      Method m = AtItd4.class.getDeclaredMethod("m",null);
      System.err.println("Method is "+m);
      Annotation[] as = m.getDeclaredAnnotations();
      System.err.println("Number of annotations "+
        (as==null?"0":new Integer(as.length).toString()));
      Ann aa = m.getAnnotation(Ann.class);
      System.err.println("Ann.class retrieved is: "+aa);

      String exp = 
//        "@Ann(strings=[a, b], enumval=A, aLong=6, id=goodbye, anInt=4)";
        "@Ann(id=goodbye, anInt=4, aLong=6, strings=[a,b], enumval=A)";

      if(!matches(aa)) {
        throw new RuntimeException("Incorrect output, expected:"+exp+
          " but got "+aa.toString());
      }
 
      if (as.length==0) 
        throw new RuntimeException("Couldn't find annotation on member!");
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
  private static boolean matches(Ann anotayshun){
	  if(!anotayshun.id().equals("goodbye")){
		  return false;
	  }
	  if(anotayshun.anInt() != 4){
		  return false;
	  }
	  if(anotayshun.aLong() != 6){
		  return false;
	  }
	  if(anotayshun.strings().length != 2){
		  return false;
	  }
	  if(!anotayshun.strings()[0].equals("a") || !anotayshun.strings()[1].equals("b")) {
		  return false;
	  }
	  if(anotayshun.enumval() != SimpleEnum.A){
		  return false;
	  }
	  //they all match
	  return true;
  }
  
  
}
