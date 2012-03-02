import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

@Aspect
public class AspectTest {

    public AspectTest() {}

    @DeclareParents(value="java.lang.Runnable+ || java.util.concurrent.Callable+",  defaultImpl=XImpl.class)
    //@DeclareParents(value="java.lang.Runnable+",  defaultImpl=XImpl.class)
    private X xImpl;

    public static void main(String []argv) {
      ((X)new Foo()).xxx();
      ((X)new Bar()).xxx();
    }

}
    interface X { void xxx();}
    class XImpl implements X {
      public XImpl() {}
      public void xxx() {}
    }


class Foo implements Runnable {
  public void run() {}
}

class Bar implements java.util.concurrent.Callable {
  public Object call() {return null;}
}
