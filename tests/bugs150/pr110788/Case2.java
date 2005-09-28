interface A<T> {}

class C implements A<String> {}

class D extends C {}

aspect X {
  declare parents: D implements A<Number>; // Can't do it, C implements A<String>
}
