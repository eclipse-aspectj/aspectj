
aspect A {
    before() : execution(void main(String[])) {
        System.setProperty("A.before", "true");
    }
}