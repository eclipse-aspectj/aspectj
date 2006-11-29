interface A<T> {}

interface B<T> extends A<T> {}

class C implements A<String> {
}

class D extends C {
}

aspect X {
  declare parents: D implements B<Number>;
}
