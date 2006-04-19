// Bug reported that we incorrectly consider static methods when looking at
// binary weaving decp rules - we shouldn't consider them overriding

import java.util.*;

public class SubClassLoader  {
  private static List l;

  class Inner {
   public void foo() {
     System.err.println(l.toString());
   }
  }


/*
  // this one would override java.lang.ClassLoader.access$000 on an IBM VM
  public static List access$000(ClassLoader cl,String s,byte[] bs,int i,int j,Object o) {
    return null;
  }

  // this one would override java.lang.ClassLoader.access$000 on an SUN VM:w
  public static List access$000() {
    return null;
  }
*/
}
