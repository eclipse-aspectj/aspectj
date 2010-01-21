import java.lang.annotation.*;

public class OnePrivateAccessors {
  public static void main(String[] argv) {
    int i = new OnePrivateAccessors().run();
    if (i!=37) throw new RuntimeException(Integer.toString(i));
  }
}

aspect X {
  @Anno
  private int OnePrivateAccessors.x;

  public int OnePrivateAccessors.run() {
    x = 37;
    return x;
  }
}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}
