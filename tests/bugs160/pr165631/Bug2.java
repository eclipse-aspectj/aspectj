interface A<T> {}

interface B<T> extends A<T> {}

class C implements A<String> {}

interface A1 {}

class D extends C implements A1 {
}

aspect X {
        declare parents: D implements B<Number>;
}

