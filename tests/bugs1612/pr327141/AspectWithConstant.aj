import java.lang.annotation.*;

public aspect AspectWithConstant {
    declare @field : * AspectWithConstant.MAX* : @Loggable;
    public static final int MAX = 9;
    public static final float MAXf = 9.0f;
    public static final double MAXd = 9.0d;
    public static final long MAXl = 9L;
    public static final Class MAXc = String.class;
    @Retention(RetentionPolicy.RUNTIME)
    @interface Loggable { }

  public static void main(String []argv) throws Exception {
    System.out.println("MAX="+MAX);
System.out.println(AspectWithConstant.class.getDeclaredField("MAX").getAnnotation(Loggable.class));
  }
}
