



/** @testcase PR853 declare class extends interface */

class C {}

aspect A {
    declare parents: C extends java.io.Serializable;  // CE 10
}