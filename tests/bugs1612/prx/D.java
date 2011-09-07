import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
int i();
}

public class D {

  @Anno(i=3)
  public int i;

  @Anno(i=4)
  public int j;

  @Anno(i=5)
  public int k;

  public static void main(String []argv) {
    System.out.println(new D().i);
    System.out.println(new D().j);
    System.out.println(new D().k);
  }
}
aspect X {
  before(): get(@Anno(i!=5) * *) {}
}
