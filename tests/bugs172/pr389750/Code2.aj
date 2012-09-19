import java.io.*;

interface Persistable<ID extends Serializable> {

}

public aspect Code2 {

  public interface I<ID extends Serializable> extends Persistable<ID> {
  }

  public static void foo() {}

  public Z I<Z>.bar(Z foo, Persistable<?> that) {
    return foo;
  }
}
