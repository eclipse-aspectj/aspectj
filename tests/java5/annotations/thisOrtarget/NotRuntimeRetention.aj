//"must have runtime retention"

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface MyRuntimeAnnotation {}

@Retention(RetentionPolicy.SOURCE)
@interface MySourceAnnotation {}

@Retention(RetentionPolicy.CLASS)
@interface MyClassAnnotation {}

@interface MyAnnotation {}

aspect X {
 @MyRuntimeAnnotation @MySourceAnnotation @MyClassAnnotation @MyAnnotation
 void a(){}
 before(MyRuntimeAnnotation a): execution(* *(..)) && @annotation(a) {} // no error
 before(MySourceAnnotation a): execution(* *(..)) && @annotation(a) {} // error expected
 before(MyClassAnnotation a): execution(* *(..)) && @annotation(a) {} // error expected
 before(MyAnnotation a): execution(* *(..)) && @annotation(a) {} // error expected
}
