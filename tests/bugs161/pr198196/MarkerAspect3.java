aspect MarkerAspect3  { // VerifyError

        declare parents: Foo implements Marker;

        public String Foo.toString() {
                new Runnable() {
                        public void run() {
                                Foo.super.toString();
                        }
                }.run();
                return "oranges";
        }
}
