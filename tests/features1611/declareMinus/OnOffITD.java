import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}

aspect Foo {
  // one way round
  declare @field: * OnOffITD.field: -@Anno;
  declare @field: * OnOffITD.field: @Anno;

  // the other way round
  declare @field: * OnOffITD.field2: @Anno;
  declare @field: * OnOffITD.field2: -@Anno;
}

aspect B {
  public static int OnOffITD.field;
  public int OnOffITD.field2;
}

public class OnOffITD {


  public static void main(String[]argv) throws Exception {
    Object o = OnOffITD.class.getDeclaredField("field").getAnnotation(Anno.class);
    System.out.println("field annotated? "+(o==null?"no":"yes"));

    o = OnOffITD.class.getDeclaredField("field2").getAnnotation(Anno.class);
    System.out.println("field2 annotated? "+(o==null?"no":"yes"));
  }
}

