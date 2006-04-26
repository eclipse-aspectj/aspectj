import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

@Retention(RUNTIME)
@Inherited
@interface MyAnnotation {}

public aspect A perthis(annotatedClasses()) {
	
	pointcut annotatedClasses() : @this(MyAnnotation);
	
	before(): initialization(*.new(..)) {System.err.println(thisJoinPoint.getSignature().getDeclaringType()); }
	
	public static void main(String []argv) {
	  new Foo();
	  new Goo();
	  new Boo();
	  new Soo();
	}
}

// yes/no indicates if runtime match expected for staticinitialization

@MyAnnotation class Foo { } // YES

class Goo { }               // NO

@MyAnnotation class Boo { } // YES

class Soo extends Boo { }   // YES