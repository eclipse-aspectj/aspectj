public aspect StarVarargsPattern {
	
	declare warning : execution(* *(..,*...)) : "you used a varargs signature";
	
	void foo(Object... objs) {}   // DW L 5
	
	void bar(String s, String... ss) {}  // DW L7
	
	void goo(Integer[] is) {}

}