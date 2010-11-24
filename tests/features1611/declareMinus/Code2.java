import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}

aspect Foo {
  declare @field: * Code2.someField: -@Anno;

  @Anno
  public int Code2.someField;
}

public class Code2 {

  public static void main(String[]argv) throws Exception {
    Object o = Code2.class.getDeclaredField("someField").getAnnotation(Anno.class);
    System.out.println(o==null?"no annotation":"has annotation");
  }
}

