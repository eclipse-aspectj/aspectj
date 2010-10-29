public aspect AJ {

    class X{}
    interface Y {}

    declare parents : X implements Y;
    declare soft : Exception : execution(void x());

    void x() {
        throw new Exception();
    }
}
