public aspect BeforeExec {
    before() : execution(* getName()) {
        System.out.println("Before execution");
    }
}
