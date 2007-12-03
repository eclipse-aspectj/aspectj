package c.d;

import java.lang.annotation.*;

public class DistantResource {
  public static void main(String []argv) {
    Annotation [] annos = DistantResource.class.getAnnotations();
    for (int i=0;annos!=null && i<annos.length;i++) {
      System.out.println("Annotation is "+annos[i]);
    }
  }
}
