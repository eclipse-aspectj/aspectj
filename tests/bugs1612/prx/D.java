import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
int i();
}

public class C {

  @Anno(i=3)
  public int i;

  public static void main(String []argv) {
    System.out.println(new C().i);
  }
}

aspect X {
  before(): get(@Anno(i!=3) * *) {}
}
