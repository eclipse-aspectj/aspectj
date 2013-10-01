import java.lang.annotation.*;

interface Behavior { 
	String hello(); 
}

aspect Trait {
//	public String Behavior.name;
	
	public String Behavior.hello() throws java.io.IOException {
		return "hello";
	}
}

public class Target2 implements Behavior {
	public static aspect A {
//	declare @field: * Target2.name: @Tagged; // NO WORKY
		declare @method: * Target2.hello(..): @Tagged; // NO WORKY
	}

    public static void main(String []argv) throws Exception {
      System.out.println(Target2.class.getDeclaredMethod("hello").getDeclaredAnnotations()[0]);
    }
}

@Retention(RetentionPolicy.RUNTIME)
@interface Tagged {}
