

class C {}
class D {}
class E {}
class F {}

public aspect XLintTypeDeclareParent {

    declare parents: UnknownType implements Cloneable; // CM -XLint unfound type
}

