import java.util.*;

class MathUtils { } 

class Sorter    { 
  public static List l;
//  <T> Sorter(List<T> elements,Comparator<? super T> comparator) { 
//	 Collections.sort(elements,comparator);
//	 l = elements;
//  }
}

public class GenericCtorITD3 {
  public static void main(String[] argv) {
    List<Simple> ls = new ArrayList<Simple>();
    ls.add(new Simple(2));
    ls.add(new Simple(1));
    ls.add(new Simple(5));
    ls.add(new Simple(3));
    new Sorter(ls,new SimpleComparator());
    System.err.println(Sorter.l.get(0));
    System.err.println(Sorter.l.get(1));
    System.err.println(Sorter.l.get(2));
    System.err.println(Sorter.l.get(3));
  }

  static class Simple {
    int n;
    Simple(int i) { n=i;}
    public String toString() { return new String(""+n); }
  }

  static class SimpleComparator implements Comparator<Simple> {
    public int compare(Simple a, Simple b) { return a.n-b.n;}
  }
}


aspect X {
  <T> Sorter.new(List<T> elements,Comparator<? super T> comparator) { 
	this();
	Collections.sort(elements,comparator);
	l = elements;
  }
}
