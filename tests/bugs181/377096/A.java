@interface A {
	int i(); 
}
class C {
  @A(wibble={},i=42) 
  public void xxx() {}
}