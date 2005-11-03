aspect A {  
    HW.new(String s) {this();}  
    declare warning : initialization(HW.new(String,A)) : "should not match";
    declare warning : initialization(HW.new(String)) : "should match";
}
class HW {}