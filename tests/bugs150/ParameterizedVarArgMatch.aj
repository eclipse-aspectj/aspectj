class Test1<E> {
    public void method1(E...args) {
    }
}

public aspect ParameterizedVarArgMatch {
	
	public static void main(String[] args) {
		new Test1<String>().method1("a","b","c");
	}
	
	
    after(Test1 test1, Object[] arg) returning: 
    	execution(* Test1.method1(Object...)) && target(test1) && args(arg) {
        System.out.println("got here");
    }
}