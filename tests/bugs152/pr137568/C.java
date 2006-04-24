interface IGuard<P> {}

interface Guard<P> extends IGuard<P> {}

class GuardImpl implements Guard<String> {}

public class C<T> {

  private boolean m1(Class<? extends IGuard<T>> guardClz) throws Exception { return false;}
  private boolean m2(Class<? extends IGuard<T>>[] guardClz) throws Exception { return false;}

  public static void main(String []argv) throws Exception {
    GuardImpl g = new GuardImpl();
    C<String> newC = new C<String>();
    newC.m1(g.getClass());
    newC.m2(new Class[]{g.getClass()});
  }

}