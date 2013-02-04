class Animal2<T> { }

class Bar2 {}

public class Cage2<T extends Animal2<?>> extends Bar2 { }

aspect X {
  declare parents: Cage2 implements java.io.Serializable;
}
