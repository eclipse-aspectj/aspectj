public class BadContinueTarget {

    public static void main(String[] args) {
        int i;
    test: {
	    continue test;
    }
    }
}
