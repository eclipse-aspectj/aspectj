public class AbstractDeclare {
    public static void main(String[] args) {
        new C().m();
        int y = new C().x;
    }
}

class C {
    int x;

    void m() {}
}


abstract aspect BaseErr {
    abstract pointcut acid();
    abstract pointcut base();

    declare error: acid() && base():
        "acid's and base's don't mix";
}

aspect CallErr extends BaseErr {
    pointcut acid(): within(AbstractDeclare);
    pointcut base(): call(* C.*());
}

aspect GetErr extends BaseErr {
    pointcut acid(): within(AbstractDeclare);
    pointcut base(): get(* C.*);
}
