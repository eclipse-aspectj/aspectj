class C {
    public void m() throws Integer { } //ERROR Integer is not a Throwable

    public C() throws C { } //ERROR C is not a Throwable
}


class Sup {
    public void m() {}
}

class Sub extends Sup {
    public void m() throws Exception {}
}
