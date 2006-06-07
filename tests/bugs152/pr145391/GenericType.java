public class GenericType<V extends Integer> {

        public GenericType(V value) {}

        public void foo() {}
//
//        public void bar() {}

        protected V getValue() {
                return null;
        }

        public static void main(String[] args) {
                new GenericType<Integer>(null).foo();
        }

}

aspect SomeAspect {
        before(GenericType t): call(* GenericType.foo()) && target(t) {
                // Direct call to non-generic method works
//                t.bar();
                // Indirect call to non-generic method works
//                t.callNormalMethod();
                // Direct call to generic method works
//                t.getValue();
                // Indirect call to generic method produces a NoSuchMethodError
                t.callGenericMethod();
        }

//        private void GenericType.callNormalMethod() {
//                bar();
//        }

        private void GenericType.callGenericMethod() {
                getValue();
        }
}