package typeNameConflicts.aspects;

aspect A {
    /*
    before(): set(int value) { //&& args(o) {
        System.out.println(thisJoinPointStaticPart);
    }
    */

    after() returning(Object o): set(int value) {
        System.out.println(o);
    }


    before(): call(void Runnable.run()) {
        System.out.println("about to run");
    }

    /*
    after(): set(int value) {
        System.out.println("set");
    }
    after() throwing: set(int value) {
        System.out.println("throwing");
    }
    */
}
