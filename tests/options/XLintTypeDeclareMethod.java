

class C {}
class D {}
class E {}
class F {}

public aspect XLintTypeDeclareMethod {

    int UnknownType.getValue() { return 0; } // CM -XLint unfound type
}

