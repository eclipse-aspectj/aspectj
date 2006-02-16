import java.util.*;

public class SimpleGenericsUsage {

  public static void main(String[]argv) {
    ArrayList<String> fruits = new ArrayList<String>();
    fruits.add("Oranges");
    fruits.add("Apples");
    fruits.add("Pears");
    System.err.println(fruits.size()+" fruits defined");
  }
}
