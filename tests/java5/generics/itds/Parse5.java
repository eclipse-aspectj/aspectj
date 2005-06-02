// ITDs on generic types
public class Parse5<T,S extends Number> {}

aspect X {
  String Parse5.m1() {}

  String Parse5<Q,R extends Number>.m2() {}

  String Parse5<T,V>.m3() {} // error
 
  String Parse5<A,B extends Number,C>.m4() {} // error

  String Parse5<A>.m5() {} // error
  
  String Parse5<String,Integer>.m6() {} // error
}
