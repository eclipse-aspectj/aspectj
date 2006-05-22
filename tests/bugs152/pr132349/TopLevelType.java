public class TopLevelType<E> {
        public static class NestedType {}
        
        public static void main(String[] args) {
			TopLevelType tlt = new TopLevelType();
			NestedType nt = new NestedType();
			nt.someMethod();
		}
}

aspect SomeAspect {
        public void TopLevelType.NestedType.someMethod() {
        }
        
}

