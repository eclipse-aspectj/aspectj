

/** @testcase PR853 declare interface implements interface CE */

interface I {}

interface I2 {}

aspect A {
    declare parents:  I implements I2;  // CE 10
}