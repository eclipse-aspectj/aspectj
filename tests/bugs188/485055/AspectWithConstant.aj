import java.lang.annotation.*;

public aspect AspectWithConstant {
    declare @field : * AspectWithConstant.MAX* : @Loggable;
public static final String MAXS = "hello";
    @Retention(RetentionPolicy.RUNTIME)
    @interface Loggable { }

  public static void main(String []argv) throws Exception {
    System.out.println("MAXS="+MAXS);
System.out.println(AspectWithConstant.class.getDeclaredField("MAXS").getAnnotation(Loggable.class));
  }
}
