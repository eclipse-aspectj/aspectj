package bug;  // I used a "bug" package under the "src" source folder.

public aspect CantMatchOnInterfaceIntroducedToGenericClass {
        public static interface Marker {}
        public static class NonGenericClass {
                public void doit(String msg) {
                        System.out.println("doit(): msg = "+msg);
                }
        }
        public static class GenericClass<T> {
                public void doit(T t) {
                        System.out.println("doit<T>(): t = "+t);
                }
        }

        declare parents: NonGenericClass implements Marker;
        declare parents: GenericClass    implements Marker;

        pointcut nonGenericCall(): call (void NonGenericClass.doit(..));
        pointcut genericCall():    call (void GenericClass.doit(..));
        pointcut markerCall():     call (void Marker+.doit(..));

        private static int mCount  = 0;
        
        before(): nonGenericCall() {
                System.out.println("nonGenericCall advice hit");
        }
        before(): genericCall() {
                System.out.println("genericCall advice hit");
        }
        before(): markerCall() {
        		mCount++;
                System.out.println("markerCall advice hit");
        }

        public static void main(String args[]) {
                new NonGenericClass().doit("message1");
                new GenericClass<Integer>().doit(new Integer(2));
                if (mCount!=2) {
                	throw new RuntimeException("Did not hit marker+ advice twice!");
                }
        }
}
