public class LocalInners {
    public static void main(String[] args) {
        class Local1 {
            public String getL() { return l.toString(); }
            private Local1 l;
        }
        Local1 lt = new Local1();
        
        class Local2 extends Local1 {
            private Local1 t;
        }
    }
}
