public class InnerClassesInAnnonymousClasses {
    public static void main(String[] args) {
        new InnerClassesInAnnonymousClasses().realMain(args);
    }
    public void realMain(String[] args) {


        new Runnable() {
                public void run() {}
                class Inner {}
                Inner inner = new Inner();
            };
    }

    
}
