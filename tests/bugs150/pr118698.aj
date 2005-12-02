public aspect pr118698 {
    private static interface Marker {}   

    private class Foo implements Marker {
		public Foo() {
            bar = null; // allowed
            listener = null; // should also be allowed
            this.listener = null; // so should this
            ((Marker)this).listener = null; // and this
        }
    }

    public static void main(String []argv) {
      pr118698.aspectOf().x();
    }
    
    public void x() {
    	new Foo();
    }

    private Object Marker.listener;
    private Object bar;
}
