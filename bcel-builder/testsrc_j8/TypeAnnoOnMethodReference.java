import java.util.function.Function;
import java.util.function.Supplier;

public class TypeAnnoOnMethodReference {
  public void m() {
    Function<String,Integer> f = @Anno(1) String::length;
    Supplier<Integer> s = "abc"::length;
  }
}
