import java.lang.reflect.*;

interface I {
}


class C implements I {
}

public aspect Case3 {

  // one order
  public int C.i = 1;
  public int I.i = 5;

  // the other order ;)
  public int I.j = 5;
  public int C.j = 1;
  
  public int I.k = 1;
  public int C.k = 5;

  public static void main(String []argv) {
    System.out.println("Value of C.i is "+new C().i);
    System.out.println("Value of C.j is "+new C().j);
    System.out.println("Value of C.k is "+new C().k);
    System.out.println("Value of I.i is "+((I)new C()).i);
    System.out.println("Value of I.j is "+((I)new C()).j);
  }
  
}
