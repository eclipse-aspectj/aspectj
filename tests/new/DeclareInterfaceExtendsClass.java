

/** @testcase PR853 declare interface extends class CE */

interface I {}

class C {}

aspect A {
    declare parents:  I extends C;  // CE 10
}