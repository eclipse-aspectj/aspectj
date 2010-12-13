public class A<T> {
  public void a(Class<? extends M> list) {}
  public void x() {
    a(null);
  }
}
