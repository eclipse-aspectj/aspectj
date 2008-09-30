aspect Asp {
  before(): execution(new(Integer,..)) {}
}

class Outer {
  public class Inner {
    Inner(Integer arg) {}
  }
}
