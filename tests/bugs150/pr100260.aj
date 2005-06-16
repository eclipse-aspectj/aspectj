class Generic_Parent<T> {}

class Child extends Generic_Parent<Integer> {}

class Generic_Child<T> extends Generic_Parent<Integer> {}

public aspect pr100260 {
  public void Generic_Parent.inherited_method() {}
  public int Generic_Parent.inherited_field;

  public static void test() {
    int inherited_field;
    inherited_field = new Generic_Child().inherited_field; // works
    inherited_field = new Generic_Child<Integer>().inherited_field; // works
    inherited_field = new Child().inherited_field; // works

    new Generic_Child().inherited_method(); // works
    new Generic_Child<Integer>().inherited_method(); // unresolved
    new Child().inherited_method(); // unresolved
  }

  public static void main(String []argv) {
    test();
  }
}
