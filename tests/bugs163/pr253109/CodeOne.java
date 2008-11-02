import java.util.*;

public aspect CodeOne {
  before(): execution(* CodeOne.*(..)) && args(List<Number>) {}
  before(): execution(* CodeOne.*(..)) && args(List<Integer>) {}

  void m(List<Integer> li) {}

  public void callm() {
    List<Number> ln = new ArrayList<Number>();
    List<Integer> li = new ArrayList<Integer>();
    // m(ln);//not allowed
    m(li);
  }

}
