public class Simple2 {
  public static void main(String []argv) {
    Base<Integer> base = new Base<Integer>();
    List<Integer> intList = new ArrayList<Integer>();
    intList.add(5);
    base.f1 = intList;
    base.copy();
    System.err.println("f2.get(0)=>"+f2.get(0));
  }
}



class Base<N extends Number> {

  public List<N> f1;

}

aspect X {

  public List<Z> Base<Z>.f2;

  public void Base.copy() {
    f2=f1;
  }
}

