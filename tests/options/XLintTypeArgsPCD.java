
class E {}
class F {
    public static void main(String[] args) {}
}

public aspect XLintTypeArgsPCD {
    public static int COUNT;

    before () : args(UnknownType) {     // CM -XLint unfound type
        COUNT++;
    }
}

