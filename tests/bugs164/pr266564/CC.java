package business;

public class CC {

        public void foo(long docId, String userid) {
        }

        public static void main(String[] args) {
                new CC().foo(12, "hello");
        }
}

aspect Asp {

        Object around(): execution(* foo(..)) {
                return proceed();
        }
}

