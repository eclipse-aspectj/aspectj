import java.lang.reflect.*;

interface I {
}


class C implements I {
  public int i = 1;
}

public aspect Case4 {

  public String I.i = "hello";

  public static void main(String []argv) {
  }
  
}
