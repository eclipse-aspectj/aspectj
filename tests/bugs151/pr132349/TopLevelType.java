public class TopLevelType<E> {
        public static class NestedType {}
}

aspect SomeAspect {
        public void TopLevelType.NestedType.someMethod() {
        }
}