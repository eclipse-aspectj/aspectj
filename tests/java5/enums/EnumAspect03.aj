public aspect EnumAspect03 {
  declare parents: SimpleEnum implements java.io.Serializable;

  class C extends Enum {
  }
  declare parents: SimpleEnum extends C;
}
