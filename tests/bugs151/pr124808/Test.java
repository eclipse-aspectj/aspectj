import java.util.*;

public class Test<T extends Integer> {
       Set<T> ints = new HashSet<T>();

       public Set<T> foo() {
               return ints;
       }


       public static void main(String[] args) {
               Test<Integer> t2 = new Test<Integer>();
               Set<Integer> ints2 = t2.foo();
       }
}
