import java.lang.Enum;

public aspect EnumAspect03 {
  interface I {}
  declare parents: SimpleEnum implements I;

  enum C {A,B,C};
  declare parents: SimpleEnum extends C;

  class D {}
  declare parents: D extends Enum;
}
