package a;

public interface Adapter<T> {
  interface Setter<V> {}

  public <V> Setter<V> makeSetter();
}
