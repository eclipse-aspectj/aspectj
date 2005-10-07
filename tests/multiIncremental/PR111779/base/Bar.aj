import java.util.*;

public aspect Bar {
  private List<String> Foo.l;

  private void Foo.foo() {
    l = new ArrayList<String>();
  }
}
