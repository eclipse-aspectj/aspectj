package p;

aspect Foo {
	
	int A.i; // removed type vars 
	
	public void A<Y,Z>.m() {}
}
