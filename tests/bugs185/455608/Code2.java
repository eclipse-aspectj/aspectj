import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Bar(String)
@Foo("abc")
public class Code2 {

}

@Retention(RetentionPolicy.RUNTIME)
@interface Foo { String value();}

@Retention(RetentionPolicy.RUNTIME)
@interface Bar {
	Class<?>[] value();
}
