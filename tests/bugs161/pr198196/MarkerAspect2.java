aspect MarkerAspect2  { // IncompatibleClassChangeError


        declare parents: Foo implements Marker;

        public String Marker.toString() {
                new Runnable() {
                        public void run() {
                                Marker.super.toString();
                        }
                }.run();
                return "banana";//super.toString();
        }
}