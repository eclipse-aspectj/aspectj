import java.util.*;

public aspect CodeTwo {
  before(): execution(* CodeTwo.*(..)) && args(List<? extends Number>) {}

  void m(List<Integer> li) {}

  public void callm() {
    List<? extends Number> lqn = new ArrayList<Number>();
    // m(lqn);
  }

}
