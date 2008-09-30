aspect Asp {
  before(): execution(new(String)) {}
}

class Outer {
  public class Inner {
    Inner(String arg) {}
  }
}
