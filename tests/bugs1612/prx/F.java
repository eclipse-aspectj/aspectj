import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
Class i() default Number.class;
}

public class F {

  @Anno(i=Integer.class)
  public int i;

  @Anno
  public int j;

  @Anno(i=String.class)
  public int k;

  public static void main(String []argv) {
    System.out.println(new F().i);
    System.out.println(new F().j);
    System.out.println(new F().k);
  }
}
aspect X {
  before(): get(@Anno(i!=String.class) * *) {}
}
