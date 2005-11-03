
// The actual ctor that gets put in type 'HW' during compilation is
// HW(String,A)
// This is an artifact of the implementation of ctor ITDing - unfortunately this artifact
// can be seen by the declare warning.  So,
//  initialization(HW.new(..))  will match our new ctor
//  initialization(HW.new(String,A)) will match our new ctor
//  initialization(HW.new(String)) will not match !

aspect A {  
	HW.new(String s) { this(); }  
	declare warning : initialization(HW.new(String,A)) : "Funky ctor found!";
}


class HW {}
