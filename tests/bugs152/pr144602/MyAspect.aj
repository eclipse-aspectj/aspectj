import org.aspectj.lang.annotation.*;

@Aspect("perthis(call( * SomeClass.someMethod(..)))")
public class MyAspect {

    @Pointcut("call( * SomeClass.someMethod(..)) && args(j,k, *)")
    public void pointcut( int j, int k) {}

    @Before("pointcut( j,k)")
    public void advice( int j, int k ) {  }

  public static void main(String []argv) { new SomeClass().foo();}
} 

class SomeClass {
 public void someMethod(int a,int b,int c) {
 }

  public void foo() {
    someMethod(1,2,3);
    someMethod(2,3,4);
    someMethod(3,4,5);
  }

}

