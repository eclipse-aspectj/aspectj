public aspect AJ2 {

    class X{}
    interface Y {}

    declare soft : Exception : execution(void x());
    declare parents : X implements Y;
    declare soft : Exception : execution(void y());
    declare soft : Exception : execution(void z());

    void x() {
        throw new Exception();
    }
    void y() {
        throw new Exception();
    }
    void z() {
        throw new Exception();
    }
}
