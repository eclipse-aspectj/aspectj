

/** @testcase PR853 declare class implements class CE */

class C {}

class B {}

aspect A {
    declare C implements B;  // CE 10
}