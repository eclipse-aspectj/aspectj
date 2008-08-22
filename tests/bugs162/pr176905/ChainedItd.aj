import java.lang.reflect.Field;
import java.lang.annotation.*;

public aspect ChainedItd {
    declare @field: long *.foo: @Deprecated;
    
    declare @field: @Deprecated * *.foo: @MyAnnotation;
    //uncomment the line below to prove our test should work
    //declare @field: long *.foo: @MyAnnotation;
    
    public static void main(String argz[]) throws Exception {
		Field idField = Test.class.getDeclaredField("foo");
		idField.setAccessible(true);
		assert idField.getAnnotation(MyAnnotation.class) != null;
    }    
}	 

class Test {
    private long foo;
}

@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnotation {
}
