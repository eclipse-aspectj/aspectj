package protectedAccess.p1;


public class C1 {
    protected C1() { }

    protected String s = "protected";

    protected String m() { return "protected"; }

    protected String mString(String s) { return s; }

    protected static class I1 {
        protected String si = "ip";
        protected String mi() { return "ip"; }
    }
}
