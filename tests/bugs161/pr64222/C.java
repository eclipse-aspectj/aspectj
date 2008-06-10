class C {
	int dummy() {return 5;}
}

aspect Foo {
	around(): call(int C.dummy()) {
		proceed();
	}
}
