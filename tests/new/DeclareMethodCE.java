

/** PR#888 ajc crashes given method in declared method */
class B {}

aspect A {
    void B.n() {
        void n() { }           // CE 8 method declared in method
    }
}
