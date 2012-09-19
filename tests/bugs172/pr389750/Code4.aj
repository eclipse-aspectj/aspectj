import java.io.*;

interface Persistable<ID extends Serializable> {

}

public aspect Code4 {

  public interface I<ID extends Serializable> extends Persistable<ID> {
  }

  public static void foo() {}

  public <T> Z I<Z>.bar(Z foo, T that) {
    return foo;
  }
}
