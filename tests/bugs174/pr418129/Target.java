import java.lang.annotation.*;

interface Behavior { 
String hello(); 
}

aspect Trait {
 //   public String Behavior.name;
	
	public String Behavior.hello() {
		return "hello";
	}
}

public class Target implements Behavior {
	public static aspect A {
//		declare @field: * Target.name: @Tagged; // NO WORKY
		declare @method: * Target.hello(..): @Tagged; // NO WORKY
	}

    public static void main(String []argv) throws Exception {
      System.out.println(Target.class.getDeclaredMethod("hello").getDeclaredAnnotations()[0]);
    }
}

@Retention(RetentionPolicy.RUNTIME)
@interface Tagged {}
