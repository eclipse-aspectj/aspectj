import org.aspectj.lang.annotation.*;

public @Aspect class Bug {
	@Pointcut("args(i) && if() && within(Foo)")
	public static boolean pc(int i) {
		return i < 0;
	}
	
	@Before("pc(*)")
	public void advice() { System.out.println("advice running");}

  public static void main(String []argv) {
    new Foo().trigger(-1);
    new Foo().trigger(+1);
  }
}

class Foo {
  public void trigger(int i) {}
}

