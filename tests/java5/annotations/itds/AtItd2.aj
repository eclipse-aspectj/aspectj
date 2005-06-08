import java.lang.annotation.*;
import java.lang.reflect.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Ann {}

aspect X {
  @Ann public void AtItd2.m() {}
}

public class AtItd2 {
  public static void main(String []argv) {
    try {
      Method m = AtItd2.class.getDeclaredMethod("m",null);
      System.err.println("Method is "+m);
      Annotation[] as = m.getDeclaredAnnotations();
      System.err.println("Number of annotations "+
        (as==null?"0":new Integer(as.length).toString()));
      Annotation aa = m.getAnnotation(Ann.class);
      System.err.println("Ann.class retrieved is: "+aa);
      if (as.length==0) 
        throw new RuntimeException("Couldn't find annotation on member!");
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
}
