// pr 45663
aspect ParserRecoveryTest1
{
	pointcut s(int x):     
	   execution(* fred(x));

	after(int xxx x x x x): this(*) {}
}