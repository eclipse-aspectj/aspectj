import java.util.*;

aspect X {

    private Set PR91053.aSet = new HashSet();

    public void PR91053.add(String s) {
        aSet.add(s);
    }

}

public class PR91053 { 

  public static void main(String[]argv) {
    new PR91053().add("hello");
  }
}
