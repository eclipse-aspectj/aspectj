import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface WebService {}

//@WebService 
class Foo {

        public Foo() {}

}
aspect X {

        after(): execution(*.new(..)) && @within(WebService) {

        }
}


