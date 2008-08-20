public class GenericType2<V extends Integer> {

        public GenericType2(V value) {}

        public void foo() {}

        protected void getValue(V aV) {
        }

        public static void main(String[] args) {
                new GenericType2<Integer>(null).foo();
        }

}

aspect SomeAspect {
        before(GenericType2 t): call(* GenericType2.foo()) && target(t) {
                // Indirect call to generic method produces a NoSuchMethodError
                t.callGenericMethod();
        }

        private void GenericType2.callGenericMethod() {
                getValue(new Integer(45));
        }
}
