package m;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect("pertarget(execution( public void m.M.run() ))")
public class A {
        int i;
        @Around("execution( public void m.M.run() )")
        public void count() {
//	        	System.out.println(this);
                System.out.println("tick " + (i++));
        }
}
