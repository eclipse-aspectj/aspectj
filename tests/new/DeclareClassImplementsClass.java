

/** @testcase PR853 declare class implements class CE */

class C {}

class B {}

aspect A {
    declare parents:  C implements B;  // CE 10
}