interface Base<T> {
    static interface Inner {
    }
}

class Test<T extends Test.Inner> implements Base<T> { }
class Test2<T extends Base.Inner> implements Base<T> { }
