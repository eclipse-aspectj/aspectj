 class Test {
	public static void main(String[] args) {
		test();
	}
	static void test() {
		throw new Error("hello");
	}
	static aspect A {
		declare soft : unknown.Error : call(void test()); // CE  should be a message saying cant bind type 'unknown.Error' and not 'unknown$Error'
	}
}