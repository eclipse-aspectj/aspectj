public class SimpleVarargs {

  public SimpleVarargs(Integer... strings) {}

  public void foo(Integer... strings) { 
  	moo();
  }

//public void bar(Integer[] array) { }

  public void fooInt(int i,Integer... strings)  {
  	moo();
  }

  private void moo() {}
//public void barInt(int i,Integer[] strings) {}

  public static void main(String[] argv) {
    SimpleVarargs s = new SimpleVarargs(new Integer(45));
    s.foo(new Integer(45));
    s.foo(new Integer(45),new Integer(45));
    s.foo(new Integer[]{new Integer(45),new Integer(45)});
//  s.bar(new Integer[]{new Integer(45),new Integer(45)});

    s.fooInt(1,new Integer(45));
    s.fooInt(2,new Integer(45),new Integer(45));
    s.fooInt(3,new Integer[]{new Integer(45),new Integer(45)});
//  s.barInt(4,new Integer[]{new Integer(45),new Integer(45)});

  }
}
