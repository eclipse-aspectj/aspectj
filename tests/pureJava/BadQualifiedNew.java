public class BadQualifiedNew {

    public static void main(String[] args) {
	new BadQualifiedNew().new Test();
    }

    static class Test {
    }
}
