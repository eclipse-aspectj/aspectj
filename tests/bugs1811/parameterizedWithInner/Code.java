public class Code {
  public static void main(String []argv) {
  }
}

class Outer<T> {
  class Inner {
    T t;
    Inner(T t) {
      this.t =t ;
    }
  }

  public Inner m() {return null;}
  public Outer<String>.Inner m2() {
    Outer<String> os = new Outer<String>();
    return os.new Inner("foo");
  }
  public Outer<?>.Inner m3() {return null;}
}
