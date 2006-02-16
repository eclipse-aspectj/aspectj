import java.util.*;

class TreasureChest<T> {

  protected Set<T> contents;

  public TreasureChest() {
    contents = new HashSet<T>();
  }

  public void add(T o) {
    contents.add(o);
  }
}

public class SimpleGenericsProgram {

  public static void main(String []argv) {
    TreasureChest<String> tc1 = new TreasureChest<String>();
    TreasureChest<Integer> tc2 = new TreasureChest<Integer>();

    tc1.add("dubloon");
    tc2.add(new Integer("777"));

  }
}
