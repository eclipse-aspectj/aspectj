interface Base<T> {
    static interface Inner {
    }
}
class Test<T extends Test.InnerTest> implements Base<T> {
    static class InnerTest implements Inner {
    }
}
