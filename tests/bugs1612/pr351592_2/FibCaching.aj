package caching;

public aspect FibCaching extends Caching<Integer,Integer> {
	
	pointcut cached() : execution(int Fib.calc(int));
	
}

