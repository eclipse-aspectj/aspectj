abstract class ValueChange<Q> {
    public ValueChange(Q initValue) { }
}

abstract class SyncValueGroup<T> extends ValueChange<T> {
	public SyncValueGroup(T initValue) {
        super(initValue);
    }
    public final synchronized void link(SyncValueGroup<T> ... list) {    }
}

class SyncValueTest {
    class SyncInteger extends SyncValueGroup<Integer> {
        public SyncInteger(int val) {
            super(new Integer(val));
        }
    }
    private SyncInteger a = new SyncInteger(1);
    public void testSyncValueGroup() {
        a.link(a);
    }
}

aspect X {
  before(): call(* *(..)) {}
}
 