import org.aspectj.lang.annotation.*;


@Aspect
public abstract class AbstractMethods {

  @Pointcut
  protected abstract void tracingScope();

  @Before("tracingScope()")
  public void doit() {
    test();
    System.out.println("advice running");
  }
  protected abstract void test();
}

/*
public abstract aspect AbstractMethods {

	protected abstract pointcut tracingScope ();
	
	before () : tracingScope () {
		test();
                System.out.println("advice running");
	}
	
	protected abstract void test ();
//	protected void test () {}
}
*/
