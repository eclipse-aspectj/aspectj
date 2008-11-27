import java.lang.reflect.*;
import java.lang.annotation.*;

public class SimpleTest {
  public static void main(String[] argv) throws Exception {
    Class<Destination> clazz = Destination.class;
    Method m = clazz.getMethod("helloWorld", String.class);
    Annotation[] ann = m.getAnnotations();
//    System.out.println(m + " has " + ann.length + " annotations");

    for (int i = 0; i < ann.length; i++) {
//      System.out.println("Method annotation: " + ann[i].getClass() + ann[i].toString());
    }

    for (int i = 0; i < m.getParameterAnnotations().length; i++) {
      int count = m.getParameterAnnotations()[i].length;
      System.out.println("Parameter " + i + " has " + count + " parameter annotations");
    }

  }
}

