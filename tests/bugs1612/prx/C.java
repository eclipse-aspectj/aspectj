import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
  Class value();
//int i();
}

public class C {

  @Anno(String.class)
  //@Anno(i=3)
  public int i;

  public static void main(String []argv) {
    System.out.println(new C().i);
  }
}

aspect X {
  //before(): get(@Anno(String.class) * *(..)) {}
  before(): get(@Anno(value=String.class) * *) {}
  //before(): get(@Anno(i=3) * *) {}
}
