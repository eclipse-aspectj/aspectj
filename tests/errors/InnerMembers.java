public class InnerMembers {
    static class StaticI {
        static int x;
        static String foo() { return "foo"; }
    }
    class Inner {
        static final int CONST=10;
        static final int NOT_CONST=new Integer(10).intValue(); //ERR: non-constant static in inner
        static int x;                        //ERR: non-constant static in inner
        static String foo() { return "foo"; }//ERR: non-constant static in inner
        interface I {}//ERR: non-constant static in inner
    }
    public static void m() {
        class Inner {
            static final int CONST=10;
            static int x;                        //ERR: non-constant static in inner
            static String foo() { return "foo"; }//ERR: non-constant static in inner
        }
    }
}
