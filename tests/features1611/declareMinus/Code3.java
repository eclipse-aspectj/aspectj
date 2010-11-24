import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnoB {}

aspect Foo {
  declare @field: * Code3.someField: @AnnoB;
  declare @field: * Code3.someField: -@Anno;
}

public class Code3 {

  @Anno
  public static int someField;

  public static void main(String[]argv) throws Exception {
    Object o = Code3.class.getDeclaredField("someField").getAnnotation(Anno.class);
    System.out.println(o==null?"no Anno":"has Anno");
    o = Code3.class.getDeclaredField("someField").getAnnotation(AnnoB.class);
    System.out.println(o==null?"no AnnoB":"has AnnoB");
  }
}

