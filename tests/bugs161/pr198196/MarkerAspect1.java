aspect MarkerAspect1  { // VerifyError

        declare parents: Foo implements Marker;

        public String Foo.toString() {
                new Runnable() {
                        public void run() {
                                super.toString();
                        }
                }.run();
                return "oranges";
        }
}
