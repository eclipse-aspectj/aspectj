package caching;

public class Fib {
	public static int calc(int n){
		if (n < 2) return 1;
		return calc(n-1) + calc(n-2);
	}
}
