interface II {}


aspect X {
  public <XXX extends I1, YYY extends I2> YYY II.foo(XXX r, Class<YYY> ct) {
return null;
  }
}

interface I1 {}
interface I2 {}
