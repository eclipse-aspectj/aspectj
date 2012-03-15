import java.lang.reflect.*;

interface I {
}


class C implements I {
  public int i = 1;
}

public aspect Case2 {

  public int I.i = 5;

  public static void main(String []argv) {
    System.out.println("Value of C.i is "+new C().i);
  }
  
}
