import java.util.*;

interface ItemWithIndexVisitor {
  public void visit(Object item, int index);
}

public class Lambda1 {
  public static void main(String[] argv) {
    List list = Arrays.asList("A","B","C");
   // eachWithIndex(list, Lambda1::printItem);
    eachWithIndex(list, 
      (value, index) -> {
        String output = String.format("%d -> %s", index, value);
        System.out.println(output);
      }
    );
  }
  public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor visitor) {
    for (int i=0;i<list.size();i++) {
      visitor.visit(list.get(i),i);
    }
  }

/*
  public static <E> void printItem(E value, int index) {
    String output = String.format("%d -> %s", index, value);
    System.out.println(output);
  }
*/
}
