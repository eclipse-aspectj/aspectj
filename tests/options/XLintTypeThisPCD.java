
class C {}
class D {}
class E {}
class F {}

public aspect XLintTypeThisPCD {
    public static int COUNT;

    before () : this(UnknownType) {     // CM -XLint unfound type
        COUNT++;
    }
}

