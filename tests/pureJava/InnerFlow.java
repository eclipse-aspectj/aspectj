public class InnerFlow { 
    static public void main(String[] params) {
        final Object o = new Interface() {
            public void m() {
                o.toString();   //ERR: o not initialized
            }};
        ((Interface) o).m(); // no exceptions
    }
}
interface Interface { void m();}
