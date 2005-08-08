import java.util.*;

class Victim {}

public class ReusingLetters {
  public static void main(String []argv) {
    Victim v = new Victim();
    
    List<A> as = new ArrayList<A>();
    v.b(as);
    v.c(as);


  }
 
}

class A implements Comparable<A> { 
  public int compareTo(A a) { return 0; }
}

aspect X { 

  public <T extends Object & Comparable<? super T>> 
    void Victim.b(List<T> l) {}

  public <P extends Object & Comparable<? super P>> 
    void Victim.c(List<P> col) {};
  
}
