aspect MarkerAspect2  { // IncompatibleClassChangeError

		int Marker.i=0;

        declare parents: Foo implements Marker;

        public String Marker.toString() {
                new Runnable() {
                        public void run() {
                    		if (i++>5) return;
                    		System.out.println("a");
                            Marker.super.toString();
                        }
                }.run();
                return "banana";//super.toString();
        }
}