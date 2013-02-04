class Animal2<T,Q> { }

class Bar2 {}

interface XXX2<T> {}

interface YYY2<T> {}

public class Cage2<T extends Animal2<? super XXX2<T>,YYY2>> extends Bar2 { }

aspect X {
  declare parents: Cage2 implements java.io.Serializable;
}
