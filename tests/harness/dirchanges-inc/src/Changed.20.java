

public class Changed {
    public static void main(String[] args) {
        Unchanged.main(args);
        String sargs = java.util.Arrays.asList(args).toString();
        if (!"[second]".equals(sargs)) {
            throw new Error("expected args [second] but got " + sargs);
        }
    }
}