interface Base<T> {
    static interface Inner {
    }
}

class Test<T extends Test.Inner> implements Base<T> {
}
