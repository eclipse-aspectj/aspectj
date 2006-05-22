public class TopLevelType3<E> {
        public static class NestedType<T> {}
        
        public static void main(String[] args) {
			TopLevelType3 tlt = new TopLevelType3();
			NestedType nt = new NestedType();
			nt.someMethod();
		}
}

aspect SomeAspect {
        public void TopLevelType3.NestedType<String>.someMethod() { // error - can't do that
        }
}



