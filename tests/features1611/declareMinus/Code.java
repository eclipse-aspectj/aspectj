import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}

aspect Foo {
  declare @field: * Code.someField: -@Anno;
}

public class Code {

  @Anno
  public static int someField;

  public static void main(String[]argv) throws Exception {
    Object o = Code.class.getDeclaredField("someField").getAnnotation(Anno.class);
    System.out.println(o==null?"no annotation":"has annotation");
  }
}

