/** @testcase PUREJAVA PR#739 local variables must be final to be accessed from inner class */
class LocalsFromInnerCE {
    void f() {
        int i = 0;
        new Runnable() {
                public void run() { i++; } // CE 6 nonfinal variable
            }.run();
    }

    static void sf() {
        int i = 0;
        new Runnable() {
                public void run() { i++; } // CE 13 nonfinal variable
            }.run();
    }

    void f(int i) {
        new Runnable() {
                public void run() { i++; } // CE 19 nonfinal variable
            }.run();
    }

    static void sf(int i) {
        new Runnable() {
                public void run() { i++; } // CE 25 nonfinal variable
            }.run();
    }

    static {
        int i = 0;
        new Runnable() {
                public void run() { i++; } // CE 32 nonfinal variable
            }.run();
    }

    void m(int i) {
        class m {
            void f(int i) {
                new Runnable() {
                        public void run() { i++; } // CE 40 nonfinal variable
                    }.run();
            }
        }
    }

    void m() {
        int i = 0;
        class m {
            void f() {
                new Runnable() {
                        public void run() { i++; } // CE 51 nonfinal variable
                    }.run();
            }
        }
    }

    class m {
        void f() {
            int i = 0;
            new Runnable() {
                    public void run() { i++; } // CE 61 nonfinal variable
                }.run();
        }
 
        void f(int i) {
            new Runnable() {
                    public void run() { i++; } // CE 67 nonfinal variable
                }.run();
        }
    }

    LocalsFromInnerCE() {
        int i = 0;
            new Runnable() {
                    public void run() { i++; } // CE 75 nonfinal variable
                }.run();
    }

    LocalsFromInnerCE(int i) {
            new Runnable() {
                    public void run() { i++; } // CE 81 nonfinal variable
                }.run();
    }
}
