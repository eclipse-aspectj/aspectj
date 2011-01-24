package ma;


public class Main {

        @Annotation1
        public int retryTranslateAndTimeLimited() {
            System.out.println("Method call");
            return 1;
        }

    public static void main(String[] args) {
        new Main().retryTranslateAndTimeLimited();
    }

}
