import java.util.*;

class Base { }

public class ParameterizedMethodITD3 {

  public static void main(String[] argv) {
    List<B> bs = new ArrayList<B>();
    new Base().simple(bs); // error: B is not a super type of A
  }
}

class A {}

class B extends A {}


aspect X {
  void Base.simple(List<? super A> list) {}
}
