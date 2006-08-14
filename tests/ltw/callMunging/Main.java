import java.lang.reflect.Method;

public class Main {

  public static void main(String []argv) {
	  try {
		  System.out.println("into:main");
		  Class clazzA = Class.forName("A");
		  Method clazzAMethod = clazzA.getMethod("method",null);
		  clazzAMethod.invoke(clazzA.newInstance(),null);
		  
		  Class clazzB= Class.forName("B");
		  Method clazzBMethod = clazzB.getMethod("method",null);
		  clazzBMethod.invoke(clazzB.newInstance(),null);
		  System.out.println("leave:main");
	  } catch (Throwable t) {
		  t.printStackTrace();
	  }
  }
  
}
