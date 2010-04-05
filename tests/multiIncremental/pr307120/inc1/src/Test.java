privileged aspect Test {

        before(): get(* foo) {}

//        before(): get(* goo) {}

        public int A.getFoo() {
                return foo;
        }

//      public int A.getGoo() {
//              return goo;
//      }
}
