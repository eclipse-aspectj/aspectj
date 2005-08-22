// ITDs on generic types
public class Parse5<T,S extends Number> {}

aspect X {
  void Parse5.m1() {}

  void Parse5<Q,R>.m2() {}

  void Parse5<T,V>.m3() {}
 
  void Parse5<A,B,C>.m4() {} // error

  void Parse5<A>.m5() {} // error
  
  void Parse5<String,Integer>.m6() {} // error
}
