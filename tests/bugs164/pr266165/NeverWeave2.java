aspect A {

  before(): execution(* NeverWeave2.*(..)) && if(false) {}

}

public class NeverWeave2 {
  public void foo() {}
}
