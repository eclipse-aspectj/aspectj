package a.b;

public class Adapter<T> implements a.Adapter<T> {

  public <V> Adapter.Setter<V> makeSetter() {
    return new Adapter.Setter<V>() {};
  }

}
