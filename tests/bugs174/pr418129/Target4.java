import java.lang.annotation.*;

interface Behavior { 
	String hello(); 
}

aspect Trait {
    @Tagged(31)
	public String Behavior.hello() throws java.io.IOException {
		return "hello";
	}
}

public class Target4 implements Behavior {
	public static aspect A {
		declare @method: * Target4.hello(..): @Tagged;
	}

    public static void main(String []argv) throws Exception {
      System.out.println(Target4.class.getDeclaredMethod("hello").getDeclaredAnnotations().length);
      System.out.println(Target4.class.getDeclaredMethod("hello").getDeclaredAnnotations()[0]);
    }
}

@Retention(RetentionPolicy.RUNTIME) @interface Tagged { int value() default 42;}
