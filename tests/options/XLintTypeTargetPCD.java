
class C {}
class D {}
class E {}
class F {}

public aspect XLintTypeTargetPCD {
    public static int COUNT;

    before () : target(UnknownType) {     // CM -XLint unfound type
        COUNT++;
    }
}

