public aspect AJ3 {

    class X{}
    interface Y {}
    interface Z {}

    declare parents : X implements Y;
    declare warning: execution(* x(..)): "";
    declare error: execution(* x(..)): "";
    declare soft : Exception : execution(void x());
    declare parents : X implements Z;
    declare warning: execution(* x(..)): "";
    declare error: execution(* x(..)): "";

    void x() {
        throw new Exception();
    }
}
