
public class Main {
    public static void main(String[] args) {
        if (!Boolean.getBoolean("A.before")) {
            throw new Error("property A.before not set by aspect A.java");
        }
    }
}