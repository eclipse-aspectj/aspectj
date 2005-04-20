import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface Ann {}

@Ann
class C{}

public class Test3 {

  public static void main(String[] args) {
    C a = new C();
    abc(a);
  }

  static void abc(C y) {}
}


aspect Annotations {
  before(Ann ann) : call(* Test3.*(..)) && @args(ann) { }
}
