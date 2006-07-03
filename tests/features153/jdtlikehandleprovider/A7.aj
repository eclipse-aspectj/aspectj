package pkg;

public aspect A7 {
	
	declare parents : C1 implements C2;
	
	declare parents : C4 extends C5;
	
}

class C1 {
}

interface C2 {
}

class C4 {
}

class C5 {
}
