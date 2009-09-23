
import java.lang.reflect.Method;
import java.util.*;

public class LTWTest {

	public void noDoubles() throws Exception {
		Method[] methods = Bottom.class.getMethods();
		System.out.println("Bottom.getMethods()");
                boolean broken = false;
                List<String> l = new ArrayList<String>();
		for (Method method : methods) {
			if (!method.getDeclaringClass().equals(Object.class)) {
			  l.add(method.getName() + " : " + method.getDeclaringClass().getName());
                        }
			if (method.getDeclaringClass().equals(Bottom.class)) {
                          if (method.getName().equals("markMethod"))  {
                            broken=true;
                          }
			}
		}
		Collections.sort(l);
                for (String s: l) {
                  System.out.println(s);
                }
                if (broken) {
                  throw new IllegalStateException("Bottom.getMethods() should not include a markMethod() declared by Bottom");
                }
	}	
	
	public void grandChildInherits() throws Exception {
		Method[] methods = Bottom.class.getDeclaredMethods();
		for (Method method : methods) {
      if (method.getName().equals("markMethod")) 
throw new RuntimeException();
//			assertThat(method.getName(), not(equalTo("doSomething")));
		}
		
/*
		methods = Bottom.class.getMethods();
		for (Method method : methods) {
			if (method.getName().equals("doSomething")) {
				System.out.println(method.getDeclaringClass().getName());
			}
		}
*/
		
	}
	
	public static void main(String[] args) throws Exception {
		LTWTest t = new LTWTest();
		t.noDoubles();
		t.grandChildInherits();
	}
	
}
