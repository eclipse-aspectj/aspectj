enum Color {R,G,B;}
@interface I { public Color a(); }
@interface J { public Color b() default Color.B; }

public class A {
  @J
  @I(a=Color.R)
  public static void main(String []argv) {
  }
}

aspect X {

  before(Color var): execution(* main(..)) && @annotation(I(var)) {
    if (var!=Color.R) {
      throw new RuntimeException("Wrong! Was "+var);
    }
  }
}
