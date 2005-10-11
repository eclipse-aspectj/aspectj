class C {}

interface I {}

aspect X {

  <T extends Number,Q extends I> void C.m0(T t,Q q) {}   // L7

  <A,B,C>  List<A> C.m1(B b,Collection<C> cs) {} // L9
}
