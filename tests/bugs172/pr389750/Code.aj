import java.io.*;

interface Persistable<ID extends Serializable> {

}

public aspect Code {

  public interface I<ID extends Serializable> extends Persistable<ID> {
  }

  public static void foo() {}

  public boolean I.equals(Persistable<?> that) {
    return false;
  }
}
