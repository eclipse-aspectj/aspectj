public aspect pr122452_2 {
    after() returning() : greeting*() {  
        System.out.println(" World!");
    }
}
