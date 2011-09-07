import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
  Class value();
}

public class G {

  @Anno(String.class)
  public int i;

  @Anno(Integer.class)
  public int j;

  public static void main(String []argv) {
    System.out.println(new G().i);
    System.out.println(new G().j);
  }
}
aspect X {
  before(): get(@Anno(value=Foo.class) * *) {}
}
