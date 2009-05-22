import java.lang.reflect.Method;

public class Basic2 {
	
 public static void main(String[] args) {
	Method[] ms = X.class.getMethods();
	for (int i = 0; i < ms.length; i++) {
		if (ms[i].getName().indexOf("if$")!=-1) {
			System.out.println("if method: "+ms[i]);
		}
	}
}
  public void m() {}
}

aspect X {
  pointcut p(): execution(* m(..)) && if(true==true) && if(true==(true || true));
  pointcut q(): execution(* m(..)) && if(true==true);
  
}
