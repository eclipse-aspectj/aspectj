import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}

aspect Foo {
  declare @field: @Anno * OnOff2.*: -@Anno;
  declare @field: * OnOff2.*: @Anno;
}

public class OnOff2 {

  public static int field;

  public int field2;

  public static void main(String[]argv) throws Exception {
    Object o = OnOff2.class.getDeclaredField("field").getAnnotation(Anno.class);
    System.out.println("field annotated? "+(o==null?"no":"yes"));

    o = OnOff2.class.getDeclaredField("field2").getAnnotation(Anno.class);
    System.out.println("field2 annotated? "+(o==null?"no":"yes"));
  }
}

