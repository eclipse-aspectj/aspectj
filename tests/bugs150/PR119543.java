public class PR119543 {
	
	public abstract static aspect A<T> {
		
		private pointcut caching();
		private pointcut permitted();
		
		before() : caching() {
		}
	}
	
	static aspect A1 extends A<String> {				
	}

}
