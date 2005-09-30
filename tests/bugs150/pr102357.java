interface Generic<T> {
  void non_generic(String i);
  void generic(T t);
}

public class pr102357 implements Generic<String> {
  public static void main(String[] args) {
    new pr102357().non_generic("works");
    new pr102357().generic("works");

    Generic<String> generic = new pr102357();
    generic.non_generic("works");
    generic.generic("AbstractMethodError");
  }
}

aspect Injector {
  public void pr102357.non_generic(String s) {}
  public void pr102357.generic(String s) {}
}
