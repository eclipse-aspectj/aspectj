package bug;

import org.aspectj.lang.annotation.*;

@Aspect
public class StaticFinalStringInPointcutAspect {
        static final String pcExpression = "within(*)";

        @Pointcut(pcExpression)
        public void pointcutThatUsesStaticFinalStringAsExpression() {}
        
        @Before("pointcutThatUsesStaticFinalStringAsExpression() && execution(* foo(..))") 
        public void m() {
        	System.out.println("advice");
        }
        
        public void foo() {	
        }
        
        public static void main(String[] args) {
        	new StaticFinalStringInPointcutAspect().foo();
		}
}
