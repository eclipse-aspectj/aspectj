package ma2;

public class Main {

    public static class Dummy {

        private int counter = 0;

        @Annotation1
        @Annotation2
        @Annotation3
        public int retryTranslateAndTimeLimited() {
            System.out.println("Method call");
            if (counter++ == 0) {
                throw new IllegalStateException();
            } else {
                return 1;
            }
        }

    }

    public static void main(String[] args) {
        new Dummy().retryTranslateAndTimeLimited();
    }

}
