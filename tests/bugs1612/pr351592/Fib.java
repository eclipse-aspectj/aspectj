package caching;

public class Fib {
	public static int calc(int n){
		if (n < 2) return 1;
		return calc(n-1) + calc(n-2);
	}
	public static Integer calc2(Integer n){
		if (n < 2) return 1;
		return calc2(n-1) + calc2(n-2);
	}
}
