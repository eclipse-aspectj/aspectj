
/** @testcase PUREJAVA nested interface does not require new qualifier */
public class InvalidNewQualifier14CE {
    interface I {}
    void test() {
        // error
//         this.new I(){};  // CE 7 new qualification unneeded
//         new Outer().new Outer.I(){}; // CE 8 new qualification bad
//         new OuterInterface(){}.new OuterInterface.I(){}; // CE 9 new qualification bad
//         new OuterOuter.Inner(){}.new OuterOuter.Inner.I(){}; // CE 10 new qualification bad
        Outer o = new Outer();
        OuterInterface oi = new OuterInterface(){};
        OuterOuter.Inner ooi = new OuterOuter.Inner(){}; 
         o.new Outer.I(){}; // CE 14 new qualification bad
//         oi.new OuterInterface.I(){}; // CE 15 new qualification bad
//         ooi.new OuterOuter.Inner.I(){}; // CE 16 new qualification bad

        // ok
        new I(){};  
        new Outer.I(){}; 
        new OuterInterface.I(){};
        new OuterOuter.Inner.I(){};
    }
}

class Outer {
    interface I {}
}
interface OuterInterface {
    interface I {}
}
class OuterOuter {
    interface Inner {
        interface I {}
    }
}
