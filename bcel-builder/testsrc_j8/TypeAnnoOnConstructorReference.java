import java.util.function.Function;
import java.util.function.Supplier;

public class TypeAnnoOnConstructorReference {
  public void m() {
    Supplier<Object> s = @Anno(1) Object::new;
    Function<Integer,int[]> f = @Anno(2) int[]::new;
  }
}
