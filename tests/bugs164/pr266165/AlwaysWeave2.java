aspect A {


  before(): execution(* AlwaysWeave2.*(..)) && if(true) {}

}

public class AlwaysWeave2 {
  public void foo() {}
}
