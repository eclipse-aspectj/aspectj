import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}

aspect Foo {
  // one way round
  declare @field: * OnOff.field: -@Anno;
  declare @field: * OnOff.field: @Anno;

  // the other way round
  declare @field: * OnOff.field2: @Anno;
  declare @field: * OnOff.field2: -@Anno;
}

public class OnOff {

  public static int field;

  public int field2;

  public static void main(String[]argv) throws Exception {
    Object o = OnOff.class.getDeclaredField("field").getAnnotation(Anno.class);
    System.out.println("field annotated? "+(o==null?"no":"yes"));

    o = OnOff.class.getDeclaredField("field2").getAnnotation(Anno.class);
    System.out.println("field2 annotated? "+(o==null?"no":"yes"));
  }
}

