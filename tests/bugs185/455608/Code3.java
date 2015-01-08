import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Bar(value=String,i=4)
//@Foo("abc")
public class Code3 {

}

@Retention(RetentionPolicy.RUNTIME)
@interface Foo { String value();}

@Retention(RetentionPolicy.RUNTIME)
@interface Bar {
	Class<?>[] value();
int i();
}
