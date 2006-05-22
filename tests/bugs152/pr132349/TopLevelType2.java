public class TopLevelType2<E> {
        public static class NestedType {}
        
        public static void main(String[] args) {
			TopLevelType2 tlt = new TopLevelType2();
			NestedType nt = new NestedType();
			nt.someMethod();
		}
}

aspect SomeAspect {
        public void TopLevelType2<String>.NestedType.someMethod() { // error - can't do that
        }
}



