import java.lang.reflect.*;

interface I {
}


class C implements I {
}

public aspect Case5 {

  public String I.str = "hello";

  public static void main(String []argv) {
	  Field[] fs = C.class.getDeclaredFields();
	  for (Field f: fs) {
		  System.out.println(f);
	  }
  }
  
}
