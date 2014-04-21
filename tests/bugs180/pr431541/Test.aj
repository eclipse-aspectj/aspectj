public aspect Test {

	Object around(String s): call(public Object foo(String)) && args(s) {
		return proceed(s);
	}

}

class C {
  public void m() {
    foo("abc");
  }
  public Object foo(String s) {
    return s;
  }
}