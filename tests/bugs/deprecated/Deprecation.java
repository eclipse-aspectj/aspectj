// bug 54098

public class Deprecation {
    public static void main(String[] args) {
        Deprecated dep = new Deprecated();
        dep.bar();
    }
}
