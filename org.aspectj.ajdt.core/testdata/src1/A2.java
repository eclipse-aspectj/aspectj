aspect A2 {
    after(Object thisObject): target(thisObject) && execution(public * Hello.*(..)) {
    }
    after(Object thisObject): this(thisObject) && execution(* Hello.*(..)) {
        System.out.println(thisObject);
    }
    after() returning (String s): execution(* Hello.*(..)) {
        //System.out.println(s);
    }
}

class Hello {
    public static void main(String[] args) {
        System.out.println("hello");
    }

    void m() {
    }
}
