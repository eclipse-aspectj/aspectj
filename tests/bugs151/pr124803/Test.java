interface Generic1<T extends Number> {
  public void foo(T p);
}


interface Generic2<T extends Number, Y extends Number> extends Generic1<T> {
  public void foo2(Y p);
}

public class Test<Y extends Number> implements Generic2<Y,Y>{
  public void foo2(Y p) { }
  public void foo(Y p) { }

  public static void main(String []argv) {
    Test<Integer> t = new Test<Integer>();
    t.foo(7);
    t.foo2(9);
  }
}
