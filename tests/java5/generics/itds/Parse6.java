import java.util.*;

class Base<N> {

  public List<N> f1;

  public void m1(List<N> ns) {}

}

aspect X {

  public Base<Z>.new(Z  aNumber) {
	 this() ;
  }
	
  public List<Z> Base<Z>.f2;

  public void Base<Z>.m2(List<Z> ns) {}

}

