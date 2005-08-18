package packag;

public class Main {
    public static void main(String[] args) {
        throw new Error("around advice should have applied here");
    }
}