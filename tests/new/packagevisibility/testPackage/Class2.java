package packagevisibility.testPackage;

class Class2 {
    String doIt(String s) {
        new Runnable() {
                public void run() {
                    System.out.println("running");
                }
            }.run();

        return s + "-class2";
    }
}
