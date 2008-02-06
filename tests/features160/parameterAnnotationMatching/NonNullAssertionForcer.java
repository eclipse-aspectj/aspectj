import java.lang.annotation.*;

public aspect NonNullAssertionForcer {

	class C {
		public void foo(@NonNull String s) {}
	}
	
	before(): execution(* *(@NonNull (*))) {
		System.out.println("Bar");
	}
	
	@Target(ElementType.PARAMETER)
	@interface NonNull {
		
	}
}
