

/** @testcase PR853 declare interface implements class CE */

interface I {}

class B {}

aspect A {
    declare parents:  I implements B;  // CE 10 - XXX error says "extends" - weak
}