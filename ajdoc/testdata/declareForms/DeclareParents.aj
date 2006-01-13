package foo;

public aspect DeclareParents {
	declare parents: Class1 implements java.io.Serializable;
    declare parents: Class2 extends java.util.Observable;
}

class Class1 {	
}

class Class2 {
}
