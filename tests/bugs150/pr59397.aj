aspect A {  
    HW.new(String s) {}  
    declare warning : initialization(HW.new(String,A)) : "should not match";
    declare warning : initialization(HW.new(String)) : "should match";
}
class HW {}