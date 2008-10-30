interface Foo {
        public static final Object dummy = new Object();
}

aspect Code2 {
        Object around(): call(Object.new(..)) {
                return proceed();
        }

        public static void main(String[] args) {
                System.out.println(Foo.dummy);
        }
}

