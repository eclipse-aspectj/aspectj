public aspect Before {
    before() : call(* getName()) {
        System.out.println("Before call");
    }
}
