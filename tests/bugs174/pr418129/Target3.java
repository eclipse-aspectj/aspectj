import java.lang.annotation.*;

interface Behavior { 
	String hello(); 
}

aspect Trait {
    @Wibble
	public String Behavior.hello() throws java.io.IOException {
		return "hello";
	}
}

public class Target3 implements Behavior {
	public static aspect A {
		declare @method: * Target3.hello(..): @Tagged;
	}

    public static void main(String []argv) throws Exception {
      System.out.println(Target3.class.getDeclaredMethod("hello").getDeclaredAnnotations().length);
      System.out.println(Target3.class.getDeclaredMethod("hello").getDeclaredAnnotations()[0]);
      System.out.println(Target3.class.getDeclaredMethod("hello").getDeclaredAnnotations()[1]);
    }
}

@Retention(RetentionPolicy.RUNTIME) @interface Tagged {}
@Retention(RetentionPolicy.RUNTIME) @interface Wibble {}
