class C<A,B> { public B getB(A a) { return null; } }

aspect X {
  public List<C> C<D,C>.getBs(D ds) { return null; }
}

public class ParameterizedMethodITD5 {

  public static void main(String[]argv) { 
    C instance = new C<Integer,String>();

    Integer i = instance.getB("hello");

    List<String> ls = instance.getBs(3);
    
  }

}
