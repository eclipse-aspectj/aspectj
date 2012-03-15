import java.lang.reflect.*;

interface I {
}


class C implements I {
}

public aspect Case1 {

  public int I.i;

  public static void main(String []argv) throws Exception {
    Field f =  C.class.getField("i");
    if (f==null) System.out.println("Couldn't find a field called i");
    else         System.out.println("Found a field called i");
  }
  
}
