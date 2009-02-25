aspect A {

  public static final boolean enabled = true;

  before(): execution(* AlwaysWeave.*(..)) && if(enabled) {}

}

public class AlwaysWeave {
  public void foo() {}
}
