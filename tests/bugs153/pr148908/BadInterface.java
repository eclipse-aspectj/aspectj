import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


public interface BadInterface {

    static final Comparator MY_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            return 0;
        }
    };
    
    public List<String> aList = new LinkedList<String>() {{ add("Busted"); }};
    
    public List<String> bList = new LinkedList<String>() {
       public int size() {
          for(int i = 0; i < 100; i++) {
             return 0;
          }
          return modCount;
       }
    };
}
