// Name patterns can't end with '..'
aspect A {
	pointcut endsDot(): call(* java.(..));
	pointcut p1(): call(* java.lang..(..));
	pointcut p2(): call((Integer || java.lang..) m(..));
	pointcut p3(): call(* m() throws java.lang..);
	
	pointcut p4(): call(* a..b..c..d..(..));
	
	pointcut p5(): call(* a....(..));
	
	pointcut p6(): call(java. m());
}