import java.util.*;

public aspect CodeThree {
  before(): execution(* CodeThree.*(..)) && args(List<Integer>) {} // yes
  before(): execution(* CodeThree.*(..)) && args(ArrayList<Integer>) {} // yes - runtime check
  before(): execution(* CodeThree.*(..)) && args(List<Number>) {} // no
  before(): execution(* CodeThree.*(..)) && args(ArrayList<Number>) {} // no
  before(): execution(* CodeThree.*(..)) && args(List<? extends Number>) {} // yes
  before(): execution(* CodeThree.*(..)) && args(ArrayList<? extends Number>) {} // yes - runtime check

  void m(List<Integer> li) {}

}
