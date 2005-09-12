class Foo {}
aspect Injector {
  Foo Foo.field;
  public Foo Foo.foo() { System.out.println("hello"); return field; }
}

public class pr105181 {
  static void sink(Foo foo) {}
  public static void main(String[] args) throws Exception {
    java.util.Vector<Foo> source = new java.util.Vector<Foo>();
    source.add(new Foo());

    /**
     * This next line causes a verify error when we try to access the ITD'd field
     */
    Foo f = source.get(0).field; 
    
    /**
     * According to the bug report, this line should to - but I couldn't get a 
     * method to fail...
     */
    Foo f2 = source.get(0).foo();
  }
  
  public void worksOK() {
	  java.util.Vector<Bar> source = new java.util.Vector<Bar>();
	  source.add(new Bar());
	  Bar b = source.get(0).field;
  }
}

class Bar {
	Bar field;
}