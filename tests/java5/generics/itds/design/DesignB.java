class C {}

interface I {}

aspect X {

  <T extends Number> void C.m0(T t) {}   // L7

  <Q extends I> void C.m1(Q q) {}        // L9

  <R extends Number,I> void C.m2(R r) {} // L11
}
