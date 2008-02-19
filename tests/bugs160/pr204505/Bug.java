import java.util.*;

interface GenericIFace<B> {
  public void save(B bean);
  public void saveAll(Collection<B> beans);
}

class C<A> implements GenericIFace<A> {
  public void save(A bean) {}
  public void saveAll(Collection<A> bean) {}
}

aspect X {
  before(): execution(* GenericIFace.save*(..)) { }
//  before(): execution(* GenericIFace+.save*(..)) { }
}
