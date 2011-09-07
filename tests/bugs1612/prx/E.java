import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
  Class value() default String.class;
}

public class E {

  @Anno
  public int i;

  @Anno(Integer.class)
  public int j;

  public static void main(String []argv) {
    System.out.println(new E().i);
    System.out.println(new E().j);
  }
}
aspect X {
  before(): get(@Anno(value=String.class) * *) {}
}
