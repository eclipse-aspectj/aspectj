public class MultipleIntros {
    public static void main(String[] args) { new C().publicM(); }
}

class C {
    private void privateM() {}
    public void publicM() { System.out.println("from C"); }
    
    private int privateF;
    public int publicF;
}


aspect A {
    private int C.privateF; // should be okay
    public int C.publicF; //ERROR conflicts with existing field

    private int C.privateFA;
    private int C.privateFA; //ERROR conflicts with the above

    private void C.privateM() {} // should be okay
    public void C.publicM() { System.out.println("from A"); } //ERROR conflicts with existing method
}

aspect AO {
    static aspect AI1 {
        private int C.privateFA;
    }
    static aspect AI2 {
        private int C.privateFA; //ERROR conflicts with field from AI1
    }
}
