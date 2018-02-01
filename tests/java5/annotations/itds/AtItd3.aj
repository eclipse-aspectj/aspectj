import java.lang.annotation.*;
import java.lang.reflect.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Ann {
  String id() default "hello";
  int anInt() default 5;
}

aspect X {
  @Ann(id="goodbye",anInt=4) public void AtItd3.m() {}
}

public class AtItd3 {
  public static void main(String []argv) {
    try {
      Method m = AtItd3.class.getDeclaredMethod("m",null);
      System.err.println("Method is "+m);
      Annotation[] as = m.getDeclaredAnnotations();
      System.err.println("Number of annotations "+
        (as==null?"0":new Integer(as.length).toString()));
      Annotation aa = m.getAnnotation(Ann.class);
      System.err.println("Ann.class retrieved is: "+aa);

      if (!aa.toString().equals("@Ann(id=goodbye, anInt=4)")) // < Java8 order
          if (!aa.toString().equals("@Ann(anInt=4, id=goodbye)")) // Java8 order
        	    if (!aa.toString().equals("@Ann(anInt=4, id=\"goodbye\")")) // Java9 quotes strings
        throw new RuntimeException("Incorrect output, expected:"+
          "@Ann(id=goodbye, anInt=4) but got "+aa.toString());
 
      if (as.length==0) 
        throw new RuntimeException("Couldn't find annotation on member!");
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
}
