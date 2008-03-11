aspect MA {
  Factory AnyClass.myfactory = Factory.create();
}

class Factory {
  public static Factory[] create() {
    return null;
  }
}

class AnyClass {
}

public class Foo {
  public static void main(String []argv) {
    new AnyClass();
  }
}