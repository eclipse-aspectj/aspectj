
public class Main {
    static {
        System.setProperty("A.before", "false");
    }
    public static void main(String[] args) {
        if (!Boolean.getBoolean("A.before")) {
            throw new Error("property A.before not set by aspect A.java");
        }
    }
}