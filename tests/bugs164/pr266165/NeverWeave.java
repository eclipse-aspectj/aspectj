aspect A {

  public static final boolean enabled = false;

  before(): execution(* NeverWeave.*(..)) && if(enabled) {}

}

public class NeverWeave {
  public void foo() {}
}
