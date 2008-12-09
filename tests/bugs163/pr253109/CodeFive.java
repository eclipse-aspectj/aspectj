import java.util.*;

public aspect CodeFive {

  void around(): execution(* m1(..)) && args(ArrayList) {}
  
}

class C {
	
	public void m1(List<Integer> li) {}
}
