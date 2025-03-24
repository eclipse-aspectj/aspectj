class A {}

class B {}

public abstract aspect F {
    @Deprecated
    public abstract void C.displaySearch(StringBuffer buffer, String name, String prefix, A criteria,
        B context);
}

interface C {
}

