//interface IGuard<P> {}
//
////interface Guard<P> extends IGuard<P> {}
//
//class GuardImpl<G> implements IGuard<G> {}
//
//public class C<T> {
//
//  private boolean m1(Class<? extends IGuard<T>> guardClz) throws Exception { return false;}
//  private boolean m2(Class<? extends IGuard<T>>[] guardClz) throws Exception { return false;}
//
//  public static void main(String []argv) throws Exception {
//    GuardImpl<String> g = new GuardImpl<String>();
//    C<String> newC = new C<String>();
//    newC.m1(g.getClass());
////    newC.m2(new Class[]{g.getClass()});
//  }
//
//}

interface IGuard<P> {}

interface Guard<P> extends IGuard<P> {}

class GuardImpl<X> implements Guard<X> {}

public class C<T> {
  
  private boolean checkGuards(Class<? extends IGuard<T>>[] guardClz) throws Exception { return false;}
  
  public static void main(String []argv) throws Exception {
    GuardImpl<String> g = new GuardImpl<String>();
    //new C<String>().checkGuards(g.getClass());//Guard.class);
    new C<String>().checkGuards(new Class[]{g.getClass()});//Guard.class);
  }
}
