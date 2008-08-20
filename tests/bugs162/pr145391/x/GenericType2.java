interface Bar {}

class B implements Bar {}

public class GenericType2<V extends Bar> {

        public GenericType2(V value) {}

        protected void getValue(V aV) {
        }
  public void m() {
    getValue(new B());
}
}

aspect SomeAspect {
        before(GenericType2 t): call(* GenericType2.foo()) && target(t) {
                // Indirect call to generic method produces a NoSuchMethodError
                t.callGenericMethod();
        }

        private void GenericType2.callGenericMethod() {
//                getValue(new Integer(45));
        }
}
