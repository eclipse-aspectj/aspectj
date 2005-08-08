public class Simple {
  public static void main(String []argv) {
    Base<Integer> base = new Base<Integer>();
    List<Integer> intList = new ArrayList<Integer>();
    intList.add(5);
    intList.add(2);
    intList.add(3);
    intList.add(8);
    System.err.println(">"+base.m(intList));
    System.err.println(">"+base.m2(intList));
  }
}



class Base<N extends Number> {

   public int m(List<N> ns) {
     return ns.size();
   }

}

aspect X {

  public int Base<Z>.m2(List<Z> zs) {
     return zs.size();
  }
}

