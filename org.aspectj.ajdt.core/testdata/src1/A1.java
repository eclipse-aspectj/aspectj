aspect A1 {
    after() throwing (Throwable t): execution(* Hello.*(..)) {
        System.out.println("enter");
    }

    after() throwing: execution(* Hello.*(..)) {
        System.out.println("enter");
    }

    after() returning: execution(* Hello.*(..)) {
        System.out.println("enter");
    }

    after(): execution(void Hello.*ai*(..)) {
    }
}


class Hello {
    public static void main(String[] args) {
        System.out.println("hello");
    }
}
