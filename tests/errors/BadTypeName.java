aspect BadTypeName {
    pointcut prints(Foo f): call(void System.out.println(..));

    Foo x = null;

    a.b.c.Foo y = null;

    System.Inner i = null;

    Object o = a.b.c.Foo.field;

    Object o1 = C.privateField;
}


class C {
    private int privateField;
}
