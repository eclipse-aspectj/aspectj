
class C {}
class D {}
class E {}
class F {}


public aspect XLintTypeDeclareField {

    public int UnknownType.i; // CM -XLint unfound type
}

